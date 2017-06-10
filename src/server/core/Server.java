package server.core;


import server.config.ServerConfig;
import server.utils.FileUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 *
 * @author andrzej.salamon@gmail.com
 *
 */
public class Server extends Thread {
    private ServerSocket serverSocket = null;
    public static final String IP = getIp();

    private static ServerConfig config;

    private Socket client;
    private Connection conn;

    public Server() {
        try {
            config = new ServerConfig("config" + FileUtils.FileSeparator + "server.xml");
            serverSocket = new ServerSocket(Integer.parseInt(config.get("port")));
        } catch (IOException e) {
            System.err.println("failed listening on port: " + config.get("port"));
            System.exit(1);
        }

        this.start();
    }
    public static ServerConfig getConfig() {
        return config;
    }

    public static void setConfig(ServerConfig config) {
        Server.config = config;
    }

    public static void main(String[] args) {
        new Server();
    }

    public void run() {

        System.out.println("Andrew Socket Server v. 1.0");

        conn = new Connection(serverSocket);

        while (true) {
            try {
                sleep(1);
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
}
