package com.diraapp.storage.attachments;

import android.content.Context;

import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.db.entities.Attachment;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.AttachmentDownloadHandler;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.CryptoUtils;
import com.diraapp.utils.Logger;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AttachmentDownloader {

    private static final List<SaveAttachmentTask> saveAttachmentTaskList = new ArrayList<>();
    private static final List<AttachmentsStorageListener> attachmentsStorageListeners = new ArrayList<>();
    private static final HashMap<Attachment, AttachmentDownloadHandler> downloadHandlerHashMap = new HashMap<>();
    private static Thread attachmentDownloader;

    public static void saveAttachmentAsync(SaveAttachmentTask saveAttachmentTask, String address) {
        if (isAttachmentSaving(saveAttachmentTask.getAttachment())) return;
        if (saveAttachmentTask.getAttachment() == null) return;

        saveAttachmentTaskList.add(saveAttachmentTask);

        if (attachmentDownloader == null) {
            attachmentDownloader = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            if (saveAttachmentTaskList.size() == 0) break;
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
                                            saveAttachmentTask.isSizeLimited(),
                                            new AttachmentDownloadHandler() {
                                                @Override
                                                public void onProgressChanged(int progress) {
                                                    AttachmentDownloadHandler handler = downloadHandlerHashMap.get(saveAttachmentTask.getAttachment());
                                                    if (handler != null)
                                                        handler.onProgressChanged(progress);
                                                }
                                            },
                                            address, UpdateProcessor.getInstance().getRoom(saveAttachmentTask.getRoomSecret()).getEncryptionKey()) != null) {
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

                                    downloadHandlerHashMap.remove(saveAttachmentTask.getAttachment());

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
                    attachmentDownloader = null;
                }
            });
            attachmentDownloader.start();
        }

    }


    public static void setDownloadHandlerForAttachment(AttachmentDownloadHandler handler, Attachment attachment) {
        downloadHandlerHashMap.put(attachment, handler);
    }

    public static void removeAttachmentDownloadHandler(AttachmentDownloadHandler handler, Attachment attachment) {
        AttachmentDownloadHandler attachmentDownloadHandler = downloadHandlerHashMap.get(attachment);
        if (attachmentDownloadHandler == handler)
            downloadHandlerHashMap.remove(attachment);
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
        for (SaveAttachmentTask saveAttachmentTask : new ArrayList<>(saveAttachmentTaskList)) {
            if (saveAttachmentTask.getAttachment().getFileUrl().equals(attachmentToCompare.getFileUrl())) {
                return true;
            }
        }
        return false;
    }

    public static File saveAttachment(Context context, Attachment attachment, String roomSecret, boolean sizeLimited, String address, String encryptionKey) throws IOException {
        return saveAttachment(context, attachment, roomSecret, sizeLimited, null, address, encryptionKey);
    }

    public static File saveAttachment(Context context, Attachment attachment, String roomSecret, boolean sizeLimited, AttachmentDownloadHandler attachmentDownloadHandler, String address, String encryptionKey) throws IOException {
        File localFile = new File(context.getExternalCacheDir(), roomSecret + "_" + attachment.getFileUrl());
        CacheUtils cacheUtils = new CacheUtils(context);

        if (!sizeLimited | attachment.getSize() < cacheUtils.getLong(CacheUtils.AUTO_LOAD_SIZE)) {
            String fileServerUrl = UpdateProcessor.getInstance(context).getFileServer(address);
            if (fileServerUrl == null) {
                Logger.logDebug("AttachmentsStorage", "Unknown file server for " + address);
                return null;
            }
            AppStorage.downloadAttachment(fileServerUrl + "/download/" + attachment.getFileUrl(),
                    attachment, localFile, attachmentDownloadHandler);


            if (!encryptionKey.equals("")) {
                File decrypted = new File(context.getExternalCacheDir(), roomSecret + "_" + attachment.getFileUrl());
                CryptoUtils.decrypt(encryptionKey, localFile, decrypted);
                localFile = decrypted;
            }


            return localFile;
        }
        return null;
    }


    /**
     * Returns null if file doesn't exist
     */
    public static File getFileFromAttachment(@NotNull Attachment attachment, Context context, String roomSecret) {
        if (attachment == null) return null;
        File localFile = new File(context.getExternalCacheDir(), roomSecret + "_" + attachment.getFileUrl());

        if (!localFile.exists()) return null;

        return localFile;
    }
}
