package io.github.thingsdb;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;

import io.github.thingsdb.connector.Connector;
import io.github.thingsdb.connector.lib.Result;

/**
 * Unit test for simple App.
 */
public class ConnectorTest
{
    private String token = System.getenv("PATH");
    /**
     * Rigorous Test :-)
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void shouldConnectToPlayground() throws IOException, InterruptedException, ExecutionException
    {
        Connector client = new Connector("localhost");
        client.connect();
        Thread.sleep(1000);
        Future<Result> fut = client.authenticate("M6oRWeLSnQcfNRAdszTkRP");
        Thread.sleep(1000);
        fut.get();
        assertTrue( true );
    }
}
