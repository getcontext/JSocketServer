package server.core.module;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SocketModule extends Module implements SocketConnection  {

    public SocketModule(ServerSocket serverSocket) {
        super(serverSocket);
    }

}
