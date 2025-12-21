package example;

import server.core.listener.HttpMethodListener;
import server.module.WebModule;

public class ExampleHttpListener extends WebModule {
    public ExampleHttpListener() {
        super();
        setRoot("/example");
        registerListener("GET /test", new HttpMethodListener() {
            @Override
            public void onBeforeReceive(Object message) {
                System.out.println("Received request for /example: " + message);
            }

            @Override
            public void onAfterReceive(Object request) {
                //do something with request here
                ExampleHttpListener thisModule = (ExampleHttpListener) getContext();
                thisModule.test((String) request); //call it from context
                //or call it directly
                test("test request from /example");
                System.out.println("Finished processing request for /example: " + request);
            }

            @Override
            public void onBeforeBroadcast(Object message) {
                //add your response here
                setResponse("test response from /example");
                System.out.println("About to broadcast response for /example: " + message);
            }
            @Override
            public void onAfterBroadcast(Object message) {
                System.out.println("Finished broadcasting response for /example: " + message);
            }
        });
    }

    public void test(String test) {
        //do something with data
        setResponse("test response from /example");
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
