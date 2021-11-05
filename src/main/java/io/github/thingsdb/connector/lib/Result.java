package io.github.thingsdb.connector.result;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.buffer.MessageBufferInput;

public class Result extends MessageUnpacker{
    public Result(MessageBufferInput in, MessagePack.UnpackerConfig config) {
        super(in, config);
    }

    public static Result newResult(byte[] contents)
    {
        MessageUnpacker unp = MessagePack.newDefaultUnpacker(contents);
        return Result.class.cast(unp);
    }
}
