package server.core;

import java.io.IOException;

public interface SocketConnection extends Connection {

    void handleStream();
}
