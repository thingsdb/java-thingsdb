package io.github.thingsdb.connector;

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
    void addNode(String host, int port);

    /**
     * Adds another {@link Node} to the connector.
     *
     * @param host the FQDN, host-name or ip-address of the host to add using the default port 9200
     */
    void addNode(String host);


}



