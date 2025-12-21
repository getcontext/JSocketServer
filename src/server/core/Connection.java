package server.core;

import java.io.IOException;

/**
 * @author wizard
 */
public interface Connection {
    void start();
    void stop() throws IOException;
    void receive() throws IOException;
    void broadcast() throws IOException;
    void broadcast(String data) throws IOException;
    int getPort();
}
