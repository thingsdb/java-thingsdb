package io.github.thingsdb.connector;

import java.io.IOException;
import java.util.List;

import org.msgpack.core.buffer.MessageBuffer;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;
import org.msgpack.value.ValueFactory.MapBuilder;

public class Vars extends VarsOrArgs {

    private final MapBuilder map;

    public Vars() {
        super();
        map = ValueFactory.newMapBuilder();
    }

    public void setNil(String key) throws IOException {
        packer = null;
        map.put(ValueFactory.newString(key), ValueFactory.newNil());
    }

    public void setInt(String key, int value) throws IOException {
        packer = null;
        map.put(ValueFactory.newString(key), ValueFactory.newInteger(value));
    }

    public void setString(String key, String value) throws IOException {
        packer = null;
        map.put(ValueFactory.newString(key), ValueFactory.newString(value));
    }

    public List<MessageBuffer> toBufferList() throws IOException {
        return super.toBufferList(map.build());
    }
}
