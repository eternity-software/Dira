package ru.dira.storage;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.dira.db.entities.Attachment;
import ru.dira.storage.attachments.SaveAttachmentTask;
import ru.dira.utils.CacheUtils;

public class AppStorage {

    public static final String IMAGE_DIR = "diraFiles";
    public static final String OFFICIAL_DOWNLOAD_STORAGE_ADDRESS = "http://164.132.138.80:4444/download/";
    public static final String OFFICIAL_UPLOAD_STORAGE_ADDRESS = "http://164.132.138.80:4444/upload/";
    public static final long MAX_DEFAULT_ATTACHMENT_AUTOLOAD_SIZE = 1024 * 1024 * 20; // 20 mb




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



    public static File getFileFromAttachment(Attachment attachment, Context context, String roomSecret) {
        File localFile = new File(context.getExternalCacheDir(), roomSecret + "_" + attachment.getFileUrl());

        if (!localFile.exists()) return null;

        return localFile;
    }


    public static void downloadFile(String url, File outputFile, DownloadHandler downloadHandler) {
        try {
            System.out.println(url);
            URL u = new URL(url);
            URLConnection conn = u.openConnection();





            int fileLength = conn.getContentLength();
            InputStream input = null;
            OutputStream output = null;
            // download the file
            input = conn.getInputStream();
            output = new FileOutputStream(outputFile);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button

                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                {
                    if(downloadHandler != null)
                    {
                        downloadHandler.onProgressChanged(((int) (total * 100 / fileLength)));
                    }

                }

                output.write(data, 0, count);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return; // swallow a 404
        } catch (IOException e) {
            e.printStackTrace();
            return; // swallow a 404
        }
    }

    public static Bitmap getBitmap(String url) {
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
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(IMAGE_DIR, Context.MODE_PRIVATE);

        if(roomSecret == null)
        {
            directory = cw.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        }
        else
        {
            File file = new File(directory, roomSecret);
            file.mkdirs();
            directory = file;
        }

        // Create imageDir
        String fileName = System.currentTimeMillis() + name + ".png";
        File imagePath = new File(directory, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imagePath);
            // Use the compress method on the BitMap object to write image to the OutputStream
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

    public static Bitmap getImage(String path) {
        try {
            System.out.println(path);
            File f = new File(path);
            return BitmapFactory.decodeStream(new FileInputStream(f));

        } catch (Exception e) {
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
