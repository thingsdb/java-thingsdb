package io.github.thingsdb;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import io.github.thingsdb.connector.exceptions.ProtoException;
import io.github.thingsdb.connector.lib.Pkg;
import io.github.thingsdb.connector.lib.Proto;

/**
 * Unit test for simple App.
 */
public class PkgTest
{
    /**
     * Rigorous Test :-)
     * @throws ProtoException
     * @throws IOException
     */
    @Test
    public void expectValidPkg() throws ProtoException, IOException
    {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        packer
                .packArrayHeader(2)
                .packString("Hello")
                .packString("ThingsDB");
        packer.close();

        int pid = 65_000;

        Pkg pkg = Pkg.newFromPacker(Proto.REQ_QUERY, pid, packer);

        assertTrue( pkg.pid == pid );

        ByteBuffer buf = pkg.getBytes();

        Pkg cmpPkg = Pkg.newFromByteBuffer(buf);

        System.out.println(cmpPkg.pid);

        assertTrue( pkg.pid == cmpPkg.pid );


        MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(cmpPkg.getBytes());
        int numStr = unpacker.unpackArrayHeader();

        assertTrue( numStr == 2 );

        String[] strs = new String[numStr];
        for (int i = 0; i < numStr; ++i) {
            strs[i] = unpacker.unpackString();
        }
        unpacker.close();

        System.out.println(strs[0]);

        assertTrue( strs[0].equals("Hello") );
        assertTrue( strs[1].equals("ThingsDB"));
    }
}
