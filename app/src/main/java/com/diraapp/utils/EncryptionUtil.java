package com.diraapp.utils;

import se.simbio.encryption.Encryption;

public class EncryptionUtil {

    public static final String SALT = "DIRA_SALT_MOTHER";

    public static String encrypt(String rawString, String key) {
        byte[] iv = new byte[16];
        Encryption encryption = Encryption.getDefault(key, SALT, iv);
        try {
            return encryption.encrypt(rawString);
        } catch (Exception e) {
            return "";
        }
    }

    public static String decrypt(String encryptedString, String key) {
        byte[] iv = new byte[16];
        Encryption encryption = Encryption.getDefault(key, SALT, iv);
        try {
            return encryption.decrypt(encryptedString);
        } catch (Exception e) {
            return "";
        }
    }
}
