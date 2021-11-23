package io.github.thingsdb.connector;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    private final Map<String, Method> eventHandlers;
    private Long id;
    private CompletableFuture<Boolean> waitJoinfuture;

    private static Logger log = LoggerFactory.getLogger(Conn.class);

    public Room(Connector client, String scope, String code) {
        this.client = client;
        this.code = code;
        this.scope = scope;
        this.id = null;
        eventHandlers = new HashMap<String, Method>();
        System.out.println("INIT ROOM..");
    }

    public Room(Connector client, String scope, Long id) {
        this.client = client;
        this.id = id;
        this.scope = scope;
        this.code = null;
        eventHandlers = new HashMap<String, Method>();
        System.out.println("INIT ROOM..");
    }

    public Room(Connector client, String code) {
        this.client = client;
        this.code = code;
        this.id = null;
        this.scope = client.getDefaultScope();
        eventHandlers = new HashMap<String, Method>();
        System.out.println("INIT ROOM..");
    }

    public Room(Connector client, Long id) {
        this.client = client;
        this.id = id;
        this.code = null;
        this.scope = client.getDefaultScope();
        eventHandlers = new HashMap<String, Method>();
        System.out.println("INIT ROOM..");
    }

    public Future<Boolean> join() {
        waitJoinfuture = new CompletableFuture<>();
        client.asyncJoin(this, waitJoinfuture);
        return waitJoinfuture;
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
                throw new JoinException(String.format("Room Id %l not found. The Id was returned using ThingsDB code: %s", roomId, code));
            }
        } else {
            roomId = id.longValue();

            try {
                res = client.join(scope, roomId).get();
                nRooms = res.unpackArrayHeader();

                if (nRooms == 0) {
                    throw new JoinException(String.format("Room Id %l not found", roomId));
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
                log.info("ON JOIN");
                System.out.println("!!! ON JOIN !!!");
                onJoin();
                if (!waitJoinfuture.isDone()) {
                    waitJoinfuture.complete(true);
                }
                break;
            case ON_ROOM_EVENT:
                Method func = eventHandlers.get(ev.event);
                if (func != null) {
                    try {
                        func.invoke(this, ev.args);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        log.error("Failed to invoke handler for event `%s`: %s", ev.event, e.getMessage());
                    }
                    return;
                }
                onEmit(ev.event, ev.args);
            default:
                break;
        };
    }

    protected void handleEvent(String event, String methodName) throws NoSuchMethodException, SecurityException {
        System.out.println("ADDING EVENT HANDLER");
        Method m = this.getClass().getMethod(methodName, Result.class);
        eventHandlers.put(event, m);
    }

    public Long getId() {
        return id;
    }

    public String getScope() {
        return scope;
    }
}
