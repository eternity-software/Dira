package com.diraapp.ui.adapters.messages.views;

import static com.diraapp.ui.adapters.messages.legacy.LegacyRoomMessagesAdapter.VIEW_TYPE_ROOM_MESSAGE_BUBBLE;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.requests.MessageReadRequest;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.exceptions.AlreadyInitializedException;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.res.Theme;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.viewholders.factories.MessageHolderType;
import com.diraapp.ui.components.MessageReplyComponent;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.Numbers;
import com.diraapp.utils.TimeConverter;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;

/**
 * ViewHolder for almost every message type
 */
public abstract class BaseMessageViewHolder extends RecyclerView.ViewHolder implements InflaterListener {

    private final MessageAdapterContract messageAdapterContract;
    protected boolean isInitialized = false, isSelfMessage;
    /**
     * Indicates that message will be without background (ex. BubbleMessage)
     */
    protected boolean isOuterContainer = false;
    protected TextView messageText;
    protected LinearLayout outerContainer, messageContainer, postInflatedViewsContainer;
    protected View messageBackground, rootView;
    private TextView nicknameText, timeText, dateText;
    private View profilePictureContainer;
    private ImageView profilePicture;
    private MessageReplyComponent replyComponent;


    public BaseMessageViewHolder(@NonNull ViewGroup itemView, MessageAdapterContract messageAdapterContract,
                                 boolean isSelfMessage) {
        super(itemView);
        this.messageAdapterContract = messageAdapterContract;

        this.isSelfMessage = isSelfMessage;

        inflatePlaceholderView();
    }


    @Override
    public void onViewInflated(View rootView) {
        if (isInitialized)
            throw new AlreadyInitializedException(MessageHolderType.values()[getItemViewType()]);

        this.rootView = rootView;
        messageText = find(R.id.message_text);
        nicknameText = find(R.id.nickname_text);
        timeText = find(R.id.time_view);
        dateText = find(R.id.date_view);
        profilePicture = find(R.id.profile_picture);
        profilePictureContainer = find(R.id.picture_container);
        outerContainer = find(R.id.bubble_view_container);
        messageContainer = find(R.id.message_container);
        messageBackground = find(R.id.message_back);
        postInflatedViewsContainer = find(R.id.views_container);

        if (getItemViewType() != MessageHolderType.ROOM_UPDATES.ordinal())
            postInflateReplyViews();

        if (isOuterContainer) {
            messageContainer.setVisibility(View.GONE);
            postInflatedViewsContainer.setVisibility(View.GONE);
        } else {
            messageContainer.setVisibility(View.VISIBLE);
            postInflatedViewsContainer.setVisibility(View.VISIBLE);
        }

        postInflate();

        isInitialized = true;
    }


    /**
     * Inflate a placeholder that will be displayed until the main inflating is completed
     */
    public void inflatePlaceholderView() {

    }

    /**
     * Inflate views after the completion of the main inflating
     */
    protected void postInflate() {
        if (isInitialized)
            throw new AlreadyInitializedException(MessageHolderType.values()[getItemViewType()]);
    }

    /**
     * Fill views with message content
     *
     * @param message
     * @param previousMessage
     */
    public void bindMessage(Message message, Message previousMessage) {
        fillDateAndTime(message, previousMessage);
        checkReadStatus(message);
        itemView.setClickable(true);
        itemView.setOnClickListener((View v) -> {
            BalloonMessageMenu balloonMessageMenu = new BalloonMessageMenu(messageAdapterContract.getContext(),
                    messageAdapterContract.getMembers(),
                    messageAdapterContract.getCacheUtils().getString(CacheUtils.ID));
            balloonMessageMenu.createBalloon(message, itemView);
        });
        bindUserPicture(message, previousMessage);
        replyComponent.fillMessageReply(message.getRepliedMessage(), messageAdapterContract);
    }

