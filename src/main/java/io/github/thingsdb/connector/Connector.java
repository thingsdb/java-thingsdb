package io.github.thingsdb.connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.thingsdb.connector.event.NodeStatus;
import io.github.thingsdb.connector.event.WarnEvent;
import io.github.thingsdb.connector.exceptions.PackageIdNotFound;
import io.github.thingsdb.connector.exceptions.ProtoUnhandled;

/**
 *
 */
public class Connector implements ConnectorInterface {

    private static Logger log = LoggerFactory.getLogger(Conn.class);

    private final List<Node> nodes;
    private char nextPid;
    private int activeNodeId;
    private MessageBufferPacker authPacker;
    private Conn conn = null;
    private RespMap respMap;
    private String defaultScope;
    private boolean autoReconnect;
    private final ExecutorService executor;

    public Function <NodeStatus, Void> onNodeStatus;
    public Function <WarnEvent, Void> onWarning;

    public Connector(String host) {
        this(host, 9200);
    }

    public Connector(String host, int port) {
        nodes = Collections.synchronizedList(new ArrayList<>());
        respMap = new RespMap();

        nextPid = 0;
        conn = null;
        authPacker = null;
        defaultScope = "/thingsdb";
        executor = Executors.newFixedThreadPool(10);
        autoReconnect = true;

        onNodeStatus = null;
        onWarning = null;

        addNode(host, port);
    }

    @Override
    public void addNode(String host, int port) {
        nodes.add(new Node(host, port));
    }

    @Override
    public void addNode(String host) {
        nodes.add(new Node(host, 9200));
    }

    @Override
    public synchronized void setDefaultScope(String scope) {
        defaultScope = scope;
    }

    @Override
    public synchronized String getDefaultScope() {
        return defaultScope;
    }

    @Override
    public synchronized void disableAutoReconnect() {
        autoReconnect = false;
    }

    public void close() throws IOException {
        disableAutoReconnect();
        conn.close();
    }

    public void connect() throws IOException {
        Node node = getNode();
        conn = new Conn(node, this);
        conn.start();
        ping();
        alive();
    }

    public Future<Result> authenticate(String token) {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        try {
            packer.packString(token);
            packer.close();

            authPacker = packer;  // For re-connect

            return write(Proto.REQ_AUTH, packer);
        } catch (IOException ex) {
            CompletableFuture<Result> future = new CompletableFuture<>();
            future.completeExceptionally(ex);
            return future;
        }
    }

    public Future<Result> authenticate(String username, String password) {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        try {
            packer.packArrayHeader(2);
            packer.packString(username);
            packer.packString(password);
            packer.close();

            authPacker = packer;  // For re-connect

            return write(Proto.REQ_AUTH, packer);
        } catch (IOException ex) {
            CompletableFuture<Result> future = new CompletableFuture<>();
            future.completeExceptionally(ex);
            return future;
        }
    }

    public Future<Result> query(String code, String scope, byte[] args) {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        try {
            packer.packArrayHeader(args == null ? 2 : 3);
            packer.packString(scope);
            packer.packString(code);
            if (args != null) {
                packer.addPayload(args);
            }
            packer.close();

            return ensureWrite(Proto.REQ_QUERY, packer);
        } catch (IOException ex) {
            CompletableFuture<Result> future = new CompletableFuture<>();
            future.completeExceptionally(ex);
            return future;
        }
    }

    public Future<Result> query(String code, String scope) {
        return query(code, scope, null);
    }

    public Future<Result> query(String code, byte[] args) {
        return query(code, getDefaultScope(), args);
    }

    public Future<Result> query(String code) {
        return query(code, getDefaultScope(), null);
    }

    public String toString() {
        return conn == null ? "no connection" : conn.toString();
    }

