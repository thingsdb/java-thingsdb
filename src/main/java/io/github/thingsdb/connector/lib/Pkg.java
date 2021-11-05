package io.github.thingsdb.connector.lib;

import java.nio.ByteBuffer;

import io.github.thingsdb.connector.exceptions.ProtoNotFound;

public class Pkg {

    public final Proto proto;
    public int pid;
    public byte[] data;

    public Pkg(Proto proto, int pid, int size) {
        this.proto = proto;
        this.pid = pid;
        this.data = new byte[size];
    }

    public static Pkg newFromByteBuffer(ByteBuffer buf) throws ProtoNotFound {
        int size = buf.getInt();
        int pid = (int) buf.getShort();
        Proto proto = Proto.fromType((int) buf.get());
        Pkg pkg = new Pkg(proto, pid, size);
        buf.get(pkg.data, 0, size);
        return pkg;

    }
}
