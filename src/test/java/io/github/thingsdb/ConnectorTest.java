package io.github.thingsdb;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import io.github.thingsdb.connector.Connector;

/**
 * Unit test for simple App.
 */
public class ConnectorTest
{
    /**
     * Rigorous Test :-)
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void shouldConnectToPlayground() throws IOException, InterruptedException
    {
        Connector client = new Connector("localhost");
        client.connect();
        Thread.sleep(1000);
        assertTrue( true );
    }
}
