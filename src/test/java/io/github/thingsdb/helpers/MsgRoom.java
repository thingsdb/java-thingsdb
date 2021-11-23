package io.github.thingsdb.helpers;

import java.lang.reflect.Method;

import io.github.thingsdb.connector.Connector;
import io.github.thingsdb.connector.Result;
import io.github.thingsdb.connector.Room;

public class MsgRoom extends Room {

    public MsgRoom(Connector client, String scope, String code) {
        super(client, scope, code);
    }

    void onMsg(Result args) {
        System.out.println("onMsg...");
    }

    @Override
    public void onInit() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onJoin() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLeave() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDelete() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onEmit(String event, Result args) {
        // TODO Auto-generated method stub

    }
}
