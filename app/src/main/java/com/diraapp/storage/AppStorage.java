package com.diraapp.storage;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.diraapp.R;
import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.db.entities.Attachment;
import com.diraapp.storage.attachments.AttachmentDownloader;
import com.diraapp.utils.CacheUtils;
import com.diraapp.utils.Logger;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;

public class AppStorage {

    public static final String DIRA_FILES_PATH = "diraFiles";
    // public static final String OFFICIAL_DOWNLOAD_STORAGE_ADDRESS = "http://diraapp.com:4444/download/";
    // public static final String OFFICIAL_UPLOAD_STORAGE_ADDRESS = "http://diraapp.com:4444/upload/";
    public static final long MAX_DEFAULT_ATTACHMENT_AUTOLOAD_SIZE = 1024 * 1024 * 20; // 20 mb


    /**
     * Save room servers to cache
     *
     * @param serverList
     * @param context
     */
    public static void saveServerList(ArrayList<String> serverList, Context context) {
        Gson gson = new Gson();
        String json = gson.toJson(serverList);
        CacheUtils cacheUtils = new CacheUtils(context);
        cacheUtils.setString(CacheUtils.SERVER_LIST, json);
    }

    /**
     * Get room servers
     */
    public static ArrayList<String> getServerList(Context context) {
        Gson gson = new Gson();
        CacheUtils cacheUtils = new CacheUtils(context);
        String json = cacheUtils.getString(CacheUtils.SERVER_LIST);
        ArrayList<String> serverList = new ArrayList<>();

        if (json != null) {
            serverList = (ArrayList<String>) gson.fromJson(json, ArrayList.class);
        }

        boolean hasOfficialServer = false;

        for (String server : serverList) {
            if (server.equals(UpdateProcessor.OFFICIAL_ADDRESS)) {
                hasOfficialServer = true;
                break;
            }
        }
        if (!hasOfficialServer) {
            serverList.add(UpdateProcessor.OFFICIAL_ADDRESS);
        }

        return serverList;

    }

    public static int getResId(String resName, Class<?> c) {

        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void downloadAttachment(String url, Attachment attachment, File outputFile, AttachmentDownloadHandler attachmentDownloadHandler) throws IOException {

        System.out.println(url);
        URL u = new URL(url);
        URLConnection conn = u.openConnection();


        int fileLength = conn.getContentLength();
        InputStream input = null;
        OutputStream output = null;
        // download the file
        input = conn.getInputStream();
        output = new FileOutputStream(outputFile);

        byte[] data = new byte[4096];
        long total = 0;

        long lastTimeProgressNotified = 0;

        int count;
        while ((count = input.read(data)) != -1) {
            // allow canceling with back button

            total += count;
            // publishing the progress....
            if (fileLength > 0) // only if total length is known
            {
                if (attachmentDownloadHandler != null) {
                    if (System.currentTimeMillis() - lastTimeProgressNotified > 500) {
                        lastTimeProgressNotified = System.currentTimeMillis();
                        attachmentDownloadHandler.onProgressChanged(((int) (total * 100 / fileLength)));
                    }

                }

            }

            output.write(data, 0, count);
        }


    }

    public static Bitmap getBitmapFromUrl(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Bitmap d = BitmapFactory.decodeStream(is);
            is.close();
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getStringSize(long size) {
        long kb = 1000;
        long mb = kb * 1000;
        long gb = mb * 1000;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }


    public static String saveToInternalStorage(Bitmap bitmapImage, String roomSecret, Context context) {
        return saveToInternalStorage(bitmapImage, (new Random()).nextInt(9000) + ".png", roomSecret, context);
    }

    public static String saveToInternalStorage(Bitmap bitmapImage, String name, String roomSecret, Context context) {
        ContextWrapper cw = new ContextWrapper(context);

        File directory = cw.getDir(DIRA_FILES_PATH, Context.MODE_PRIVATE);

        if (roomSecret == null) {
            directory = cw.getDir(DIRA_FILES_PATH, Context.MODE_PRIVATE);
        } else {
            File file = new File(directory, roomSecret);
            file.mkdirs();
            directory = file;
        }


        String fileName = System.currentTimeMillis() + name + ".png";

        File imagePath = new File(directory, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imagePath);

            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath() + "/" + fileName;
    }

    public static void saveFileToDownloads(Attachment attachment, Context context,
                                           String roomSecret) {
        File sourceFile = AttachmentDownloader.getFileFromAttachment(attachment, context, roomSecret);

        File downloadsDir = Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        String fileName = attachment.getFileUrl() + "_" + attachment.getDisplayFileName();

        File downloadsFile = new File(downloadsDir, fileName);

        try {
            FileInputStream inStream = new FileInputStream(sourceFile);
            FileOutputStream outStream = new FileOutputStream(downloadsFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

            inStream.close();
            outStream.close();

            // File copied successfully
            Toast.makeText(context, context.getString(R.string.file_saved_successfully), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the file copy error
            Toast.makeText(context, context.getString(R.string.file_saving_failed), Toast.LENGTH_SHORT).show();
        }
    }

    public static Bitmap getBitmapFromPath(String sourceFileUri, Context context) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Bitmap bitmap = null;

            if (Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver,
                        Uri.fromFile(new File(sourceFileUri)));
            } else {
                ImageDecoder.Source source = ImageDecoder.createSource(contentResolver,
                        Uri.fromFile(new File(sourceFileUri)));
                bitmap = ImageDecoder.decodeBitmap(source);
            }

            return bitmap;
        } catch (IOException exception) {
            return getBitmapFromPath(sourceFileUri);
        }
    }

    @Deprecated
    public static Bitmap getBitmapFromPath(String path) {

        try {
            Logger.logDebug("AppStorage",
                    "Getting bitmap from file " + path);
            File f = new File(path);
            return BitmapFactory.decodeStream(new FileInputStream(f));

        } catch (Exception e) {
            if (e instanceof NullPointerException) return null;
            e.printStackTrace();
            return null;
        }

    }

    public static Bitmap getBitmapFromBase64(@Nullable String base64Str) throws IllegalArgumentException {
        try {
            if (base64Str == null) return null;
            byte[] decodedBytes = Base64.decode(
                    base64Str.substring(base64Str.indexOf(",") + 1),
                    Base64.DEFAULT
            );


            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getBase64FromBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{split[1]};
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static void deleteAttachment(Context context, Attachment attachment, String roomSecret) {
        File file = new File(context.getExternalCacheDir(),
                roomSecret + "_" + attachment.getFileUrl());

        if (file.exists()) {
            file.delete();
            Logger.logDebug(AppStorage.class.toString(), "Attachment deleted successfully");
        } else {
            Logger.logDebug(AppStorage.class.toString(), "Attachment " +
                    file + " doesn't exist");
        }
    }
}
