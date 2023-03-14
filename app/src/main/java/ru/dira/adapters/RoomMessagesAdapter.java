package ru.dira.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.dira.R;
import ru.dira.attachments.ImageStorage;
import ru.dira.db.entities.Member;
import ru.dira.db.entities.Message;
import ru.dira.utils.CacheUtils;
import ru.dira.utils.StringFormatter;
import ru.dira.utils.TimeConverter;

public class RoomMessagesAdapter extends RecyclerView.Adapter<RoomMessagesAdapter.ViewHolder> {



    public static final int VIEW_TYPE_SELF_MESSAGE = 1;
    public static final int VIEW_TYPE_ROOM_MESSAGE = 0;


    private Activity context;
    private final LayoutInflater layoutInflater;
    private List<Message> messages = new ArrayList<>();
    private HashMap<String, Member> members = new HashMap<>();


    public RoomMessagesAdapter(Activity context)
    {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void setMembers(HashMap<String, Member> members) {
        this.members = members;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_ROOM_MESSAGE)
        {
            return new ViewHolder(layoutInflater.inflate(R.layout.room_message, parent, false));
        }
        else
        {
            return new ViewHolder(layoutInflater.inflate(R.layout.self_message, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(CacheUtils.getInstance().getString(CacheUtils.ID, context).equals(
                messages.get(position).getAuthorId()
        ))
        {
            return VIEW_TYPE_SELF_MESSAGE;
        }
        return VIEW_TYPE_ROOM_MESSAGE;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Message message = messages.get(position);

        Message previousMessage = null;

        if(position < messages.size() - 1)
        {
            previousMessage = messages.get(position + 1);
        }

        boolean isSelfMessage = CacheUtils.getInstance().getString(CacheUtils.ID, context).equals(
                messages.get(position).getAuthorId());

        if(StringFormatter.isEmoji(message.getText()) && StringFormatter.getEmojiCount(message.getText()) < 3)
        {
            holder.messageContainer.setVisibility(View.GONE);
            holder.emojiText.setVisibility(View.VISIBLE);
            holder.emojiText.setText(message.getText());
        }
        else
        {
            holder.messageContainer.setVisibility(View.VISIBLE);
            holder.emojiText.setVisibility(View.GONE);
            holder.messageText.setText(message.getText());
        }

        if(!isSelfMessage) {
            holder.nicknameText.setText(message.getAuthorNickname());
            holder.pictureContainer.setVisibility(View.VISIBLE);
            holder.nicknameText.setVisibility(View.VISIBLE);
            if (members.containsKey(message.getAuthorId())) {

                 Member member = members.get(message.getAuthorId());
                 holder.nicknameText.setText(member.getNickname());

                 if(member.getImagePath() != null)
                 {
                    holder.profilePicture.setImageBitmap(ImageStorage.getImage(member.getImagePath()));
                 }
                 else
                 {
                     holder.profilePicture.setImageResource(R.drawable.placeholder);
                 }

                 if(previousMessage != null)
                 {
                     if(previousMessage.getAuthorId().equals(message.getAuthorId()))
                     {
                         holder.pictureContainer.setVisibility(View.INVISIBLE);
                         holder.nicknameText.setVisibility(View.GONE);
                     }
                 }

            }
        }


        holder.timeText.setText(TimeConverter.getTimeFromTimestamp(message.getTime(), context));
    }

    public HashMap<String, Member> getMembers() {
        return members;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {

        TextView messageText;
        TextView emojiText;
        TextView nicknameText;
        TextView timeText;
        ImageView profilePicture;
        CardView pictureContainer;
        LinearLayout messageContainer;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            nicknameText = itemView.findViewById(R.id.nickname_text);
            timeText = itemView.findViewById(R.id.time_view);
            emojiText = itemView.findViewById(R.id.emoji_view);
            profilePicture = itemView.findViewById(R.id.profile_picture);
            pictureContainer = itemView.findViewById(R.id.picture_container);
            messageContainer = itemView.findViewById(R.id.message_container);
        }
    }
}
