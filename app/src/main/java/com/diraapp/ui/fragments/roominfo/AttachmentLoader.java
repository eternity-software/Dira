package com.diraapp.ui.fragments.roominfo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.diraapp.db.DiraMessageDatabase;
import com.diraapp.db.daos.AttachmentDao;
import com.diraapp.db.daos.auxiliaryobjects.AttachmentMessagePair;
import com.diraapp.db.entities.Attachment;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class AttachmentLoader<ConvertedType> {

    // Pairs contain fully loaded Attachment and Message objects
    private final List<AttachmentMessagePair> pairs;
    private final boolean useConverter;
    private final Context context;
    private final String roomSecret;
    // Types searching for in db requests
    private final AttachmentType[] types;
    // Fragments listening for results of db requests
    private final AttachmentLoaderListener listener;
    private final AttachmentDao attachmentDao;
    // Specific data type for adapter (in use only if it needs (use right constructor))
    private List<ConvertedType> attachments;
    // Use only if it's necessary
    private AttachmentDataConverter<ConvertedType> converter;
    private boolean isNewestLoaded = true;

    private boolean isOldestLoaded = false;

    private final int loadCount;
    private final int maxLoadedCount;

    // without converter
    public AttachmentLoader(Context context,
                            List<AttachmentMessagePair> pairs,
                            String roomSecret, AttachmentType[] types,
                            AttachmentLoaderListener listener, int loadCount) {
        this.context = context;
        this.pairs = pairs;
        this.roomSecret = roomSecret;
        this.types = types;
        this.listener = listener;

        useConverter = false;

        this.loadCount = loadCount;
        maxLoadedCount = 4 * loadCount;

        attachmentDao = DiraMessageDatabase.getDatabase(context).getAttachmentDao();
    }

    public AttachmentLoader(Context context, List<ConvertedType> attachments,
                            List<AttachmentMessagePair> pairs,
                            String roomSecret, AttachmentType[] types,
                            AttachmentLoaderListener listener,
                            AttachmentDataConverter<ConvertedType> converter,
                            int loadCount) {
        this.context = context;
        this.attachments = attachments;
        this.pairs = pairs;
        this.roomSecret = roomSecret;
        this.types = types;
        this.listener = listener;
        this.converter = converter;

        useConverter = true;

        this.loadCount = loadCount;
        maxLoadedCount = 4 * loadCount;

        attachmentDao = DiraMessageDatabase.getDatabase(context).getAttachmentDao();
    }

    public boolean isNewestLoaded() {
        return isNewestLoaded;
    }

    public AttachmentMessagePair getPairAtPosition(int i) {
        return pairs.get(i);
    }

    public void loadNewerAttachments(long newestId) {
        if (isNewestLoaded) return;

        List<AttachmentMessagePair> answer = attachmentDao.getNewerAttachments
                (roomSecret, newestId, types, loadCount);
        for (AttachmentMessagePair pair : answer) {
            pair.getMessage().getAttachments().add(pair.getAttachment());
        }

        Logger.logDebug(AttachmentLoader.class.getSimpleName(), "Loaded newer - " + answer.size());

        new Handler(Looper.getMainLooper()).post(() -> {

            Collections.reverse(answer);

            if (useConverter) {
                List<ConvertedType> attachmentList = convertList(answer);

                attachments.addAll(0, attachmentList);
            }

            int insertedCount = answer.size();
            boolean success = insertedCount != 0;
            if (!success) {
                isNewestLoaded = true;
                return;
            }

            pairs.addAll(0, answer);
            listener.notifyItemsInserted(0, insertedCount);

            if (pairs.size() > maxLoadedCount) {
                if (useConverter) {
                    attachments.subList(pairs.size() - loadCount,
                            pairs.size()).clear();
                }
                pairs.subList(pairs.size() - loadCount,
                        pairs.size()).clear();

                isOldestLoaded = false;

                listener.notifyItemsRemoved(
                        pairs.size() - loadCount,
                        loadCount);
            }
        });

    }

    public void loadOlderAttachments(long oldestId) {
        if (isOldestLoaded) return;

        List<AttachmentMessagePair> answer = attachmentDao.
                getOlderAttachments(roomSecret, oldestId, types, loadCount);
        for (AttachmentMessagePair pair : answer) {
            pair.getMessage().getAttachments().add(pair.getAttachment());
        }

        Logger.logDebug(AttachmentLoader.class.getSimpleName(), "Loaded older - " + answer.size());

        new Handler(Looper.getMainLooper()).post(() -> {

            if (useConverter) {
                List<ConvertedType> attachmentList = convertList(answer);

                attachments.addAll(attachmentList);
            }

            int insertedCount = answer.size();
            boolean success = insertedCount != 0;
            if (!success) {
                isOldestLoaded = true;
                return;
            }

            pairs.addAll(answer);
            listener.notifyItemsInserted(pairs.size() - insertedCount, insertedCount);

            boolean withRemoving = pairs.size() > maxLoadedCount;
            if (withRemoving) {
                if (useConverter)
                    attachments.subList(0, loadCount).clear();
                pairs.subList(0, loadCount).clear();

                isNewestLoaded = false;

                listener.notifyItemsRemoved(0, loadCount);
            }
        });
    }

    public void loadLatestAttachments() {
        List<AttachmentMessagePair> answer = attachmentDao.getLatestAttachments(roomSecret, types, loadCount);
        for (AttachmentMessagePair pair : answer) {
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

    public void loadNear(long startId) {
        List<AttachmentMessagePair> older = attachmentDao.
                getOlderAttachments(roomSecret, startId, types, loadCount);
        for (AttachmentMessagePair pair : older) {
            pair.getMessage().getAttachments().add(pair.getAttachment());
        }

        AttachmentMessagePair current = attachmentDao.getAttachmentMessagePairById(startId);

        List<AttachmentMessagePair> newer = attachmentDao.getNewerAttachments
                (roomSecret, startId, types, loadCount);
        for (AttachmentMessagePair pair : newer) {
            pair.getMessage().getAttachments().add(pair.getAttachment());
        }
        Collections.reverse(newer);

        pairs.addAll(newer);
        pairs.add(current);
        pairs.addAll(older);

        if (useConverter) attachments.addAll(convertList(pairs));

        new Handler(Looper.getMainLooper()).post(listener::notifyDataSetChanged);
    }

    public void insertNewPairs(final ArrayList<AttachmentMessagePair> newPairs) {
        ArrayList<AttachmentMessagePair> toRemove = new ArrayList<>();
        for (AttachmentMessagePair pair : newPairs) {
            if (!isValidType(pair.getAttachment())) {
                Logger.logDebug(AttachmentLoader.class.getSimpleName(), "New update: attachment removed");
                toRemove.add(pair);
            }
        }
        newPairs.removeAll(toRemove);

        if (useConverter) {
            List<ConvertedType> newConverted = convertList(newPairs);

            attachments.addAll(0, newConverted);
        }

        pairs.addAll(0, newPairs);

    }

    private List<ConvertedType> convertList(final List<AttachmentMessagePair> attachmentList) {
        List<ConvertedType> tList = new ArrayList<>(attachmentList.size());

        HashSet<AttachmentMessagePair> toRemove = new HashSet<>();
        for (AttachmentMessagePair attachment : attachmentList) {
            ConvertedType element = converter.convert(attachment);
            if (element == null) {
                toRemove.add(attachment);
                continue;
            }

            tList.add(element);
        }

        attachmentList.removeAll(toRemove);

        return tList;
    }

    private boolean isValidType(Attachment attachment) {
        if (attachment == null) return false;

        for (AttachmentType t : types) {
            if (t == attachment.getAttachmentType()) return true;
        }

        return false;
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
