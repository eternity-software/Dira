package com.diraapp.ui.adapters.messages;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.attachments.AttachmentsStorageListener;
import com.diraapp.ui.components.BubbleMessageView;
import com.diraapp.ui.components.MultiAttachmentMessageView;
import com.diraapp.ui.components.VoiceMessageView;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.ui.components.dynamic.DynamicTextView;
import com.diraapp.utils.Numbers;
import com.felipecsl.asymmetricgridview.library.widget.AsymmetricGridView;
import com.masoudss.lib.WaveformSeekBar;

import java.util.Calendar;
import java.util.Date;

public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

    private boolean isInitialised = false;
    private boolean isSelfMessage;
    public LinearLayout messageContainer;
    public LinearLayout viewsContainer;
    public LinearLayout bubbleViewContainer;

    private TextView nicknameText;
    private TextView timeText;
    private TextView dateText;
    private ImageView profilePicture;
    private CardView pictureContainer;
    private LinearLayout loading;
    private ProgressBar attachmentProgressbar;
    private LinearLayout messageBackground;
    private LinearLayout replyContainer;
    private CardView replyImageCard;
    private ImageView replyImage;
    private DynamicTextView replyText;
    private DynamicTextView replyAuthor;
    AttachmentsStorageListener attachmentsStorageListener;

    //AsymmetricGridView multiAttachmentsView;

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);

        isSelfMessage = getItemViewType() < RoomMessagesAdapter.VIEW_TYPE_ROOM_MESSAGE;
    }

    public boolean isInitialised() {
        return isInitialised;
    }

    public void setInitialised(boolean initialised) {
        isInitialised = initialised;
    }

    public boolean isSelfMessage() {
        return isSelfMessage;
    }

    protected void updateReplies() {
        replyImage = itemView.findViewById(R.id.message_reply_image);
        replyImageCard = itemView.findViewById(R.id.message_reply_image_card);
        replyContainer = itemView.findViewById(R.id.message_reply_container);
        replyText = itemView.findViewById(R.id.message_reply_text);
        replyAuthor = itemView.findViewById(R.id.message_reply_author_name);
    }

    public void updateViews() {

    }

    public void onCreate() {
        nicknameText = itemView.findViewById(R.id.nickname_text);
        timeText = itemView.findViewById(R.id.time_view);
        attachmentProgressbar = itemView.findViewById(R.id.attachment_progressbar);
        dateText = itemView.findViewById(R.id.date_view);
        loading = itemView.findViewById(R.id.loading_attachment_layout);
        profilePicture = itemView.findViewById(R.id.profile_picture);
        pictureContainer = itemView.findViewById(R.id.picture_container);
        messageContainer = itemView.findViewById(R.id.message_container);
        messageBackground = itemView.findViewById(R.id.message_background);
        viewsContainer = itemView.findViewById(R.id.views_container);
        bubbleViewContainer = itemView.findViewById(R.id.bubble_view_container);
    }

    public void onBind(Message message, Message previousMessage) {
        fillDateAndTime(message, previousMessage);
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
}
