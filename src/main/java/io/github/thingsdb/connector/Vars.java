package io.github.thingsdb.connector;

import java.io.IOException;
import java.util.List;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.buffer.MessageBuffer;
import org.msgpack.value.ValueFactory;
import org.msgpack.value.ValueFactory.MapBuilder;


public class Vars implements ArgumentInterface {

    private final MapBuilder map;
    private MessageBufferPacker packer;

    public Vars() {
        super();
        map = ValueFactory.newMapBuilder();
    }

    public Vars setNil(String key) throws IOException {
        packer = null;
        map.put(ValueFactory.newString(key), ValueFactory.newNil());
        return this;
    }

    public Vars setInt(String key, int value) throws IOException {
        packer = null;
        map.put(ValueFactory.newString(key), ValueFactory.newInteger(value));
        return this;
    }

    public Vars setString(String key, String value) throws IOException {
        packer = null;
        map.put(ValueFactory.newString(key), ValueFactory.newString(value));
        return this;
    }

    @Override
    public List<MessageBuffer> toBufferList() throws IOException {
        if (packer == null) {
            packer = MessagePack.newDefaultBufferPacker();
            packer.packValue(map.build());
            packer.close();
        }
        return packer.toBufferList();
    }
}
