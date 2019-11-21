package no.ntnu.trostespel.model;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static no.ntnu.trostespel.model.ConnectionStatus.CONNECTED;
import static no.ntnu.trostespel.model.ConnectionStatus.DISCONNECTED;

public class Connection {

    private final InetAddress address;
    private final int port;
    private DatagramSocket clientUdpSocket;
    private Socket clientTcpSocket;

    private double timeArrived;

    private final String username;
    private final long pid;
    private static AtomicLong idCounter = new AtomicLong(100);
    private volatile ConnectionStatus connectionStatus;
    private final ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

    public Connection(Socket clientTcpSocket, int port, String username) {
        this.address = clientTcpSocket.getInetAddress();
        this.pid = createID();
        this.username = username;
        this.port = port;
        try {
            this.clientUdpSocket = new DatagramSocket();
            this.clientUdpSocket.connect(address, port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        this.connectionStatus = CONNECTED;
    }

    public DatagramSocket getClientUdpSocket() {
        return clientUdpSocket;
    }

    public InetAddress getAddress() {
        return address;
    }

    public long getPid() {
        return pid;
    }

    public double getTimeArrived() {
        return timeArrived;
    }

    public void setTimeArrivedToCurrentTime() {
        this.timeArrived = System.currentTimeMillis();
    }

    public String getUsername() {
        return username;
    }

    public int getPort() {
        return port;
    }

    public void setDisconnected() {
        Lock writeLock = reentrantReadWriteLock.writeLock();
        writeLock.lock();
        try {
            this.connectionStatus = DISCONNECTED;
        } finally {
            writeLock.unlock();
        }
    }

    public ConnectionStatus getConnectionStatus() {
        Lock readLock = reentrantReadWriteLock.readLock();
        readLock.lock();
        try {
            return connectionStatus;
        } finally {
            readLock.unlock();
        }
    }

    public static long createID() {
        return idCounter.getAndIncrement();
    }

    public Socket getClientTcpSocket() {
        return clientTcpSocket;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + this.clientUdpSocket.toString() + ", " + this.username + ", " + this.pid + "]";
    }
}
