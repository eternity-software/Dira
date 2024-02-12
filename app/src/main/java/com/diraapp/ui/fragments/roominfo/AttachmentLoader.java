package com.diraapp.ui.fragments.roominfo;

import android.content.Context;

import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.daos.AttachmentDao;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttachmentLoader<ConvertedType> {

    private static final int MAX_ATTACHMENTS_COUNT = 240;

    private Context context;

    private String roomSecret;

    // Types searching for in db requests
    private AttachmentType[] types;

    // Specific data type for adapter
    private final List<ConvertedType> attachments;

    // Pairs contain fully loaded Attachment and Message objects
    private final List<AttachmentMessagePair> pairs;

    // Fragments listening for results of db requests
    private AttachmentLoaderListener listener;

    private AttachmentDataConverter<ConvertedType> converter;

    private AttachmentDao attachmentDao;

    boolean isNewestLoaded = true;

    boolean isOldestLoaded = false;

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
        List<ConvertedType> attachmentList = convertList(answer);

        attachments.addAll(0, attachmentList);
        listener.notifyItemsInserted(0, insertedCount);
        pairs.addAll(0, answer);

        if (attachments.size() > MAX_ATTACHMENTS_COUNT) {
            attachments.subList(attachments.size() - AttachmentDao.ATTACHMENT_LOAD_COUNT,
                    attachments.size()).clear();
            isOldestLoaded = false;

            listener.notifyItemsRemoved(
                    attachments.size() - AttachmentDao.ATTACHMENT_LOAD_COUNT,
                    AttachmentDao.ATTACHMENT_LOAD_COUNT);
            pairs.subList(attachments.size() - AttachmentDao.ATTACHMENT_LOAD_COUNT,
                    attachments.size()).clear();
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

        List<ConvertedType> attachmentList = convertList(answer);

        attachments.addAll(attachmentList);
        listener.notifyItemsInserted(attachments.size() - insertedCount, insertedCount);
        pairs.addAll(answer);

        boolean withRemoving = attachments.size() > MAX_ATTACHMENTS_COUNT;
        if (withRemoving) {
            attachments.subList(0, AttachmentDao.ATTACHMENT_LOAD_COUNT).clear();
            isNewestLoaded = false;

            listener.notifyItemsRemoved(0, AttachmentDao.ATTACHMENT_LOAD_COUNT);
            pairs.subList(0, AttachmentDao.ATTACHMENT_LOAD_COUNT).clear();
        }

        return true;
    }

    public boolean loadLatestAttachments() {
        List<AttachmentMessagePair> answer = attachmentDao.getLatestAttachments(roomSecret, types);

        attachments.addAll(
                convertList(answer));
        pairs.addAll(answer);

        listener.notifyDataSetChanged();

        return attachments.size() != 0;
    }

    private List<ConvertedType> convertList(List<AttachmentMessagePair> attachmentList) {
        List<ConvertedType> tList = new ArrayList<>(attachmentList.size());

        for (AttachmentMessagePair attachment: attachmentList) {
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
