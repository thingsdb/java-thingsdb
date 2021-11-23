package io.github.thingsdb.connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.msgpack.core.buffer.MessageBuffer;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;


public class Args extends VarsOrArgs {

    private final List<Value> arr;

    public Args() {
        super();
        arr = new ArrayList<>();
    }

    public void addNil(String key) throws IOException {
        packer = null;
        arr.add(ValueFactory.newNil());
    }

    public void addInt(String key, int value) throws IOException {
        packer = null;
        arr.add(ValueFactory.newInteger(value));
    }

    public void addLong(String key, long value) throws IOException {
        packer = null;
        arr.add(ValueFactory.newInteger(value));
    }

    public void addString(String key, String value) throws IOException {
        packer = null;
        arr.add(ValueFactory.newString(value));
    }

    public List<MessageBuffer> toBufferList() throws IOException {
        return super.toBufferList(((Value) arr));
    }
}
