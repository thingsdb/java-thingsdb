package io.github.thingsdb.connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.buffer.MessageBuffer;
import org.msgpack.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.thingsdb.connector.event.NodeStatus;
import io.github.thingsdb.connector.event.RoomEvent;
import io.github.thingsdb.connector.event.WarnEvent;
import io.github.thingsdb.connector.exceptions.JoinException;
import io.github.thingsdb.connector.exceptions.PackageIdNotFound;
import io.github.thingsdb.connector.exceptions.ProtoUnhandled;


/**
 *
 */
public class Connector implements ConnectorInterface {

    private static Logger log = LoggerFactory.getLogger(Conn.class);

    private Conn conn = null;
    private MessageBufferPacker authPacker;
    private String defaultScope;
    private boolean autoReconnect;
    private char nextPid;
    private int activeNodeId;
    private final ExecutorService executor;
    private final List<Node> nodes;
    private final RespMap respMap;
    private final Map<Long, Room> rooms;

    public Function <NodeStatus, Void> onNodeStatus;
    public Function <WarnEvent, Void> onWarning;

    public Connector(String host) {
        this(host, 9200);
    }

    public Connector(String host, int port) {
        nodes = Collections.synchronizedList(new ArrayList<>());
        respMap = new RespMap();
        rooms = Collections.synchronizedMap(new HashMap<>());
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
    public Connector addNode(String host, int port) {
        nodes.add(new Node(host, port));
        return this;
    }

    @Override
    public Connector addNode(String host) {
        nodes.add(new Node(host, 9200));
        return this;
    }

    @Override
    public synchronized Connector setDefaultScope(String scope) {
        defaultScope = scope;
        return this;
    }

    @Override
    public synchronized String getDefaultScope() {
        return defaultScope;
    }

    @Override
    public synchronized Connector disableAutoReconnect() {
        autoReconnect = false;
        return this;
    }

    public void close() throws IOException {
        disableAutoReconnect();
        conn.close();
    }

    public Connector connect() throws IOException {
        Node node = getNode();
        conn = new Conn(node, this);
        conn.start();
        ping();
        alive();
        return this;
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

    public Future<Result> query(String scope, String code, Vars vars) {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        try {
            packer.packArrayHeader(vars == null ? 2 : 3);
            packer.packString(scope);
            packer.packString(code);
            if (vars != null) {
                List<MessageBuffer> args = vars.toBufferList();
                for (MessageBuffer buffer : args) {
                    packer.addPayload(buffer.array());
                }
            }
            packer.close();

            return ensureWrite(Proto.REQ_QUERY, packer);
        } catch (IOException ex) {
            CompletableFuture<Result> future = new CompletableFuture<>();
            future.completeExceptionally(ex);
            return future;
        }
    }

    public Future<Result> query(String scope, String code) {
        return query(scope, code, (Vars) null);
    }

    public Future<Result> query(String code, Vars args) {
        return query(getDefaultScope(), code, args);
    }

    public Future<Result> query(String code) {
        return query(getDefaultScope(), code, (Vars) null);
    }

    public Future<Result> run(String scope, String procedure, ArgumentInterface arguments) {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        try {
            packer.packArrayHeader(arguments == null ? 2 : 3);
            packer.packString(scope);
            packer.packString(procedure);
            if (arguments != null) {
                List<MessageBuffer> args = arguments.toBufferList();
                for (MessageBuffer buffer : args) {
                    packer.addPayload(buffer.array());
                }
            }
            packer.close();

            return ensureWrite(Proto.REQ_RUN, packer);
        } catch (IOException ex) {
            CompletableFuture<Result> future = new CompletableFuture<>();
            future.completeExceptionally(ex);
            return future;
        }
    }

    public Future<Result> run(String procedure, ArgumentInterface arguments) {
        return run(getDefaultScope(), procedure, arguments);
    }

    public Future<Result> run(String scope, String procedure) {
        return run(scope, procedure, (Vars) null);
    }

    public Future<Result> run(String procedure) {
        return run(getDefaultScope(), procedure, (Vars) null);
    }

    public String toString() {
        return conn == null ? "no connection" : conn.toString();
    }

    protected Future<Result> join(String scope, ArrayList<Long> roomIds) {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        try {
            packer.packArrayHeader(1 + roomIds.size());
            packer.packString(scope);
            for (Long roomId : roomIds) {
                packer.packLong(roomId.longValue());
            }
            packer.close();
            return ensureWrite(Proto.REQ_JOIN, packer);
        } catch (IOException ex) {
            CompletableFuture<Result> future = new CompletableFuture<>();
            future.completeExceptionally(ex);
            return future;
        }
    }

    protected Future<Result> join(String scope, Long roomId) {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        try {
            packer.packArrayHeader(2);
            packer.packString(scope);
            packer.packLong(roomId.longValue());
            packer.close();
            return ensureWrite(Proto.REQ_JOIN, packer);
        } catch (IOException ex) {
            CompletableFuture<Result> future = new CompletableFuture<>();
            future.completeExceptionally(ex);
            return future;
        }
    }

    protected Future<Result> leave(String scope, Long roomId) {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        try {
            packer.packArrayHeader(2);
            packer.packString(scope);
            packer.packLong(roomId.longValue());
            packer.close();
            return ensureWrite(Proto.REQ_LEAVE, packer);
        } catch (IOException ex) {
            CompletableFuture<Result> future = new CompletableFuture<>();
            future.completeExceptionally(ex);
            return future;
        }
    }

    protected Future<Result> emit(String scope, Room room, String event, Args args) {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        List<Value> list = args.getList();
        try {
            packer.packArrayHeader(3 + list.size());
            packer.packString(scope);
            packer.packLong(room.getId().longValue());
            packer.packString(event);

            for (Value v : list) {
                packer.packValue(v);
            }
            packer.close();
            return ensureWrite(Proto.REQ_EMIT, packer);
        } catch (IOException ex) {
            CompletableFuture<Result> future = new CompletableFuture<>();
            future.completeExceptionally(ex);
            return future;
        }
    }

    protected void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    protected void delRoom(Room room) {
        rooms.remove(room.getId());
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
                return;
            case ON_WARN:
                res = Result.newResult(pkg.getData());
                WarnEvent warnEvent;
                try {
                    warnEvent = WarnEvent.newFromResult(res);
                } catch (IOException e) {
                    log.error("Failed to unpack ThingsDB warning event");
                    return;
                }
                if (onWarning == null) {
                    log.warn("ThingsDB: %s (%d)", warnEvent.msg, warnEvent.code);
                } else {
                    onWarning.apply(warnEvent);
                }
                return;
            case ON_ROOM_JOIN:
            case ON_ROOM_LEAVE:
            case ON_ROOM_EVENT:
            case ON_ROOM_DELETE:

                res = Result.newResult(pkg.getData());
                RoomEvent ev;
                Room room;
                try {
                    ev = RoomEvent.newFromResult(res, pkg.proto);
                }  catch (IOException e) {
                    log.error("Failed to unpack ThingsDB room event");
                    return;
                }
                room = rooms.get(ev.id);
                if (room != null) {
                    room.onEvent(ev);
                }

                return;
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
                try {
                    write(Proto.REQ_AUTH, authPacker).get();
                } catch (Exception e) {
                    conn.close();
                    return;
                }
                // Re-join rooms
                Map<String, ArrayList<Long>> roomMap = new HashMap<>();
                for (Room room : rooms.values()) {
                    roomMap.computeIfAbsent(room.getScope(), s -> new ArrayList<>()).add(room.getId());
                }
                for (String scope : roomMap.keySet()) {
                    join(scope, roomMap.get(scope));
                }
            }
        } catch (IOException ex) {};
    }

    private void alive() {
        executor.execute(() -> {
            try {
                while (autoReconnect) {
                    if (!conn.isConnected()) {
                        reconnect();
                    }
                    TimeUnit.MILLISECONDS.sleep(500);
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

    protected void asyncJoin(Room room, CompletableFuture<Room> future) {
        executor.execute(() -> {
            try {
                room.asyncJoin();
            } catch (JoinException ex) {
                future.completeExceptionally(ex);
            }
        });
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