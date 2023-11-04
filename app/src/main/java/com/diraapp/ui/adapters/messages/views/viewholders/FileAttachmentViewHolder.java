package com.diraapp.ui.adapters.messages.views.viewholders;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.diraapp.R;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;
import com.diraapp.ui.components.FileAttachmentComponent;

import java.io.File;

public class FileAttachmentViewHolder extends TextMessageViewHolder {

    private TextView fileAttachmentName;
    private TextView fileAttachmentSize;

    private ImageView fileIcon;

    public FileAttachmentViewHolder(@NonNull ViewGroup itemView,
                                    MessageAdapterContract messageAdapterContract,
                                    ViewHolderManagerContract viewHolderManagerContract,
                                    boolean isSelfMessage) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, isSelfMessage);
    }

    @Override
    public void onAttachmentLoaded(Attachment attachment, File file, Message message) {
        if (file == null) return;

        // stop animation

        fileIcon.setOnClickListener((View view) -> {
            openFile(file);
        });

    }

    @Override
    protected void postInflate() {
        super.postInflate();
        View view = new FileAttachmentComponent(itemView.getContext(), isSelfMessage);
        messageContainer.setVisibility(View.VISIBLE);
        postInflatedViewsContainer.addView(view);

        fileAttachmentName = itemView.findViewById(R.id.message_file_attachment_name);
        fileAttachmentSize = itemView.findViewById(R.id.message_file_attachment_size);
        fileIcon = itemView.findViewById(R.id.message_file_attachment_icon);
    }

    @Override
    public void bindMessage(@NonNull Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);

        // start animation

        Attachment attachment = message.getSingleAttachment();
        String name = attachment.getFileName();

        if (name.length() > 22) {
            String[] s = name.split("\\.");
            String type = s[s.length - 1];

            int endIndex = 22 - type.length();
            if (endIndex < 2) endIndex = 22;
            name = name.substring(0, endIndex) + type;
        }

        fileAttachmentName.setText(name);

        fileAttachmentSize.setText(AppStorage.getStringSize(attachment.getSize()));

        if (!AttachmentDownloader.isAttachmentSaving(message.getSingleAttachment()))
            onAttachmentLoaded(message.getSingleAttachment(),
                    AttachmentDownloader.getFileFromAttachment(message.getSingleAttachment(),
                            itemView.getContext(), message.getRoomSecret()), message);
    }

    private void openFile(File file) {
        Uri fileUri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.setType("*/*");

        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        getMessageAdapterContract().getContext().
                startActivity(Intent.createChooser(intent, "Share Image:"));
    }
}
