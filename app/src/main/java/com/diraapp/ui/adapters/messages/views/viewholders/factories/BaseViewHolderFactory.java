package com.diraapp.ui.adapters.messages.views.viewholders.factories;

import android.view.View;
import android.view.ViewGroup;

import com.diraapp.db.entities.messages.Message;
import com.diraapp.exceptions.UnknownViewTypeException;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;

public interface BaseViewHolderFactory {

    BaseMessageViewHolder createViewHolder(int intType, ViewGroup parent);
    MessageHolderType getViewHolderType(Message message, boolean isSelfMessage);

}
