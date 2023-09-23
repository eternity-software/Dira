package com.diraapp.api;

import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.processors.listeners.SocketListener;
import com.diraapp.utils.Logger;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

/**
 * Base class for WebSocket connections
 * Handles raw json
 */
public class SocketClient extends WebSocketClient {

    private final String address;

    private SocketListener socketListener;


    public SocketClient(String address) throws URISyntaxException {
        super(new URI(address));
        this.address = address;
    }

    public void setSocketListener(SocketListener socketListener) {
        this.socketListener = socketListener;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Logger.logDebug(this.getClass().getSimpleName(), "Opened new socket");
        if (socketListener != null) {
            socketListener.onSocketOpened();
        }

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Logger.logDebug(this.getClass().getSimpleName(),
                "Socket closed: " + reason + "(" + code + ", by remote " + remote + ")");
        System.out.println("closed with exit code " + code + " additional info: " + reason);
        if (socketListener != null) {
            socketListener.onSocketClosed();
        }
    }

    @Override
    public void onMessage(String message) {

        Logger.logDebug(this.getClass().getSimpleName(), address + " -> " + message);
        try {
            UpdateProcessor.getInstance().notifyMessage(message, address);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(ByteBuffer message) {
        Logger.logDebug(this.getClass().getSimpleName(), "Received ByteBuffer");

    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

}