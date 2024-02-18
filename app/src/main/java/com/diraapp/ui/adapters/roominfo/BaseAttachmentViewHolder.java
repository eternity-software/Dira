package com.diraapp.ui.adapters.roominfo;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;

public abstract class BaseAttachmentViewHolder extends RecyclerView.ViewHolder {

    protected final FragmentViewHolderContract contract;

    public BaseAttachmentViewHolder(@NonNull View itemView,
                                    FragmentViewHolderContract listener) {
        super(itemView);
        this.contract = listener;
    }

    public void callScrollToMessage(String messageId, long messageTime) {
        contract.callScrollToMessage(messageId, messageTime);
    }

    protected abstract void bind(AttachmentMessagePair pair);

    public interface FragmentViewHolderContract {

        void callScrollToMessage(String messageId, long messageTime);

        String getMemberName(String memberId);
    }
}
