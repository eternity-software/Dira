package com.diraapp.ui.singlemediaplayer;

import com.diraapp.db.entities.messages.Message;

import java.io.File;

public interface GlobalMediaPlayerListener {

    void onGlobalMediaPlayerPauseClicked(boolean isPaused, float progress);

    void onGlobalMediaPlayerClose();

    void onGlobalMediaPlayerStart(Message message, File file);

    void onGlobalMediaPlayerProgressChanged(float progress, Message message);
}
