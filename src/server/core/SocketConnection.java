package server.core;

import java.io.IOException;
import java.net.Socket;

/**
 * @todo pull up
 */
public interface SocketConnection extends Connection {
	String getId();
    void handleStream();
    void handleStream(Socket client);
}
