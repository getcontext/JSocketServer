package server.core.listener;

import server.core.HttpMethod;
import server.core.Listener;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HttpMethodListener implements Listener {
    private Map<String, Object> uris = new HashMap<>();
    private Object context;
    public HttpMethodListener(Object context) {
        this.context = context;
    }

    @Override
    public void onBeforeReceive(Object message) {

    }

    @Override
    public void onBeforeBroadcast(Object message) {

    }

    @Override
    public Object onBeforeBroadcast() {
        return null;
    }

    @Override
    public Object onBeforeReceive() {
        return null;
    }

    @Override
    public void onAfterReceive(Object message) {

    }

    @Override
    public void onAfterBroadcast(Object message) {

    }

    @Override
    public Object onAfterBroadcast() {
        return null;
    }

    @Override
    public Object onAfterReceive() {
        return null;
    }

    public boolean addUriListener(String uri, HttpMethodListener callback) {
        uris.put(uri, callback);
        return true;
    }

    public Object getContext() {
        return context;
    }
}
