package com.diraapp.ui.adapters.messages.views.viewholders;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.messages.customclientdata.KeyGenerateStartClientData;
import com.diraapp.db.entities.messages.customclientdata.KeyGeneratedClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomIconChangeClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomJoinClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomNameAndIconChangeClientData;
import com.diraapp.db.entities.messages.customclientdata.RoomNameChangeClientData;
import com.diraapp.db.entities.messages.customclientdata.UnencryptedMessageClientData;
import com.diraapp.res.Theme;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.BaseMessageViewHolder;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;
import com.diraapp.ui.components.RoomMessageCustomClientDataView;
import com.diraapp.utils.android.DeviceUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Objects;

public class RoomUpdatesViewHolder extends BaseMessageViewHolder {

    private LinearLayout roomUpdatesLayout;
    private ImageView roomUpdatesIcon;
    private TextView roomUpdatesMainText;
    private TextView roomUpdatesText;

    public RoomUpdatesViewHolder(@NonNull ViewGroup itemView,
                                 MessageAdapterContract messageAdapterContract,
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
    public void bindMessage(@NonNull Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);
        itemView.setClickable(false);

        profilePictureContainer.setVisibility(View.VISIBLE);

        boolean isRoomUpdate = true;

        Context context = this.itemView.getContext();
        if (message.getCustomClientData() instanceof RoomJoinClientData) {

            String path = ((RoomJoinClientData) message.getCustomClientData()).getPath();
            setImageOnRoomUpdateMessage(path);
            roomUpdatesIcon.setImageDrawable(ContextCompat.
                    getDrawable(context, R.drawable.ic_room_updates));
            roomUpdatesText.setVisibility(View.GONE);
        } else if (message.getCustomClientData() instanceof RoomNameChangeClientData) {

            roomUpdatesIcon.setImageDrawable(ContextCompat.
                    getDrawable(context, R.drawable.ic_room_updates));
            roomUpdatesText.setText(((RoomNameChangeClientData) message.
                    getCustomClientData()).getOldName());
            roomUpdatesText.setVisibility(View.VISIBLE);
            applyDefaultIconOnUpdateMessage();
        } else if (message.getCustomClientData() instanceof RoomIconChangeClientData) {

            String path = ((RoomIconChangeClientData) message.getCustomClientData()).getImagePath();
            setImageOnRoomUpdateMessage(path);

            roomUpdatesText.setVisibility(View.GONE);
        } else if (message.getCustomClientData() instanceof RoomNameAndIconChangeClientData) {

            roomUpdatesText.setText(((RoomNameAndIconChangeClientData)
                    message.getCustomClientData()).getOldName());

            String path = ((RoomNameAndIconChangeClientData) message.
                    getCustomClientData()).getPath();
            setImageOnRoomUpdateMessage(path);

            roomUpdatesText.setVisibility(View.VISIBLE);
        } else if (message.getCustomClientData() instanceof KeyGenerateStartClientData) {
            roomUpdatesIcon.setImageDrawable(ContextCompat.
                    getDrawable(context, R.drawable.ic_encryption));
            roomUpdatesText.setVisibility(View.GONE);
            applyDefaultIconOnUpdateMessage();
        } else if (message.getCustomClientData() instanceof KeyGeneratedClientData) {

            if (Objects.equals(((KeyGeneratedClientData) message.
                    getCustomClientData()).getResult(), KeyGeneratedClientData.RESULT_CANCELLED)) {
                roomUpdatesIcon.setImageDrawable(ContextCompat.
                        getDrawable(context, R.drawable.ic_encryption_disabled));
            } else {
                roomUpdatesIcon.setImageDrawable(ContextCompat.
                        getDrawable(context, R.drawable.ic_encryption));
            }
            roomUpdatesText.setVisibility(View.GONE);
            applyDefaultIconOnUpdateMessage();
        } else if (message.getCustomClientData() instanceof UnencryptedMessageClientData) {
            roomUpdatesText.setVisibility(View.GONE);

            roomUpdatesIcon.setImageDrawable(ContextCompat.
                    getDrawable(context, R.drawable.ic_encrypted));
            applyDefaultIconOnUpdateMessage();

            isRoomUpdate = false;
        }

        roomUpdatesMainText.setText(message.getCustomClientData().getText(context));

        if (!isRoomUpdate) return;

        nicknameText.setVisibility(View.GONE);

        String imagePath = getMessageAdapterContract().getRoom().getImagePath();
        if (imagePath != null) {
            int imageSize = DeviceUtils.dpToPx(40, itemView.getContext());
            Picasso.get().load(new File(imagePath))
                    .resize(imageSize, imageSize).into(profilePicture);
        } else {
            profilePicture.setImageDrawable(itemView.getContext().getDrawable(R.drawable.placeholder));
        }

    }

    @Override
    public void postInflateReplyViews() {

    }

    public void updateMessageReading(Message message) {

    }

    private void setImageOnRoomUpdateMessage(String path) {
        if (path == null) {
            roomUpdatesIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.placeholder));
            roomUpdatesIcon.setColorFilter(Theme.getColor(itemView.getContext(), R.color.accent));
            roomUpdatesIcon.getBackground().setColorFilter(
                    Theme.getColor(itemView.getContext(), R.color.dark), PorterDuff.Mode.SRC_IN);
        } else {
            Picasso.get().load(new File(path)).into(roomUpdatesIcon);
            roomUpdatesIcon.setImageTintList(null);
            roomUpdatesIcon.setColorFilter(null);
        }
        roomUpdatesIcon.setPadding(0, 0, 0, 0);
    }

    private void applyDefaultIconOnUpdateMessage() {
        roomUpdatesIcon.setImageTintList(ColorStateList.valueOf(
                Theme.getColor(itemView.getContext(), R.color.client_data_icon_color)));
        roomUpdatesIcon.getBackground().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);
        int padding = DeviceUtils.dpToPx(6, itemView.getContext());
        roomUpdatesIcon.setPadding(padding, padding, padding, padding);
    }
}
