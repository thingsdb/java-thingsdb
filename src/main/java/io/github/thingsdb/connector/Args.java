package io.github.thingsdb.connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.buffer.MessageBuffer;
import org.msgpack.value.Value;
import org.msgpack.value.ValueFactory;


public class Args implements ArgumentInterface {

    private final List<Value> arr;
    private MessageBufferPacker packer;

    public Args() {
        packer = null;
        arr = new ArrayList<>();
    }

    public void addNil() throws IOException {
        packer = null;
        arr.add(ValueFactory.newNil());
    }

    public void addInt(int value) throws IOException {
        packer = null;
        arr.add(ValueFactory.newInteger(value));
    }

    public void addLong(long value) throws IOException {
        packer = null;
        arr.add(ValueFactory.newInteger(value));
    }

    public void addString(String value) throws IOException {
        packer = null;
        arr.add(ValueFactory.newString(value));
    }

    protected List<Value> getList() {
        return arr;
    }

    @Override
    public List<MessageBuffer> toBufferList() throws IOException {
        if (packer == null) {
            packer = MessagePack.newDefaultBufferPacker();
            packer.packValue(ValueFactory.newArray(arr));
            packer.close();
        }
        return packer.toBufferList();
    }
}
