package io.github.thingsdb.connector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.thingsdb.connector.event.RoomEvent;
import io.github.thingsdb.connector.exceptions.JoinException;


abstract public class Room implements RoomInterface {

    public final Connector client;

    private final String code;
    private final String scope;
    private final Map<String, Handler> eventHandlers;
    private Long id;
    private CompletableFuture<Room> waitJoinfuture;

    private static Logger log = LoggerFactory.getLogger(Conn.class);

    public interface Handler {
        void call(Result args);
    }

    public Room on(String event, Handler fn) {
        this.eventHandlers.put(event, fn);
        return this;
    }

    public Room(Connector client, String scope, String code) {
        this.client = client;
        this.code = code;
        this.scope = scope;
        this.id = null;
        eventHandlers = new HashMap<String, Handler>();
    }

    public Room(Connector client, String scope, Long id) {
        this.client = client;
        this.id = id;
        this.scope = scope;
        this.code = null;
        eventHandlers = new HashMap<String, Handler>();
    }

    public Room(Connector client, String code) {
        this.client = client;
        this.code = code;
        this.id = null;
        this.scope = client.getDefaultScope();
        eventHandlers = new HashMap<String, Handler>();
    }

    public Room(Connector client, Long id) {
        this.client = client;
        this.id = id;
        this.code = null;
        this.scope = client.getDefaultScope();
        eventHandlers = new HashMap<String, Handler>();
    }

    public Future<Room> join() {
        waitJoinfuture = new CompletableFuture<>();
        client.asyncJoin(this, waitJoinfuture);
        return waitJoinfuture;
    }

    public Future<Result> leave() throws JoinException {
        if (id == null) {
            throw new JoinException("Room Id is zero (0), most likely the room has never been joined");
        }
        return client.leave(scope, id);
    }

    public Future<Result> emit(String event, Args args) {
        return client.emit(scope, this, event, args);
    }

    protected void asyncJoin() throws JoinException {
        long roomId;
        int nRooms;
        Result res;

        if (id == null) {
            try {
                res = client.query(scope, code).get();
            } catch (Exception e) {
                throw new JoinException(e.getMessage());
            }

            try {
                roomId = res.unpackLong();
            } catch (IOException e) {
                throw new JoinException(String.format("Expecting code `%s` to return with a room Id (type integer)", code));
            }

            try {
                res = client.join(scope, roomId).get();
                nRooms = res.unpackArrayHeader();
            } catch (Exception e) {
                throw new JoinException(e.getMessage());
            }

            if (nRooms == 0) {
                throw new JoinException(String.format("Room Id %d not found. The Id was returned using ThingsDB code: %s", roomId, code));
            }
        } else {
            roomId = id.longValue();

            try {
                res = client.join(scope, roomId).get();
                nRooms = res.unpackArrayHeader();

                if (nRooms == 0) {
                    throw new JoinException(String.format("Room Id %d not found", roomId));
                }
                roomId = res.unpackLong();
            } catch (Exception e) {
                throw new JoinException(e.getMessage());
            }
        }

        id = Long.valueOf(roomId);
        client.addRoom(this);
        this.onInit();
    }

    protected void onEvent(RoomEvent ev) {
        switch (ev.proto) {
            case ON_ROOM_JOIN:
                onJoin();
                if (!waitJoinfuture.isDone()) {
                    waitJoinfuture.complete(this);
                }
                break;
            case ON_ROOM_LEAVE:
                client.delRoom(this);
                this.onLeave();
                break;
            case ON_ROOM_EVENT:
                Handler fn = eventHandlers.get(ev.event);
                if (fn != null) {
                    try {
                        fn.call(ev.args);
                    } catch (Exception ex) {
                        log.error(ex.getMessage());
                    }
                    return;
                }
                onEmit(ev.event, ev.args);
                break;
            case ON_ROOM_DELETE:
                client.delRoom(this);
                this.onDelete();
                break;

            default:
                break;
        };
    }

    public Long getId() {
        return id;
    }

    public String getScope() {
        return scope;
    }
}
