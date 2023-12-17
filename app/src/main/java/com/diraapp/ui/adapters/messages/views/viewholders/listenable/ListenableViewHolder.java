package com.diraapp.ui.adapters.messages.views.viewholders.listenable;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;
import com.diraapp.ui.adapters.messages.views.viewholders.AttachmentViewHolder;
import com.diraapp.ui.singlemediaplayer.GlobalMediaPlayer;

public abstract class ListenableViewHolder extends AttachmentViewHolder {

    private ListenableViewHolderState state = ListenableViewHolderState.UNSELECTED;

    public ListenableViewHolder(@NonNull ViewGroup itemView, MessageAdapterContract messageAdapterContract, ViewHolderManagerContract viewHolderManagerContract, boolean isSelfMessage) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, isSelfMessage);
    }

    abstract public void clearProgress();

    abstract public void setProgress(float progress);

    abstract public void pause(boolean isPaused, float progress);

    abstract public void start();

    @Override
    public void bindMessage(@NonNull Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
        initStateOnAppearance();
    }

    @Override
    public void onViewRecycled() {
        super.onViewRecycled();
        state = ListenableViewHolderState.UNSELECTED;
        if (!isInitialized) return;
        if (getCurrentMessage() == null) return;

        getMessageAdapterContract().isCurrentListeningDisappeared(this);

    }

    @Override
    public void onViewDetached() {
        super.onViewDetached();
        state = ListenableViewHolderState.UNSELECTED;
        if (!isInitialized || getCurrentMessage() == null) {
            return;
        }

        if (getMessageAdapterContract().isCurrentListeningDisappeared(this)) {
            clearProgress();
        }

    }

    @Override
    public void onViewAttached() {
        super.onViewAttached();
        if (!isInitialized || getCurrentMessage() == null) {
            state = ListenableViewHolderState.UNSELECTED;
            return;
        }

        initStateOnAppearance();
    }

    private void initStateOnAppearance() {
        if (getMessageAdapterContract().isCurrentListeningAppeared(this)) return;

        state = ListenableViewHolderState.UNSELECTED;
    }


    public ListenableViewHolderState getState() {
        return state;
    }

    public void setState(ListenableViewHolderState state) {
        this.state = state;
    }
}
