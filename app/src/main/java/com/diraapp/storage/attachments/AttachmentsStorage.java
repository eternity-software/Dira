package com.diraapp.storage.attachments;

import android.content.Context;

import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.db.entities.Attachment;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.DownloadHandler;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.CryptoUtils;

import org.jetbrains.annotations.NotNull;

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
        if (saveAttachmentTask.getAttachment() == null) return;
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
                                            saveAttachmentTask.isSizeLimited(), address, UpdateProcessor.getInstance().getRoom(saveAttachmentTask.getRoomSecret()).getEncryptionKey()) != null) {
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
        if (attachmentToCompare == null) return false;
        for (SaveAttachmentTask saveAttachmentTask : saveAttachmentTaskList) {
            if (saveAttachmentTask.getAttachment().getFileUrl().equals(attachmentToCompare.getFileUrl())) {
                return true;
            }
        }
        return false;
    }

    public static File saveAttachment(Context context, Attachment attachment, String roomSecret, boolean sizeLimited, String address, String encryptionKey) throws IOException {
        return saveAttachment(context, attachment, roomSecret, sizeLimited, null, address, encryptionKey);
    }

    public static File saveAttachment(Context context, Attachment attachment, String roomSecret, boolean sizeLimited, DownloadHandler downloadHandler, String address, String encryptionKey) throws IOException {
        File localFile = new File(context.getExternalCacheDir(), roomSecret + "_" + attachment.getFileUrl());
        CacheUtils cacheUtils = new CacheUtils(context);

        if (!sizeLimited | attachment.getSize() < cacheUtils.getLong(CacheUtils.AUTO_LOAD_SIZE)) {
            AppStorage.downloadFile(UpdateProcessor.getInstance(context).getFileServer(address) + "/download/" + attachment.getFileUrl(), localFile, downloadHandler);


            if (!encryptionKey.equals("")) {
                File decrypted = new File(context.getExternalCacheDir(), roomSecret + "_" + attachment.getFileUrl());
                CryptoUtils.decrypt(encryptionKey, localFile, decrypted);
                localFile = decrypted;
            }


            return localFile;
        }
        return null;
    }


    public static File getFileFromAttachment(@NotNull Attachment attachment, Context context, String roomSecret) {
        if (attachment == null) return null;
        File localFile = new File(context.getExternalCacheDir(), roomSecret + "_" + attachment.getFileUrl());

        if (!localFile.exists()) return null;

        return localFile;
    }
}
