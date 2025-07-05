package com.shirongbao.timenest.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author: ShiRongbao
 * @date: 2025-04-27
 * @description:
 */
public class SHA1Utils {
    /**
     * Generates a SHA1 hash from the provided parameters
     *
     * @param token     票据
     * @param timestamp 时间戳
     * @param nonce     随机字符串
     * @param encrypt   密文
     * @return 安全签名
     */
    public static String getSHA1(String token, String timestamp, String nonce, String encrypt) {
        try {
            // Sort the parameters alphabetically
            String[] array = {token, timestamp, nonce, encrypt};
            java.util.Arrays.sort(array);

            // Concatenate all parameters into one string
            StringBuilder content = new StringBuilder();
            for (String item : array) {
                content.append(item);
            }

            // Create SHA1 MessageDigest instance
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");

            // Compute the SHA1 hash
            byte[] digest = messageDigest.digest(content.toString().getBytes(StandardCharsets.UTF_8));

            // Convert the byte array to hexadecimal string
            return byteToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert byte array to hexadecimal string
     *
     * @param bytes byte array to convert
     * @return hexadecimal string
     */
    private static String byteToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Example usage of the SHA1 utility class
     */
    public static void main(String[] args) {
        String token = "your_token";
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = "random_nonce_string";
        String encrypt = "data_to_encrypt";

        String signature = getSHA1(token, timestamp, nonce, encrypt);
        System.out.println("Generated SHA1 signature: " + signature);
    }

}
