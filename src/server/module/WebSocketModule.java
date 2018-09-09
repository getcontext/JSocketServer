package server.module;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import server.core.Module;
import server.core.WebSocketConnection;

/**
 * @author andrzej.salamon@gmail.com
 */
public final class WebSocketModule extends Module implements Runnable, WebSocketConnection {
    public final static String MODULE_NAME = "websocket";

    private ObjectOutputStream out;
    private ObjectInputStream in;

    private byte[] requestByte;
    private byte[] responseByte;
    private byte[] frame = new byte[10];

    private String response;
    private String request;
    private String secWebSocketKey;

    private boolean close = false;
    private static int counter = 0;
    private int instanceNo;
    private boolean stop = false;

    private Socket client;
    private ServerSocket serverSocket;
    private final Thread thread;


    public WebSocketModule(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.instanceNo = counter++;
        this.thread = new Thread(this, MODULE_NAME + "_" + instanceNo);
    }

    public static int getCounter() {
        return counter;
    }

    public static void setCounter(int counter) {
        WebSocketModule.counter = counter;
    }

    public static void incrementCounter(int counter) {
        ++WebSocketModule.counter;
    }

    @Override
    public void start() {
        thread.start();
    }

    @Override
    public void stop() {
        thread.stop();
        stop = true;
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
                client.close();
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

            if (isGet()) {
                if (isHandshake()) {
                    try {
                        sendHandshake();
                    } catch (NoSuchAlgorithmException | IOException e) {
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
                    client.close();
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

    /**
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @todo refactor it to separate Handshake class
     */
    @Override
    public void sendHandshake() throws NoSuchAlgorithmException, IOException {
        responseByte = ("HTTP/1.1 101 Switching Protocols\r\n"
                + "WebSocketConnection: Upgrade\r\n"
                + "Upgrade: websocket\r\n"
                + "Sec-WebSocket-Accept: "
                + DatatypeConverter
                .printBase64Binary(
                        MessageDigest
                                .getInstance("SHA-1")
                                .digest((secWebSocketKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                        .getBytes("UTF-8")))
                + "\r\n\r\n")
                .getBytes("UTF-8");
        out.write(responseByte, 0, responseByte.length);
    }

    @Override
    public boolean isHandshake() {
        Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(request);
        secWebSocketKey = match.group(1);
        return match.find();
    }

    @Override
    public boolean isHandshake(String data) {
        return false;
    }

    @Override
    public boolean isGet() {
        Matcher get = Pattern.compile("^GET").matcher(request);
        return get.find();
    }

    @Override
    public boolean isGet(String data) {
        return false;
    }

    @Override
    public String getRequestAsString() {
        return new Scanner(in, "UTF-8").useDelimiter("\\r\\n\\r\\n").next();
    }

    @Override
    public void receive() throws IOException {
        byte[] buffer = new byte[MAX_BUFFER];
        byte length;
        int messageLength, maskIndex, dataStart;

        messageLength = in.read(buffer);
        if (messageLength == -1) {
            return;
        }

        requestByte = new byte[messageLength];

        //b[0] is always text in my case so no need to check;
        byte data = buffer[1];
        byte op = (byte) 127;
        length = (byte) (data & op);

        maskIndex = 2;
        if (length == (byte) 126) maskIndex = 4;
        if (length == (byte) 127) maskIndex = 10;

        byte[] masks = new byte[4];

        int j = 0;
        int i;
        for (i = maskIndex; i < (maskIndex + 4); i++) {
            masks[j] = buffer[i];
            j++;
        }

        dataStart = maskIndex + 4;

        for (i = dataStart, j = 0; i < messageLength; i++, j++) {
            requestByte[j] = (byte) (buffer[i] ^ masks[j % 4]);
        }

        response = new String(requestByte);
    }

    @Override
    public void broadcast(String data) throws IOException {
        byte[] rawData = data.getBytes();
        int len = rawData.length, frameCount;

        frame[0] = (byte) 129;
        /* @TODO: loop it */
        if (rawData.length <= 125) {
            frame[1] = (byte) len;
            frameCount = 2;
        } else if (rawData.length <= 65535) {
            frame[1] = (byte) 126;
            frame[2] = (byte) ((len >> 8) & (byte) 255);
            frame[3] = (byte) (len & (byte) 255);
            frameCount = 4;
        } else {
            frame[1] = (byte) 127;
            frame[2] = (byte) ((len >> 56) & (byte) 255);
            frame[3] = (byte) ((len >> 48) & (byte) 255);
            frame[4] = (byte) ((len >> 40) & (byte) 255);
            frame[5] = (byte) ((len >> 32) & (byte) 255);
            frame[6] = (byte) ((len >> 24) & (byte) 255);
            frame[7] = (byte) ((len >> 16) & (byte) 255);
            frame[8] = (byte) ((len >> 8) & (byte) 255);
            frame[9] = (byte) (len & (byte) 255);
            frameCount = 10;
        }

        int responseLength = frameCount + rawData.length;
        int responseLimit = 0;

        responseByte = new byte[responseLength];

        for (; responseLimit < frameCount; responseLimit++) {
            responseByte[responseLimit] = frame[responseLimit];
        }

        for (byte dataByte : rawData) {
            responseByte[responseLimit++] = dataByte;
        }

        out.write(responseByte);
        out.flush();

    }

    public Thread getThread() {
        return thread;
    }

    private Socket getClient() {
        return client;
    }

    private void setClient(Socket client) {
        this.client = client;
    }
}
