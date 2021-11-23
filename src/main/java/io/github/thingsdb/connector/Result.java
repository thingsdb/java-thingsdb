package io.github.thingsdb.connector;

import java.nio.ByteBuffer;

import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.MessagePack.UnpackerConfig;
import org.msgpack.core.buffer.ByteBufferInput;
import org.msgpack.core.buffer.MessageBufferInput;

public class Result extends MessageUnpacker{

    private static final UnpackerConfig DEFAULT_UNPACKER_CONFIG = new UnpackerConfig();
    private static final MessageBufferInput EMPTY = new ByteBufferInput(ByteBuffer.allocateDirect(0));

    public final ResultType TYPE;
    public static final Result RESULT_OK = new Result(ResultType.OK, EMPTY, DEFAULT_UNPACKER_CONFIG);
    public static final Result RESULT_PONG = new Result(ResultType.PONG, EMPTY, DEFAULT_UNPACKER_CONFIG);

    protected Result(ResultType type, MessageBufferInput in, UnpackerConfig config) {
        super(in, config);
        TYPE = type;
    }

    protected static Result newResult(ByteBuffer contents) {
        return new Result(ResultType.DATA, new ByteBufferInput(contents), DEFAULT_UNPACKER_CONFIG);
    }
}
