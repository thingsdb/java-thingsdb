package io.github.thingsdb.connector;

import java.io.IOException;
import java.util.List;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.buffer.MessageBuffer;
import org.msgpack.value.Value;


class VarsOrArgs {

    protected MessageBufferPacker packer;

    VarsOrArgs() {
        packer = null;
    }

    public List<MessageBuffer> toBufferList(Value v) throws IOException {
        if (packer == null) {
            packer = MessagePack.newDefaultBufferPacker();
            packer.packValue(v);
            packer.close();
        }
        return packer.toBufferList();
    }
}
