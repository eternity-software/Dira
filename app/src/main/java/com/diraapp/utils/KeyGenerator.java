package com.diraapp.utils;

import java.security.SecureRandom;

public class KeyGenerator {

    private static final int ROOM_SECRET_LENGTH = 64;
    private static final int MESSAGE_ID_LENGTH = 12;

    public static String generateId() {
        return generateString(new SecureRandom(), MESSAGE_ID_LENGTH);
    }

    public static String generateRoomSecret() {
        return generateString(new SecureRandom(), ROOM_SECRET_LENGTH);
    }

    public static String generateString(SecureRandom rng, int length) {
        String characters = "abcdefjhijklmnopqrst1234567890ABCDEFGHIJKLMNOPQRSTXYZ!@#$%^&*()_+";
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text) + "_" + System.currentTimeMillis();
    }

}