    protected void handle(Pkg pkg) throws PackageIdNotFound {
        Result res;

        switch (pkg.proto) {
            case ON_NODE_STATUS:
                res = Result.newResult(pkg.getData());
                NodeStatus nodeStatus;
                try {
                    nodeStatus = NodeStatus.newFromResult(res);
                } catch (IOException e) {
                    log.error("Failed to unpack node status event");
                    return;
                }
                if (onNodeStatus != null) {
                    onNodeStatus.apply(nodeStatus);
                }
                if (nodeStatus.isShuttingDown()) {
                    log.warn("Node Id %d is shutting down... (%s)", nodeStatus.id, toString());
                    reconnect();
                }
                break;
            case ON_WARN:
                res = Result.newResult(pkg.getData());
                WarnEvent warnEvent;
                try {
                    warnEvent = WarnEvent.newFromResult(res);
                }catch (IOException e) {
                    log.error("Failed to unpack ThingsDB warning event");
                    return;
                }
                if (onWarning == null) {
                    log.warn("ThingsDB: %s (%d)", warnEvent.msg, warnEvent.code);
                } else {
                    onWarning.apply(warnEvent);
                }
            case ON_ROOM_JOIN:
                break;
            case ON_ROOM_LEAVE:
                break;
            case ON_ROOM_EVENT:
                break;
            case ON_ROOM_DELETE:
                break;
            default:
                break;
        }

        Integer pid = Integer.valueOf(pkg.pid);
        CompletableFuture<Result> future = respMap.get(pid);

        if (future == null) {
            throw new PackageIdNotFound(String.format("Package Id %d not found", pkg.pid));
        }

        switch (pkg.proto) {
            case RES_PONG:
                future.complete(Result.RESULT_PONG);
                return;
            case RES_OK:
                future.complete(Result.RESULT_OK);
                return;
            case RES_DATA:
                res = Result.newResult(pkg.getData());
                future.complete(res);
                return;
            case RES_ERROR:
                future.completeExceptionally(TiError.fromData(pkg.getData()));
                return;
            default:
                Exception e = new ProtoUnhandled(String.format("Unhandled protocol (%s)", pkg.proto.toString()));
                future.completeExceptionally(e);
        }
    }

    private void reconnect() {
        try {
            try {
                conn.close();
            } finally {
                Node node = getNextNode();
                conn = new Conn(node, this);
                conn.start();
                Future<Result> fut = write(Proto.REQ_AUTH, authPacker);
                try {
                    fut.get();
                } catch (Exception e) {
                    conn.close();
                    return;
                }
                // TODO: re-join rooms
            }
        } catch (IOException e) {};
    }

    private void alive() {
        executor.execute(() -> {
            try {
                while (autoReconnect) {
                    TimeUnit.MILLISECONDS.sleep(500);
                    if (!conn.isConnected()) {
                        reconnect();
                    }
                }
            } catch (InterruptedException e) { }
        });
    };

    private void ping() {
        executor.execute(() -> {
            try {
                while (true) {
                    TimeUnit.SECONDS.sleep(5);
                    write(Proto.REQ_PING, null);
                }

            } catch (InterruptedException e) { }
        });
    }

    private Node getNode() {
        return nodes.get(activeNodeId);
    }

    private Node getNextNode() {
        activeNodeId++;
        activeNodeId %= nodes.size();
        return nodes.get(activeNodeId);
    }

    private synchronized Integer getNextPid() {
        char pid = nextPid;
        nextPid++;
        return Integer.valueOf(pid);
    }

    private CompletableFuture<Result> write(Proto proto, MessageBufferPacker packer) {
        Integer pid = getNextPid();

        CompletableFuture<Result> future = respMap.register(pid);

        Pkg pkg = packer != null
            ? Pkg.newFromPacker(proto, pid.intValue(), packer)
            : new Pkg(proto, pid, 0);

        executor.execute(() -> {
            try {
                conn.write(pkg.getBytes());
            } catch (IOException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    private CompletableFuture<Result> ensureWrite(Proto proto, MessageBufferPacker packer) {
        Integer pid = getNextPid();

        CompletableFuture<Result> future = respMap.register(pid);

        Pkg pkg = packer != null
            ? Pkg.newFromPacker(proto, pid.intValue(), packer)
            : new Pkg(proto, pid, 0);

        executor.execute(() -> {
            boolean fistAttempt = true;
            try {
                while (autoReconnect || fistAttempt && future.isDone()) {
                    fistAttempt = false;
                    if (!conn.isConnected()) {
                        TimeUnit.MILLISECONDS.sleep(500);
                        continue;
                    }

                    try {
                        conn.write(pkg.getBytes());
                    } catch (IOException e) {
                        TimeUnit.MILLISECONDS.sleep(500);
                        continue;
                    }
                    break;  // success
                }
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }
}