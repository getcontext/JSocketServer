package server.core;

import server.config.*;
import server.module.*;
import server.utils.FileUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author andrzej.salamon@gmail.com
 */
public final class Server extends Thread {

//    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    private static final String ERR_PORT_PREFIX = "failed listening on port: ";
    private static final String FILE_CONFIG_SERVER_PROPS = "server.properties";
    private static final String DIR_CONFIG = "config";
    private static final String FORMAT_PARAM_LOGGER = "{0} -> {1}";
    public static final String IP = getIp();

    public enum MODULES {
        SOCKET,
        WEBSOCKET,
        WEB

    }
    private static ServerProperties serverProperties;
    private ServerSocket serverSocket = null;
    private ServerSocket serverWebSocket = null;
    private ServerSocket serverWeb = null;

    // thread-safe list wrapper
    private final List<SocketConnection> connections = new ArrayList<SocketConnection>();

    // flags used across threads
    private volatile boolean running = false;
    private volatile boolean stop = false;

    public boolean isExitOnFail() {
        return exitOnFail;
    }

    public void setExitOnFail(boolean exitOnFail) {
        this.exitOnFail = exitOnFail;
    }

    private volatile boolean exitOnFail = true;
    private volatile boolean startFailed = false;

    public Server() {
        // we are not using nio because we want to have it synchronous
        try {
            setServerProperties(
                    FileUtils.loadServerProperties(DIR_CONFIG + FileUtils.FILE_SEPARATOR + FILE_CONFIG_SERVER_PROPS));
        } catch (IOException e) {
            e.printStackTrace();
//            LOGGER.log(Level.SEVERE, "No configuration found: {0}", e.getMessage());
            System.err.println("No configuration found");
            startFailed = true;
            System.exit(-1);
        }

        if (getSocketPort() < 1 || getWebsocketPort() < 1 || getWebPort() < 1) {
//            LOGGER.severe("Invalid port configuration. Ports must be greater than 0.");
            System.err.println("Invalid port number");
            startFailed = true;
            System.exit(-1);
        }

        configureModule(ConfigConstant.SOCKET_ENABLED, getSocketPort(), MODULES.SOCKET);
        configureModule(ConfigConstant.WEBSOCKET_ENABLED, getWebsocketPort(), MODULES.WEBSOCKET);
        configureModule(ConfigConstant.WEB_ENABLED, getWebPort(), MODULES.WEB);

        if (startFailed && exitOnFail) {
            System.exit(-1);
        }

        this.start();
    }

    private void configureModule(String socketEnabled, int socketPort, MODULES module) {
        if (isEnabled(socketEnabled))
            try {
                setupModule(module, socketPort);
            } catch (IOException e) {
//                LOGGER.log(Level.SEVERE, ERR_PORT_PREFIX + FORMAT_PARAM_LOGGER,
//                        new Object[] { socketPort, e.getMessage() });
                System.err.println("Failed to configure module: " + e.getMessage());
                startFailed = true;
            }
    }

    private static boolean isEnabled(String socketEnabled) {
        return ServerPropertiesValue.getConfigValueAsBoolean(socketEnabled);
    }

    private void setupModule(MODULES module, int socketPort) throws IOException {
        switch (module) {
            case SOCKET:
                setServerSocket(createSocket(socketPort));
                addSocketModule();
                break;
            case WEBSOCKET:
                setServerWebSocket(createSocket(socketPort));
                addWebsocketModule();
                break;
            case WEB:
                setServerWeb(createSocket(socketPort));
                addWebModule();
                break;
            default:
                System.err.println("Unknown module: " + module);
//                LOGGER.severe("Unknown module type");
                break;
        }
    }

    private void addModule() {
    }

    private static ServerSocket createSocket(int port) throws IOException {
        return new ServerSocket(port);
    }

    private static int getWebPort() {
        return ServerPropertiesValue.getConfigValueAsInt(ConfigConstant.WEB_PORT);
    }

