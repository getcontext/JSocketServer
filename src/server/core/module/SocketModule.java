package server.core.module;

import server.core.AbstractModule;
import server.core.SocketConnection;

import java.net.ServerSocket;

public abstract class SocketModule extends AbstractModule implements SocketConnection {

    public SocketModule(ServerSocket serverSocket) {
        super(serverSocket);
    }

}
