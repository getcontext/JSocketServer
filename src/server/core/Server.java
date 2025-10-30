package server.core;

import server.config.ServerConfig;
import server.utils.FileUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import server.module.*;

/**
 * @author andrzej.salamon@gmail.com
 */
public final class Server extends Thread {
    private static final String FILE_CONFIG_SERVER_XML = "server.xml";
    private static final String FILE_CONFIG_SERVER_PROPS = "server.properties";
    private static final String FILE_CONFIG_SERVER_YML = "server.yml";

    public static final String DIR_CONFIG = "config";

    private Properties properties;

    private ServerSocket serverSocket = null;
    private ServerSocket serverWebSocket = null;
    private ServerSocket serverWeb = null;
    public static final String IP = getIp();

    private static ServerConfig config;

    // thread-safe list wrapper
    private final List<SocketConnection> connections = Collections.synchronizedList(new ArrayList<SocketConnection>());

    // flags used across threads
    private volatile boolean running;
    private volatile boolean stop = false;
    private volatile boolean exitOnFail = true;
    private volatile boolean startFailed = false;

    public Server() {

        Server.setConfig(new ServerConfig(DIR_CONFIG + FileUtils.FILE_SEPARATOR + FILE_CONFIG_SERVER_XML));

        try {
            setServerSocket(new ServerSocket(getSocketPort()));
        } catch (IOException e) {
            System.err.println("failed listening on port: " + getSocketPort() + " -> " + e.getMessage());
            startFailed = true;
        }

        try {
            setServerWebSocket(new ServerSocket(getWebsocketPort()));
        } catch (IOException e) {
            System.err.println("failed listening on port: " + getWebsocketPort() + " -> " + e.getMessage());
            startFailed = true;
        }

        try {
            setServerWeb(new ServerSocket(getWebPort()));
        } catch (IOException e) {
            System.err.println("failed listening on port: " + getWebPort() + " -> " + e.getMessage());
            startFailed = true;
        }

        if (startFailed && exitOnFail) {
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
        ServerConfig cfg = getConfig();
        if (cfg == null) return "";
        String v = cfg.get(val);
        return v == null ? "" : v;
    }

    private static int getConfigValueAsInt(String val) {
        String v = getConfigValue(val);
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void setServerWeb(ServerSocket serverWebPort) {
        this.serverWeb = serverWebPort;
    }

    private void addDefaultModules() {
        // add modules; connections list is thread-safe
        addModule(new WebSocketModule(getServerWebSocket())); // sharing sockets intentionally
        addModule(new SocketModule(getServerSocket()));
        addModule(new WebModule(getServerWeb()));
    }

    private ServerSocket getServerWeb() {
        return serverWeb;
    }

    public void addModule(SocketConnection socketConnection) {
        if (socketConnection == null) return;
        if (!connections.contains(socketConnection)) {
            connections.add(socketConnection);
        }
    }

    private void startModules() {
        synchronized (connections) {
            if (connections.isEmpty()) return;
            for (SocketConnection conn : connections) {
                try {
                    conn.start();
                } catch (IllegalThreadStateException e) {
                    // already started or cannot start; log and continue
                    System.err.println("module start failed: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("unexpected module start error: " + e.getMessage());
                }
            }
        }
    }

    private void stopModules() {
        synchronized (connections) {
            if (connections.isEmpty()) return;
            for (SocketConnection conn : connections) {
                try {
                    conn.stop();
                } catch (Exception e) {
                    System.err.println("module stop failed: " + e.getMessage());
                }
            }
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

        running = true;
        while (running) {
            try {
                Thread.sleep(1000); // sleep server for a while
            } catch (InterruptedException e) {
                // restore interrupted status and exit loop
                Thread.currentThread().interrupt();
                running = false;
            }
            if (stop) {
                stopModules();
                stopServer(); // closes sockets and stops thread
                return;
            }
        }
        // ensure cleanup in case loop exits
        stopModules();
        stopServer();
    }

    private static String getIp() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
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

    /**
     * Gracefully stop the server: stop the run loop and close sockets.
     */
    public void stopServer() {
        running = false;
        // close server sockets quietly
        closeQuietly(serverSocket);
        closeQuietly(serverWebSocket);
        closeQuietly(serverWeb);
    }

    private void closeQuietly(ServerSocket s) {
        if (s == null) return;
        try {
            s.close();
        } catch (IOException e) {
            // swallow - best effort close
        }
    }

    public void requestStop() {
        // external callers should use this to request orderly shutdown
        stop = true;
    }

    public static void main(String[] args) {
        Server server = new Server();
        // optionally handle start failure
        if (server.startFailed && server.exitOnFail) {
            System.exit(1);
        }
    }

}
