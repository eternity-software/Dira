package com.diraapp.ui.adapters.messages.views.viewholders.factories;

import android.view.ViewGroup;

import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;

public interface BaseViewHolderFactory {

    BaseMessageViewHolder createViewHolder(int intType, ViewGroup parent,
                                           MessageAdapterContract messageAdapterContract,
                                           ViewHolderManagerContract viewHolderManagerContract);

    MessageHolderType getViewHolderType(Message message, boolean isSelfMessage);

}