    private static int getWebsocketPort() {
        return ServerPropertiesValue.getConfigValueAsInt(ConfigConstant.WEBSOCKET_PORT);
    }

    private static int getSocketPort() {
        return ServerPropertiesValue.getConfigValueAsInt(ConfigConstant.SOCKET_PORT);
    }

    private void setServerWeb(ServerSocket serverWebPort) {
        this.serverWeb = serverWebPort;
    }

    private void addWebModule() {
        addModule(new WebModule(getServerWeb()));
    }

    private void addSocketModule() {
        addModule(new SocketModule(getServerSocket()));
    }

    private void addWebsocketModule() {
        addModule(new WebSocketModule(getServerWebSocket()));
    }

    private ServerSocket getServerWeb() {
        return serverWeb;
    }

    public void addModule(SocketConnection socketConnection) {
        if (socketConnection == null) {
            return;
        }
        if (!connections.contains(socketConnection)) {
            connections.add(socketConnection);
        }
    }

    private void startModules() {
        synchronized (connections) {
            if (connections.isEmpty()) {
                return;
            }
            for (SocketConnection conn : connections) {
                try {
//                    LOGGER.log(Level.INFO, "Starting module: {0} at port {1}",
//                            new Object[] { conn.getClass().getSimpleName(), conn.getPort() });
                    System.out.println("Starting module: " + conn.getClass().getSimpleName() + " at port " + conn.getPort());
                    conn.start();
                } catch (IllegalThreadStateException e) {
                    // already started or cannot start; log and continue
//                    LOGGER.log(Level.SEVERE, "module start failed: {0}", e.getMessage());
                    System.err.println("module start failed: " + e.getMessage());
                } catch (Exception e) {
//                    LOGGER.log(Level.SEVERE, "unexpected module start error: {0}", e.getMessage());
                    System.err.println("unexpected module start error: " + e.getMessage());
                }
            }
        }
    }

    private void stopModules() {
        synchronized (connections) {
            if (connections.isEmpty()) {
                return;
            }
            for (SocketConnection conn : connections) {
                try {
                    conn.stop();
                } catch (Exception e) {
//                    LOGGER.log(Level.SEVERE, "module stop failed: {0}", e.getMessage());
                    System.err.println("unexpected module stop error: " + e.getMessage());
                }
            }
        }
    }

    public static ServerProperties getServerProperties() {
        return serverProperties;
    }

    public static void setServerProperties(ServerProperties config) {
        Server.serverProperties = config;
    }

    @Override
    @SuppressWarnings("unused")
    public void run() {
//        LOGGER.info("Andrew (Web)Socket(s) Server v. 1.1");
        System.out.println("Andrew (Web)Socket(s) Server v. 1.1");
//        LOGGER.log(Level.INFO, "Started at IP: {0}", IP);
        System.out.println("Started at IP: " + IP);
        startModules();

        running = true;
        while (running) {
            try {
                Thread.sleep(1000); // sleep server for a while
            } catch (InterruptedException e) {
//                LOGGER.log(Level.WARNING, "InterruptedException: {0}", e.getMessage());
                System.err.println("Server sleep interrupted: " + e.getMessage());
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
//            LOGGER.log(Level.WARNING, "UnknownHostException: {0}", e.getMessage());
            System.err.println("Unable to get IP address UnknownHostException: " + e.getMessage());
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
        if (s == null) {
            return;
        }
        try {
            s.close();
        } catch (IOException e) {
//            LOGGER.log(Level.WARNING, "IOException closing socket: {0}", e.getMessage());
            System.err.println("Unable to close server socket: " + e.getMessage());
            // swallow - best effort close
        }
    }

    public void requestStop() {
        // external callers should use this to request orderly shutdown
        stop = true;
    }

    public static void main(String[] args) {
        Server server = new Server();
    }

}
