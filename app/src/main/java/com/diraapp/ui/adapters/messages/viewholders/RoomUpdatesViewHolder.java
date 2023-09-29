package com.diraapp.ui.adapters.messages.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.ui.adapters.messages.BaseViewHolder;
import com.diraapp.ui.adapters.messages.ViewHolder;
import com.diraapp.ui.components.MessageReplyComponent;
import com.diraapp.ui.components.RoomMessageCustomClientDataView;
import com.diraapp.ui.components.VoiceMessageView;

public class RoomUpdatesViewHolder extends BaseViewHolder {

    LinearLayout roomUpdatesLayout;
    ImageView roomUpdatesIcon;
    TextView roomUpdatesMainText;
    TextView roomUpdatesText;

    public RoomUpdatesViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void updateViews() {
        roomUpdatesLayout = itemView.findViewById(R.id.room_updates_layout);
        roomUpdatesIcon = itemView.findViewById(R.id.room_updates_icon);
        roomUpdatesMainText = itemView.findViewById(R.id.room_updates_main_text);
        roomUpdatesText = itemView.findViewById(R.id.room_updates_text);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        View view = new RoomMessageCustomClientDataView(itemView.getContext());
        messageContainer.setVisibility(View.VISIBLE);
        viewsContainer.addView(view);

        setInitialised(true);
        updateViews();
    }

    @Override
    public void onBind(Message message, Message previousMessage) {

    }
}