    public void bindUserPicture(Message message, Message previousMessage) {
        if (!isSelfMessage) {

            boolean showProfilePicture = isProfilePictureRequired(message, previousMessage);

            if (!showProfilePicture) {
                profilePictureContainer.setVisibility(View.INVISIBLE);
                nicknameText.setVisibility(View.GONE);
            }

            HashMap<String, Member> members = messageAdapterContract.getMembers();
            Member member = members.get(message.getAuthorId());
            if (member != null) {
                nicknameText.setText(member.getNickname());
                if (showProfilePicture) {
                    if (member.getImagePath() != null) {
                        // TODO: custom image loader
                        Picasso.get().load(new File(member.getImagePath())).into(profilePicture);
                    } else {
                        profilePicture.setImageResource(R.drawable.placeholder);
                    }
                    profilePictureContainer.setVisibility(View.VISIBLE);
                    nicknameText.setText(message.getAuthorNickname());
                    nicknameText.setVisibility(View.VISIBLE);
                }
            } else if (showProfilePicture) {
                nicknameText.setText(message.getAuthorNickname());
                nicknameText.setVisibility(View.VISIBLE);
            }
        } else if (message.getMessageReadingList() != null && messageBackground.getBackground() != null) {
            if (message.getMessageReadingList().size() == 0) {
                messageBackground.getBackground().setColorFilter(
                        Theme.getColor(messageAdapterContract.getContext(),
                                R.color.unread_message_background), PorterDuff.Mode.SRC_IN);
            } else {
                messageBackground.getBackground().setColorFilter(
                        Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);
            }
        }
    }

    private boolean isProfilePictureRequired(Message message, Message previousMessage) {
        if (previousMessage != null)
            if (previousMessage.hasAuthor())
                return !previousMessage.getAuthorId().equals(message.getAuthorId()) ||
                        !message.isSameDay(previousMessage) ||
                        !message.isSameYear(previousMessage);
        return true;
    }

    protected <T extends View> T find(int id) {
        return rootView.findViewById(id);
    }

    private void fillDateAndTime(Message message, Message previousMessage) {

        boolean isSameDay = false;
        boolean isSameYear = false;

        if (previousMessage != null) {
            isSameDay = message.isSameDay(previousMessage);
            isSameYear = message.isSameYear(previousMessage);
        }

        if (!isSameDay || !isSameYear) {
            String dateString = Numbers.getDateFromTimestamp(message.getTime(), !isSameYear);
            dateText.setVisibility(View.VISIBLE);
            dateText.setText(dateString);
        } else {
            dateText.setVisibility(View.GONE);
        }

        timeText.setText(TimeConverter.getTimeFromTimestamp(message.getTime()));
    }

    protected void postInflateReplyViews() throws AlreadyInitializedException {
        if (isInitialized)
            throw new AlreadyInitializedException(MessageHolderType.values()[getItemViewType()]);
        replyComponent = new MessageReplyComponent(itemView.getContext(),
                VIEW_TYPE_ROOM_MESSAGE_BUBBLE, isSelfMessage);

        if (isOuterContainer) {
            outerContainer.addView(replyComponent);
        } else {
            postInflatedViewsContainer.addView(replyComponent);
        }


    }

    private void checkReadStatus(Message message) {
        String selfId = getSelfId();
        if (!message.isReadable()) return;
        if (message.getAuthorId().equals(selfId)) return;

        message.setRead(true);

        MessageReadRequest request = new MessageReadRequest(selfId, System.currentTimeMillis(),
                message.getId(), message.getRoomSecret());
        try {
            UpdateProcessor.getInstance().sendRequest(request, messageAdapterContract.getRoom().getServerAddress());
        } catch (UnablePerformRequestException e) {
            e.printStackTrace();
        }
    }

    protected String getSelfId() {
        return messageAdapterContract.getCacheUtils().getString(CacheUtils.ID);
    }

    public void setOuterContainer(boolean outerContainer) {
        isOuterContainer = outerContainer;
    }

    public void onViewRecycled() {
    }

    public void onViewDetached() {
    }

    public void onViewAttached() {
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public boolean isSelfMessage() {
        return isSelfMessage;
    }
}
