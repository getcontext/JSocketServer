package server.module;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import server.core.WebSocketConnection;

/**
 * @author andrzej.salamon@gmail.com
 */
public final class WebSocket extends server.core.module.WebSocketModule { //double inheritance, not ellegant, ref,mv
    public static final String MODULE_NAME = "websocket";


    public WebSocket(ServerSocket serverSocket) {
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
            } catch (IOException e) {
                e.printStackTrace();
            }

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
                    out.flush();
                    out.close();
                    in.close();
                    getClient().close();
                } catch (IOException e) {
                    System.err.println("unable to close");
                    System.err.println("Failed processing client request");
                }
            }
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
