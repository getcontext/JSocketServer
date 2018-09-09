package server.module;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.ServerSocket;
import java.net.Socket;

import server.core.Module;
import server.core.SocketConnection;

/**
 * @author andrzej.salamon@gmail.com
 */
class SocketModule extends Module implements Runnable, SocketConnection {
    public final static String MODULE_NAME = "socket";

    ObjectOutputStream out;
    ObjectInputStream in;

    private boolean close = false;
    private static int counter = 0;
    private int instanceNo;
    private boolean stop = false;

    private Socket client;
    private ServerSocket serverSocket;
    private final Thread thread;

    public SocketModule(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.instanceNo = counter++;
        this.thread = new Thread(this, MODULE_NAME + "_" + instanceNo);
    }

    @Override
    public String getId() {
        return MODULE_NAME;
    }

    @Override
    public void handleStream() {

    }

    @Override
    public void handleStream(Socket client) {
        try {
            setClient(client);
            out = new ObjectOutputStream(getClient().getOutputStream());
            in = new ObjectInputStream(getClient().getInputStream());
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


    public void run() {
        try {
//            request = (SerializedSocketObject)in.readObject();
//            response = process(request);
//            out.writeObject(response);
            out.flush();
            try {
                out.close();
                in.close();
                client.close();
            } catch (IOException e) {
                // e.printStackTrace();
            }
        } catch (Exception e) {
        }
    }

    private Socket getClient() {
        return client;
    }

    private void setClient(Socket client) {
        this.client = client;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void receive() throws IOException {

    }

    @Override
    public void broadcast(String data) throws IOException {

    }
}
