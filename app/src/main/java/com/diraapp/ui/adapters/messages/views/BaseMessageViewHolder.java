package com.diraapp.ui.adapters.messages.views;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.api.requests.MessageReadRequest;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.Member;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.exceptions.HolderAlreadyInitializedException;
import com.diraapp.exceptions.UnablePerformRequestException;
import com.diraapp.res.Theme;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.viewholders.DelayedMessageBind;
import com.diraapp.ui.adapters.messages.views.viewholders.factories.MessageHolderType;
import com.diraapp.ui.components.MessageReplyComponent;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.Logger;
import com.diraapp.utils.TimeConverter;
import com.diraapp.utils.android.DeviceUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;

/**
 * ViewHolder for almost every message type
 */
public abstract class BaseMessageViewHolder extends RecyclerView.ViewHolder implements InflaterListener {

    private final MessageAdapterContract messageAdapterContract;
    private final ViewHolderManagerContract viewHolderManagerContract;
    protected boolean isInitialized = false, isSelfMessage;
    /**
     * Indicates that message will be without background (ex. BubbleMessage)
     */
    protected boolean isOuterContainer = false;
    protected TextView messageText;
    protected LinearLayout outerContainer, messageContainer, postInflatedViewsContainer, timeContainer;
    protected View messageBackground, rootView;
    protected TextView nicknameText;
    protected ImageView profilePicture;
    protected View profilePictureContainer;
    private FrameLayout readingIndicator;
    private Message currentMessage = null;
    private TextView timeText;
    private TextView dateText;

    private LinearLayout outerReplyContainer;

    private MessageReplyComponent replyComponent;

    /**
     * Delays bind if view has not been initialized
     */
    private DelayedMessageBind delayedMessageBind;

    private ValueAnimator messageBackgroundAnimator;

    private boolean isOnScreen = true;

    public BaseMessageViewHolder(@NonNull ViewGroup itemView, MessageAdapterContract messageAdapterContract,
                                 ViewHolderManagerContract viewHolderManagerContract, boolean isSelfMessage) {
        super(itemView);
        this.messageAdapterContract = messageAdapterContract;
        this.viewHolderManagerContract = viewHolderManagerContract;

        this.isSelfMessage = isSelfMessage;

        inflatePlaceholderView();
    }


    @Override
    public void onViewInflated(View rootView) {
        if (isInitialized)
            throw new HolderAlreadyInitializedException(MessageHolderType.values()[getItemViewType()]);

        this.rootView = rootView;
        messageText = find(R.id.message_text);
        nicknameText = find(R.id.nickname_text);
        timeText = find(R.id.progress_time);
        dateText = find(R.id.date_view);
        profilePicture = find(R.id.profile_picture);
        profilePictureContainer = find(R.id.picture_container);
        outerContainer = find(R.id.bubble_view_container);
        timeContainer = find(R.id.time_container);
        messageContainer = find(R.id.message_container);
        messageBackground = find(R.id.message_background);
        readingIndicator = find(R.id.read_indicator);
        postInflatedViewsContainer = find(R.id.views_container);
        outerReplyContainer = find(R.id.outer_reply_container);

        if (hasReplySupport())
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

        if (delayedMessageBind != null)
            bindMessage(delayedMessageBind.getMessage(), delayedMessageBind.getPreviousMessage());
    }

    public MessageAdapterContract getMessageAdapterContract() {
        return messageAdapterContract;
    }

    public ViewHolderManagerContract getViewHolderManagerContract() {
        return viewHolderManagerContract;
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
            throw new HolderAlreadyInitializedException(MessageHolderType.values()[getItemViewType()]);

    }

    /**
     * Fill views with message content
     *
     * @param message
     * @param previousMessage
     */
    public void bindMessage(@NonNull Message message, @Nullable Message previousMessage) {
        isOnScreen = true;
        currentMessage = message;

        clearBackgroundAnimator();

        fillDateAndTime(message, previousMessage);
        checkReadStatus(message);
        itemView.setClickable(true);
        itemView.setOnClickListener((View v) -> {
            BalloonMessageMenu balloonMessageMenu = new BalloonMessageMenu(itemView.getContext(),
                    messageAdapterContract.getMembers(),
                    messageAdapterContract.getCacheUtils().getString(CacheUtils.ID),
                    messageAdapterContract.getBalloonMessageListener());
            balloonMessageMenu.createBalloon(message, itemView);
        });
        bindUserPicture(message, previousMessage);
        if (hasReplySupport())
            replyComponent.fillMessageReply(message.getRepliedMessage(), messageAdapterContract, currentMessage);

        if (messageAdapterContract.isMessageNeedBlink(message.getId())) {
            blink();
        }
    }

