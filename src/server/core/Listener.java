package server.core;

public interface Listener {
    public enum WHEN {
        BEFORE,
        AFTER
    }
    WHEN defaultWhen = WHEN.AFTER;

    void onBeforeReceive(Object message);
    void onBeforeBroadcast(Object message);
    Object onBeforeBroadcast();
    Object onBeforeReceive();
    void onAfterReceive(Object message);
    void onAfterBroadcast(Object message);
    Object onAfterBroadcast();
    Object onAfterReceive();
}
