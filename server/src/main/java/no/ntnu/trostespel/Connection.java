package no.ntnu.trostespel;

import java.net.InetAddress;

public class Connection {

    public static final int GAME_DATA_RETRIEVE_PORT = 8070;
    private InetAddress address;
    private long playerId;

    public Connection(InetAddress address, long playerId) {
        this.address = address;
        this.playerId = playerId;
    }

    public InetAddress getAddress() {
        return address;
    }

    public long getPlayerId() {
        return playerId;
    }
}