package io.github.thingsdb.connector;

public interface RoomInterface {
    void onInit();
    void onJoin();
    void onLeave();
    void onDelete();
    void onEmit(String event, Result args);
}
