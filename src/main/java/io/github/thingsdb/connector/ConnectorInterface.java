package io.github.thingsdb.connector;

import java.io.IOException;
import java.util.concurrent.Future;

/**
 * Client connector for communicating with ThingsDB.
 * <p>
 * The instances of classes that implement this interface are thread-safe and immutable.
 */
public interface ConnectorInterface {

    /**
     * Adds another {@link Node} to the connector.
     *
     * @param host the FQDN, host-name or ip-address of the host to add
     * @param port the port number to which the node is listening
     */
    ConnectorInterface addNode(String host, int port);

    /**
     * Adds another {@link Node} to the connector.
     *
     * @param host the FQDN, host-name or ip-address of the host to add using the default port 9200
     */
    ConnectorInterface addNode(String host);

    /**
     * Set the default scope for the connector. Initially the default scope is set to `/thingsdb`.
     *
     * @param scope will be set to the new default scope.
     */
    ConnectorInterface setDefaultScope(String scope);

    /**
     * @return the default scope of the connector
     */
    String getDefaultScope();

    /**
     * Disable auto re-connect. Auto re-connect is enabled be default.
     * Once auto re-connect is disabled, it is not possible enable this option.
     */
    ConnectorInterface disableAutoReconnect();

    /**
     * Close the connection.
     * @throws IOException
     */
    void close() throws IOException;

    /**
     * Connect to ThingsDB.
     * @throws IOException
     */
    Connector connect() throws IOException;

    public Future<Result> authenticate(String token);
    public Future<Result> authenticate(String username, String password);

    public Future<Result> query(String code, String scope, Vars args);
    public Future<Result> query(String code, String scope);
    public Future<Result> query(String code, Vars args);
    public Future<Result> query(String code);

    /**
     * @return a {@link String} representing the connection.
     */
    public String toString();
}

