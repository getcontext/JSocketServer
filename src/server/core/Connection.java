package server.core;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by wizard on 6/11/17.
 */
public interface Connection {
    Integer MAX_BUFFER = 5000;

    void handleStream();

    void sendHandshake() throws NoSuchAlgorithmException, IOException;

    boolean isHandshake(String data);

    boolean isGet(String data);

    String getRequestAsString();

    String read() throws IOException;

    void brodcast(String data) throws IOException;
}
