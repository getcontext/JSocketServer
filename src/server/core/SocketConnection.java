package server.core;

import java.io.IOException;
import java.net.Socket;

/**
 * @todo pull up
 */
public interface SocketConnection extends Connection {
	String getId();
    void processStream();
    void processStream(Socket client);
}
