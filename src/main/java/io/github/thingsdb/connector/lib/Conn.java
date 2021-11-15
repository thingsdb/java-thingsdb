package io.github.thingsdb.connector.lib;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.thingsdb.connector.exceptions.PackageIdNotFound;
import io.github.thingsdb.connector.exceptions.ProtoException;

public class Conn extends Thread {

    private static Logger log = LoggerFactory.getLogger(Conn.class);

    private final SocketChannel channel;
    private ByteBuffer buf = null;
    private final RespMap respMap;

    public Conn(Node node, RespMap respMap) throws IOException {
        channel = SocketChannel.open();
        channel.configureBlocking(true);
        channel.connect(node.getSocketAddress());
        buf = ByteBuffer.allocate(0xffff);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        this.respMap = respMap;
    }

    public void run() {

        Pkg pkg = null;
        ByteBuffer rbuf = buf;

        while (true) {
            int n;
            try {
                n = channel.read(rbuf);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

            if (n == -1) {
                break;
            }

            rbuf.flip();

            if (pkg != null) {

                if (rbuf.remaining() == rbuf.capacity()) {
                    try {
                        respMap.handle(pkg);
                    } catch (PackageIdNotFound e) {
                        log.error(e.getMessage());
                    }
                    pkg = null;
                    rbuf = buf;
                    rbuf.rewind();
                } else {
                    // Restore the position to the limit
                    rbuf.position(rbuf.limit());
                }
                continue;
            }

            while (true) {
                if (rbuf.remaining() < Pkg.PKG_HEADER_SIZE) {
                    rbuf.position(rbuf.limit());
                    break;
                }
                try {
                    pkg = Pkg.newFromByteBuffer(rbuf);
                } catch (ProtoException e) {
                    log.error(e.getMessage());
                    rbuf.rewind();
                    break;
                }

                if (rbuf.remaining() >= pkg.getDataSize()) {
                    pkg.setData(rbuf);
                    try {
                        respMap.handle(pkg);
                    } catch (PackageIdNotFound e) {
                        log.error(e.getMessage());
                    }
                    pkg = null;

                    if (rbuf.hasRemaining()) {
                        continue;
                    }

                    rbuf.rewind();
                    break;
                }

                rbuf = pkg.getWriteBuffer();
                break;
            }
        }

        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int write(ByteBuffer buf) throws IOException {
        return channel.write(buf);
    }
}
