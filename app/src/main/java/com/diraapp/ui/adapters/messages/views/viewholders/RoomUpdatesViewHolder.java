package com.diraapp.ui.adapters.messages.views.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.components.RoomMessageCustomClientDataView;

public class RoomUpdatesViewHolder extends BaseMessageViewHolder {

    LinearLayout roomUpdatesLayout;
    ImageView roomUpdatesIcon;
    TextView roomUpdatesMainText;
    TextView roomUpdatesText;

    public RoomUpdatesViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void onViewInflated(View rootView) {
        super.onViewInflated(rootView);
        View view = new RoomMessageCustomClientDataView(itemView.getContext());
        messageContainer.setVisibility(View.VISIBLE);
        postInflatedViewsContainer.addView(view);

        roomUpdatesLayout = itemView.findViewById(R.id.room_updates_layout);
        roomUpdatesIcon = itemView.findViewById(R.id.room_updates_icon);
        roomUpdatesMainText = itemView.findViewById(R.id.room_updates_main_text);
        roomUpdatesText = itemView.findViewById(R.id.room_updates_text);
        isInitialised = true;
    }

    @Override
    public void bindMessage(Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
    }
}
