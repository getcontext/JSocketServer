package example;

import server.core.listener.HttpMethodListener;
import server.module.WebModule;

public class ExampleHttpListener extends WebModule {
    public ExampleHttpListener() {
        super();
        setRoot("example");

        addUriListener("GET /" + this.getRoot() + "/test", new HttpMethodListener(this) {
            @Override
            public void onBeforeReceive(Object message) {
                System.out.println("Received request for /example: " + message);
            }

            @Override
            public void onAfterReceive(Object message) {
                ExampleHttpListener thisModule = (ExampleHttpListener) getContext();
                thisModule.test((String) message);
                System.out.println("Finished processing request for /example: " + message);
            }
        });
    }

    public void test(String test) {
    }
    //    public static void main(String[] args) {
//        HttpMethodListener listener = new HttpMethodListener() {
//            @Override
//            public void onBeforeReceive(Object message) {
//                System.out.println("Before receiving: " + message);
//            }
//
//            @Override
//            public void onAfterReceive(Object message) {
//                System.out.println("After receiving: " + message);
//            }
//        };
//
//        // Simulate receiving a message
//        String simulatedMessage = "GET /index.html HTTP/1.1";
//        listener.onBeforeReceive(simulatedMessage);
//        // Here would be the logic to process the message
//        listener.onAfterReceive(simulatedMessage);
//    }
}
