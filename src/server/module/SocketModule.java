package server.module;

import java.io.IOException;

import java.net.ServerSocket;

import server.core.connection.SocketConnectionAbstract;

/**
 * @author andrzej.salamon@gmail.com
 */
public class SocketModule extends SocketConnectionAbstract {
    public static final String MODULE_NAME = "socketModule";

    public SocketModule(ServerSocket serverSocket) {
        super(serverSocket);
    }

    @Override
    public String getId() {
        return MODULE_NAME;
    }

    public void run() {
        while (!stop) {
            try {
                processStream();
                receive();
                broadcast();
                flushOutputStream();
                // out.close();
                // in.close();
                // getClient().close();
                // try {
                //// out.close();
                //// in.close();
                // client.close();
                // } catch (IOException e) {
                // // e.printStackTrace();
                // }
            } catch (Exception e) {
            }
        }
    }

    // @Override
    // public void start() {
    //
    // }
    //
    // @Override
    // public void stop() {
    //
    // }

    @Override
    public void receive() throws IOException {
        // request = (SerializedSocketObject)in.readObject();
    }

    @Override
    public void broadcast() throws IOException {
    }

    @Override
    public void broadcast(String data) throws IOException {
        // response = process(request);
        // out.writeObject(response);
    }
}
