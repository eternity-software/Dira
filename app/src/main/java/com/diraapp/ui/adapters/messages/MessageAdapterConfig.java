package com.diraapp.ui.adapters.messages;

import android.content.Context;

import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.Room;
import com.diraapp.ui.adapters.messages.legacy.MessageReplyListener;
import com.diraapp.utils.CacheUtils;

import java.util.HashMap;

public interface MessageAdapterConfig {
    Room getRoom();

    HashMap<String, Member> getMembers();

    CacheUtils getCacheUtils();

    Context getContext();
    MessageReplyListener getReplyListener();
}
