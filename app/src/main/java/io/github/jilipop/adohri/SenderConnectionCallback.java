package io.github.jilipop.adohri;

public interface SenderConnectionCallback {
    void onSenderConnected();
    void onSenderDisconnected();
    void onUserDeniedConnection();
    void onConnectionFailed();
}
