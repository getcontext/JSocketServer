package server.core;

import java.io.IOException;
import java.net.Socket;

public interface SocketConnection extends Connection {

    void handleStream();
    void handleStream(Socket client);
}
