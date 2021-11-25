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

    public Args addNil() throws IOException {
        packer = null;
        arr.add(ValueFactory.newNil());
        return this;
    }

    public Args addBool(boolean value) throws IOException {
        packer = null;
        arr.add(ValueFactory.newBoolean(value));
        return this;
    }

    public Args addInt(int value) throws IOException {
        packer = null;
        arr.add(ValueFactory.newInteger(value));
        return this;
    }

    public Args addLong(long value) throws IOException {
        packer = null;
        arr.add(ValueFactory.newInteger(value));
        return this;
    }

    public Args addFloat(float value) throws IOException {
        packer = null;
        arr.add(ValueFactory.newFloat(value));
        return this;
    }

    public Args addDouble(double value) throws IOException {
        packer = null;
        arr.add(ValueFactory.newFloat(value));
        return this;
    }

    public Args addString(String value) throws IOException {
        packer = null;
        arr.add(ValueFactory.newString(value));
        return this;
    }

    public Args addArgs(Args value) throws IOException {
        packer = null;
        arr.add(ValueFactory.newArray(value.getList()));
        return this;
    }

    public Args addVars(Vars value) throws IOException {
        packer = null;
        arr.add(value.getMap().build());
        return this;
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
