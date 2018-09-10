package server.core;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class WebSocketModule extends Module {
    protected String secWebSocketKey;

    public WebSocketModule(ServerSocket serverSocket) {
        super(serverSocket);
    }

    /**
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @todo refactor it to separate Handshake class
     */
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

    public boolean isHandshake() {
        Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(request);
        secWebSocketKey = match.group(1);
        return match.find();
    }

    public boolean isHandshake(String data) {
        return false;
    }

    public boolean isGet() {
        Matcher get = Pattern.compile("^GET").matcher(request);
        return get.find();
    }

    public boolean isGet(String data) {
        return false;
    }

    public void receive() throws IOException {
        byte[] buffer = new byte[WebSocketConnection.MAX_BUFFER];
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
}
