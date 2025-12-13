package server.core.connection;

//import server.core;
import server.core.ConnectionAbstract;
import server.core.HttpMethod;
import server.core.WebSocketConnection;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class WebSocketConnectionAbstract extends ConnectionAbstract implements WebSocketConnection {
    protected static final String UUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    protected String secWebSocketKey; //@todo cache it with timeout
    private byte[] responseBuffer;

    public WebSocketConnectionAbstract(ServerSocket serverSocket) {
        super(serverSocket);
    }

    /**
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @todo refactor it to separate Handshake class
     */
    public void sendHandshake() throws NoSuchAlgorithmException, IOException {
        //no text in class plz, mv, to cfg
        responseByte = ("HTTP/1.1 101 Switching Protocols\r\n"
                + "WebSocketConnection: Upgrade\r\n"
                + "Upgrade: websocket\r\n"
                + "Sec-WebSocket-Accept: "
                + DatatypeConverter
                .printBase64Binary(
                        MessageDigest
                                .getInstance("SHA-1")
                                .digest((secWebSocketKey + UUID)
                                        .getBytes(StandardCharsets.UTF_8)))
                + "\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8);
        outputStream.write(responseByte, 0, responseByte.length);
    }

    public boolean isHandshake() {
        Matcher match = setSecWebSocketKey();
        return match.find();
    }

    private Matcher setSecWebSocketKey() {
        Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(request);
        secWebSocketKey = match.group(1);
        return match;
    }

    public boolean isGet() {
        Matcher get = Pattern.compile("^"+ HttpMethod.GET).matcher(request);
        return get.find();
    }

    public void receive() throws IOException {
        responseBuffer = new byte[WebSocketConnection.MAX_BUFFER];
        int messageLength, mask, dataStart;

        messageLength = inputStream.read(responseBuffer);
        if (messageLength == -1) {
            return;
        }

        responseByte = new byte[messageLength];

        //b[0] is always text in my case so no need to check;
        byte data = responseBuffer[1]; //does it cause a problem ?
        byte op = (byte) 127;
        byte length;

        length = (byte) (data & op);

        mask = 2;  //lowest mask
        if (length == (byte) 126) mask = 4;//med
        if (length == (byte) 127) mask = 10; //max mask

        byte[] masks = new byte[4];

        int j = 0, i=mask;
        for (; i < (mask + 4); i++) { //start at mask, stop at last + 4
            masks[j] = responseBuffer[i]; //problem here
            j++;
        }

        dataStart = mask + 4;

        for (i = dataStart, j = 0; i < messageLength; i++, j++) {
            responseByte[j] = (byte) (responseBuffer[i] ^ masks[j % 4]);
        }

        response = new String(responseByte); //why now string copy of byte ?
    }

    public void broadcast(String data) throws IOException {
        byte[] rawData = data.getBytes();
        int len = rawData.length, frameCount;

        frame[0] = (byte) 129;
        /* @TODO: loop it */ //or no, loop is more expensive in dat case
        //is fixed, make it 2 dim static pre-comp,
        //heart of app
        //const logic and byte 255
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

        requestByte = new byte[responseLength];

        for (; responseLimit < frameCount; responseLimit++) {
            requestByte[responseLimit] = frame[responseLimit];
        }

        for (byte dataByte : rawData) {
            requestByte[responseLimit++] = dataByte;
        }

        outputStream.write(requestByte);
        flushOutputStream();

    }

    @Override
    public void broadcast() throws IOException {

    }

    @Override
    public String getRequestAsString() {
        return new Scanner(inputStream, "UTF-8").useDelimiter("\\r\\n\\r\\n").next();
    }
}
