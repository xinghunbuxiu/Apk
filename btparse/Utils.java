package com.bt.sdk;

/**
 * Created by LIXH on 2019/7/5.
 * email lixhVip9@163.com
 * des
 */
public class Utils {
    /**
     * Get hex-encoded representation of a binary array.
     *
     * @param bytes Binary data
     * @return String containing hex-encoded representation (lower case)
     * @since 1.3
     */
    public static String toHex(byte[] bytes) {
        if (bytes.length == 0) {
            throw new IllegalArgumentException("Empty array");
        }
        char[] chars = new char[bytes.length * 2];
        for (int i = 0, j = 0; i < bytes.length; i++, j = i * 2) {
            int b = bytes[i] & 0xFF;
            chars[j] = forHexDigit(b / 16);
            chars[j + 1] = forHexDigit(b % 16);
        }
        return new String(chars);
    }

    private static char forHexDigit(int b) {
        if (b < 0 || b >= 16) {
            throw new IllegalArgumentException("Illegal hexadecimal digit: " + b);
        }
        return (b < 10) ? (char) ('0' + b) : (char) ('a' + b - 10);
    }

    /**
     * Get binary data from its' hex-encoded representation (regardless of case).
     *
     * @param s Hex-encoded representation of binary data
     * @return Binary data
     * @since 1.3
     */
    public static byte[] fromHex(String s) {
        if (s.isEmpty() || s.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid string: " + s);
        }
        char[] chars = s.toCharArray();
        int len = chars.length / 2;
        byte[] bytes = new byte[len];
        for (int i = 0, j = 0; i < len; i++, j = i * 2) {
            bytes[i] = (byte) (hexDigit(chars[j]) * 16 + hexDigit(chars[j + 1]));
        }
        return bytes;
    }

    /**
     * Get 20-bytes long info hash from its' base32-encoded representation (regardless of case).
     *
     * @param s base32-encoded representation of info hash
     * @return Binary data
     * @throws IllegalArgumentException if {@code s.length()} is not 32 characters long
     * @since 1.8
     */
    public static byte[] infoHashFromBase32(String s) {
        if (s.isEmpty() || s.length() != 32) {
            throw new IllegalArgumentException("Invalid string: " + s);
        }
        String base32CodeBase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        StringBuilder hexCache = new StringBuilder();
        for (int i = 0; i < s.length(); i += 4) {
            int hexValue = base32CodeBase.indexOf(s.charAt(i)) << 15 |
                    base32CodeBase.indexOf(s.charAt(i + 1)) << 10 |
                    base32CodeBase.indexOf(s.charAt(i + 2)) << 5 |
                    base32CodeBase.indexOf(s.charAt(i + 3));
            hexCache.append(String.format("%05X", hexValue));
        }
        return fromHex(hexCache.toString());
    }

    private static int hexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - '0';
        } else if (c >= 'A' && c <= 'F') {
            return c - 'A' + 10;
        } else if (c >= 'a' && c <= 'f') {
            return c - 'a' + 10;
        }
        throw new IllegalArgumentException("Illegal hexadecimal character: " + c);
    }
}
