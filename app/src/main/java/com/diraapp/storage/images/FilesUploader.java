package com.diraapp.storage.images;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.diraapp.storage.AppStorage;
import com.diraapp.storage.FileClassifier;
import com.diraapp.api.processors.UpdateProcessor;
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

    public static boolean uploadFile(String sourceFileUri, Callback callback, Context context, boolean deleteAfterUpload, String serverAddress) throws IOException {

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

                int maxFrameSize = 800;

                if (bitmap.getHeight() * bitmap.getWidth() > maxFrameSize * maxFrameSize) {
                    if (bitmap.getHeight() > bitmap.getWidth()) {
                        float r = bitmap.getHeight() / (float) bitmap.getWidth();
                        bitmap = Bitmap.createScaledBitmap(bitmap, maxFrameSize, (int) (r * maxFrameSize), true);
                    } else {
                        float r = bitmap.getWidth() / (float) bitmap.getHeight();
                        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (r * maxFrameSize), maxFrameSize, true);
                    }


                }
                deleteAfterUpload = true;

                sourceFileUri = AppStorage.saveToInternalStorage(bitmap,
                        null, context);
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
