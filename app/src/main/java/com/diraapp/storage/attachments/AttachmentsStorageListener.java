package com.diraapp.storage.attachments;

import androidx.annotation.Nullable;

import com.diraapp.db.entities.Attachment;
import com.diraapp.storage.AttachmentDownloadHandler;

public interface AttachmentsStorageListener {
    void onAttachmentBeginDownloading(Attachment attachment);

    void onAttachmentDownloaded(Attachment attachment);

    void onAttachmentDownloadFailed(Attachment attachment);
}
