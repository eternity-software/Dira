package com.diraapp.ui.adapters.selector;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.db.entities.rooms.RoomType;
import com.diraapp.storage.AppStorage;
import com.diraapp.ui.activities.room.RoomActivity;
import com.diraapp.ui.components.dynamic.ThemeImageView;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.Logger;
import com.diraapp.utils.StringFormatter;
import com.diraapp.utils.TimeConverter;

public class SelectorViewHolder extends RecyclerView.ViewHolder {

    private final CacheUtils cacheUtils;

    private final SelectorAdapterContract contract;

    TextView roomName;
    TextView messageText;
    TextView timeText;
    TextView accentText;
    ImageView roomPicture;
    LinearLayout roomContainer;
    View rootView;

    TextView unreadMessagesCount;

    ImageView publicRoomIndicator;

    ThemeImageView readingIndicator;


    public SelectorViewHolder(@NonNull View itemView, SelectorAdapterContract contract) {
        super(itemView);

        rootView = itemView;
        roomName = itemView.findViewById(R.id.room_name);
        roomPicture = itemView.findViewById(R.id.room_picture);
        messageText = itemView.findViewById(R.id.message_text);
        roomContainer = itemView.findViewById(R.id.room_container);
        timeText = itemView.findViewById(R.id.time_text);
        accentText = itemView.findViewById(R.id.author_text);
        unreadMessagesCount = itemView.findViewById(R.id.unread_messages_count);
        publicRoomIndicator = itemView.findViewById(R.id.public_room_indicator);
        readingIndicator = itemView.findViewById(R.id.read_indicator);

        cacheUtils = new CacheUtils(itemView.getContext());
        this.contract = contract;
    }

    public void onBind(Room room) {
        if (room == null) return;
        roomName.setText(room.getName());

        Message message = room.getMessage();

        if (room.getRoomType() == RoomType.PUBLIC) {
            publicRoomIndicator.setVisibility(View.VISIBLE);
        } else {
            publicRoomIndicator.setVisibility(View.GONE);
        }

        if (room.getUnreadMessagesIds().size() == 0) {
            roomContainer.setBackground(itemView.getContext().getResources().getDrawable(R.drawable.room_header_clickable));
            unreadMessagesCount.setVisibility(View.GONE);
        } else {
            roomContainer.setBackground(itemView.getContext().getResources().getDrawable(R.drawable.room_unread_background));
            unreadMessagesCount.setVisibility(View.VISIBLE);
            unreadMessagesCount.setText(String.valueOf(room.getUnreadMessagesIds().size()));
        }

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(itemView.getContext(), RoomActivity.class);

                contract.onRoomOpen(room.getSecretName());
                RoomActivity.putRoomExtrasInIntent(intent, room.getSecretName(), room.getName());
                itemView.getContext().startActivity(intent);
            }
        });

        try {
            if (message != null) {
                boolean hasMessageText = message.hasText();

                if (message.isUserMessage()) {
                    String authorPrefix = message.getShortAuthorNickname();
                    if (authorPrefix.length() > 12) {
                        authorPrefix = authorPrefix.substring(0, 11) + "..";
                    }

                    boolean isSelfMessage = cacheUtils.getString(CacheUtils.ID).
                            equals(message.getAuthorId());

                    if (isSelfMessage) {
                        authorPrefix = itemView.getContext().getString(R.string.you);
                    }

                    if (message.hasCustomClientData()) {
                        messageText.setText(StringFormatter.EMPTY_STRING);
                        accentText.setText(authorPrefix + ": " + message.
                                getCustomClientData().getText(itemView.getContext()));
                    } else if (hasMessageText) {
                        messageText.setText(message.getText());
                        accentText.setText(authorPrefix + ": ");
                    } else if (message.getAttachments().size() > 0) {
                        String attachmentText = message.getAttachmentText(itemView.getContext());

                        messageText.setText(StringFormatter.EMPTY_STRING);
                        accentText.setText(authorPrefix + ": " + attachmentText);
                    }

                    timeText.setText(TimeConverter.getTimeFromTimestamp(message.getTime()));

                } else {
                    accentText.setText(message.getCustomClientData().getText(itemView.getContext()));
                    hasMessageText = false;
                }

                if (!hasMessageText) {
                    messageText.setText("");
                }
            }

            bindReading(message);

            if (room.getImagePath() != null) {
                roomPicture.setImageBitmap(AppStorage.getBitmapFromPath(room.getImagePath()));
            } else {
                roomPicture.setImageDrawable(
                        itemView.getContext().getResources().getDrawable(R.drawable.placeholder));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void bindReading(Message message) {
        if (message == null) {
            readingIndicator.setVisibility(View.GONE);
            return;
        }

        boolean isSelfMessage = cacheUtils.getString(CacheUtils.ID).
                equals(message.getAuthorId());
        if (isSelfMessage &&
                message.getMessageReadingList().size() == 0) {
            readingIndicator.setVisibility(View.VISIBLE);
        } else {
            readingIndicator.setVisibility(View.GONE);
        }
    }
}
