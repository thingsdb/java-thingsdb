package io.github.thingsdb.connector.event;

import java.io.IOException;

import io.github.thingsdb.connector.Proto;
import io.github.thingsdb.connector.Result;

public class RoomEvent {
    public final Proto proto;
    public final String scope;
    public final long id;
    public final String event;
    public final Result args;

    static public RoomEvent newFromResult(Result result, Proto proto) throws IOException {
        int size = result.unpackMapHeader();
        long id = 0;
        String event = null;
        String scope = null;
        Result args = null;

        for (int i = 0; i < size; i++) {
            String key = result.unpackString();

            if ("id".equals(key)) {
                id = result.unpackLong();
            } else if ("scope".equals(key)) {
                scope = result.unpackString();
            } else if ("event".equals(key)) {
                event = result.unpackString();
            } else if ("args".equals(key)) {
                args = result;
                // Assign the remaining unpacked to args
                // Since "args" is always last, we can stop processing keys
                break;
            } else {
                result.skipValue();
            }
        }
        return new RoomEvent(proto, scope, id, event, args);
    }

    public RoomEvent(Proto proto, String scope, long id, String event, Result args) {
        this.proto = proto;
        this.scope = scope;
        this.id = id;
        this.event = event;
        this.args = args;
    }
}
