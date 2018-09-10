package server.core;

import java.io.IOException;

/**
 * @author wizard
 */
public interface Connection {
    void start();
    void stop();
    void receive() throws IOException;
    void broadcast() throws IOException;
    void broadcast(String data) throws IOException;
}
