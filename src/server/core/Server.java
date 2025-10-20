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
    private static final String FILE_CONFIG_SERVER_PROPS = "server.properties";
    private static final String FILE_CONFIG_SERVER_YML = "server.yml";

    public static final String DIR_CONFIG = "config";
    private ServerSocket serverSocket = null;
    private ServerSocket serverWebSocket = null;
    private ServerSocket serverWeb = null;
    public static final String IP = getIp();

    private static ServerConfig config;

//    private Socket client;
//    private SocketConnection socketConnection;
//    private WebSocketConnection webSocketConnection;

    private final List<SocketConnection> connections = new ArrayList<SocketConnection>();
    private boolean running;
    private boolean stop = false;
    private boolean exitOnFail = true;
    private boolean startFailed = false;

    public Server() {

        Server.setConfig(new ServerConfig(DIR_CONFIG + FileUtils.FILE_SEPARATOR + FILE_CONFIG_SERVER_XML));

        try {
            setServerSocket(new ServerSocket(getSocketPort()));
        } catch (IOException e) {
            System.err.println("failed listening on port: " + getSocketPort());
            startFailed = true;
        }

        try {
            setServerWebSocket(new ServerSocket(getWebsocketPort()));
        } catch (IOException e) {
            System.err.println("failed listening on port: " + getWebsocketPort());
            startFailed = true;
        }


        try {
            setServerWeb(new ServerSocket(getWebPort()));
        } catch (IOException e) {
            System.err.println("failed listening on port: " + getWebPort());
            startFailed = true;
        }

        if(startFailed && exitOnFail) {
            System.exit(1);
        }

        addDefaultModules();

        this.start();
    }

    private static int getWebPort() {
        return getConfigValueAsInt("webPort");
    }

    private static int getWebsocketPort() {
        return getConfigValueAsInt("websocketPort");
    }

    private static int getSocketPort() {
        return getConfigValueAsInt("socketPort");
    }

    private static String getConfigValue(String val) {
        return getConfig().get(val);
    }

    private static int getConfigValueAsInt(String val) {
        return Integer.parseInt(getConfigValue(val));
    }

    private void setServerWeb(ServerSocket serverWebPort) {
        this.serverWeb = serverWebPort;
    }


    private void addDefaultModules() {

        addModule(new WebSocketModule(getServerWebSocket())); //not sure if it is good to share
        addModule(new SocketModule(getServerSocket()));
        addModule(new WebModule(getServerWeb()));
    }

    private ServerSocket getServerWeb() {
        return serverWeb;
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
                sleep(1000); //sleep server for a while
            } catch (InterruptedException e) {
                System.err.println("sleep failed");
            }
            if (stop) {
                stopModules();
                stopServer();
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

        Server server = new Server();
    }


}
