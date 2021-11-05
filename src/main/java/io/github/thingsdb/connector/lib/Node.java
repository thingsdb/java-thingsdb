package io.github.thingsdb.connector.node;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class Node {
    public final String host;
    public final int port;

    public Node(String host, int port)  {
        this.host = host;
        this.port = port;
    }

    public InetSocketAddress getSocketAddress() throws UnknownHostException {
        InetAddress nodeInetAddress = InetAddress.getByName(host);
        return new InetSocketAddress(nodeInetAddress, port);
    }
}