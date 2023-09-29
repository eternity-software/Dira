package com.diraapp.ui.adapters.messages.views;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;

/**
 * ViewHolder for almost every message type
 */
public class BaseMessageViewHolder extends RecyclerView.ViewHolder implements InflaterListener {

    private TextView messageText, emojiText, nicknameText, timeText, dateText;

    private View profilePictureContainer;

    private ImageView profilePicture;

    protected LinearLayout bubbleContainer, messageContainer, postInflatedViewsContainer;
    protected View messageBackground, rootView;

    public BaseMessageViewHolder(@NonNull View itemView) {
        super(itemView);
    }


    @Override
    public void onViewInflated(View rootView) {
        this.rootView = rootView;
        messageText = find(R.id.message_text);
        emojiText = find(R.id.emoji_view);
        nicknameText = find(R.id.nickname_text);
        timeText = find(R.id.time_view);
        dateText = find(R.id.date_view);
        profilePicture = find(R.id.profile_picture);
        profilePictureContainer = find(R.id.picture_container);
        bubbleContainer = find(R.id.bubble_view_container);
        messageContainer = find(R.id.message_container);
        messageBackground = find(R.id.message_back);
        postInflatedViewsContainer = find(R.id.views_container);
    }

    public void bindMessage(Message message)
    {

    }

    private <T extends View> T find(int id)
    {
        return rootView.findViewById(id);
    }
}