    public boolean hasReplySupport() {
        return getItemViewType() != MessageHolderType.ROOM_UPDATES.ordinal();
    }

    private void setOnClickListener() {

    }

    public void bindUserPicture(@NonNull Message message, @Nullable Message previousMessage) {
        if (!isInitialized) return;
        if (profilePicture != null)
            profilePicture.setImageBitmap(null);
        if (isSelfMessage) {
            updateMessageReading(message, false);
            return;
        }

        if (messageAdapterContract.isSecurePrivateRoom()) {
            profilePictureContainer.setVisibility(View.GONE);
            nicknameText.setVisibility(View.GONE);
            return;
        } else {
            profilePictureContainer.setVisibility(View.VISIBLE);
            nicknameText.setVisibility(View.VISIBLE);
        }

        boolean showProfilePicture = isProfilePictureRequired(message, previousMessage);

        if (!showProfilePicture) {
            profilePictureContainer.setVisibility(View.INVISIBLE);
            profilePicture.setVisibility(View.GONE);
            nicknameText.setVisibility(View.GONE);
            return;
        }

        if (!message.hasAuthor()) return;

        HashMap<String, Member> members = messageAdapterContract.getMembers();
        Member member = members.get(message.getAuthorId());
        String nickName = null;

        if (member != null) {
            nickName = member.getNickname();
            if (member.getImagePath() != null) {

                int imageSize = DeviceUtils.dpToPx(40, itemView.getContext());
                Picasso.get().load(new File(member.getImagePath()))
                        .resize(imageSize, imageSize)
                        .into(profilePicture);
            } else {
                profilePicture.setImageResource(R.drawable.placeholder);
            }
        } else {
            profilePicture.setImageDrawable(itemView.getContext().
                    getDrawable(R.drawable.placeholder));

        }

        profilePicture.setVisibility(View.VISIBLE);
        profilePictureContainer.setVisibility(View.VISIBLE);

        if (nickName == null) nickName = message.getAuthorNickname();
        nicknameText.setText(nickName);
        nicknameText.setVisibility(View.VISIBLE);
    }

    private boolean isProfilePictureRequired(@NonNull Message message, @Nullable Message previousMessage) {
        if (previousMessage != null)
            if (previousMessage.hasAuthor() && message.hasAuthor()) {
                return !previousMessage.getAuthorId().equals(message.getAuthorId()) ||
                        !message.isSameDay(previousMessage) ||
                        !message.isSameYear(previousMessage);
            }
        return true;
    }

    protected <T extends View> T find(int id) {
        return rootView.findViewById(id);
    }

    public void fillDateAndTime(Message message, Message previousMessage) {
        if (!isInitialized) return;

        boolean isSameDay = false;
        boolean isSameYear = false;

        if (previousMessage != null) {
            isSameDay = message.isSameDay(previousMessage);
            isSameYear = message.isSameYear(previousMessage);
        }

        if (!isSameDay || !isSameYear) {
            String dateString = DeviceUtils.getDateFromTimestamp(message.getTime(), !isSameYear);
            dateText.setVisibility(View.VISIBLE);
            dateText.setText(dateString);
        } else {
            dateText.setVisibility(View.GONE);
        }

        timeText.setText(TimeConverter.getTimeFromTimestamp(message.getTime()));
    }

    protected void postInflateReplyViews() throws HolderAlreadyInitializedException {
        if (isInitialized)
            throw new HolderAlreadyInitializedException(MessageHolderType.values()[getItemViewType()]);
        replyComponent = new MessageReplyComponent(itemView.getContext(),
                getItemViewType(), isSelfMessage);

        if (isOuterContainer) {
            outerReplyContainer.addView(replyComponent);
        } else {
            postInflatedViewsContainer.addView(replyComponent);
        }


    }

