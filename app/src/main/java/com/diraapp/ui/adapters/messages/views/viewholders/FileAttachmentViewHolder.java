package com.diraapp.ui.adapters.messages.views.viewholders;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.diraapp.R;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.ui.adapters.messages.MessageAdapterContract;
import com.diraapp.ui.adapters.messages.views.ViewHolderManagerContract;
import com.diraapp.ui.components.FileAttachmentComponent;
import com.diraapp.utils.Logger;
import com.diraapp.utils.StringFormatter;

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
            openFile(file, attachment);
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
        String name = attachment.getRealFileName();

        if (name.equals(StringFormatter.EMPTY_STRING)) {
            name = "attachment";
        }

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

    private void openFile(File file, Attachment attachment) {
        Uri uri = FileProvider.getUriForFile(getMessageAdapterContract().getContext(),
                getMessageAdapterContract().getContext().
                        getApplicationContext().getPackageName() + ".provider", file);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);

        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(attachment.getRealFileName());
        Logger.logDebug("File opening", "fileExtention = " + fileExtension);

        String type;
        if (fileExtension.equals("")) {
            type = "*/*";
        } else {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        }

        intent.setDataAndType(uri, type);
        Logger.logDebug("File opening", "File type - " + type + ", " + attachment.getRealFileName());

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        getMessageAdapterContract().getContext().
                startActivity(intent);
    }
}
