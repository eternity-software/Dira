package com.diraapp.ui.adapters.messages;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.legacy.LegacyRoomMessagesAdapter;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.factories.BaseViewHolderFactory;
import com.diraapp.utils.CacheUtils;

import java.util.ArrayList;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<BaseMessageViewHolder> {

    private final BaseViewHolderFactory factory;
    /**
     * List of messages to display
     */
    private List<Message> messages = new ArrayList<>();
    private final AsyncLayoutInflater layoutInflater;
    private LegacyRoomMessagesAdapter.MessageAdapterListener messageAdapterListener;
    private final MessageAdapterContract messageAdapterContract;

    private final CacheUtils cacheUtils;

    public MessagesAdapter(MessageAdapterContract messageAdapterContract, List<Message> messages, Room room, AsyncLayoutInflater asyncLayoutInflater,
                           BaseViewHolderFactory factory, CacheUtils cacheUtils) {
        this.messages = messages;
        this.layoutInflater = asyncLayoutInflater;
        this.factory = factory;
        this.cacheUtils = cacheUtils;
        this.messageAdapterContract = messageAdapterContract;
    }

    @NonNull
    @Override
    public BaseMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        BaseMessageViewHolder viewHolder = factory.createViewHolder(viewType,
                parent, messageAdapterContract);

        AsyncLayoutInflater.OnInflateFinishedListener listener = new AsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
                viewHolder.onViewInflated(view);
            }
        };

        if (viewHolder.isSelfMessage()) {
            layoutInflater.inflate(R.layout.self_message, parent, listener);
        } else {
            layoutInflater.inflate(R.layout.room_message, parent, listener);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseMessageViewHolder holder, int position) {
        if (!holder.isInitialized()) return;

        Message message = messages.get(position);
        Message previousMessage = null;
        if (position < messages.size() - 1) {
            previousMessage = messages.get(position + 1);
        }

        holder.bindMessage(message, previousMessage);

        notifyItemScrolled(message, position);

    }


    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        boolean isSelfMessage = message.getAuthorId().equals(cacheUtils.getString(CacheUtils.ID));
        return factory.getViewHolderType(message, isSelfMessage).ordinal();
    }

    public void setMessageAdapterListener(LegacyRoomMessagesAdapter.MessageAdapterListener messageAdapterListener) {
        this.messageAdapterListener = messageAdapterListener;
    }

    private void notifyItemScrolled(Message message, int position) {
        if (messageAdapterListener == null) return;
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
