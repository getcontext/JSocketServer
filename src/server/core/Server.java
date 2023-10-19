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
public final class Server extends Thread {
    private static final String FILE_CONFIG_SERVER_XML = "server.xml";
    public static final String DIR_CONFIG = "config";
    private ServerSocket serverSocket = null;
    private ServerSocket serverWebSocket = null;
    public static final String IP = getIp();

    private static ServerConfig config;

//    private Socket client;
//    private SocketConnection socketConnection;
//    private WebSocketConnection webSocketConnection;

    private final List<SocketConnection> connections = new ArrayList<SocketConnection>();
    private boolean running;

    public Server() {
        try {
            Server.setConfig(new ServerConfig(DIR_CONFIG + FileUtils.FILE_SEPARATOR + FILE_CONFIG_SERVER_XML));
            setServerSocket(new ServerSocket(Integer.parseInt(config.get("port"))));
            setServerWebSocket(new ServerSocket(Integer.parseInt(config.get("websocketPort"))));
            //for performance reasons, it should be separate websocket serversocket on different port
            //each of socket thread has own
        } catch (IOException e) {
            System.err.println("failed listening on port: " + config.get("port"));
            System.exit(1);
        }

        addDefaultModule();

        this.start();
    }


    private void addDefaultModule() {

        addModule(new WebSocketModule(getServerWebSocket())); //not sure if it is good to share
        addModule(new SocketModule(getServerSocket()));
    }

    public void addModule(SocketConnection socketConnection) {
        if (!connections.contains(socketConnection))
            connections.add(socketConnection);
    }

    private void startModules() {
        if (connections.isEmpty()) return;

        for (SocketConnection conn : connections) {
            conn.start();
        }
    }

    private void stopModules() {
        if (connections.isEmpty()) return;

        for (SocketConnection conn : connections) {
            conn.stop();
        }
    }
    public static ServerConfig getConfig() {
        return config;
    }

    public static void setConfig(ServerConfig config) {
        Server.config = config;
    }

    @Override
    public void run() {
        System.out.println("Andrew (Web)Socket(s) Server v. 1.1");
        startModules();

//        webSocketConnection = new WebSocket(serverSocket);
//        socketConnection = new Socket(serverSocket);

        running = true;
        while (running) {
            try {//@todo thread pooling
                sleep(10000);
            } catch (InterruptedException e) {
                System.err.println("sleep failed");
            }
            if(!running) {
                stopModules();
                return;
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

    public void setServerWebSocket(ServerSocket serverWebSocket) {
        this.serverWebSocket = serverWebSocket;
    }

    public ServerSocket getServerWebSocket() {
        return this.serverWebSocket;
    }

    public void stopServer() {
        running = false;
    }

    public static void main(String[] args) {
        new Server();
    }


}
