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
    public final static String MODULE_NAME = "websocket";


    public WebSocket(ServerSocket serverSocket) {
        super(serverSocket);
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

    @Override
    public void broadcast() throws IOException {

    }

    @Override
    public void handleStream(Socket client) {
        try {
            setClient(client);
            //und dat naked fields ?
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

    @Override
    public String getId() {
        return MODULE_NAME;
    }

    @Override
    public void handleStream() {
        try {
            setClient(serverSocket.accept());
            out = new ObjectOutputStream(getClient().getOutputStream());
            in = new ObjectInputStream(getClient().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { //try to close gracefully
                getClient().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        while (!stop) {
            try {
                handleStream(serverSocket.accept());
            } catch (IOException e) {
                e.printStackTrace();
            }

            request = getRequestAsString();

            if (isGet()) { //wat dat iz ? mv, lambda, listener
                if (isHandshake()) {
                    try {
                        sendHandshake();
                    } catch (NoSuchAlgorithmException | IOException e) { //ref ?
                        e.printStackTrace();
                    }
                } else {
                    try {
                        receive();
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

    @Override
    public String getRequestAsString() {
        return new Scanner(in, "UTF-8").useDelimiter("\\r\\n\\r\\n").next(); //bullshit is slow, is immediate release of object
    }

}
