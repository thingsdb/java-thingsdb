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
     */
    @Test
    public void shouldConnectToPlayground() throws IOException
    {
        Connector client = new Connector("localhost");
        client.connect();

        assertTrue( true );
    }
}
