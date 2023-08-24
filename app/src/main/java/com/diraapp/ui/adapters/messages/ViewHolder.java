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
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.attachments.AttachmentsStorageListener;
import com.diraapp.ui.components.VideoPlayer;
import com.diraapp.utils.CacheUtils;
import com.masoudss.lib.WaveformSeekBar;

public class ViewHolder extends RecyclerView.ViewHolder {

    TextView messageText;
    TextView emojiText;
    TextView nicknameText;
    TextView timeText;
    TextView buttonDownload;
    TextView sizeText;
    TextView dateText;
    ImageView profilePicture;
    ImageView imageView;
    VideoPlayer videoPlayer;
    CardView pictureContainer;
    LinearLayout messageContainer;
    CardView imageContainer;
    LinearLayout sizeContainer;
    LinearLayout loading;

    TextView attachmentTooLargeText;

    AttachmentsStorageListener attachmentsStorageListener;

    ProgressBar attachmentProgressbar;

    LinearLayout roomUpdatesLayout;

    ImageView roomUpdatesIcon;

    TextView roomUpdatesMainText;

    TextView roomUpdatesText;
    WaveformSeekBar waveformSeekBar;
    LinearLayout voiceLayout;
    ImageView playButton;
    VideoPlayer bubblePlayer;
    CardView bubbleContainer;

    LinearLayout messageBackground;

    LinearLayout viewsContainer;

    LinearLayout bubbleViewContainer;


    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        messageText = itemView.findViewById(R.id.message_text);
        nicknameText = itemView.findViewById(R.id.nickname_text);
        timeText = itemView.findViewById(R.id.time_view);
        attachmentProgressbar = itemView.findViewById(R.id.attachment_progressbar);
//        buttonDownload = itemView.findViewById(R.id.download_button);
//        sizeContainer = itemView.findViewById(R.id.attachment_too_large);
        emojiText = itemView.findViewById(R.id.emoji_view);
//        sizeText = itemView.findViewById(R.id.size_view);
        dateText = itemView.findViewById(R.id.date_view);
        loading = itemView.findViewById(R.id.loading_attachment_layout);
//        imageView = itemView.findViewById(R.id.image_view);
//        videoPlayer = itemView.findViewById(R.id.video_player);
        profilePicture = itemView.findViewById(R.id.profile_picture);
        pictureContainer = itemView.findViewById(R.id.picture_container);
        messageContainer = itemView.findViewById(R.id.message_container);
//        attachmentTooLargeText = itemView.findViewById(R.id.attachment_too_large_text);
//        attachmentProgressbar = itemView.findViewById(R.id.attachment_progressbar);
//        roomUpdatesLayout = itemView.findViewById(R.id.room_updates_layout);
//        roomUpdatesIcon = itemView.findViewById(R.id.room_updates_icon);
//        roomUpdatesMainText = itemView.findViewById(R.id.room_updates_main_text);
//        roomUpdatesText = itemView.findViewById(R.id.room_updates_text);
//        waveformSeekBar = itemView.findViewById(R.id.waveform_seek_bar);
//        playButton = itemView.findViewById(R.id.play_button);
//        voiceLayout = itemView.findViewById(R.id.voice_layout);
//        bubbleContainer = itemView.findViewById(R.id.bubble_container);
//        bubblePlayer = itemView.findViewById(R.id.bubble_player);
        messageBackground = itemView.findViewById(R.id.message_background);
        viewsContainer = itemView.findViewById(R.id.views_container);
        bubbleViewContainer = itemView.findViewById(R.id.bubble_view_container);
    }

    public void updateViews() {
        attachmentTooLargeText = itemView.findViewById(R.id.attachment_too_large_text);
        roomUpdatesLayout = itemView.findViewById(R.id.room_updates_layout);
        roomUpdatesIcon = itemView.findViewById(R.id.room_updates_icon);
        roomUpdatesMainText = itemView.findViewById(R.id.room_updates_main_text);
        roomUpdatesText = itemView.findViewById(R.id.room_updates_text);
        waveformSeekBar = itemView.findViewById(R.id.waveform_seek_bar);
        playButton = itemView.findViewById(R.id.play_button);
        voiceLayout = itemView.findViewById(R.id.voice_layout);
        bubbleContainer = itemView.findViewById(R.id.bubble_container);
        bubblePlayer = itemView.findViewById(R.id.bubble_player);

        imageView = itemView.findViewById(R.id.image_view);
        videoPlayer = itemView.findViewById(R.id.video_player);

        sizeText = itemView.findViewById(R.id.size_view);

        buttonDownload = itemView.findViewById(R.id.download_button);
        sizeContainer = itemView.findViewById(R.id.attachment_too_large);

        imageContainer = itemView.findViewById(R.id.image_container);
    }
}
