package io.github.thingsdb;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.apache.log4j.BasicConfigurator;

import io.github.thingsdb.connector.Args;
import io.github.thingsdb.connector.Connector;
import io.github.thingsdb.helpers.ChatRoom;

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
        BasicConfigurator.configure();

        if (token == null) {
            // A valid TOKEN is required to perform this test
            return;
        }

        Connector client = new Connector("localhost");
        ChatRoom chatRoom = new ChatRoom(client, "//stuff", ".chat.id();");

        client.connect();
        client.authenticate(token).get();
        client.query("//stuff", ".chat = room();").get();

        Args args = new Args();
        args.addString("Hello!");
        chatRoom.join().get().emit("new-msg", args).get();

        TimeUnit.SECONDS.sleep(1);

        client.close();
    }
}
