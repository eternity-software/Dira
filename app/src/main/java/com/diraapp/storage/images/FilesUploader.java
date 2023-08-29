package com.diraapp.storage.images;


import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.db.entities.AttachmentType;
import com.diraapp.storage.AppStorage;
import com.diraapp.storage.FileClassifier;
import com.diraapp.ui.activities.room.RoomActivityPresenter;
import com.diraapp.utils.CryptoUtils;
import com.diraapp.utils.ImageRotationFix;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FilesUploader {

    public static boolean uploadFile(String sourceFileUri,
                                     Callback callback,
                                     Context context,
                                     boolean deleteAfterUpload,
                                     String serverAddress,
                                     String encryptionKey) throws IOException {

        try {

            if (FileClassifier.isImageFile(sourceFileUri)) {
                Bitmap bitmap = null;
                try {
                    bitmap = ImageRotationFix.rotateImageIfRequired(context, AppStorage.getBitmapFromUrl(sourceFileUri), Uri.fromFile(new File(sourceFileUri)));
                } catch (IOException e) {
                    Handler mainHandler = new Handler(context.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    };
                    mainHandler.post(myRunnable);

                }

                if (bitmap == null) {
                    bitmap = AppStorage.getBitmapFromPath(sourceFileUri);
                }

                int maxFrameSize = 2000;
                int height = bitmap.getHeight();
                int width = bitmap.getWidth();
                if (bitmap.getHeight() * bitmap.getWidth() > maxFrameSize * maxFrameSize) {

                    float scaleFactor = (bitmap.getHeight() * bitmap.getWidth()) / (float) (maxFrameSize * maxFrameSize);

                    System.out.println(scaleFactor + " scale " + (maxFrameSize * maxFrameSize) + " " + (bitmap.getHeight() * bitmap.getWidth()));

                    bitmap = Bitmap.createScaledBitmap(bitmap, (int) (width / scaleFactor),
                            (int) (height / scaleFactor), true);

                }

                if (callback instanceof RoomActivityPresenter.RoomAttachmentCallback) {
                    ((RoomActivityPresenter.RoomAttachmentCallback) callback).
                            setWidthAndHeight(bitmap.getWidth(), bitmap.getHeight());
                }

                deleteAfterUpload = true;

                sourceFileUri = AppStorage.saveToInternalStorage(bitmap,
                        null, context);
            } else if (callback instanceof RoomActivityPresenter.RoomAttachmentCallback) {
                RoomActivityPresenter.RoomAttachmentCallback roomAttachmentCallback =
                        (RoomActivityPresenter.RoomAttachmentCallback) callback;
                if (roomAttachmentCallback.getAttachmentType() == AttachmentType.VIDEO) {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(sourceFileUri);
                    int width = Integer.parseInt(retriever.
                            extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
                    int height = Integer.parseInt(retriever.
                            extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
                    retriever.release();

                    roomAttachmentCallback.setWidthAndHeight(width, height);
                }
            }

            if (!encryptionKey.equals("")) {
                File rawFile = new File(sourceFileUri);
                File outputFile = new File(rawFile.getPath() + "enctypted_" + rawFile.getName());
                CryptoUtils.encrypt(encryptionKey, rawFile, outputFile);
                sourceFileUri = outputFile.getAbsolutePath();
                System.out.println("encrypted " + sourceFileUri);
                deleteAfterUpload = true;
            }


            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", "file", RequestBody.create(new File(sourceFileUri),
                            MultipartBody.FORM))
                    .build();

            Request request = new Request.Builder()
                    .url(UpdateProcessor.getInstance().getFileServer(serverAddress) + "/upload/")
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();

            String finalSourceFileUri = sourceFileUri;
            boolean finalIsCopied = deleteAfterUpload;
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    if (finalIsCopied) new File(finalSourceFileUri).delete();
                    callback.onFailure(call, e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (finalIsCopied) new File(finalSourceFileUri).delete();
                    callback.onResponse(call, response);
                }
            });

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            // Handle the error
        }
        return false;
    }


}
