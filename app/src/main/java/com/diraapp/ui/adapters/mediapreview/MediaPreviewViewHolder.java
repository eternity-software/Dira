package com.diraapp.ui.adapters.mediapreview;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.diraapp.R;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.AppStorage;
import com.diraapp.ui.components.TouchImageView;
import com.diraapp.ui.components.VideoPlayer;
import com.diraapp.utils.android.DeviceUtils;

public class MediaPreviewViewHolder extends RecyclerView.ViewHolder {

    private AttachmentMessagePair pair;

    private final ConstraintLayout imageContainer;

    private final CardView cardView;

    private final TouchImageView imageView;

    private final VideoPlayer videoPlayer;

    private final TextView messageText, memberName, timeText, sizeView;

    private final FrameLayout watchButton;

    private final LinearLayout saveButton;

    public MediaPreviewViewHolder(@NonNull View itemView, WatchCallBack watchCallBack) {
        super(itemView);

        imageContainer = itemView.findViewById(R.id.imageContainer);
        cardView = itemView.findViewById(R.id.card_view);
        imageView = itemView.findViewById(R.id.image_view);
        videoPlayer = itemView.findViewById(R.id.video_player);
        messageText = itemView.findViewById(R.id.message_text);
        memberName = itemView.findViewById(R.id.member_name);
        timeText = itemView.findViewById(R.id.time);
        watchButton = itemView.findViewById(R.id.watch);
        saveButton = itemView.findViewById(R.id.save_button);
        sizeView = itemView.findViewById(R.id.size_view);

        watchButton.setOnClickListener((View v) -> watchCallBack.onWatchClicked());
    }

    public void bind(AttachmentMessagePair attachmentMessagePair) {
        pair = attachmentMessagePair;

        Message message = pair.getMessage();
        messageText.setText(message.getText());
        memberName.setText(message.getAuthorNickname());
        timeText.setText(DeviceUtils.getDateFromTimestamp(message.getTime(), false));
        sizeView.setText(AppStorage.getStringSize(pair.getAttachment().getSize()));

        boolean isVideo = pair.getAttachment().getAttachmentType() == AttachmentType.VIDEO;

        if (isVideo) {
            setupVideo(pair.getAttachment().getFileUrl());
        } else {
            // image
        }
    }

    private void setupVideo(String path) {

    }

    public interface WatchCallBack {

        void onWatchClicked();
    }
}
