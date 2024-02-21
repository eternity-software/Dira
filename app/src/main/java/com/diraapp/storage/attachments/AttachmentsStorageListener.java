package com.diraapp.storage.attachments;

import com.diraapp.db.entities.Attachment;

public interface AttachmentsStorageListener {

    void onAttachmentBeginDownloading(Attachment attachment);

    void onAttachmentDownloaded(Attachment attachment);

    void onAttachmentDownloadFailed(Attachment attachment);
}
