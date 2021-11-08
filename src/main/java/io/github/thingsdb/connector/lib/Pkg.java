package io.github.thingsdb.connector.lib;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.buffer.MessageBuffer;

import io.github.thingsdb.connector.exceptions.ProtoCheckbitFailure;
import io.github.thingsdb.connector.exceptions.ProtoException;

public class Pkg {

    public final Proto proto;
    public int pid;

    private byte[] data;
    private static final int PKG_HEADER_SIZE = 8;

    public Pkg(Proto proto, int pid, int size) {
        this.proto = proto;
        this.pid = pid;

        data = new byte[PKG_HEADER_SIZE + size];
        ByteBuffer buf = ByteBuffer.wrap(data, 0, PKG_HEADER_SIZE);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(size);
        buf.putShort((short) pid);
        buf.put(proto.type());
        buf.put((byte) (proto.type() ^ 0xff));
    }

    public static Pkg newFromPacker(Proto proto, int pid, MessageBufferPacker packer) {
        int size = packer.getBufferSize();
        Pkg pkg = new Pkg(proto, pid, size);

        List<MessageBuffer> list = packer.toBufferList();
        int off = Pkg.PKG_HEADER_SIZE;
        for (MessageBuffer buffer : list) {
            buffer.getBytes(0, pkg.data, off, buffer.size());
            off += buffer.size();
        }

        return pkg;
    }

    public static Pkg newFromByteBuffer(ByteBuffer buf) throws ProtoException {
        // Read all the Package header variable
        int size = buf.getInt();
        int pid = (int) (buf.getShort() & 0xffff);
        byte protoType = buf.get();
        byte protoChk = buf.get();

        // Check if the checkbit is correct
        if (protoChk != (byte) (protoType ^ 0xff)) {
            throw new ProtoCheckbitFailure("Invalid package check bit");
        }
        // Find the correct proto type
        Proto proto = Proto.fromType(protoType);

        // Create the package
        Pkg pkg = new Pkg(proto, pid, size);

        // Load the package data into the package buffer
        buf.get(pkg.data, buf.position(), size);

        return pkg;
    }

    public int getDataSize() {
        return data.length - PKG_HEADER_SIZE;
    }

    public ByteBuffer getData() {
        // We do not care about ENDIANESS here since this is the data which
        // will most likely be used for MessagePack or something similar
        return ByteBuffer.wrap(data, PKG_HEADER_SIZE, getDataSize());
    }

    public ByteBuffer getBytes() {
        ByteBuffer buf = ByteBuffer.wrap(data, 0, data.length);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf;
    }
}
