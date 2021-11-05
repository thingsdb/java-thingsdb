package io.github.thingsdb.connector.lib;

import io.github.thingsdb.connector.exceptions.ProtoNotFound;


public enum Proto {
	/**
	 * Events
	 */

    ON_NODE_STATUS  (0, "ProtoOnNodeStatus"),
    ON_WARN         (5, "ProtoOnWarn"),
    ON_ROOM_JOIN    (6, "ProtoOnRoomJoin"),
    ON_ROOM_LEAVE   (7, "ProtoOnRoomLeave"),
    ON_ROOM_EVENT   (8, "ProtoOnRoomEvent"),
    ON_ROOM_DELETE  (9, "ProtoOnRoomDelete"),

	/**
	 * Responses
	 */
    RES_PONG        (16, "ProtoResPong"),
    RES_OK          (17, "ProtoResOk"),
    RES_DATA        (18, "ProtoResData"),
    RES_ERROR       (19, "ProtoResError"),

	/**
	 * Requests
	 */

    REQ_PING        (32, "ProtoReqPing"),
    REQ_AUTH        (33, "ProtoReqAuth"),
    REQ_QUERY       (34, "ProtoReqQuery"),
    REQ_RUN         (37, "ProtoReqRun"),
    REQ_JOIN        (38, "ProtoReqJoin"),
    REQ_LEAVE       (39, "ProtoReqLeave"),
    REQ_EMIT        (40, "ProtoReqEmit");

    private final int tp;
    private final String name;

    Proto(int tp, String name) {
        this.tp = tp;
        this.name = name;
    }

    public int type() { return tp; };
    public String toString() { return name; };

    public static Proto fromType(int tp) throws ProtoNotFound {
        for (Proto proto : Proto.values()) {
            if (proto.tp == tp) {
                return proto;
            }
        }
        throw new ProtoNotFound(String.format("Proto with type %d not found", tp));
    }
}
