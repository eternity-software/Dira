package com.diraapp.ui.singlemediaplayer;

import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.views.viewholders.listenable.ListenableViewHolder;

import java.io.File;

public interface GlobalMediaPlayerListener {

    void onGlobalMediaPlayerPauseClicked(boolean isPaused, float progress);

    void onGlobalMediaPlayerClose();

    void onGlobalMediaPlayerStart(Message message, File file, ListenableViewHolder viewHolder);

    void onGlobalMediaPlayerProgressChanged(float progress, Message message);
}
