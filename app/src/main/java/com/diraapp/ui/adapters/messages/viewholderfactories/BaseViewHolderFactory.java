package com.diraapp.ui.adapters.messages.viewholderfactories;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.db.entities.messages.Message;
import com.diraapp.exceptions.UnknownViewTypeException;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;

public interface BaseViewHolderFactory {

    BaseMessageViewHolder createViewHolder(int intType, View parent) throws UnknownViewTypeException;
    ViewHolderType getViewHolderType(Message message, boolean isSelfMessage) throws UnknownViewTypeException;

}
