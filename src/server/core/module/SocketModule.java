package server.core.module;

import server.core.SocketConnection;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SocketModule extends server.core.Module implements SocketConnection {

    public SocketModule(ServerSocket serverSocket) {
        super(serverSocket);
    }

}
