package com.diraapp.ui.fragments.roominfo;

import android.content.Context;

import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.daos.AttachmentDao;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;

import java.util.Collections;
import java.util.List;

public class AttachmentLoader {

    private static final int MAX_ATTACHMENTS_COUNT = 240;

    private Context context;

    private AttachmentType attachmentType;

    private AttachmentDao attachmentDao;

    private String roomSecret;

    private List<Attachment> attachments;

    boolean isNewestLoaded = true;

    boolean isOldestLoaded = false;

    public AttachmentLoader(Context context, AttachmentType attachmentType,
                            String roomSecret, List<Attachment> attachments) {
        this.context = context;
        this.attachmentType = attachmentType;
        this.roomSecret = roomSecret;
        this.attachments = attachments;

        attachmentDao = DiraMessageDatabase.getDatabase(context).getAttachmentDao();
    }

    public boolean loadNewerAttachments(long newestId) {
        if (isOldestLoaded) return false;
        List<Attachment> attachmentList = attachmentDao.getNewerAttachments(roomSecret, newestId, attachmentType);

        boolean success = attachmentList.size() != 0;
        if (!success) {
            isNewestLoaded = true;
            return false;
        }

        Collections.reverse(attachmentList);

        attachmentList.addAll(attachments);
        attachments = attachmentList;

        if (attachments.size() > MAX_ATTACHMENTS_COUNT) {
            attachments = attachmentList.subList(0, attachments.size() - AttachmentDao.ATTACHMENT_LOAD_COUNT);
            isOldestLoaded = false;
        }

        return true;
    }

    public boolean loadOlderAttachments(long oldestId) {
        if (isOldestLoaded) return false;
        List<Attachment> attachmentList = attachmentDao.getOlderAttachments(roomSecret, oldestId, attachmentType);

        boolean success = attachmentList.size() != 0;
        if (!success) {
            isOldestLoaded = true;
            return false;
        }

        attachments.addAll(attachmentList);

        if (attachments.size() > MAX_ATTACHMENTS_COUNT) {
            attachments = attachments.subList(
                    AttachmentDao.ATTACHMENT_LOAD_COUNT, attachments.size());
            isNewestLoaded = false;
        }

        return true;
    }

    public boolean loadLatestAttachments() {
        attachments = attachmentDao.getLatestAttachments(roomSecret, attachmentType);

        return attachments.size() != 0;
    }
}
