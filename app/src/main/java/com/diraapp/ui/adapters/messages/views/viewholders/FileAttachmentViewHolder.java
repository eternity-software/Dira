package com.diraapp.ui.adapters.messages.views.viewholders;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.diraapp.R;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.storage.attachments.SaveAttachmentTask;
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
    private ProgressBar progressBar;


    public FileAttachmentViewHolder(@NonNull ViewGroup itemView,
                                    MessageAdapterContract messageAdapterContract,
                                    ViewHolderManagerContract viewHolderManagerContract,
                                    boolean isSelfMessage) {
        super(itemView, messageAdapterContract, viewHolderManagerContract, isSelfMessage);
    }

    @Override
    public void onAttachmentLoaded(Attachment attachment, File file, Message message) {
        if (file == null) {
            progressBar.setVisibility(View.GONE);
            fileIcon.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_download));
            fileIcon.setVisibility(View.VISIBLE);
            return;
        }
        progressBar.setVisibility(View.GONE);
        fileIcon.setVisibility(View.VISIBLE);
        // stop animation

        fileIcon.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_file));
        messageContainer.setOnClickListener((View view) -> {
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
        progressBar = itemView.findViewById(R.id.progress_circular);
        fileIcon = itemView.findViewById(R.id.message_file_attachment_icon);
    }

    @Override
    public void bindMessage(@NonNull Message message, Message previousMessage) {
        super.bindMessage(message, previousMessage);

        // start animation

        Attachment attachment = message.getSingleAttachment();
        String name = attachment.getDisplayFileName();

        if (name.equals(StringFormatter.EMPTY_STRING)) {
            name = itemView.getContext().getString(R.string.unknown);
        }


        String[] s = name.split("\\.");

        String type = s[s.length - 1];
        try {
            int endIndex = name.length() - type.length() - 1;

            name = name.substring(0, endIndex);

            if (name.length() > 18)
                name = name.substring(0, 18);

        } catch (Exception e) {
            type = itemView.getContext().getString(R.string.unknown);
        }

        fileAttachmentName.setText(name);

        fileAttachmentSize.setText(AppStorage.getStringSize(attachment.getSize()) + ", " + type.toUpperCase());
        progressBar.setVisibility(View.GONE);
        fileIcon.setVisibility(View.VISIBLE);
        fileIcon.setImageDrawable(itemView.getContext().getDrawable(R.drawable.ic_download));
        messageContainer.setOnClickListener(v -> {
            SaveAttachmentTask saveAttachmentTask = new SaveAttachmentTask(itemView.getContext(), false,
                    attachment,
                    getMessageAdapterContract().getRoom().getSecretName());

            AttachmentDownloader.saveAttachmentAsync(saveAttachmentTask,
                    getMessageAdapterContract().getRoom().getServerAddress());
            progressBar.setVisibility(View.VISIBLE);
            fileIcon.setVisibility(View.INVISIBLE);
        });
        if (!AttachmentDownloader.isAttachmentSaving(message.getSingleAttachment())) {
            File attachmentFile = AttachmentDownloader.getFileFromAttachment(message.getSingleAttachment(),
                    itemView.getContext(), message.getRoomSecret());
            onAttachmentLoaded(message.getSingleAttachment(),
                    attachmentFile, message);
        } else {
            progressBar.setVisibility(View.VISIBLE);
            fileIcon.setVisibility(View.INVISIBLE);
        }

    }

    private void openFile(File file, Attachment attachment) {
        try {
            Uri uri = FileProvider.getUriForFile(getMessageAdapterContract().getContext(),
                    getMessageAdapterContract().getContext().
                            getApplicationContext().getPackageName() + ".provider", file);

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);

            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(attachment.getDisplayFileName());
            Logger.logDebug("File opening", "fileExtension = " + fileExtension);

            String type;

            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);


            intent.setDataAndType(uri, type);
            Logger.logDebug("File opening", "File type - " + type + ", " + attachment.getDisplayFileName());

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            getMessageAdapterContract().getContext().
                    startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getMessageAdapterContract().getContext(),
                    getMessageAdapterContract().getContext().getString(R.string.file_opening_failed),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
