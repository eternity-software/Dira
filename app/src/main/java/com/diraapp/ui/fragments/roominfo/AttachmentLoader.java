package com.diraapp.ui.fragments.roominfo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.daos.AttachmentDao;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.db.entities.messages.Message;
import com.diraapp.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttachmentLoader<ConvertedType> {

    private static final int MAX_ATTACHMENTS_COUNT = 240;

    private Context context;

    private String roomSecret;

    // Types searching for in db requests
    private AttachmentType[] types;

    // Specific data type for adapter (in use only if it needs (use right constructor))
    private List<ConvertedType> attachments;

    // Pairs contain fully loaded Attachment and Message objects
    private final List<AttachmentMessagePair> pairs;

    // Fragments listening for results of db requests
    private AttachmentLoaderListener listener;

    // Use only if it's necessary
    private AttachmentDataConverter<ConvertedType> converter;

    private final boolean useConverter;

    private AttachmentDao attachmentDao;

    private boolean isNewestLoaded = true;

    private boolean isOldestLoaded = false;

    // without converter
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

    public void loadNewerAttachments(long newestId) {
        if (isNewestLoaded) return;

        List<AttachmentMessagePair> answer = attachmentDao.getNewerAttachments
                (roomSecret, newestId, types);
        for (AttachmentMessagePair pair: answer) {
            pair.getMessage().getAttachments().add(pair.getAttachment());
        }

        Logger.logDebug(AttachmentLoader.class.getSimpleName(), "Loaded newer - " + answer.size());

        new Handler(Looper.getMainLooper()).post(() -> {

            int insertedCount = answer.size();

            boolean success = answer.size() != 0;
            if (!success) {
                isNewestLoaded = true;
                return;
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
                    attachments.subList(pairs.size() - AttachmentDao.ATTACHMENT_LOAD_COUNT,
                            pairs.size()).clear();
                }
                pairs.subList(pairs.size() - AttachmentDao.ATTACHMENT_LOAD_COUNT,
                        pairs.size()).clear();

                isOldestLoaded = false;

                listener.notifyItemsRemoved(
                        pairs.size() - AttachmentDao.ATTACHMENT_LOAD_COUNT,
                        AttachmentDao.ATTACHMENT_LOAD_COUNT);
            }
        });

    }

    public void loadOlderAttachments(long oldestId) {
        if (isOldestLoaded) return;

        List<AttachmentMessagePair> answer = attachmentDao.
                getOlderAttachments(roomSecret, oldestId, types);
        for (AttachmentMessagePair pair: answer) {
            pair.getMessage().getAttachments().add(pair.getAttachment());
        }

        Logger.logDebug(AttachmentLoader.class.getSimpleName(), "Loaded older - " + answer.size());

        new Handler(Looper.getMainLooper()).post(() -> {
            int insertedCount = answer.size();

            boolean success = answer.size() != 0;
            if (!success) {
                isOldestLoaded = true;
                return;
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
                pairs.subList(0, AttachmentDao.ATTACHMENT_LOAD_COUNT).clear();

                isNewestLoaded = false;

                listener.notifyItemsRemoved(0, AttachmentDao.ATTACHMENT_LOAD_COUNT);
            }
        });
    }

    public void loadLatestAttachments() {
        List<AttachmentMessagePair> answer = attachmentDao.getLatestAttachments(roomSecret, types);
        for (AttachmentMessagePair pair: answer) {
            pair.getMessage().getAttachments().add(pair.getAttachment());
        }

        Logger.logDebug(AttachmentLoader.class.getSimpleName(), "Loaded latest - " + answer.size());

        if (answer.size() == 0) return;

        new Handler(Looper.getMainLooper()).post(() -> {
            if (useConverter) attachments.addAll(convertList(answer));

            pairs.addAll(answer);

            listener.notifyDataSetChanged();
        });
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
