package server.core.connection;

import server.core.ConnectionAbstract;
import server.core.SocketConnection;

import java.net.ServerSocket;

public abstract class SocketConnectionAbstract extends ConnectionAbstract implements SocketConnection {

    public SocketConnectionAbstract(ServerSocket serverSocket) {
        super(serverSocket);
    }

}
