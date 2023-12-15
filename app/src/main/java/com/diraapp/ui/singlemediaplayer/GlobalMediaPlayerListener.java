package com.diraapp.ui.singlemediaplayer;

import com.diraapp.db.entities.messages.Message;

import java.io.File;

public interface GlobalMediaPlayerListener {

    void onPauseClicked(boolean isPaused);

    void onClose();

    void onStart(Message message, File file);

    void onProgressChanged(float progress, Message message);
}
