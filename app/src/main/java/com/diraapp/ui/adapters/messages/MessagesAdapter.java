package com.diraapp.ui.adapters.messages;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.legacy.LegacyRoomMessagesAdapter;
import com.diraapp.ui.adapters.messages.legacy.LegacyViewHolder;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;

import java.util.ArrayList;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<BaseMessageViewHolder> {

    /**
     * List of messages to display
     */
    private List<Message> messages = new ArrayList<>();

    private AsyncLayoutInflater layoutInflater;

    private LegacyRoomMessagesAdapter.MessageAdapterListener messageAdapterListener;

    public MessagesAdapter(List<Message> messages, AsyncLayoutInflater asyncLayoutInflater) {
        this.messages = messages;
        this.layoutInflater = asyncLayoutInflater;
    }

    public void setMessageAdapterListener(LegacyRoomMessagesAdapter.MessageAdapterListener messageAdapterListener) {
        this.messageAdapterListener = messageAdapterListener;
    }

    @NonNull
    @Override
    public BaseMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseMessageViewHolder holder, int position) {

        Message message = messages.get(position);

        notifyItemScrolled(message, position);

    }


    private void notifyItemScrolled(Message message, int position)
    {
        if (position == messages.size() - 1) {
            messageAdapterListener.onFirstItemScrolled(message, position);
        } else if (position == 0) {
            messageAdapterListener.onLastLoadedScrolled(message, position);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
