package ru.dira.storage.attachments;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.dira.db.entities.Attachment;
import ru.dira.storage.AppStorage;
import ru.dira.storage.DownloadHandler;
import ru.dira.utils.CacheUtils;

public class AttachmentsStorage {

    private static Thread attachmentDownloader;
    private static final List<SaveAttachmentTask> saveAttachmentTaskList = new ArrayList<>();

    public static void saveAttachmentAsync(SaveAttachmentTask saveAttachmentTask) {
        if (attachmentDownloader == null) {
            attachmentDownloader = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {


                            for (SaveAttachmentTask saveAttachmentTask : new ArrayList<>(saveAttachmentTaskList)) {
                                try {
                                    saveAttachment(saveAttachmentTask.getContext(),
                                            saveAttachmentTask.getAttachment(),
                                            saveAttachmentTask.getRoomSecret(),
                                            true);

                                    saveAttachmentTaskList.remove(saveAttachmentTask);
                                } catch (Exception e) {
                                    e.printStackTrace();
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
    public static File saveAttachment(Context context, Attachment attachment, String roomSecret, boolean autoLoad) {
        return saveAttachment(context, attachment, roomSecret, autoLoad, null);
    }

    public static File saveAttachment(Context context, Attachment attachment, String roomSecret, boolean autoLoad, DownloadHandler downloadHandler) {
        File localFile = new File(context.getExternalCacheDir(), roomSecret + "_" + attachment.getFileUrl());


        if (!autoLoad | attachment.getSize() < CacheUtils.getInstance().getLong(CacheUtils.AUTO_LOAD_SIZE, context)) {
            AppStorage.downloadFile(AppStorage.OFFICIAL_DOWNLOAD_STORAGE_ADDRESS + attachment.getFileUrl(), localFile, downloadHandler);
            return localFile;
        }
        return null;
    }
}
