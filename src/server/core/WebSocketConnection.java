package server.core;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by wizard on 6/11/17.
 * @todo pull up
 */
public interface WebSocketConnection extends SocketConnection {
    Integer MAX_BUFFER = 5000;

    void sendHandshake() throws NoSuchAlgorithmException, IOException;
    boolean isHandshake();
    boolean isHandshake(String data);
    boolean isGet();
    boolean isGet(String data);
    String getRequestAsString();
}
