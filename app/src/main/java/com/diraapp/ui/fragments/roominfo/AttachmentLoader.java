package com.diraapp.ui.fragments.roominfo;

import android.content.Context;

import com.diraapp.db.daos.AttachmentDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttachmentLoader<ConvertedType, DbType> {

    private static final int MAX_ATTACHMENTS_COUNT = 240;

    private Context context;

    private final List<ConvertedType> attachments;

    private AttachmentLoaderListener listener;

    private AttachmentDataHelper<ConvertedType, DbType> converter;

    boolean isNewestLoaded = true;

    boolean isOldestLoaded = false;

    public AttachmentLoader(Context context, List<ConvertedType> attachments,
                            AttachmentLoaderListener listener,
                            AttachmentDataHelper<ConvertedType, DbType> converter) {
        this.context = context;
        this.attachments = attachments;
        this.listener = listener;
        this.converter = converter;
    }

    public boolean loadNewerAttachments(long newestId) {
        if (isNewestLoaded) return false;
        List<ConvertedType> attachmentList = convertList(converter.getNewer(newestId));
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
        List<ConvertedType> attachmentList = convertList(converter.getOlder(oldestId));
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
                convertList(converter.getLatest()));

        listener.notifyDataSetChanged();

        return attachments.size() != 0;
    }

    private List<ConvertedType> convertList(List<DbType> attachmentList) {
        List<ConvertedType> tList = new ArrayList<>(attachmentList.size());

        for (DbType attachment: attachmentList) {
            ConvertedType element = converter.convert(attachment);
            if (element == null) continue;

            tList.add(element);
        }

        return tList;
    }

    public interface AttachmentLoaderListener {

        void notifyItemsInserted(int from, int count);

        void notifyItemsRemoved(int from, int count);

        void notifyDataSetChanged();
    }

    public interface AttachmentDataHelper<T, dbType> {

        T convert(dbType data);

        List<dbType> getLatest();

        List<dbType> getNewer(long id);

        List<dbType> getOlder(long id);

    }
}
