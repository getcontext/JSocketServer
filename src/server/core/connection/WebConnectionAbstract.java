package server.core.connection;

import server.core.ConnectionAbstract;
import server.core.SocketConnection;

import java.net.ServerSocket;

public abstract class WebConnectionAbstract extends ConnectionAbstract implements SocketConnection {

    public WebConnectionAbstract(ServerSocket serverSocket) {
        super(serverSocket);
    }

}
