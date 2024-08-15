package io.github.jilipop.adohri.wifi;

public interface SenderConnectionCallback {
    void onSenderConnected();
    void onSenderDisconnected();
    void onUserDeniedConnection();
    void onConnectionFailed();
}
