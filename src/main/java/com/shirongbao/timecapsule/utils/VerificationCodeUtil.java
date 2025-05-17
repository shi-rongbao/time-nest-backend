package com.shirongbao.timecapsule.utils;

import java.util.Random;

public class VerificationCodeUtil {

    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final Random RANDOM = new Random();

    /**
     * 生成指定位数的包含数字和字母的随机验证码。
     *
     * @param count 验证码的位数
     * @return 生成的随机验证码字符串
     * @throws IllegalArgumentException 如果 count 小于等于 0
     */
    public static String generateVerificationCode(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("验证码位数必须大于 0");
        }
        StringBuilder sb = new StringBuilder(count);
        int charactersLength = CHARACTERS.length();
        for (int i = 0; i < count; i++) {
            int randomIndex = RANDOM.nextInt(charactersLength);
            sb.append(CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

    /**
     * 将验证码字符串中的所有小写字母转换为大写字母，数字保持不变。
     *
     * @param verificationCode 要转换的验证码字符串
     * @return 转换后的全大写验证码字符串
     */
    public static String convertToUpperCase(String verificationCode) {
        if (verificationCode == null) {
            return null;
        }
        return verificationCode.toUpperCase();
    }

}