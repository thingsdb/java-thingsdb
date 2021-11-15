package io.github.thingsdb;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Test;

import io.github.thingsdb.connector.Connector;
import io.github.thingsdb.connector.lib.Result;
import io.github.thingsdb.connector.lib.ResultType;

/**
 * Unit test for simple App.
 */
public class ConnectorTest
{
    private String token = System.getenv("TI_TOKEN");
    /**
     * Rigorous Test :-)
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void shouldConnectToPlayground() throws IOException, InterruptedException, ExecutionException
    {
        Future<Result> fut;
        Result res;
        Connector client = new Connector("localhost");
        client.connect();

        fut = client.authenticate(token);
        res = fut.get();
        assertTrue( res.TYPE == ResultType.OK );

        fut = client.query("6 * 7;");
        res = fut.get();
        assertTrue( res.TYPE == ResultType.DATA );
        assertTrue( res.unpackInt() == 42 );
    }
}
