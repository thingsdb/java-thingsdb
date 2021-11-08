package io.github.thingsdb.connector.lib;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

public class Conn extends Thread {

    public final SocketChannel channel;
    static ByteBuffer buf = null;

    public Conn(Node node) throws IOException {
        channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(node.getSocketAddress());
        buf = ByteBuffer.allocate(8192);
        buf.order(ByteOrder.LITTLE_ENDIAN);
    }

    public void run() {
        while (true) {
            int n;
            try {
                n = channel.read(buf);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            if (n == -1) {
                break;
            } else {
                buf.flip();
            }
        }

        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
