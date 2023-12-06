package com.diraapp.ui.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.entities.rooms.Room;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.rooms.RoomType;
import com.diraapp.storage.AppStorage;
import com.diraapp.ui.activities.room.RoomActivity;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.StringFormatter;
import com.diraapp.utils.TimeConverter;

import java.util.ArrayList;
import java.util.List;

public class RoomSelectorAdapter extends RecyclerView.Adapter<RoomSelectorAdapter.ViewHolder> {


    private final LayoutInflater layoutInflater;
    private final Activity context;
    private List<Room> roomList = new ArrayList<>();

    private final SelectorAdapterContract contract;


    public RoomSelectorAdapter(Activity context, SelectorAdapterContract contract) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.contract = contract;
    }

    public void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(layoutInflater.inflate(R.layout.room_element, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Room room = roomList.get(position);

        holder.onBind(room);
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView roomName;
        TextView messageText;
        TextView timeText;
        TextView accentText;
        ImageView roomPicture;
        LinearLayout roomContainer;
        View rootView;

        TextView unreadMessagesCount;

        ImageView publicRoomIndicator;


        public ViewHolder(@NonNull View itemView) {
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
                roomContainer.setBackground(context.getResources().getDrawable(R.drawable.room_header_clickable));
                unreadMessagesCount.setVisibility(View.GONE);
            } else {
                roomContainer.setBackground(context.getResources().getDrawable(R.drawable.room_unread_background));
                unreadMessagesCount.setVisibility(View.VISIBLE);
                unreadMessagesCount.setText(String.valueOf(room.getUnreadMessagesIds().size()));
            }

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, RoomActivity.class);

                    contract.onRoomOpen(room.getSecretName());
                    RoomActivity.putRoomExtrasInIntent(intent, room.getSecretName(), room.getName());
                    context.startActivity(intent);
                }
            });

            try {
                if (message != null) {
                    boolean hasMessageText = message.hasText();

                    if (message.isUserMessage()) {
                        String authorPrefix = message.getAuthorNickname();
                        if (authorPrefix.length() > 12) {
                            authorPrefix = authorPrefix.substring(0, 11) + "..";
                        }

                        CacheUtils cacheUtils = new CacheUtils(context);
                        boolean isSelfMessage = cacheUtils.getString(CacheUtils.ID).
                                equals(message.getAuthorId());

                        if (isSelfMessage) {
                            authorPrefix = context.getString(R.string.you);
                            if (message.getMessageReadingList() != null) {
                                if (message.getMessageReadingList().size() == 0) {
                                    authorPrefix = authorPrefix + context.
                                            getString(R.string.room_last_not_read);
                                }
                            }
                        }

                        if (message.hasCustomClientData()) {
                            messageText.setText(StringFormatter.EMPTY_STRING);
                            accentText.setText(authorPrefix + ": " + message.
                                    getCustomClientData().getText(context));
                        } else if (hasMessageText) {
                            messageText.setText(message.getText());
                            accentText.setText(authorPrefix + ": ");
                        } else if (message.getAttachments().size() > 0) {
                            String attachmentText = message.getAttachmentText(context);

                            messageText.setText(StringFormatter.EMPTY_STRING);
                            accentText.setText(authorPrefix + ": " + attachmentText);
                        }

                        timeText.setText(TimeConverter.getTimeFromTimestamp(message.getTime()));

                    } else {
                        accentText.setText(message.getCustomClientData().getText(context));
                        hasMessageText = false;
                    }

                    if (!hasMessageText) {
                        messageText.setText("");
                    }
                }

                if (room.getImagePath() != null) {
                    roomPicture.setImageBitmap(AppStorage.getBitmapFromPath(room.getImagePath()));
                } else {
                    roomPicture.setImageDrawable(
                            context.getResources().getDrawable(R.drawable.placeholder));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface SelectorAdapterContract {

        void onRoomOpen(String roomSecret);
    }
}
