package io.github.thingsdb.connector;

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

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import io.github.thingsdb.connector.lib.Node;
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
    private int activeNodeId = 0;
    private ReentrantLock mutex = new ReentrantLock();

    public Connector(String host) {
        this(host, 9200, 4);
    }

    public Connector(String host, int port, int nThreads) {
        nodes = Collections.synchronizedList(new ArrayList<>());
        respMap =  Collections.synchronizedMap(new HashMap<>());

        executor = Executors.newFixedThreadPool(nThreads);
        nextPid = 0;

        addNode(host, port);
        // completionHandlers = new HashMap<>();
    }

    @Override
    public void addNode(String host, int port) {
        nodes.add(new Node(host, port));
    }

    @Override
    public void addNode(String host) {
        nodes.add(new Node(host, 9200));
    }

    public query(String code) {
        ensureWrite()
    }

    private void connect() {
        InetAddress serverIPAddress = InetAddress.getByName("localhost");
        int port = 19000;
        InetSocketAddress serverAddress = new InetSocketAddress(
            serverIPAddress, port);
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(serverAddress);
        Selector selector = Selector.open();
        int operations = SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE;
        channel.register(selector, operations);
    }

    private synchronized Integer getNextPid() {
        char pid = nextPid;
        nextPid++;
        return Integer.valueOf(pid);
    }

    private Future<Result> ensureWrite(String code) {
        Integer pid = getNextPid();

        CompletableFuture<byte[]> future = new CompletableFuture<>();

        respMap.put(pid, future);

        return executor.submit(() -> {
            Thread.sleep(1000);
            return Result.newResult(code.getBytes());
        });

    }

}