package com.diraapp.storage;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.net.URLConnection;

public class FileClassifier {

    public static final String EXT_VIDEO = ".vid";
    public static final String EXT_IMAGE = ".img";
    public static final String EXT_UNKNOWN = ".ukwn";

    public static boolean isImageFile(String path) {
        try {
            String mimeType = URLConnection.guessContentTypeFromName(path);
            if (mimeType == null) return AppStorage.getBitmapFromPath(path) != null;
            return mimeType.startsWith("image");
        } catch (Exception e) {
            return false;
        }

    }

    public static String getMimeType(Uri uri, Context context) {
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public static boolean isDiraUnknownType(String path) {

        if (path.endsWith(EXT_UNKNOWN)) return true;


        return false;

    }

    public static boolean isVideoFile(String path, Context context) {
        try {


            String mimeType = getMimeType(Uri.fromFile(new File(path)), context);
            System.out.println(mimeType + " " + path);
            if (mimeType == null) if (path.endsWith(EXT_VIDEO)) return true;
            return mimeType != null && mimeType.startsWith("video");
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isVideoFile(String path) {
        try {

            if (path.endsWith(EXT_VIDEO)) return true;
            String mimeType = URLConnection.guessContentTypeFromName(path);
            System.out.println(mimeType + " " + path);
            return mimeType != null && mimeType.startsWith("video");
        } catch (Exception e) {
            return false;
        }
    }
}
