package server.module;

import server.core.connection.SocketConnectionAbstract;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;

/**
 * @author andrzej.salamon@gmail.com
 */
public final class WebModule extends SocketConnectionAbstract {
    public static final String MODULE_NAME = "webModuleSocket";

    private PrintWriter printWriter;

    public WebModule(ServerSocket serverSocket) {
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
                outputStream.flush();
//                out.close();
//                in.close();
//                getClient().close();
//                try {
////                out.close();
////                in.close();
//                    client.close();
//                } catch (IOException e) {
//                    // e.printStackTrace();
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void receive() throws IOException {

    }

    @Override
    public void broadcast() throws IOException {

    }

    @Override
    public void broadcast(String data) throws IOException {

    }
}
