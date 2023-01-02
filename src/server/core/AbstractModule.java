package server.core;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static server.module.WebSocket.MODULE_NAME; //@todo alias? or problem

/**
 * @todo add factory
 */
public abstract class AbstractModule implements Runnable, Connection {
    protected static int counter = 0;
    protected final Thread thread;

    protected ObjectOutputStream out;
    protected ObjectInputStream in;
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

    public AbstractModule(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.instanceNo = counter++;
        this.thread = new Thread(this, MODULE_NAME + "_" + instanceNo);

    }

    public static int getCounter() {
        return counter;
    }

    public static void setCounter(int counter) {
        AbstractModule.counter = counter;
    }

    public static void incrementCounter(int counter) {
        ++AbstractModule.counter;
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
    }

    @Override
    public void stop() {
        getThread().stop();
        stop = true;
    }
}
