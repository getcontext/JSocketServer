package server.core.listener;

import server.core.Listener;

public class HttpMethodListener implements Listener {
    private Object context;
    public HttpMethodListener() {
    }

    @Override
    public void onBeforeReceive(Object message) {

    }

    @Override
    public void onBeforeBroadcast(Object message) {

    }

    @Override
    public void onAfterReceive(Object message) {

    }

    @Override
    public void onAfterBroadcast(Object message) {

    }

    public Object getContext() {
        return context;
    }

    public void setContext(Object context) {
        this.context = context;
    }
}
