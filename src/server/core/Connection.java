package server.core;

import java.io.IOException;

public interface Connection {
    String receive() throws IOException;

    void brodcast(String data) throws IOException;
}
