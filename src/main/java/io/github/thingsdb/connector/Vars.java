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

    public Vars setBool(String key, boolean value) throws IOException {
        packer = null;
        map.put(ValueFactory.newString(key), ValueFactory.newBoolean(value));
        return this;
    }

    public Vars setInt(String key, int value) throws IOException {
        packer = null;
        map.put(ValueFactory.newString(key), ValueFactory.newInteger(value));
        return this;
    }

    public Vars setLong(String key, long value) throws IOException {
        packer = null;
        map.put(ValueFactory.newString(key), ValueFactory.newInteger(value));
        return this;
    }

    public Vars setFloat(String key, float value) throws IOException {
        packer = null;
        map.put(ValueFactory.newString(key), ValueFactory.newFloat(value));
        return this;
    }

    public Vars setDouble(String key, double value) throws IOException {
        packer = null;
        map.put(ValueFactory.newString(key), ValueFactory.newFloat(value));
        return this;
    }

    public Vars setString(String key, String value) throws IOException {
        packer = null;
        map.put(ValueFactory.newString(key), ValueFactory.newString(value));
        return this;
    }

    public Vars setArgs(String key, Args value) throws IOException {
        packer = null;
        map.put(ValueFactory.newString(key), ValueFactory.newArray(value.getList()));
        return this;
    }

    public Vars setVars(String key, Vars value) throws IOException {
        packer = null;
        map.put(ValueFactory.newString(key), value.getMap().build());
        return this;
    }

    protected MapBuilder getMap() {
        return map;
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
