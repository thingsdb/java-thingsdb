package io.github.thingsdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

import io.github.thingsdb.connector.Args;
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

    { BasicConfigurator.configure(); }

    /**
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void shouldConnectAndQuery() throws IOException, InterruptedException, ExecutionException
    {
        if (token == null) {
            // A valid TOKEN is required to perform this test
            return;
        }

        Vars vars1, vars2;
        Result res, res1, res2;
        Future<Result> fut1, fut2;
        Connector client = new Connector("localhost");
        client.connect();

        res = client.authenticate(token).get();
        assertEquals(res.TYPE, ResultType.OK);

        res = client.query("6 * 7;").get();
        assertEquals(res.TYPE, ResultType.DATA);
        assertEquals(res.unpackInt(),  42);

        try {
            client.query("6 / 0;").get();
            assertTrue("Exceptoin ZeroDivError should have been raided", 1 == 0);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "io.github.thingsdb.connector.exceptions.ZeroDivError: division or modulo by zero");
        }

        vars1 = new Vars();
        vars1.setInt("val", 10);

        vars2 = new Vars();
        vars2.setInt("val", 42);

        fut1 = client.query("range(500).reduce(|a, b| a + b, val);", vars1);
        fut2 = client.query("range(500).reduce(|a, b| a + b, val);", vars2);

        res1 = fut1.get();
        res2 = fut2.get();

        assertEquals(res1.unpackInt(), 124760);
        assertEquals(res2.unpackInt(), 124792);

        client.close();
    }

    /**
     * @throws IOException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void shouldConnectAndRun() throws IOException, InterruptedException, ExecutionException
    {
        if (token == null) {
            // A valid TOKEN is required to perform this test
            return;
        }

        Vars vars;
        Args args;
        Result res;
        Connector client = new Connector("localhost");
        res = client
            .setDefaultScope("//stuff")
            .connect()
            .authenticate(token)
            .get();
        assertEquals(res.TYPE, ResultType.OK);

        res = client.query("try(new_procedure('multiply', |a, b| a*b));").get();
        assertEquals(res.TYPE, ResultType.DATA);

        vars = new Vars();
        vars.setInt("a", 6);
        vars.setInt("b", 7);

        res = client.run("multiply", vars).get();
        assertEquals(res.unpackInt(), 42);

        args = new Args();
        args.addInt(8);
        args.addInt(9);

        res = client.run("multiply", args).get();
        assertEquals(res.unpackInt(), 72);

        // TimeUnit.SECONDS.sleep(1);
        client.close();
    }
}
