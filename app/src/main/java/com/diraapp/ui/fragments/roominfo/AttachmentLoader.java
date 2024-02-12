package com.diraapp.ui.fragments.roominfo;

import android.content.Context;

import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.daos.AttachmentDao;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.AttachmentType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttachmentLoader<ConvertedType> {

    private static final int MAX_ATTACHMENTS_COUNT = 240;
    // Pairs contain fully loaded Attachment and Message objects
    private final List<AttachmentMessagePair> pairs;
    private final boolean useConverter;
    private Context context;
    private String roomSecret;
    // Types searching for in db requests
    private AttachmentType[] types;
    // Specific data type for adapter (in use only if it needs (use right constructor))
    private List<ConvertedType> attachments;
    // Fragments listening for results of db requests
    private AttachmentLoaderListener listener;
    private AttachmentDataConverter<ConvertedType> converter;
    private AttachmentDao attachmentDao;

    private boolean isNewestLoaded = true;

    private boolean isOldestLoaded = false;

    public AttachmentLoader(Context context,
                            List<AttachmentMessagePair> pairs,
                            String roomSecret, AttachmentType[] types,
                            AttachmentLoaderListener listener) {
        this.context = context;
        this.pairs = pairs;
        this.roomSecret = roomSecret;
        this.types = types;
        this.listener = listener;

        useConverter = false;

        attachmentDao = DiraMessageDatabase.getDatabase(context).getAttachmentDao();
    }

    public AttachmentLoader(Context context, List<ConvertedType> attachments,
                            List<AttachmentMessagePair> pairs,
                            String roomSecret, AttachmentType[] types,
                            AttachmentLoaderListener listener,
                            AttachmentDataConverter<ConvertedType> converter) {
        this.context = context;
        this.attachments = attachments;
        this.pairs = pairs;
        this.roomSecret = roomSecret;
        this.types = types;
        this.listener = listener;
        this.converter = converter;

        useConverter = true;

        attachmentDao = DiraMessageDatabase.getDatabase(context).getAttachmentDao();
    }

    public AttachmentMessagePair getPairAtPosition(int i) {
        return pairs.get(i);
    }

    public boolean loadNewerAttachments(long newestId) {
        if (isNewestLoaded) return false;

        List<AttachmentMessagePair> answer = attachmentDao.getNewerAttachments
                (roomSecret, newestId, types);

        int insertedCount = answer.size();

        boolean success = answer.size() != 0;
        if (!success) {
            isNewestLoaded = true;
            return false;
        }

        Collections.reverse(answer);

        if (useConverter) {
            List<ConvertedType> attachmentList = convertList(answer);

            attachments.addAll(0, attachmentList);
        }

        pairs.addAll(0, answer);
        listener.notifyItemsInserted(0, insertedCount);

        if (pairs.size() > MAX_ATTACHMENTS_COUNT) {
            if (useConverter) {
                attachments.subList(attachments.size() - AttachmentDao.ATTACHMENT_LOAD_COUNT,
                        attachments.size()).clear();
            }

            isOldestLoaded = false;

            pairs.subList(pairs.size() - AttachmentDao.ATTACHMENT_LOAD_COUNT,
                    pairs.size()).clear();
            listener.notifyItemsRemoved(
                    pairs.size() - AttachmentDao.ATTACHMENT_LOAD_COUNT,
                    AttachmentDao.ATTACHMENT_LOAD_COUNT);
        }

        return true;
    }

    public boolean loadOlderAttachments(long oldestId) {
        if (isOldestLoaded) return false;

        List<AttachmentMessagePair> answer = attachmentDao.
                getOlderAttachments(roomSecret, oldestId, types);

        int insertedCount = answer.size();

        boolean success = answer.size() != 0;
        if (!success) {
            isOldestLoaded = true;
            return false;
        }

        if (useConverter) {
            List<ConvertedType> attachmentList = convertList(answer);

            attachments.addAll(attachmentList);
        }

        pairs.addAll(answer);
        listener.notifyItemsInserted(pairs.size() - insertedCount, insertedCount);

        boolean withRemoving = pairs.size() > MAX_ATTACHMENTS_COUNT;
        if (withRemoving) {
            if (useConverter) attachments.subList(0, AttachmentDao.ATTACHMENT_LOAD_COUNT).clear();

            isNewestLoaded = false;

            pairs.subList(0, AttachmentDao.ATTACHMENT_LOAD_COUNT).clear();
            listener.notifyItemsRemoved(0, AttachmentDao.ATTACHMENT_LOAD_COUNT);
        }

        return true;
    }

    public boolean loadLatestAttachments() {
        List<AttachmentMessagePair> answer = attachmentDao.getLatestAttachments(roomSecret, types);

        if (useConverter) attachments.addAll(convertList(answer));

        pairs.addAll(answer);

        listener.notifyDataSetChanged();

        return pairs.size() != 0;
    }

    private List<ConvertedType> convertList(List<AttachmentMessagePair> attachmentList) {
        List<ConvertedType> tList = new ArrayList<>(attachmentList.size());

        for (AttachmentMessagePair attachment : attachmentList) {
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

    public interface AttachmentDataConverter<ConvertedType> {

        ConvertedType convert(AttachmentMessagePair data);

    }
}
