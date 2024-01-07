package com.diraapp.ui.adapters.messages;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.ui.activities.DiraActivityListener;
import com.diraapp.ui.activities.PreparedActivity;
import com.diraapp.ui.adapters.messages.legacy.MessageReplyListener;
import com.diraapp.ui.adapters.messages.views.BalloonMessageMenu;
import com.diraapp.ui.adapters.messages.views.viewholders.listenable.ListenableViewHolder;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.ui.waterfalls.WaterfallBalancer;
import com.diraapp.utils.CacheUtils;

import java.io.File;
import java.util.HashMap;

/**
 * Data that is used for MessagesAdapter to work properly
 */

public interface MessageAdapterContract {
    WaterfallBalancer getWaterfallBalancer();

    Room getRoom();

    HashMap<String, Member> getMembers();

    CacheUtils getCacheUtils();

    @Deprecated
    Context getContext();

    MessageReplyListener getReplyListener();

    void runOnUiThread(Runnable runnable);

    void onFirstMessageScrolled(Message message, int index);

    default void onLastLoadedMessageDisplayed(Message message, int index) {
    }

    PreparedActivity preparePreviewActivity(String filePath, boolean isVideo, Bitmap preview, View transitionSource);

    void attachVideoPlayer(DiraVideoPlayer player);

    void addListener(DiraActivityListener player);

    BalloonMessageMenu.BalloonMenuListener getBalloonMessageListener();

    void onMessageAttached(Message message);

    void onMessageDetached(Message message);

    boolean isMessageNeedBlink(String messageId);

    boolean isCurrentListeningAppeared(ListenableViewHolder viewHolder);

    boolean isCurrentListeningDisappeared(ListenableViewHolder viewHolder);

    void currentListenableStarted(ListenableViewHolder viewHolder, File file, float progress);

    void currentListenablePaused(ListenableViewHolder viewHolder);

    void currentListenableProgressChangedByUser(float progress, ListenableViewHolder viewHolder);
}
