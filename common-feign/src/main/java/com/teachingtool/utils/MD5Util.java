package com.teachingtool.utils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    /**
     * Performs MD5 encryption on plaintext strings
     */
    public static String encode(String source) {

        // Determine if a plaintext string is valid
        if (source == null || "".equals(source)) {
            throw new RuntimeException("The plaintext used for encryption cannot be empty");
        }

        String algorithm = "md5";
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Get the byte array corresponding to the plaintext string
        byte[] input = source.getBytes();

        // Enforce encryption
        byte[] output = messageDigest.digest(input);

        // Creating a BigInteger object
        int signum = 1;
        BigInteger bigInteger = new BigInteger(signum, output);

        // Convert a bigInteger value to a string in hexadecimal.
        int radix = 16;
        String encoded = bigInteger.toString(radix).toUpperCase();

        return encoded;
    }
}
