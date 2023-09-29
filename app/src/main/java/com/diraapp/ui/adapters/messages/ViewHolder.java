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
import com.diraapp.storage.attachments.AttachmentsStorage;
import com.diraapp.storage.attachments.AttachmentsStorageListener;
import com.diraapp.ui.components.BubbleMessageView;
import com.diraapp.ui.components.MultiAttachmentMessageView;
import com.diraapp.ui.components.VoiceMessageView;
import com.diraapp.ui.components.diravideoplayer.DiraVideoPlayer;
import com.diraapp.ui.components.dynamic.DynamicTextView;
import com.diraapp.ui.components.viewswiper.ViewSwiper;
import com.felipecsl.asymmetricgridview.library.widget.AsymmetricGridView;
import com.masoudss.lib.WaveformSeekBar;

public class ViewHolder extends RecyclerView.ViewHolder {

    private boolean isInitialised = false;
    private boolean isSelfMessage;

    // TextMessageViewHolder ?
    TextView messageText;
    TextView emojiText;
    public LinearLayout messageContainer;
    //

    TextView nicknameText;
    TextView timeText;
    TextView dateText;
    ImageView profilePicture;
    CardView pictureContainer;
    LinearLayout loading;
    ProgressBar attachmentProgressbar;
    LinearLayout messageBackground;
    public LinearLayout viewsContainer;
    public LinearLayout bubbleViewContainer;
    // Replies will stay here
    LinearLayout replyContainer;
    CardView replyImageCard;
    ImageView replyImage;
    DynamicTextView replyText;
    DynamicTextView replyAuthor;
    AttachmentsStorageListener attachmentsStorageListener;


    // will be deleted(refactored)
    TextView buttonDownload;
    TextView sizeText;
    LinearLayout sizeContainer;
    TextView attachmentTooLargeText;
    //



    // AttachmentViewHolder
    DiraVideoPlayer videoPlayer;
    ImageView imageView;
    CardView imageContainer;
    //


    // RoomUpdatesViewHolder
    LinearLayout roomUpdatesLayout;
    ImageView roomUpdatesIcon;
    TextView roomUpdatesMainText;
    TextView roomUpdatesText;
    //


    // VoiceViewHolder
    WaveformSeekBar waveformSeekBar;
    LinearLayout voiceLayout;
    ImageView playButton;
    //


    //BubbleViewHolder
    DiraVideoPlayer bubblePlayer;
    BubbleMessageView bubbleContainer;
    //


    // hz
    AsymmetricGridView multiAttachmentsView;
    //

    public ViewHolder(@NonNull View itemView) {
        super(itemView);

        messageText = itemView.findViewById(R.id.message_text);
        nicknameText = itemView.findViewById(R.id.nickname_text);
        timeText = itemView.findViewById(R.id.time_view);
        attachmentProgressbar = itemView.findViewById(R.id.attachment_progressbar);
        emojiText = itemView.findViewById(R.id.emoji_view);
        dateText = itemView.findViewById(R.id.date_view);
        loading = itemView.findViewById(R.id.loading_attachment_layout);
        profilePicture = itemView.findViewById(R.id.profile_picture);
        pictureContainer = itemView.findViewById(R.id.picture_container);
        messageContainer = itemView.findViewById(R.id.message_container);
        messageBackground = itemView.findViewById(R.id.message_background);
        viewsContainer = itemView.findViewById(R.id.views_container);
        bubbleViewContainer = itemView.findViewById(R.id.bubble_view_container);

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

    public void updateViews() {
        roomUpdatesLayout = itemView.findViewById(R.id.room_updates_layout);
        roomUpdatesIcon = itemView.findViewById(R.id.room_updates_icon);
        roomUpdatesMainText = itemView.findViewById(R.id.room_updates_main_text);
        roomUpdatesText = itemView.findViewById(R.id.room_updates_text);


        waveformSeekBar = itemView.findViewById(R.id.waveform_seek_bar);
        playButton = itemView.findViewById(R.id.play_button);
        voiceLayout = itemView.findViewById(VoiceMessageView.VOICE_CONTAINER_ID);


        bubbleContainer = itemView.findViewById(BubbleMessageView.BUBBLE_CONTAINER_ID);
        bubblePlayer = itemView.findViewById(R.id.bubble_player);

        imageView = itemView.findViewById(R.id.image_view);
        videoPlayer = itemView.findViewById(R.id.video_player);
        imageContainer = itemView.findViewById(R.id.image_container);


        attachmentTooLargeText = itemView.findViewById(R.id.attachment_too_large_text);
        sizeText = itemView.findViewById(R.id.size_view);
        buttonDownload = itemView.findViewById(R.id.download_button);
        sizeContainer = itemView.findViewById(R.id.attachment_too_large);


        multiAttachmentsView = itemView.findViewById(MultiAttachmentMessageView.
                MULTI_ATTACHMENT_MESSAGE_VIEW_ID);




        // only this should stay
        // message reply
        replyImage = itemView.findViewById(R.id.message_reply_image);
        replyImageCard = itemView.findViewById(R.id.message_reply_image_card);
        replyContainer = itemView.findViewById(R.id.message_reply_container);
        replyText = itemView.findViewById(R.id.message_reply_text);
        replyAuthor = itemView.findViewById(R.id.message_reply_author_name);
    }
}
