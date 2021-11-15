package io.github.thingsdb.connector.lib;

import io.github.thingsdb.connector.exceptions.ProtoUnknown;


public enum Proto {
	/**
	 * Events
	 */

    ON_NODE_STATUS  ((byte) 0, "ProtoOnNodeStatus"),
    ON_WARN         ((byte) 5, "ProtoOnWarn"),
    ON_ROOM_JOIN    ((byte) 6, "ProtoOnRoomJoin"),
    ON_ROOM_LEAVE   ((byte) 7, "ProtoOnRoomLeave"),
    ON_ROOM_EVENT   ((byte) 8, "ProtoOnRoomEvent"),
    ON_ROOM_DELETE  ((byte) 9, "ProtoOnRoomDelete"),

	/**
	 * Responses
	 */
    RES_PONG        ((byte) 16, "ProtoResPong"),
    RES_OK          ((byte) 17, "ProtoResOk"),
    RES_DATA        ((byte) 18, "ProtoResData"),
    RES_ERROR       ((byte) 19, "ProtoResError"),

	/**
	 * Requests
	 */

    REQ_PING        ((byte) 32, "ProtoReqPing"),
    REQ_AUTH        ((byte) 33, "ProtoReqAuth"),
    REQ_QUERY       ((byte) 34, "ProtoReqQuery"),
    REQ_RUN         ((byte) 37, "ProtoReqRun"),
    REQ_JOIN        ((byte) 38, "ProtoReqJoin"),
    REQ_LEAVE       ((byte) 39, "ProtoReqLeave"),
    REQ_EMIT        ((byte) 40, "ProtoReqEmit");

    private final byte tp;
    private final String name;

    Proto(byte tp, String name) {
        this.tp = tp;
        this.name = name;
    }

    public byte type() { return tp; };
    public String toString() { return name; };

    public static Proto fromType(byte tp) throws ProtoUnknown {
        for (Proto proto : Proto.values()) {
            if (proto.tp == tp) {
                return proto;
            }
        }
        throw new ProtoUnknown(String.format("Proto with type %d not found", (int) tp));
    }
}
