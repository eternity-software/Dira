package com.diraapp.ui.adapters.messages;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.exceptions.UnknownViewTypeException;
import com.diraapp.ui.adapters.messages.legacy.LegacyRoomMessagesAdapter;
import com.diraapp.ui.adapters.messages.views.viewholders.factories.BaseViewHolderFactory;
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

    private final BaseViewHolderFactory factory;

    public MessagesAdapter(List<Message> messages, AsyncLayoutInflater asyncLayoutInflater,
                           BaseViewHolderFactory factory) {
        this.messages = messages;
        this.layoutInflater = asyncLayoutInflater;
        this.factory = factory;
    }
    @NonNull
    @Override
    public BaseMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BaseMessageViewHolder viewHolder = factory.createViewHolder(viewType, parent);

        AsyncLayoutInflater.OnInflateFinishedListener listener = new AsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                viewHolder.onViewInflated(view);
            }
        };

        if(viewHolder.isSelfMessage())
        {
            layoutInflater.inflate(R.layout.self_message, parent, listener);
        }
        else
        {
            layoutInflater.inflate(R.layout.room_message, parent, listener);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseMessageViewHolder holder, int position) {
        if (!holder.isInitialized()) return;

        Message message = messages.get(position);
        Message previousMessage = null;
        if(position < messages.size() - 1)
        {
            previousMessage = messages.get(position + 1);
        }

        holder.bindMessage(message, previousMessage);

        notifyItemScrolled(message, position);

    }

    public void setMessageAdapterListener(LegacyRoomMessagesAdapter.MessageAdapterListener messageAdapterListener) {
        this.messageAdapterListener = messageAdapterListener;
    }

    private void notifyItemScrolled(Message message, int position)
    {
        if(messageAdapterListener == null) return;
        if (position == messages.size() - 1) {
            messageAdapterListener.onFirstItemScrolled(message, position);
        } else if (position == 0) {
            messageAdapterListener.onLastLoadedScrolled(message, position);
        }
    }

    @Override
    public void onViewRecycled(@NonNull BaseMessageViewHolder holder) {
        super.onViewRecycled(holder);
        holder.onViewRecycled();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull BaseMessageViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onViewAttached();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull BaseMessageViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onViewDetached();
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
