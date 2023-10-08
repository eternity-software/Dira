package com.diraapp.ui.adapters.messages.views.viewholders;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;
import com.diraapp.ui.components.RoomMessageCustomClientDataView;

public class RoomUpdatesViewHolder extends BaseMessageViewHolder {

    private LinearLayout roomUpdatesLayout;
    private ImageView roomUpdatesIcon;
    private TextView roomUpdatesMainText;
    private TextView roomUpdatesText;

    public RoomUpdatesViewHolder(@NonNull ViewGroup itemView, MessageAdapterContract messageAdapterContract,
                                 ViewHolderManagerContract viewHolderManagerContract) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, false);

    }


    @Override
    protected void postInflate() {
        super.postInflate();
        View view = new RoomMessageCustomClientDataView(itemView.getContext());
        messageContainer.setVisibility(View.VISIBLE);
        postInflatedViewsContainer.addView(view);

        messageText.setVisibility(View.GONE);

        roomUpdatesLayout = itemView.findViewById(R.id.room_updates_layout);
        roomUpdatesIcon = itemView.findViewById(R.id.room_updates_icon);
        roomUpdatesMainText = itemView.findViewById(R.id.room_updates_main_text);
        roomUpdatesText = itemView.findViewById(R.id.room_updates_text);
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
    }

    @Override
    public void postInflateReplyViews() {

    }
}
