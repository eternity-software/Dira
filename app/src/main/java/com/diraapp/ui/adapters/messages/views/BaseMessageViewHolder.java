package com.diraapp.ui.adapters.messages.views;

import static com.diraapp.ui.adapters.messages.legacy.LegacyRoomMessagesAdapter.VIEW_TYPE_ROOM_MESSAGE;
import static com.diraapp.ui.adapters.messages.legacy.LegacyRoomMessagesAdapter.VIEW_TYPE_ROOM_MESSAGE_BUBBLE;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.exceptions.AlreadyInitializedException;
import com.diraapp.ui.adapters.messages.views.viewholders.factories.MessageHolderType;
import com.diraapp.ui.components.MessageReplyComponent;
import com.diraapp.ui.components.dynamic.DynamicTextView;
import com.diraapp.utils.Numbers;

import java.util.Calendar;
import java.util.Date;

/**
 * ViewHolder for almost every message type
 */
public abstract class BaseMessageViewHolder extends RecyclerView.ViewHolder implements InflaterListener {

    protected boolean isInitialized = false, isSelfMessage, isOuterContainer = false;
    private TextView messageText, emojiText, nicknameText, timeText, dateText;

    private View profilePictureContainer;

    private ImageView profilePicture;

    protected LinearLayout outerContainer, messageContainer, postInflatedViewsContainer;
    protected View messageBackground, rootView;

    private LinearLayout replyContainer;
    private CardView replyImageCard;
    private ImageView replyImage;
    private DynamicTextView replyText, replyAuthor;

    public BaseMessageViewHolder(@NonNull ViewGroup itemView) {
        super(itemView);

        isSelfMessage = getItemViewType() < VIEW_TYPE_ROOM_MESSAGE;

        inflatePlaceholderView();
    }

    @Override
    public void onViewInflated(View rootView) {
        if(isInitialized) throw new AlreadyInitializedException(MessageHolderType.values()[getItemViewType()]);

        this.rootView = rootView;
        messageText = find(R.id.message_text);
        emojiText = find(R.id.emoji_view);
        nicknameText = find(R.id.nickname_text);
        timeText = find(R.id.time_view);
        dateText = find(R.id.date_view);
        profilePicture = find(R.id.profile_picture);
        profilePictureContainer = find(R.id.picture_container);
        outerContainer = find(R.id.bubble_view_container);
        messageContainer = find(R.id.message_container);
        messageBackground = find(R.id.message_back);
        postInflatedViewsContainer = find(R.id.views_container);

        if(getItemViewType() != MessageHolderType.ROOM_UPDATES.ordinal())
            postInflateReplyViews();

        if(isOuterContainer)
        {
            messageContainer.setVisibility(View.GONE);
            postInflatedViewsContainer.setVisibility(View.GONE);
        }
        else
        {
            messageContainer.setVisibility(View.VISIBLE);
            postInflatedViewsContainer.setVisibility(View.VISIBLE);
        }

        postInflate();

        isInitialized = true;
    }


    /**
     * Inflate a placeholder that will be displayed until the main inflating is completed
     */
    public void inflatePlaceholderView()
    {

    }

    /**
     * Inflate views after the completion of the main inflating
     */
    protected void postInflate() {
        if(isInitialized) throw new AlreadyInitializedException(MessageHolderType.values()[getItemViewType()]);
    }

    /**
     * Fill views with message content
     * @param message
     * @param previousMessage
     */
    public void bindMessage(Message message, Message previousMessage) {
        fillDateAndTime(message, previousMessage);
    }

    private <T extends View> T find(int id)
    {
        return rootView.findViewById(id);
    }

    private void fillDateAndTime(Message message, Message previousMessage) {

        boolean isSameDay = false;
        boolean isSameYear = false;

        if (previousMessage != null) {
            Date date = new Date(message.getTime());
            Date datePrev = new Date(previousMessage.getTime());

            Calendar calendar = Calendar.getInstance();
            Calendar calendarPrev = Calendar.getInstance();

            calendar.setTime(date);
            calendarPrev.setTime(datePrev);

            if (calendar.get(Calendar.DAY_OF_YEAR) == calendarPrev.get(Calendar.DAY_OF_YEAR)) {
                isSameDay = true;
            }
            if (calendar.get(Calendar.YEAR) == calendarPrev.get(Calendar.YEAR)) {
                isSameYear = true;
            }
        }

        if (!isSameDay || !isSameYear) {
            String dateString = Numbers.getDateFromTimestamp(message.getTime(), !isSameYear);
            dateText.setVisibility(View.VISIBLE);
            dateText.setText(dateString);
        } else {
            dateText.setVisibility(View.GONE);
        }
    }

    protected void postInflateReplyViews() throws AlreadyInitializedException {
        if(isInitialized) throw new AlreadyInitializedException(MessageHolderType.values()[getItemViewType()]);
        MessageReplyComponent replyComponent = new MessageReplyComponent(itemView.getContext(),
                VIEW_TYPE_ROOM_MESSAGE_BUBBLE, isSelfMessage);

        if(isOuterContainer)
        {
            outerContainer.addView(replyComponent);
        }
        else
        {
            postInflatedViewsContainer.addView(replyComponent);
        }

        replyImage = itemView.findViewById(R.id.message_reply_image);
        replyImageCard = itemView.findViewById(R.id.message_reply_image_card);
        replyContainer = itemView.findViewById(R.id.message_reply_container);
        replyText = itemView.findViewById(R.id.message_reply_text);
        replyAuthor = itemView.findViewById(R.id.message_reply_author_name);
    }

    public void setOuterContainer(boolean outerContainer) {
        isOuterContainer = outerContainer;
    }

    public void onViewRecycled(){}
    public void onViewDetached(){}
    public void onViewAttached(){}

    public boolean isInitialized() {
        return isInitialized;
    }

    public boolean isSelfMessage() {
        return isSelfMessage;
    }
}
