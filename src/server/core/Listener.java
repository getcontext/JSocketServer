package server.core;

public interface Listener<T> {
    void onReceive(T message);
    void onBroadcast(T message);
    T onBroadcast();
    T onReceive();
}
