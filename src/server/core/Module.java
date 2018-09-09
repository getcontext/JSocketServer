package server.core;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static server.module.WebSocketModule.MODULE_NAME; //@todo alias? or problem

/**
 * @todo add factory
 */
public abstract class Module implements Runnable {
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
    protected Socket client;
    protected ServerSocket serverSocket;

    public Module(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.instanceNo = counter++;
        this.thread = new Thread(this, MODULE_NAME + "_" + instanceNo);

    }

    public static int getCounter() {
        return counter;
    }

    public static void setCounter(int counter) {
        Module.counter = counter;
    }

    public static void incrementCounter(int counter) {
        ++Module.counter;
    }

    public Thread getThread() {
        return thread;
    }

    protected Socket getClient() {
        return client;
    }

    protected void setClient(Socket client) {
        this.client = client;
    }
}
