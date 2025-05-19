package com.shirongbao.timecapsule.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.security.SecureRandom;

/**
 * @author: ShiRongbao
 * @date: 2025-05-15
 * @description: 安全工具类，用于加密密码和加密，解密手机号。
 */
public class SecurityUtil {

    private static final String PASSWORD_HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16; // 盐值的长度，单位为字节
    private static final String PHONE_ENCRYPTION_ALGORITHM = "AES";
    // 请务必更换成你自己的 16/24/32 字节密钥
    private static final String PHONE_ENCRYPTION_KEY_STRING = "a1b2c3d4e5f678901234567890abcdef";

    /**
     * 生成随机盐值。
     *
     * @return Base64 编码的随机盐值字符串
     */
    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 使用 SHA-256 对密码进行加盐哈希。
     *
     * @param password 原始密码
     * @return 包含盐值和哈希值的字符串，格式为：salt$hash
     */
    public static String encryptPassword(String password) {
        String salt = generateSalt();
        String saltedPassword = salt + password;
        try {
            MessageDigest digest = MessageDigest.getInstance(PASSWORD_HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            String hash = bytesToHex(hashBytes);
            return salt + "$" + hash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("不支持的哈希算法：" + PASSWORD_HASH_ALGORITHM, e);
        }
    }

    /**
     * 验证原始密码是否与存储的加盐哈希匹配。
     *
     * @param rawPassword  用户输入的原始密码
     * @param storedHash   数据库中存储的哈希值
     * @param storedSalt   数据库中存储的盐值
     * @return 如果密码匹配则返回 true，否则返回 false
     */
    public static boolean verifyPassword(String rawPassword, String storedHash, String storedSalt) {
        String saltedPassword = storedSalt + rawPassword;
        try {
            MessageDigest digest = MessageDigest.getInstance(PASSWORD_HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            String computedHash = bytesToHex(hashBytes);
            return computedHash.equals(storedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("不支持的哈希算法：" + PASSWORD_HASH_ALGORITHM, e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
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
     * 加密手机号。
     *
     * @param phoneNumber 原始手机号码
     * @return 加密后的 Base64 编码字符串
     */
    public static String encryptPhoneNumber(String phoneNumber) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(PHONE_ENCRYPTION_KEY_STRING.getBytes(StandardCharsets.UTF_8), PHONE_ENCRYPTION_ALGORITHM);
            Cipher cipher = Cipher.getInstance(PHONE_ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedBytes = cipher.doFinal(phoneNumber.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("加密手机号失败", e);
        }
    }

    /**
     * 解密手机号。
     *
     * @param encryptedPhoneNumber 加密后的 Base64 编码字符串
     * @return 原始手机号码
     */
    public static String decryptPhoneNumber(String encryptedPhoneNumber) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedPhoneNumber);
            SecretKeySpec keySpec = new SecretKeySpec(PHONE_ENCRYPTION_KEY_STRING.getBytes(StandardCharsets.UTF_8), PHONE_ENCRYPTION_ALGORITHM);
            Cipher cipher = Cipher.getInstance(PHONE_ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("解密手机号失败", e);
        }
    }
}