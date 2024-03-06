package com.diraapp.ui.adapters.roominfo.documents;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.diraapp.R;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.MessageAttachmentLoader;
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.ui.adapters.roominfo.BaseAttachmentViewHolder;
import com.diraapp.ui.components.dynamic.ThemeLinearLayout;
import com.diraapp.utils.android.DeviceUtils;

import java.io.File;

public class FileAttachmentViewHolder extends BaseAttachmentViewHolder
        implements MessageAttachmentLoader.AttachmentHolder {

    private final TextView mainText;

    private final TextView fileName;

    private final ThemeLinearLayout fileButton;

    private final ImageView watchButton;

    private final ImageView fileIcon;

    private final View progress;
    private final String roomSecret;
    private final String serverAddress;
    private final FileAdapterContract fileAdapterContract;
    private AttachmentMessagePair pair;
    private IconState state = IconState.ic_file;
    private MessageAttachmentLoader.MessageAttachmentStorageListener attachmentStorageListener = null;

    public FileAttachmentViewHolder(@NonNull View itemView,
                                    FragmentViewHolderContract scrollToMessageButtonListener,
                                    String roomSecret, String serverAddress,
                                    FileAdapterContract contract) {
        super(itemView, scrollToMessageButtonListener);

        mainText = itemView.findViewById(R.id.main_text);
        fileName = itemView.findViewById(R.id.type_text);
        fileButton = itemView.findViewById(R.id.file_button);
        watchButton = itemView.findViewById(R.id.watch);
        fileIcon = itemView.findViewById(R.id.file_icon);
        progress = itemView.findViewById(R.id.progress);

        this.roomSecret = roomSecret;
        this.serverAddress = serverAddress;
        this.fileAdapterContract = contract;
    }

    @Override
    public void bind(AttachmentMessagePair attachmentMessagePair) {
        pair = attachmentMessagePair;

        fileAdapterContract.getLoader().loadFileAttachment(
                pair.getMessage(), pair.getAttachment(), this, false);

        fillMainText();
        String fileName = pair.getAttachment().getDisplayFileName();
        this.fileName.setText(AppStorage.getStringSize(
                pair.getAttachment().getSize()) + ", " + fileName);

        fileButton.setOnClickListener((View v) -> {
            fileAdapterContract.getLoader().loadFileAttachment(
                    pair.getMessage(), pair.getAttachment(), this, true);

            setLoading();
            fileButton.setOnClickListener(null);
        });

        watchButton.setOnClickListener((View v) -> {
            super.callScrollToMessage(pair.getMessage().getId(), pair.getMessage().getTime());
        });

        if (!AttachmentDownloader.isAttachmentSaving(pair.getAttachment())) {
            File file = AttachmentDownloader.getFileFromAttachment(pair.getAttachment(),
                    itemView.getContext(), roomSecret);
            onAttachmentLoaded(pair.getAttachment(),
                    file, pair.getMessage());
        } else {
            setLoading();
            fileButton.setOnClickListener(null);
        }
    }

    private void fillMainText() {

        String text = itemView.getContext().getString(R.string.voice_item_text);

        String name = contract.getMemberName(pair.getMessage().getAuthorId());

        long messageTime = pair.getMessage().getTime();
        String date = DeviceUtils.getShortDateFromTimestamp(messageTime);
        String time = DeviceUtils.getTimeFromTimestamp(messageTime);

        mainText.setText(text.replace("%n", name).
                replace("%d", date).replace("%t", time));
    }

    private void setLoading() {
        fileIcon.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        state = IconState.ic_progress;
    }

    private void setDownload() {
        progress.setVisibility(View.GONE);
        fileIcon.setVisibility(View.VISIBLE);
        if (state == IconState.ic_download) return;

        fileIcon.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_download));
        state = IconState.ic_download;
    }

    private void setFileButton() {
        progress.setVisibility(View.GONE);
        fileIcon.setVisibility(View.VISIBLE);
        if (state == IconState.ic_file) return;

        fileIcon.setImageDrawable(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_file));
        state = IconState.ic_file;
    }

    @Override
    public void onAttachmentLoaded(Attachment attachment, File file, Message message) {
        if (file == null) {
            setDownload();
            return;
        }

        setFileButton();

        fileButton.setOnClickListener((View view) -> {
            AppStorage.openFile(itemView.getContext(), file, attachment);
        });
    }

    @Override
    public void onLoadFailed(Attachment attachment) {

    }

    public MessageAttachmentLoader.MessageAttachmentStorageListener getAttachmentStorageListener() {
        return attachmentStorageListener;
    }

    public void setAttachmentStorageListener(MessageAttachmentLoader.MessageAttachmentStorageListener attachmentStorageListener) {
        this.attachmentStorageListener = attachmentStorageListener;
    }

    public void removeAttachmentStorageListener() {
        if (attachmentStorageListener != null) {
            attachmentStorageListener.removeViewHolder();
        }
        attachmentStorageListener = null;
    }

    private enum IconState {
        ic_file,

        ic_download,

        ic_progress
    }
}
