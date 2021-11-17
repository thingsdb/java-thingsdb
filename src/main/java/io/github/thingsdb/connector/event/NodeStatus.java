package io.github.thingsdb.connector.event;

import java.io.IOException;

import io.github.thingsdb.connector.Result;

public class NodeStatus {
    public static final String AWAY = "AWAY";
    public static final String AWAY_SOON = "AWAY_SOON";
    public static final String OFFLINE = "OFFLINE";
    public static final String READY = "READY";
    public static final String SHUTTING_DOWN = "SHUTTING_DOWN";

    public final int id;
    public final String status;

    static public NodeStatus newFromResult(Result result) throws IOException {
        result.unpackMapHeader();

        result.unpackString();  // id
        int id = result.unpackInt();

        result.unpackString();  // status
        String status = result.unpackString();

        return new NodeStatus(id, status);
    }

    public NodeStatus(int id, String status) {
        this.id = id;
        this.status = status;
    }

    public boolean isAway() {
        return status.equals(NodeStatus.AWAY);
    }

    public boolean isAwaySoon() {
        return status.equals(NodeStatus.AWAY_SOON);
    }

    public boolean isOffline() {
        return status.equals(NodeStatus.OFFLINE);
    }

    public boolean isReady() {
        return status.equals(NodeStatus.READY);
    }

    public boolean isShuttingDown() {
        return status.equals(NodeStatus.SHUTTING_DOWN);
    }
}
