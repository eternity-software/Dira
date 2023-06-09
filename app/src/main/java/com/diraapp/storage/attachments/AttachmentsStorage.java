package com.diraapp.storage.attachments;

import android.content.Context;

import com.diraapp.db.entities.Attachment;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.DownloadHandler;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.utils.CacheUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AttachmentsStorage {

    private static final List<SaveAttachmentTask> saveAttachmentTaskList = new ArrayList<>();
    private static final List<AttachmentsStorageListener> attachmentsStorageListeners = new ArrayList<>();
    private static Thread attachmentDownloader;

    public static void saveAttachmentAsync(SaveAttachmentTask saveAttachmentTask, String address) {
        if (isAttachmentSaving(saveAttachmentTask.getAttachment())) return;
        if (attachmentDownloader == null) {
            attachmentDownloader = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {

                            for (SaveAttachmentTask saveAttachmentTask : new ArrayList<>(saveAttachmentTaskList)) {
                                try {
                                    for (AttachmentsStorageListener attachmentsStorageListener : attachmentsStorageListeners) {
                                        try {
                                            attachmentsStorageListener.onAttachmentBeginDownloading(saveAttachmentTask.getAttachment());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (saveAttachment(saveAttachmentTask.getContext(),
                                            saveAttachmentTask.getAttachment(),
                                            saveAttachmentTask.getRoomSecret(),
                                            true, address) != null) {
                                        for (AttachmentsStorageListener attachmentsStorageListener : attachmentsStorageListeners) {
                                            try {
                                                attachmentsStorageListener.onAttachmentDownloaded(saveAttachmentTask.getAttachment());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        for (AttachmentsStorageListener attachmentsStorageListener : attachmentsStorageListeners) {
                                            try {
                                                attachmentsStorageListener.onAttachmentDownloadFailed(saveAttachmentTask.getAttachment());
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }

                                    saveAttachmentTaskList.remove(saveAttachmentTask);

                                } catch (FileNotFoundException fileNotFoundException) {
                                    saveAttachmentTaskList.remove(saveAttachmentTask);
                                    for (AttachmentsStorageListener attachmentsStorageListener : attachmentsStorageListeners) {
                                        try {
                                            attachmentsStorageListener.onAttachmentDownloadFailed(saveAttachmentTask.getAttachment());
                                        } catch (Exception e2) {
                                            e2.printStackTrace();
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    for (AttachmentsStorageListener attachmentsStorageListener : attachmentsStorageListeners) {
                                        try {
                                            attachmentsStorageListener.onAttachmentDownloadFailed(saveAttachmentTask.getAttachment());
                                        } catch (Exception e2) {
                                            e2.printStackTrace();
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            attachmentDownloader.start();
        }
        saveAttachmentTaskList.add(saveAttachmentTask);
    }


    public static void addAttachmentsStorageListener(AttachmentsStorageListener attachmentsStorageListener) {
        if (attachmentsStorageListeners.contains(attachmentsStorageListener)) return;
        attachmentsStorageListeners.add(attachmentsStorageListener);
    }


    public static void removeAttachmentsStorageListener(AttachmentsStorageListener attachmentsStorageListener) {
        attachmentsStorageListeners.remove(attachmentsStorageListener);
    }

    public static boolean isAttachmentSaving(Attachment attachmentToCompare) {
        for (SaveAttachmentTask saveAttachmentTask : saveAttachmentTaskList) {
            if (saveAttachmentTask.getAttachment().getFileUrl().equals(attachmentToCompare.getFileUrl())) {
                return true;
            }
        }
        return false;
    }

    public static File saveAttachment(Context context, Attachment attachment, String roomSecret, boolean autoLoad, String address) throws IOException {
        return saveAttachment(context, attachment, roomSecret, autoLoad, null, address);
    }

    public static File saveAttachment(Context context, Attachment attachment, String roomSecret, boolean autoLoad, DownloadHandler downloadHandler, String address) throws IOException {
        File localFile = new File(context.getExternalCacheDir(), roomSecret + "_" + attachment.getFileUrl());
        CacheUtils cacheUtils = new CacheUtils(context);

        if (!autoLoad | attachment.getSize() < cacheUtils.getLong(CacheUtils.AUTO_LOAD_SIZE)) {
            AppStorage.downloadFile(UpdateProcessor.getInstance(context).getFileServer(address) + "/download/" + attachment.getFileUrl(), localFile, downloadHandler);
            return localFile;
        }
        return null;
    }

    public static File getFileFromAttachment(Attachment attachment, Context context, String roomSecret) {
        File localFile = new File(context.getExternalCacheDir(), roomSecret + "_" + attachment.getFileUrl());

        if (!localFile.exists()) return null;

        return localFile;
    }
}
