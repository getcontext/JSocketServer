package server.core;

import server.config.ServerConfig;
import server.utils.FileUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.ArrayList;

import server.module.*;
//import server.module.WebSocket;

/**
 * @author andrzej.salamon@gmail.com
 */
public final class Server extends Thread { //lets keep it extend
    private ServerSocket serverSocket = null;
    private ServerSocket serverWebSocket = null;
    public static final String IP = getIp();

    private static ServerConfig config;

//    private Socket client;
    private SocketConnection socketConnection;
    private WebSocketConnection webSocketConnection;

    private List<SocketConnection> connections = new ArrayList<SocketConnection>();

    public Server() {
        try {
            Server.setConfig(new ServerConfig("config" + FileUtils.FILE_SEPARATOR + "server.xml"));
            setServerSocket(new ServerSocket(Integer.parseInt(config.get("port"))));
            //for performance reasons, it should be separate websocket serversocket on different port
            //each of socket thread has own
        } catch (IOException e) {
            System.err.println("failed listening on port: " + config.get("port"));
            System.exit(1);
        }

        addDefaultModule();

        this.start();
    }


    protected void addDefaultModule() {

        addModule(new WebSocket(getServerSocket())); //not sure if it is good to share 
        addModule(new Socket(getServerSocket()));
    }

    public void addModule(SocketConnection socketConnection) {
        if (!connections.contains(socketConnection))
            connections.add(socketConnection);
    }

    protected void startModules() {
        if (connections.size() <= 0) return;

        for (SocketConnection conn : connections) {
            conn.start();
        }
    }

    public static ServerConfig getConfig() {
        return config;
    }

    public static void setConfig(ServerConfig config) {
        Server.config = config;
    }

    public void run() {
        System.out.println("Andrew (Web)Socket(s) Server v. 1.1");
        startModules();

//        webSocketConnection = new WebSocket(serverSocket);
//        socketConnection = new Socket(serverSocket);

        while (true) {
            try {//@todo thread pooling
                sleep(10000);
            } catch (InterruptedException e) {
                System.err.println("sleep failed");
            }
        }
    }

    private static String getIp() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getAddress().toString();
        } catch (UnknownHostException e) {
            return "";
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public static void main(String[] args) {
        new Server();
    }


}
