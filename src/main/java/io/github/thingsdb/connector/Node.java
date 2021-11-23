package io.github.thingsdb.connector;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

class Node {
    final String host;
    final int port;

    Node(String host, int port)  {
        this.host = host;
        this.port = port;
    }

    InetSocketAddress getSocketAddress() throws UnknownHostException {
        InetAddress nodeInetAddress = InetAddress.getByName(host);
        return new InetSocketAddress(nodeInetAddress, port);
    }
}