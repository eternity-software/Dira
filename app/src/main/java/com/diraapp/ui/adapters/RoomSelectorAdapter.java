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
import com.diraapp.db.entities.messages.RoomJoinClientData;
import com.diraapp.db.entities.messages.RoomIconChangeClientData;
import com.diraapp.db.entities.messages.RoomNameChangeClientData;
import com.diraapp.ui.activities.RoomActivity;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.db.entities.Room;
import com.diraapp.storage.AppStorage;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.TimeConverter;

import java.util.ArrayList;
import java.util.List;

public class RoomSelectorAdapter extends RecyclerView.Adapter<RoomSelectorAdapter.ViewHolder> {


    private final LayoutInflater layoutInflater;
    private final Activity context;
    private List<Room> roomList = new ArrayList<>();


    public RoomSelectorAdapter(Activity context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);

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
        holder.roomName.setText(room.getName());

        Message message = room.getMessage();

        if (room.isUpdatedRead()) {
            holder.roomContainer.setBackground(context.getResources().getDrawable(R.drawable.room_header_clickable));
        } else {
            holder.roomContainer.setBackground(context.getResources().getDrawable(R.drawable.room_unread_background));
        }

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomActivity.pendingRoomName = room.getName();
                RoomActivity.pendingRoomSecret = room.getSecretName();
                Intent intent = new Intent(context, RoomActivity.class);
                context.startActivity(intent);
            }
        });

        try {
            if (message != null) {
                if (message.getCustomClientData() == null) {
                    String authorPrefix = message.getAuthorNickname();
                    if (authorPrefix.length() > 12) {
                        authorPrefix = authorPrefix.substring(0, 11) + "..";
                    }
                    CacheUtils cacheUtils = new CacheUtils(context);
                    if (cacheUtils.getString(CacheUtils.ID).equals(message.getAuthorId())) {
                        authorPrefix = context.getString(R.string.you);
                    }
                    holder.messageText.setText(message.getText());
                    holder.authorText.setText(authorPrefix + ": ");
                    holder.timeText.setText(TimeConverter.getTimeFromTimestamp(message.getTime(), context));
                } else if (message.getCustomClientData() instanceof RoomJoinClientData) {

                } else if (message.getCustomClientData() instanceof RoomNameChangeClientData) {

                } else if (message.getCustomClientData() instanceof RoomIconChangeClientData) {

                }
            }
            if (room.getImagePath() != null) {
                holder.roomPicture.setImageBitmap(AppStorage.getBitmapFromPath(room.getImagePath()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return roomList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView roomName;
        TextView messageText;
        TextView timeText;
        TextView authorText;
        ImageView roomPicture;
        LinearLayout roomContainer;
        View rootView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            rootView = itemView;
            roomName = itemView.findViewById(R.id.room_name);
            roomPicture = itemView.findViewById(R.id.room_picture);
            messageText = itemView.findViewById(R.id.message_text);
            roomContainer = itemView.findViewById(R.id.room_container);
            timeText = itemView.findViewById(R.id.time_text);
            authorText = itemView.findViewById(R.id.author_text);
        }
    }
}
