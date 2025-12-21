package server.module;

import server.core.HttpMethod;
import server.core.Server;
import server.core.connection.WebConnectionAbstract;
import server.core.listener.HttpMethodListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

/**
 * @author andrzej.salamon@gmail.com
 */
public class WebModule extends WebConnectionAbstract {
    public static final String MODULE_NAME = "webModuleSocket";
    public static final Server.MODULES MODULE_TYPE = Server.MODULES.WEB;

    private PrintWriter printWriter;
    private Map<String, HttpMethodListener> listeners = new HashMap<String, HttpMethodListener>();
    private String signature;
    private String root;
    private String method;
    private String uri;
    private String uriWithMethod;

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
                setRequest("<html><body><h1>Default Response</h1></body></html>");
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
        callBeforeReceive(uriWithMethod);
        StringBuilder requestBuilder = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String line = in.readLine();
        signature = line;
        requestBuilder.append(line);
        while ((line = in.readLine()) != null) {
            requestBuilder.append(line).append("\r\n");
        }
        parseSignature();
        request = requestBuilder.toString();
        callAfterReceive(uriWithMethod);
        System.out.println("Received request:\n" + request);
    }

    private void callBeforeReceive(String uriWithMethod) {
        checkListener(uriWithMethod);
        HttpMethodListener callback = listeners.get(uri);
        callback.setContext(this);
        callback.onBeforeReceive(request);
        callback.setContext(null); //lets clear context after use
    }

    private void checkListener(String uriWithMethod) {
        if (!listeners.containsKey(uriWithMethod)) {
            throw new RuntimeException("No listener for uriWithMethod: " + uriWithMethod);
        }
    }

    private void callAfterReceive(String uriWithMethod) {
        checkListener(uriWithMethod);
        HttpMethodListener callback = listeners.get(uri);
        callback.setContext(this); //current module that extends WebModule
        callback.onAfterReceive(request);
        callback.setContext(null); //lets clear context after use
    }

    @Override
    public void broadcast() {
        printWriter = new PrintWriter(outputStream, true);
        printWriter.println("HTTP/1.1 200 OK");
        printWriter.println("Content-Type: text/html");
        printWriter.println();
        printWriter.println(getResponseAsString());
        flushPrintWriter();
    }

    private void flushPrintWriter() {
        printWriter.flush();
    }

    @Override
    public void broadcast(String data) throws IOException {
        callBeforeBrodcast(uriWithMethod, data);
        printWriter = new PrintWriter(outputStream, true);
        printWriter.println("HTTP/1.1 200 OK");
        printWriter.println("Content-Type: text/html");
        printWriter.println();
        printWriter.println(data);
        flushPrintWriter();
        callAfterBrodcast(uriWithMethod, data);
    }

    private void callAfterBrodcast(String uriWithMethod, String data) {
        checkListener(uriWithMethod);
        HttpMethodListener callback = listeners.get(uri);
        callback.setContext(this); //current module that extends WebModule
        callback.onAfterBroadcast(data);
        callback.setContext(null); //lets clear context after use
    }

    private void callBeforeBrodcast(String uriWithMethod, String data) {
        checkListener(uriWithMethod);
        HttpMethodListener callback = listeners.get(uri);
        callback.setContext(this); //current module that extends WebModule
        callback.onBeforeBroadcast(data);
        callback.setContext(null); //lets clear context after use
    }

    public void parseSignature() {
        String[] parts = signature.split(" ");
        if (parts.length >= 2) {
            this.method = parts[0];
            this.uri = parts[1];
            this.uriWithMethod = method + " " + getRoot() + uri;
        }
    }

    public void registerListener(String uri, HttpMethodListener callback) {
        if (listeners.containsKey(uri)) {
            throw new IllegalArgumentException("Listener for URI already registered: " + uri);
        }
        listeners.put(uri, callback);
    }

    public void registerListener(HttpMethod method, String uri, HttpMethodListener callback) {
        registerListener(method.toString() + " " + uri, callback);
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }
}
