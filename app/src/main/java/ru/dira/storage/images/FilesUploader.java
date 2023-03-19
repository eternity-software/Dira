package ru.dira.storage.images;


import java.io.File;
import java.io.IOException;

import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import ru.dira.storage.AppStorage;

public class FilesUploader {

    public static boolean uploadFile(String sourceFileUri, Callback callback) throws IOException {

        try {

            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", "file", RequestBody.create(new File(sourceFileUri),
                            MultipartBody.FORM))
                    .build();

            Request request = new Request.Builder()
                    .url(AppStorage.OFFICIAL_UPLOAD_STORAGE_ADDRESS)
                    .post(requestBody)
                    .build();

            OkHttpClient client = new OkHttpClient();

            client.newCall(request).enqueue(callback);

            return true;
        } catch (Exception ex) {
            // Handle the error
        }
        return false;
    }


}
