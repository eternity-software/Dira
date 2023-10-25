package com.diraapp.ui.adapters.messages;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.entities.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.media.DiraMediaPlayer;
import com.diraapp.ui.activities.DiraActivity;
import com.diraapp.ui.adapters.messages.legacy.LegacyRoomMessagesAdapter;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.MessageAttachmentLoader;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;
import com.diraapp.ui.adapters.messages.views.viewholders.AttachmentViewHolder;
import com.diraapp.ui.adapters.messages.views.viewholders.DelayedMessageBind;
import com.diraapp.ui.adapters.messages.views.viewholders.factories.BaseViewHolderFactory;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.android.DeviceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import linc.com.amplituda.Amplituda;

public class MessagesAdapter extends RecyclerView.Adapter<BaseMessageViewHolder>
        implements ViewHolderManagerContract {

    private final BaseViewHolderFactory factory;
    private final AsyncLayoutInflater layoutInflater;
    private final MessageAdapterContract messageAdapterContract;
    private final CacheUtils cacheUtils;
    /**
     * MediaPlayer for audio. Should be transferred to DiraActivity for event handling
     */
    private final DiraMediaPlayer diraMediaPlayer = new DiraMediaPlayer();
    private final Amplituda amplituda;
    private final Executor voiceWaveformsThread = Executors.newSingleThreadExecutor();
    private final MessageAttachmentLoader messageAttachmentLoader;
    /**
     * List of messages to display
     */
    private List<Message> messages = new ArrayList<>();
    private LegacyRoomMessagesAdapter.MessageAdapterListener messageAdapterListener;


    public MessagesAdapter(MessageAdapterContract messageAdapterContract, List<Message> messages,
                           Room room, AsyncLayoutInflater asyncLayoutInflater,
                           BaseViewHolderFactory factory, CacheUtils cacheUtils) {
        this.messages = messages;
        this.layoutInflater = asyncLayoutInflater;
        this.factory = factory;
        this.cacheUtils = cacheUtils;
        this.messageAdapterContract = messageAdapterContract;
        amplituda = new Amplituda(messageAdapterContract.getContext());

        messageAttachmentLoader = new MessageAttachmentLoader(room, messageAdapterContract.getContext());
    }

    @NonNull
    @Override
    public BaseMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        FrameLayout container = new FrameLayout(messageAdapterContract.getContext());
        ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);

        params.height = DeviceUtils.dpToPx(68, messageAdapterContract.getContext());

        container.setLayoutParams(params);

        BaseMessageViewHolder viewHolder = factory.createViewHolder(viewType,
                container, messageAdapterContract, this);


        AsyncLayoutInflater.OnInflateFinishedListener listener = new AsyncLayoutInflater.OnInflateFinishedListener() {
            @Override
            public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup ignoredParent) {
               /* ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);

                container.setLayoutParams(params);*/
                DiraActivity.runOnMainThread(() -> {
                    container.getLayoutParams().height = FrameLayout.LayoutParams.WRAP_CONTENT;
                    container.addView(view);



                    viewHolder.onViewInflated(view);
                    container.requestLayout();
                });


            }
        };


        //LayoutInflater.from(messageAdapterContract.getContext()).inflate(R.layout.self_message, parent, false);
        if (viewHolder.isSelfMessage()) {
            new AsyncLayoutInflater(messageAdapterContract.getContext()).inflate(R.layout.self_message, container, listener);
        } else {
            new AsyncLayoutInflater(messageAdapterContract.getContext()).inflate(R.layout.room_message, container, listener);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseMessageViewHolder holder, int position) {
        Message message = messages.get(position);
        Message previousMessage = null;
        if (position < messages.size() - 1) {
            previousMessage = messages.get(position + 1);
        }

        if (holder instanceof AttachmentViewHolder) {

            messageAttachmentLoader.removeListener(((AttachmentViewHolder) holder)
                    .getAttachmentStorageListener());
            ((AttachmentViewHolder) holder).removeAttachmentStorageListener();
        }

        if (!holder.isInitialized()) {
            holder.setDelayedMessageBind(new DelayedMessageBind(message, previousMessage));
        } else {
            holder.bindMessage(message, previousMessage);
        }


        notifyItemScrolled(message, position);

    }


    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        boolean isSelfMessage = false;
        if (message.hasAuthor())
            isSelfMessage = message.getAuthorId().equals(cacheUtils.getString(CacheUtils.ID));
        return factory.getViewHolderType(message, isSelfMessage).ordinal();
    }


    private void notifyItemScrolled(Message message, int position) {

        if (position == messages.size() - 1) {
            messageAdapterContract.onFirstMessageScrolled(message, position);
        } else if (position == 0) {
            messageAdapterContract.onLastLoadedMessageDisplayed(message, position);
        }
    }

    public void release() {
        messageAttachmentLoader.release();
        diraMediaPlayer.reset();
        diraMediaPlayer.release();
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

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public Amplituda getAmplituda() {
        return amplituda;
    }

    @Override
    public DiraMediaPlayer getDiraMediaPlayer() {
        return diraMediaPlayer;
    }

    @Override
    public Executor getVoiceMessageThread() {
        return voiceWaveformsThread;
    }

    @Override
    public MessageAttachmentLoader getMessageAttachmentLoader() {
        return messageAttachmentLoader;
    }
}
