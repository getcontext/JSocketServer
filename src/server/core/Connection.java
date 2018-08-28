package server.core;

import java.io.IOException;

public interface Connection {
    abstract void start();

    abstract void stop();

    abstract String receive() throws IOException;

    abstract void brodcast(String data) throws IOException;
}
