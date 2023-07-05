package com.diraapp.api;

import com.diraapp.updates.UpdateProcessor;
import com.diraapp.updates.listeners.SocketListener;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;

public class SocketClient extends WebSocketClient {

    private final String address;

    private SocketListener socketListener;

    public SocketClient(URI serverUri, Draft draft) {
        super(serverUri, draft);
        this.address = serverUri.getHost();
    }

    public SocketClient(URI serverUri) {
        super(serverUri);
        this.address = serverUri.getHost();
    }

    public void setSocketListener(SocketListener socketListener) {
        this.socketListener = socketListener;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("new connection opened");
        if (socketListener != null) {
            socketListener.onSocketOpened();
        }

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("closed with exit code " + code + " additional info: " + reason);
        if (socketListener != null) {
            socketListener.onSocketClosed();
        }
    }

    @Override
    public void onMessage(String message) {
        System.out.println( address + " In ->> " + message);
        try {
            UpdateProcessor.getInstance().notifyMessage(message, address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(ByteBuffer message) {
        System.out.println("received ByteBuffer");
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("an error occurred:" + ex);
    }

}