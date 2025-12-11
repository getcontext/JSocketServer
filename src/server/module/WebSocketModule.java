package server.module;

import server.core.connection.WebSocketConnectionAbstract;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.NoSuchAlgorithmException;

/**
 * @author andrzej.salamon@gmail.com
 */
public class WebSocketModule extends WebSocketConnectionAbstract { //double inheritance, not ellegant, ref,mv
    public static final String MODULE_NAME = "websocketModule";


    public WebSocketModule(ServerSocket serverSocket) {
        super(serverSocket);
    }

    @Override
    public String getId() {
        return MODULE_NAME;
    }

    public void run() {
        while (!stop) {
            processStream();

            request = getRequestAsString();

            if (isGet()) { //mv, lambda, listener
                if (isHandshake()) {
                    try {
                        sendHandshake();
                    } catch (NoSuchAlgorithmException | IOException e) { //ref ?
                        e.printStackTrace();
                    }
                } else {
                    try {
                        receive(); //@todo add responsive listener(s) as many as someone wants
//                        System.out.println(response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (close) {
                try {
                    broadcast(response); //@todo return resp headers is closed
                } catch (IOException e) {
                    System.err.println("cant broadcast");
                    e.printStackTrace();
                }
                try {
                    flushOutputStream();
                } catch (IOException e) {
                    System.err.println("cant flush");
                    e.printStackTrace();
                }
                try {
                    outputStream.close();
                } catch (IOException e) {
                    System.err.println("cant close output stream");
                    e.printStackTrace();
                }
                try {
                    inputStream.close();
                } catch (IOException e) {
                    System.err.println("cant inputSTream close");
                    e.printStackTrace();
                }
                try {
                    getClient().close();
                } catch (IOException e) {
                    System.err.println("unable to close client");
                    System.err.println("Failed processing client request");
                }
            }
//            try {
//                Thread.sleep(1);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
    }

}
