package no.ntnu.trostespel.model;

import java.util.ArrayList;
import java.util.List;

public class Connections {

    private List<Connection> connections;

    // static variable single_instance of type Singleton
    private static Connections single_instance = null;

    public static Connections getInstance() {
        if (single_instance == null) {
            single_instance = new Connections();
        }
        return single_instance;
    }

    private Connections() {
        connections = new ArrayList<>();
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnection(Connection connection) {
        getConnections().add(connection);
        System.out.println("New connection added: " + connection.getAddress() + " - " + connection.getPlayerId());
    }
}
