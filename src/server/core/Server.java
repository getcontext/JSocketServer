package server.core;

import server.config.*;
import server.utils.FileUtils;

import java.util.logging.Logger;
// import java.util.logging.Level; // unused

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;

import server.module.*;

/**
 * @author andrzej.salamon@gmail.com
 */
public final class Server extends Thread {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    private static final String ERR_PORT_PREFIX = "failed listening on port: ";
    // private static final String FILE_CONFIG_SERVER_XML = "server.xml";
    private static final String FILE_CONFIG_SERVER_PROPS = "server.properties";
    // private static final String FILE_CONFIG_SERVER_YML = "server.yml"; // wont
    // implement till now

    private static final String DIR_CONFIG = "config";
    private static final String CFG_ENABLED_PROTOCOLS = "enabledProtocols";
    private static final String CFG_WEB_ENABLED = "webEnabled";
    private static final String CFG_WEBSOCKET_ENABLED = "websocketEnabled";
    private static final String CFG_SOCKET_ENABLED = "socketEnabled";

    // removed unused commented code
    private static ServerProperties serverProperties;

    private ServerSocket serverSocket = null;
    private ServerSocket serverWebSocket = null;
    private ServerSocket serverWeb = null;
    public static final String IP = getIp();

    private static XmlServerConfig config;

    // thread-safe list wrapper
    private final List<SocketConnection> connections = Collections.synchronizedList(new ArrayList<SocketConnection>());

    // flags used across threads
    private volatile boolean running = false;
    private volatile boolean stop = false;
    private volatile boolean exitOnFail = true;
    private volatile boolean startFailed = false;

    public Server() {

        // Server.setConfig(new XmlServerConfig(DIR_CONFIG + FileUtils.FILE_SEPARATOR +
        // FILE_CONFIG_SERVER_XML));
        try {
            setServerProperties(
                    FileUtils.loadServerProperties(DIR_CONFIG + FileUtils.FILE_SEPARATOR + FILE_CONFIG_SERVER_PROPS));
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            startFailed = true;
            System.exit(-1);
        }

        if (getSocketPort() < 1 || getWebsocketPort() < 1 || getWebPort() < 1) {
            LOGGER.severe("Invalid port configuration. Ports must be greater than 0.");
            startFailed = true;
            System.exit(-1);
        }

        if (ServerPropertiesValue.getConfigValueAsBoolean(CFG_SOCKET_ENABLED)
                || ServerPropertiesValue.getConfigValueAsString(CFG_ENABLED_PROTOCOLS).contains("socket"))
            try {
                setServerSocket(new ServerSocket(getSocketPort()));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, ERR_PORT_PREFIX + "{0} -> {1}",
                        new Object[] { getSocketPort(), e.getMessage() });
                startFailed = true;
            }

        if (ServerPropertiesValue.getConfigValueAsBoolean(CFG_WEBSOCKET_ENABLED)
                || ServerPropertiesValue.getConfigValueAsString(CFG_ENABLED_PROTOCOLS).contains("websocket"))
            try {
                setServerWebSocket(new ServerSocket(getWebsocketPort()));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, ERR_PORT_PREFIX + "{0} -> {1}",
                        new Object[] { getWebsocketPort(), e.getMessage() });
                startFailed = true;
            }

        if (ServerPropertiesValue.getConfigValueAsBoolean(CFG_WEB_ENABLED))
            try {
                setServerWeb(new ServerSocket(getWebPort()));
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, ERR_PORT_PREFIX + "{0} -> {1}", new Object[] { getWebPort(), e.getMessage() });
                startFailed = true;
            }

        if (startFailed && exitOnFail) {
            System.exit(-1);
        }

        addDefaultModules();

        this.start();
    }

    private static int getWebPort() {
        return ServerPropertiesValue.getConfigValueAsInt("webPort");
    }

    private static int getWebsocketPort() {
        return ServerPropertiesValue.getConfigValueAsInt("websocketPort");
    }

    private static int getSocketPort() {
        return ServerPropertiesValue.getConfigValueAsInt("socketPort");
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
                    conn.start();
                } catch (IllegalThreadStateException e) {
                    // already started or cannot start; log and continue
                    LOGGER.log(Level.SEVERE, "module start failed: {0}", e.getMessage());
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "unexpected module start error: {0}", e.getMessage());
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
                    LOGGER.log(Level.SEVERE, "module stop failed: {0}", e.getMessage());
                }
            }
        }
    }

    public static XmlServerConfig getXmlConfig() {
        return config;
    }

    public static void setXmlConfig(XmlServerConfig config) {
        Server.config = config;
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
        LOGGER.info("Andrew (Web)Socket(s) Server v. 1.1");
        LOGGER.log(Level.INFO, "Started at IP: {0}", IP);
        startModules();

        running = true;
        while (running) {
            try {
                Thread.sleep(1000); // sleep server for a while
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, "InterruptedException: {0}", e.getMessage());
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

    @SuppressWarnings("unused")
    private static String getIp() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.log(Level.WARNING, "UnknownHostException: {0}", e.getMessage());
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

    @SuppressWarnings("unused")
    private void closeQuietly(ServerSocket s) {
        if (s == null) {
            return;
        }
        try {
            s.close();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "IOException closing socket: {0}", e.getMessage());
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
