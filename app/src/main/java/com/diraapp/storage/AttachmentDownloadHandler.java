package com.diraapp.storage;

import com.diraapp.db.entities.Attachment;

public interface AttachmentDownloadHandler {
    void onProgressChanged(int progress);
}
