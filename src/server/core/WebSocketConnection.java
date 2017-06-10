package server.core;

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
class WebSocketConnection extends Thread implements Connection {

    ObjectOutputStream out;
    ObjectInputStream in;
    String requestString;
    byte[] requestByte;
    String response;
    Socket client;
    ServerSocket serverSocket;
    private boolean close = false;
    private boolean isGet;
    private String secWebSocketKey;


    public WebSocketConnection(ServerSocket serverSocket) {
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
                        String msg = read();
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
                + "Connection: Upgrade\r\n"
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
    public String read() throws IOException {
        byte[] buffer = new byte[MAX_BUFFER];
        int len;
        int messLen;
        byte rLength = 0;
        int rMaskIndex = 2;
        int rDataStart = 0;
        byte[] message;

        len = in.read(buffer);
        messLen = len - rDataStart;
        message = new byte[messLen];

        if (len != -1) {
            //b[0] is always text in my case so no need to check;
            byte data = buffer[1];
            byte op = (byte) 127;
            rLength = (byte) (data & op);

            if (rLength == (byte) 126) rMaskIndex = 4;
            if (rLength == (byte) 127) rMaskIndex = 10;

            byte[] masks = new byte[4];

            int j = 0;
            int i;
            for (i = rMaskIndex; i < (rMaskIndex + 4); i++) {
                masks[j] = buffer[i];
                j++;
            }

            rDataStart = rMaskIndex + 4;


            for (i = rDataStart, j = 0; i < len; i++, j++) {
                message[j] = (byte) (buffer[i] ^ masks[j % 4]);
            }
        }
        return new String(message);
    }

    @Override
    public void brodcast(String data) throws IOException{
        byte[] rawData = data.getBytes();

        int frameCount  = 0;
        byte[] frame = new byte[10];

        frame[0] = (byte) 129;

        if(rawData.length <= 125){
            frame[1] = (byte) rawData.length;
            frameCount = 2;
        }else if(rawData.length >= 126 && rawData.length <= 65535){
            frame[1] = (byte) 126;
            int len = rawData.length;
            frame[2] = (byte)((len >> 8 ) & (byte)255);
            frame[3] = (byte)(len & (byte)255);
            frameCount = 4;
        }else{
            frame[1] = (byte) 127;
            int len = rawData.length;
            frame[2] = (byte)((len >> 56 ) & (byte)255);
            frame[3] = (byte)((len >> 48 ) & (byte)255);
            frame[4] = (byte)((len >> 40 ) & (byte)255);
            frame[5] = (byte)((len >> 32 ) & (byte)255);
            frame[6] = (byte)((len >> 24 ) & (byte)255);
            frame[7] = (byte)((len >> 16 ) & (byte)255);
            frame[8] = (byte)((len >> 8 ) & (byte)255);
            frame[9] = (byte)(len & (byte)255);
            frameCount = 10;
        }

        int bLength = frameCount + rawData.length;

        byte[] reply = new byte[bLength];

        int bLim = 0;
        for(int i=0; i<frameCount;i++){
            reply[bLim] = frame[i];
            bLim++;
        }
        for(int i=0; i<rawData.length;i++){
            reply[bLim] = rawData[i];
            bLim++;
        }

        out.write(reply);
        out.flush();

    }
}
