package server.module;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.ServerSocket;

import server.core.Module;
import server.core.SocketConnection;

/**
 * @author andrzej.salamon@gmail.com
 */
class Socket extends SocketModule {
    public final static String MODULE_NAME = "socket";

    public Socket(ServerSocket serverSocket) {
        super(serverSocket);
    }

    @Override
    public String getId() {
        return MODULE_NAME;
    }

    @Override
    public void handleStream() {

    }

    @Override
    public void handleStream(java.net.Socket client) {
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
receive();
broadcast();
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

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void receive() throws IOException {
//            request = (SerializedSocketObject)in.readObject();
    }

    @Override
    public void broadcast() throws IOException {

    }

    @Override
    public void broadcast(String data) throws IOException {
//            response = process(request);
//            out.writeObject(response);
    }
}
