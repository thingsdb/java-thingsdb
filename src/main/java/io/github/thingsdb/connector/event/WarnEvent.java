package io.github.thingsdb.connector.event;

import java.io.IOException;

import io.github.thingsdb.connector.Result;

public class WarnEvent {
    public final int code;
    public final String msg;

    static public WarnEvent newFromResult(Result result) throws IOException {
        result.unpackMapHeader();

        result.unpackString();  // warn_code
        int code = result.unpackInt();

        result.unpackString();  // warn_msg
        String msg = result.unpackString();

        return new WarnEvent(code, msg);
    }

    public WarnEvent(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
