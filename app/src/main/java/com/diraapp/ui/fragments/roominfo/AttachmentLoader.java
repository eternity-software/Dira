package com.diraapp.ui.fragments.roominfo;

import android.content.Context;

import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.daos.AttachmentDao;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttachmentLoader<T> {

    private static final int MAX_ATTACHMENTS_COUNT = 240;

    private Context context;

    private AttachmentType[] attachmentTypes;

    private AttachmentDao attachmentDao;

    private String roomSecret;

    private final List<T> attachments;

    private AttachmentLoaderListener listener;

    private AttachmentConverter<T> converter;

    boolean isNewestLoaded = true;

    boolean isOldestLoaded = false;

    public AttachmentLoader(Context context, AttachmentType[] attachmentType,
                            String roomSecret, List<T> attachments,
                            AttachmentLoaderListener listener, AttachmentConverter<T> converter) {
        this.context = context;
        this.attachmentTypes = attachmentType;
        this.roomSecret = roomSecret;
        this.attachments = attachments;
        this.listener = listener;
        this.converter = converter;

        attachmentDao = DiraMessageDatabase.getDatabase(context).getAttachmentDao();
    }

    public boolean loadNewerAttachments(long newestId) {
        if (isNewestLoaded) return false;
        List<T> attachmentList = convertList(attachmentDao.getNewerAttachments(roomSecret, newestId, attachmentTypes));
        int insertedCount = attachmentList.size();

        boolean success = attachmentList.size() != 0;
        if (!success) {
            isNewestLoaded = true;
            return false;
        }

        Collections.reverse(attachmentList);

        attachments.addAll(0, attachmentList);
        listener.notifyItemsInserted(0, insertedCount);

        if (attachments.size() > MAX_ATTACHMENTS_COUNT) {
            attachments.subList(attachments.size() - AttachmentDao.ATTACHMENT_LOAD_COUNT,
                    attachments.size()).clear();
            isOldestLoaded = false;

            listener.notifyItemsRemoved(
                    attachments.size() - AttachmentDao.ATTACHMENT_LOAD_COUNT,
                    AttachmentDao.ATTACHMENT_LOAD_COUNT);
        }

        return true;
    }

    public boolean loadOlderAttachments(long oldestId) {
        if (isOldestLoaded) return false;
        List<T> attachmentList = convertList(attachmentDao.getOlderAttachments(roomSecret, oldestId, attachmentTypes));
        int insertedCount = attachmentList.size();

        boolean success = attachmentList.size() != 0;
        if (!success) {
            isOldestLoaded = true;
            return false;
        }

        attachments.addAll(attachmentList);
        listener.notifyItemsInserted(attachments.size() - insertedCount, insertedCount);

        boolean withRemoving = attachments.size() > MAX_ATTACHMENTS_COUNT;
        if (withRemoving) {
            attachments.subList(0, AttachmentDao.ATTACHMENT_LOAD_COUNT).clear();
            isNewestLoaded = false;

            listener.notifyItemsRemoved(0, AttachmentDao.ATTACHMENT_LOAD_COUNT);
        }

        return true;
    }

    public boolean loadLatestAttachments() {
        attachments.addAll(
                convertList(attachmentDao.getLatestAttachments(roomSecret, attachmentTypes)));

        listener.notifyDataSetChanged();

        return attachments.size() != 0;
    }

    private List<T> convertList(List<Attachment> attachmentList) {
        List<T> tList = new ArrayList<>(attachmentList.size());

        for (Attachment attachment: attachmentList) {
            T element = converter.convert(attachment);
            if (element == null) continue;

            tList.add(element);
        }

        return tList;
    }

    public interface AttachmentLoaderListener {

        void notifyItemsInserted(int from, int count);

        void notifyItemsRemoved(int from, int count);

        void notifyItemsInsertedAndRemoved(int fromI, int countI, int fromR, int countR);

        void notifyDataSetChanged();
    }

    public interface AttachmentConverter<T> {

        T convert(Attachment attachment);

    }
}
