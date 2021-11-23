package io.github.thingsdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import io.github.thingsdb.connector.Connector;
import io.github.thingsdb.connector.Result;
import io.github.thingsdb.connector.ResultType;
import io.github.thingsdb.connector.Room;
import io.github.thingsdb.helpers.MsgRoom;

/**
 * Unit test for simple App.
 */
public class RoomTest
{
    private String token = System.getenv("TI_TOKEN");
    /**
     * Rigorous Test :-)
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void shouldJoinRoom() throws IOException, InterruptedException, ExecutionException
    {
        Future<Boolean> waitRoom;
        Connector client = new Connector("localhost");
        MsgRoom chatRoom = new MsgRoom(client, "//stuff", ".chat.id();");

        client.connect();
        client.authenticate(token).get();
        client.query("//stuff", ".chat = room();").get();

        waitRoom = chatRoom.join();
        waitRoom.get();

        TimeUnit.SECONDS.sleep(1);

    }
}
