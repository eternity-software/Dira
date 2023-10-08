package com.diraapp.ui.adapters.messages;

import android.content.Context;
import android.view.View;

import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Room;
import com.diraapp.media.DiraMediaPlayer;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.legacy.MessageReplyListener;
import com.diraapp.ui.components.VideoPlayer;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.utils.CacheUtils;

import java.util.HashMap;

import linc.com.amplituda.Amplituda;

/**
 * Data that is used for MessagesAdapter to work properly
 */

public interface MessageAdapterContract {
    Room getRoom();

    HashMap<String, Member> getMembers();

    CacheUtils getCacheUtils();

    @Deprecated
    Context getContext();

    MessageReplyListener getReplyListener();

    void onFirstMessageScrolled(Message message, int index);

    default void onLastLoadedMessageDisplayed(Message message, int index) {
    }

    void openPreviewActivity(String filePath, boolean isVideo, View transitionSource);

    void attachVideoPlayer(DiraVideoPlayer player);
}
