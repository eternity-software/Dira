package com.diraapp.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import se.simbio.encryption.Encryption;

public class EncryptionUtil {

    public static final String SALT = "DIRA_SALT_MOTHER";

    public static String encrypt(String rawString, String key)
    {
        byte[] iv = new byte[16];
        Encryption encryption = Encryption.getDefault(key, SALT, iv);
        try {
            return encryption.encrypt(rawString);
        } catch (Exception e) {
            return "";
        }
    }

    public static String decrypt(String encryptedString, String key)
    {
        byte[] iv = new byte[16];
        Encryption encryption = Encryption.getDefault(key, SALT, iv);
        try {
            return encryption.decrypt(encryptedString);
        } catch (Exception e) {
            return "";
        }
    }
}
