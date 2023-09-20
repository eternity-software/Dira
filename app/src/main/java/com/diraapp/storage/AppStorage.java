package com.diraapp.storage;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Base64;

import com.diraapp.api.processors.UpdateProcessor;
import com.diraapp.utils.CacheUtils;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
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

    public static void downloadFile(String url, File outputFile, DownloadHandler downloadHandler) throws IOException {

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
                if (downloadHandler != null) {
                    if (System.currentTimeMillis() - lastTimeProgressNotified > 500) {
                        lastTimeProgressNotified = System.currentTimeMillis();
                        downloadHandler.onProgressChanged(((int) (total * 100 / fileLength)));
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
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

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
            System.out.println(path);
            File f = new File(path);
            return BitmapFactory.decodeStream(new FileInputStream(f));

        } catch (Exception e) {
            if (e instanceof NullPointerException) return null;
            e.printStackTrace();
            return null;
        }

    }

    public static Bitmap getBitmapFromBase64(String base64Str) throws IllegalArgumentException {
        byte[] decodedBytes = Base64.decode(
                base64Str.substring(base64Str.indexOf(",") + 1),
                Base64.DEFAULT
        );

        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static String getBase64FromBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }
}