    public void updateMessageReading(Message message, boolean isAnimated) {
        if (!isInitialized) return;
        if (!message.hasAuthor()) return;
        if (!isSelfMessage) return;

        int endTimeMargin = DeviceUtils.dpToPx(8, this.itemView.getContext());
        if (message.getMessageReadingList() == null) return;

        // Just set default margins (no readings) (indicator is shown)
        if (message.getMessageReadingList().size() == 0) {
            readingIndicator.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.gravity = Gravity.BOTTOM;

            params.setMargins(0, 0, endTimeMargin, 0);

            timeContainer.setLayoutParams(params);

        } else {
            int dp10 = DeviceUtils.dpToPx(10, itemView.getContext());

            // Process animation to hide indicator
            if (isAnimated) {
                ValueAnimator animator = ValueAnimator.ofInt(dp10, 0);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                        int value = (int) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams params = readingIndicator.getLayoutParams();
                        params.width = value;
                        readingIndicator.setLayoutParams(params);

                        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        timeParams.gravity = Gravity.BOTTOM;

                        timeParams.setMargins(dp10 - value, 0, endTimeMargin, 0);

                        timeContainer.setLayoutParams(timeParams);
                    }
                });

                animator.setInterpolator(new DecelerateInterpolator(2f));
                animator.setDuration(150);
                animator.start();

                // Hide indicator without animation
            } else {
                readingIndicator.setVisibility(View.GONE);

                LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                timeParams.gravity = Gravity.BOTTOM;

                timeParams.setMargins(dp10, 0, endTimeMargin, 0);

                timeContainer.setLayoutParams(timeParams);
            }

        }
    }

    public void blink() {
        int colorFrom = Color.TRANSPARENT;

        int colorTo = Theme.getColor(itemView.getContext(),
                R.color.unread_message_background);

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(300); // milliseconds
        colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
        colorAnimation.setRepeatCount(5);
        colorAnimation.setInterpolator(new AccelerateInterpolator(0.5f));
        colorAnimation.addUpdateListener((animator) -> {
            messageBackground.getBackground().setColorFilter((Integer)
                    animator.getAnimatedValue(), PorterDuff.Mode.SRC_IN);
        });

        if (messageBackgroundAnimator != null) {
            if (!messageBackgroundAnimator.isRunning()) {
                messageBackgroundAnimator = colorAnimation;
                Logger.logDebug("blink", "blink anim");
                colorAnimation.start();
            }
        } else {
            messageBackgroundAnimator = colorAnimation;
            Logger.logDebug("blink", "blink anim");
            colorAnimation.start();
        }
    }

    private void checkReadStatus(Message message) {
        String selfId = getSelfId();
        if (!message.isReadable()) return;
        if (message.getAuthorId().equals(selfId)) return;

        MessageReadRequest request = new MessageReadRequest(selfId, System.currentTimeMillis(),
                message.getId(), message.getRoomSecret());
        try {
            UpdateProcessor.getInstance().sendRequest(request, messageAdapterContract.getRoom().getServerAddress());
            message.setRead(true);
        } catch (UnablePerformRequestException e) {
            e.printStackTrace();
        }
    }

    public void updateListeningIndicator(Attachment attachment) {

    }

    protected String getSelfId() {
        return messageAdapterContract.getCacheUtils().getString(CacheUtils.ID);
    }

    public void setOuterContainer(boolean outerContainer) {
        isOuterContainer = outerContainer;
    }

    public void onViewRecycled() {
        currentMessage = null;
        isOnScreen = false;
    }

    public void onViewDetached() {
        if (!isInitialized) return;
        if (!isOnScreen) return;
        isOnScreen = false;
    }

    public void onViewAttached() {
        if (currentMessage != null) {
            if (messageAdapterContract.isMessageNeedBlink(currentMessage.getId())) {
                blink();
            }
        }
        if (!isInitialized) return;
        if (isOnScreen) return;
        isOnScreen = true;
    }

    public Message getCurrentMessage() {
        return currentMessage;
    }

    public void setCurrentMessage(Message currentMessage) {
        this.currentMessage = currentMessage;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public boolean isSelfMessage() {
        return isSelfMessage;
    }

    public void setDelayedMessageBind(DelayedMessageBind delayedMessageBind) {
        this.delayedMessageBind = delayedMessageBind;
    }

    public boolean canBeSwiped() {
        return true;
    }

    private void clearBackgroundAnimator() {
        if (messageBackgroundAnimator == null) return;
        if (!messageBackgroundAnimator.isRunning()) return;

        messageBackgroundAnimator.end();
    }

    public interface ViewHolderNotification {

        void notifyViewHolder(BaseMessageViewHolder holder);
    }
}
