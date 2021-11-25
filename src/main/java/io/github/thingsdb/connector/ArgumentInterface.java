package io.github.thingsdb.connector;

import java.io.IOException;
import java.util.List;

import org.msgpack.core.buffer.MessageBuffer;

public interface ArgumentInterface {

    /**
     * Used to create a buffer list which is the fastest method to gain the
     * underlying MessagePack data.
     *
     * This method may be called multiple times.
     *
     * @return the written data as a list of MessageBuffer.
     * @throws IOException
     */
    public List<MessageBuffer> toBufferList() throws IOException;
}
