package server.module;

import java.io.IOException;

import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

import server.core.connection.SocketConnectionAbstract;

/**
 * @author andrzej.salamon@gmail.com
 */
public final class SocketModule extends SocketConnectionAbstract {
    public final static String MODULE_NAME = "socket";

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
                handleStream(serverSocket.accept());
                receive();
                broadcast();
                out.flush();
                out.close();
                in.close();
                getClient().close();
//                try {
////                out.close();
////                in.close();
//                    client.close();
//                } catch (IOException e) {
//                    // e.printStackTrace();
//                }
            } catch (Exception e) {
            }
        }
    }

//    @Override
//    public void start() {
//
//    }
//
//    @Override
//    public void stop() {
//
//    }

    @Override
    public void receive() throws IOException {
//            request = (SerializedSocketObject)in.readObject();
    }

    @Override
    public void broadcast() throws IOException {
        responseByte = ("HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "<p> Hello world </p>"
                + "\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8);

        out.write(responseByte, 0, responseByte.length);
    }

    @Override
    public void broadcast(String data) throws IOException {
//            response = process(request);
//            out.writeObject(response);
    }
}
