package no.ntnu.trostespel.udpServer;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.ImageResolver;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import no.ntnu.trostespel.ConnectionManager;
import no.ntnu.trostespel.config.CommunicationConfig;
import no.ntnu.trostespel.game.MasterGameState;
import no.ntnu.trostespel.model.Connection;
import no.ntnu.trostespel.model.Connections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static no.ntnu.trostespel.config.MapConfig.PVP_VILLAGE_FILENAME;


/**
 * This class will do every task the server need to do each tick.
 */
public class GameServer{


    private List<Connection> connections = Connections.getInstance().getConnections();
    private MasterGameState masterGameState;

    private static AtomicLong tickCounter = new AtomicLong(0);
    private long timerCounter = 0;

    private List<Connection> connectionsToDrop = new ArrayList<>();

    GameDataReceiver receiver;
    GameDataSender sender;

    public GameServer() {
        masterGameState = MasterGameState.getInstance();

        // Load map to be played
        String mapFileName = PVP_VILLAGE_FILENAME;
        System.out.println("Loading map " + mapFileName);
        try {
            this.receiver = new GameDataReceiver(CommunicationConfig.SERVER_UDP_GAMEDATA_RECEIVE_PORT);
            this.sender = new GameDataSender();
            System.out.println("Started GameDataSender " + this.sender.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread receiverThread = new Thread(receiver, "GameDataReceiver");
        receiverThread.start();
        System.out.println("Started GameDataReceiver " + this.receiver.toString());

        ConnectionManager TCPClient = null;
        try {
            TCPClient = new ConnectionManager(CommunicationConfig.SERVER_TCP_CONNECTION_RECEIVE_PORT, mapFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread TcpThread = new Thread(TCPClient);
        TcpThread.setName("ConnectionClient");
        TcpThread.start();
        System.out.println("Started TCPClient with address " + TCPClient.getSocketAddress());


        System.out.println("Server is ready to handle incoming connections!");
        heartbeat();
    }
    private void heartbeat() {
        double ns = 1000000000.0 / CommunicationConfig.TICKRATE;
        double delta = 0;

        long lastTime = System.nanoTime();

        System.out.println("Starting tick-loop with frequecy " + ns + "ns");
        while (true) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                tick();
                delta--;
            }
        }
    }


    public void tick() {
        if (!connections.isEmpty()) {
            update();
        } else {
            long currentTick;
            if ((currentTick = tickCounter.get()) >= timerCounter) {
                System.out.println("Waiting for at least one connection..");
                timerCounter = currentTick + 1000;
            }
        }
        tickCounter.incrementAndGet();
    }

    private void update() {
        dropIdleConnections();
        broadcastUpdate();
    }

    private void broadcastUpdate() {
        try {
            sender.broadcast(Connections.getInstance().getConnections());
        } catch (InterruptedException e) {
            // Server is running too slow
            e.printStackTrace();
            sender.purge();
        }
    }

    private void dropIdleConnections() {
        for (Connection connection : Connections.getInstance().getConnections()) {
            double currentTime = System.currentTimeMillis();
            double timeArrived = connection.getTimeArrived();
            double timeSinceMillis = currentTime - timeArrived;
            if (timeSinceMillis > CommunicationConfig.RETRY_CONNECTION_TIMEOUT && timeArrived != 0.0) {
                System.out.println(connection.getAddress() + " - Timed out!");
                connectionsToDrop.add(connection);
            }
        }
        if (connectionsToDrop.size() > 0) {
            for (Connection connection : connectionsToDrop) {
                masterGameState.getGameState().players.remove(connection.getPid());
                Connections.getInstance().getConnections().remove(connection);
                System.out.println(connection.getUsername() + " - Got dropped from the game!");
            }
            connectionsToDrop.clear();
        }
    }



    public static long getTickcounter() {
        return tickCounter.get();
    }
}