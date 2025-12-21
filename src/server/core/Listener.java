package server.core;

public interface Listener {
    void onBeforeReceive(Object message);
    void onBeforeBroadcast(Object message);
    void onAfterReceive(Object message);
    void onAfterBroadcast(Object message);
}
