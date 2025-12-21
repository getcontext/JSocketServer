package server.module;

import server.core.Listener;
import server.core.Server;
import server.core.connection.WebConnectionAbstract;
import server.core.listener.HttpMethodListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;

/**
 * @author andrzej.salamon@gmail.com
 */
public class WebModule extends WebConnectionAbstract {
    public static final String MODULE_NAME = "webModuleSocket";
    public static final Server.MODULES MODULE_TYPE = Server.MODULES.WEB;

    private PrintWriter printWriter;

    private HttpMethodListener httpMethodListener = new HttpMethodListener(this);
    private String signature;
    private String root;

    public WebModule(ServerSocket serverSocket) {
        super(serverSocket);
    }

    public WebModule() {
        super();
    }

    @Override
    public String getId() {
        return MODULE_NAME;
    }

    public void run() {
        while (!stop) {
            try {
                processStream();
                receive();
                broadcast();
                flushOutputStream();
                closeOutpInputStreams();
                getClient().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void closeOutpInputStreams() throws IOException {
        outputStream.close();
        inputStream.close();
    }

    @Override
    public void receive() throws IOException {
        StringBuilder requestBuilder = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String line = in.readLine();
        signature = line;
        requestBuilder.append(line);
        while ((line = in.readLine()) != null) {
            requestBuilder.append(line).append("\r\n");
        }
        request = requestBuilder.toString();
        System.out.println("Received request:\n" + request);
    }

    @Override
    public void broadcast() throws IOException {
        printWriter = new PrintWriter(outputStream, true);
        printWriter.println("HTTP/1.1 200 OK");
        printWriter.println("Content-Type: text/html");
        printWriter.println();
        printWriter.println("<html><body><h1>Hello, World!</h1></body></html>");
        flushPrintWriter();
    }

    private void flushPrintWriter() {
        printWriter.flush();
    }

    @Override
    public void broadcast(String data) throws IOException {
        printWriter = new PrintWriter(outputStream, true);
        printWriter.println("HTTP/1.1 200 OK");
        printWriter.println("Content-Type: text/html");
        printWriter.println();
        printWriter.println(data);
        flushPrintWriter();
    }

    public String getUri() {
        String[] parts = signature.split(" ");
        if (parts.length >= 2) {
            return parts[1];
        }
        return null;
    }

    public boolean addUriListener(String uri, HttpMethodListener callback) {
        return httpMethodListener.addUriListener(uri, callback);
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }
}
