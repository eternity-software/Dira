package com.diraapp.storage;

import java.net.URLConnection;

public class FileClassifier {
    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        if (mimeType == null) return AppStorage.getBitmapFromPath(path) != null;
        return mimeType.startsWith("image");
    }

    public static boolean isVideoFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("video");
    }
}
