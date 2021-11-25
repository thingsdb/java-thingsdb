package io.github.thingsdb.helpers;

import io.github.thingsdb.connector.Connector;
import io.github.thingsdb.connector.Result;
import io.github.thingsdb.connector.Room;

public class ChatRoom extends Room {

    public ChatRoom(Connector client, String scope, String code) {
        super(client, scope, code);

        on("new-msg", new Room.Handler() {

            @Override
            public void call(Result args) {
                try {
                    args.unpackArrayHeader();
                    System.out.println(String.format("onMsg: %s (room Id %d)", args.unpackString(), getId()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    @Override
    public void onInit() {
    }

    @Override
    public void onJoin() {
    }

    @Override
    public void onLeave() {
    }

    @Override
    public void onDelete() {
    }

    @Override
    public void onEmit(String event, Result args) {
        System.out.println(String.format("no event handle for: %s", event));
    }
}
