package server.core;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static server.module.WebSocketModule.MODULE_NAME; //@todo alias? or problem

/**
 * @todo add factory
 */
public abstract class ConnectionAbstract implements Runnable, Connection {
    protected static int counter;
    protected final Thread thread;
//    protected ObjectOutputStream out;
//    protected ObjectInputStream in;
    protected OutputStream outputStream;
    protected InputStream inputStream;
    protected byte[] requestByte;
    protected byte[] responseByte;
    protected byte[] frame = new byte[10];
    protected String response;
    protected String request;
    protected boolean close = false;
    protected int instanceNo;
    protected boolean stop = false;
    protected java.net.Socket client;
    protected ServerSocket serverSocket;
    protected int port;

    protected ConnectionAbstract(ServerSocket serverSocket) {
        setCounter(0);
        this.serverSocket = serverSocket;
        this.port = serverSocket.getLocalPort();
        this.instanceNo = getCounter();
        incrementCounter();
        this.thread = new Thread(this, MODULE_NAME + instanceNo);

    }

    public int getPort() {
        return port;
    }

    public static int getCounter() {
        return counter;
    }

    public static void setCounter(int counter) {
        ConnectionAbstract.counter = counter;
    }

    public static void incrementCounter() {
        ++ConnectionAbstract.counter;
    }

    public static void decrementCounter() {
        --ConnectionAbstract.counter;
    }

    public Thread getThread() {
        return thread;
    }

    protected Socket getClient() {
        return client;
    }

    protected void setClient(java.net.Socket client) {
        this.client = client;
    }

    @Override
    public void start() {
        getThread().start();
        incrementCounter();
    }

    @Override
    public void stop() {
//        getThread().stop();
        getThread().interrupt();
        stop = true;
        decrementCounter();
        instanceNo = -1;
    }

    public void processStream(Socket client) {
        try {
            setClient(client);
            outputStream = new ObjectOutputStream(getClient().getOutputStream());
            inputStream = new ObjectInputStream(getClient().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { //try to close gracefully
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void processStream() {
        try {
            processStream(serverSocket.accept());
        } catch (IOException e) {
            e.printStackTrace();
//            throw new RuntimeException(e);
        }
    }

    protected void flushOutputStream() throws IOException {
        outputStream.flush();
    }
}
