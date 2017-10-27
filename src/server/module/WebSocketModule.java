package server.module;

import server.core.WebSocketConnection;

import javax.xml.bind.DatatypeConverter;
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

/**
 * @author andrzej.salamon@gmail.com
 */
public class WebSocketModule extends Thread implements WebSocketConnection {

    ObjectOutputStream out;
    ObjectInputStream in;
    byte[] requestByte;
    byte[] responseByte;
    byte[] frame = new byte[10];
    String response;
    Socket client;
    ServerSocket serverSocket;
    private boolean close = false;
    private String secWebSocketKey;


    public WebSocketModule(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.start();
    }

    @Override
    public void handleStream() {
        try {
            client = serverSocket.accept();
            out = new ObjectOutputStream(client.getOutputStream());
            in = new ObjectInputStream(client.getInputStream());
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
        while (true) {
            handleStream();
            String data = getRequestAsString();

            if (isGet(data)) {
                if (isHandshake(data)) {
                    try {
                        sendHandshake();
                    } catch (NoSuchAlgorithmException | IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        String msg = receive();
                        System.out.println(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (close) {
                try {
                    out.writeObject(response);
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
                sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void sendHandshake() throws NoSuchAlgorithmException, IOException {
        byte[] response;
        response = ("HTTP/1.1 101 Switching Protocols\r\n"
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
        out.write(response, 0, response.length);
    }

    @Override
    public boolean isHandshake(String data) {
        Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
        secWebSocketKey = match.group(1);
        return match.find();
    }

    @Override
    public boolean isGet(String data) {
        Matcher get = Pattern.compile("^GET").matcher(data);
        return get.find();
    }

    @Override
    public String getRequestAsString() {
        return new Scanner(in, "UTF-8").useDelimiter("\\r\\n\\r\\n").next();
    }

    @Override
    public String receive() throws IOException {
        byte[] buffer = new byte[MAX_BUFFER];
        byte length = 0;
        int messageLength, maskIndex = 2, dataStart = 0;

        messageLength = in.read(buffer);
        requestByte = new byte[messageLength];

        if (messageLength != -1) {
            //b[0] is always text in my case so no need to check;
            byte data = buffer[1];
            byte op = (byte) 127;
            length = (byte) (data & op);

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
        }

        return new String(requestByte);
    }

    @Override
    public void brodcast(String data) throws IOException {
        byte[] rawData = data.getBytes();

        int frameCount;

        frame[0] = (byte) 129;
/** @TODO: loop it */
        if (rawData.length <= 125) {
            frame[1] = (byte) rawData.length;
            frameCount = 2;
        } else if (rawData.length >= 126 && rawData.length <= 65535) {
            frame[1] = (byte) 126;
            int len = rawData.length;
            frame[2] = (byte) ((len >> 8) & (byte) 255);
            frame[3] = (byte) (len & (byte) 255);
            frameCount = 4;
        } else {
            frame[1] = (byte) 127;
            int len = rawData.length;
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

        int bLength = frameCount + rawData.length;

        responseByte = new byte[bLength];

        int bLim = 0;
        for (int i = 0; i < frameCount; i++) {
            responseByte[bLim] = frame[i];
            bLim++;
        }
        for (int i = 0; i < rawData.length; i++) {
            responseByte[bLim] = rawData[i];
            bLim++;
        }

        out.write(responseByte);
        out.flush();

    }
}
