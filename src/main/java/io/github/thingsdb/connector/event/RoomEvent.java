package io.github.thingsdb.connector.event;

import java.io.IOException;

import io.github.thingsdb.connector.Proto;
import io.github.thingsdb.connector.Result;

public class RoomEvent {
    public final Proto proto;
    public final int id;
    public final String event;
    public final Result args;

    static public RoomEvent newFromResult(Result result, Proto proto) throws IOException {
        int size = result.unpackMapHeader();

        result.unpackString();  // id
        int id = result.unpackInt();
        String event = null;
        Result args = null;

        if (size == 3) {
            result.unpackString();  // event
            event = result.unpackString();

            result.unpackString();  // args
            args = result;
        }
        return new RoomEvent(proto, id, event, args);
    }

    public RoomEvent(Proto proto, int id, String event, Result args) {
        this.proto = proto;
        this.id = id;
        this.event = event;
        this.args = args;
    }
}
