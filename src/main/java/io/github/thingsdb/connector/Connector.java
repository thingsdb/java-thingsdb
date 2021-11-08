package io.github.thingsdb.connector;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
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
import io.github.thingsdb.connector.lib.Result;

/**
 *
 */
public class Connector implements ConnectorInterface {

    public boolean autoReconnect = true;

    private final Map<Integer, CompletableFuture<byte[]>> respMap;
    private final List<Node> nodes;
    private final ExecutorService executor;
    private char nextPid;
    private int activeNodeId;
    private ReentrantLock mutex = new ReentrantLock();
    private Conn conn = null;

    public Connector(String host) {
        this(host, 9200, 4);
    }

    public Connector(String host, int port, int nThreads) {
        nodes = Collections.synchronizedList(new ArrayList<>());
        respMap =  Collections.synchronizedMap(new HashMap<>());

        executor = Executors.newFixedThreadPool(nThreads);
        nextPid = 0;
        conn = null;

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

    public void query(String code) {
        ensureWrite(code);
    }

    public void connect() throws IOException {
        Node node = getNode();
        conn = new Conn(node);
        conn.start();
    }

    public void authenticate(String token) {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer.packString(token);
        packer.close();

        ensureWrite(Proto.REQ_AUTH, packer);

        conn.channel.write(buf);
    }

    private Node getNode() {
        return nodes.get(activeNodeId);
    }

    private synchronized Integer getNextPid() {
        char pid = nextPid;
        nextPid++;
        return Integer.valueOf(pid);
    }

    private Future<Result> ensureWrite(Proto proto, MessageBufferPacker packer) throws IOException {
        Integer pid = getNextPid();

        CompletableFuture<byte[]> future = new CompletableFuture<>();

        respMap.put(pid, future);

        Pkg pkg = Pkg.newFromPacker(proto, pid, packer);

        conn.channel.write(pkg.getBytes());

        future.completedFuture();

        return future;
    }
}