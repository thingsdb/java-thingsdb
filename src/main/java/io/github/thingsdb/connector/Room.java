package io.github.thingsdb.connector;

import io.github.thingsdb.connector.event.RoomEvent;

abstract public class Room implements RoomInterface {
    protected void onEvent(RoomEvent ev) {

    }
}
