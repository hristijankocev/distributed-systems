package mk.ukim.finki.lab_01.ccmp;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class AES {
    private static SecretKeySpec secretKey;

    public static void setKey(String myKey) {
        MessageDigest sha;
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static byte[] encrypt(String message, String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

            byte[] paddedMessage = getPaddedMessage(messageBytes);

            return cipher.doFinal(paddedMessage);
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e);
        }
        return null;
    }

    public static byte[] decrypt(byte[] strToDecrypt, String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(strToDecrypt);
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e);
        }
        return null;
    }

    private static byte[] getPaddedMessage(byte[] messageBytes) {
        // Pad the message to make it a multiple of 16 bytes
        int blockSize = 16;
        int paddingLength = blockSize - (messageBytes.length % blockSize);
        byte[] paddedMessage = new byte[messageBytes.length + paddingLength];
        System.arraycopy(messageBytes, 0, paddedMessage, 0, messageBytes.length);
        return paddedMessage;
    }
}