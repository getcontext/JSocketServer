package server.module;

import server.core.connection.WebConnectionAbstract;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;

/**
 * @author andrzej.salamon@gmail.com
 */
public class WebModule extends WebConnectionAbstract {
    public static final String MODULE_NAME = "webModuleSocket";

    private PrintWriter printWriter;

    public WebModule(ServerSocket serverSocket) {
        super(serverSocket);
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
//                out.close();
//                in.close();
//                getClient().close();
//                try {
////                out.close();
////                in.close();
//                    client.close();
//                } catch (IOException e) {
//                    // e.printStackTrace();
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void receive() throws IOException {

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

    }
}
