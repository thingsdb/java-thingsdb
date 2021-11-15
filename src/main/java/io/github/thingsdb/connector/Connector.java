package io.github.thingsdb.connector;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import io.github.thingsdb.connector.lib.Conn;
import io.github.thingsdb.connector.lib.Node;
import io.github.thingsdb.connector.lib.Pkg;
import io.github.thingsdb.connector.lib.Proto;
import io.github.thingsdb.connector.lib.RespMap;
import io.github.thingsdb.connector.lib.Result;

/**
 *
 */
public class Connector implements ConnectorInterface {

    public boolean autoReconnect = true;

    private final List<Node> nodes;
    private final ExecutorService executor;
    private char nextPid;
    private int activeNodeId;
    private ReentrantLock mutex = new ReentrantLock();
    private Conn conn = null;
    private RespMap respMap;
    private String scope;

    public Connector(String host) {
        this(host, 9200, 4);
    }

    public Connector(String host, int port, int nThreads) {
        nodes = Collections.synchronizedList(new ArrayList<>());
        respMap = new RespMap();

        executor = Executors.newFixedThreadPool(nThreads);
        nextPid = 0;
        conn = null;
        scope = "/thingsdb";

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

    public void connect() throws IOException {
        Node node = getNode();
        conn = new Conn(node, respMap);
        conn.start();
    }

    public Future<Result> authenticate(String token) {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        try {
            packer.packString(token);
            packer.close();

            return ensureWrite(Proto.REQ_AUTH, packer);
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
        return query(code, scope, args);
    }

    public Future<Result> query(String code) {
        return query(code, scope, null);
    }

    private Node getNode() {
        return nodes.get(activeNodeId);
    }

    private synchronized Integer getNextPid() {
        char pid = nextPid;
        nextPid++;
        return Integer.valueOf(pid);
    }

    private CompletableFuture<Result> ensureWrite(Proto proto, MessageBufferPacker packer) throws IOException {
        Integer pid = getNextPid();

        CompletableFuture<Result> future = respMap.register(pid);

        Pkg pkg = Pkg.newFromPacker(proto, pid.intValue(), packer);

        conn.write(pkg.getBytes());

        return future;
    }
}