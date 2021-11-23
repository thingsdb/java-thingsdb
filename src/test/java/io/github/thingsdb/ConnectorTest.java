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
import io.github.thingsdb.connector.Vars;

/**
 * Unit test for the connector
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
        Vars vars1, vars2;
        Future<Result> fut, fut2;
        Result res, res2;
        Connector client = new Connector("localhost");
        client.connect();

        fut = client.authenticate(token);
        res = fut.get();
        assertEquals(res.TYPE, ResultType.OK);

        fut = client.query("6 * 7;");
        res = fut.get();
        assertEquals(res.TYPE, ResultType.DATA);
        assertEquals(res.unpackInt(),  42);

        fut = client.query("6 / 0;");
        try {
            fut.get();
            assertTrue("Exceptoin ZeroDivError should have been raided", 1 == 0);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "io.github.thingsdb.connector.exceptions.ZeroDivError: division or modulo by zero");
        }

        vars1 = new Vars();
        vars1.setInt("val", 10);

        vars2 = new Vars();
        vars2.setInt("val", 42);

        fut = client.query("range(500).reduce(|a, b| a + b, val);", vars1);

        fut2 = client.query("range(500).reduce(|a, b| a + b, val);", vars2);

        res = fut.get();
        res2 = fut2.get();

        assertEquals(res.unpackInt(), 124760);
        assertEquals(res2.unpackInt(), 124792);

        TimeUnit.SECONDS.sleep(1);
    }
}
