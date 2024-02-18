package com.diraapp.ui.adapters.roominfo;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseAttachmentViewHolder extends RecyclerView.ViewHolder {

    ScrollToMessageButtonListener listener;

    public BaseAttachmentViewHolder(@NonNull View itemView,
                                    ScrollToMessageButtonListener listener) {
        super(itemView);
        this.listener = listener;
    }

    public void callScrollToMessage(String messageId, long messageTime) {
        listener.callScrollToMessage(messageId, messageTime);
    }

    public interface ScrollToMessageButtonListener {

        void callScrollToMessage(String messageId, long messageTime);
    }
}
