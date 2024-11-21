package com.google.credentialmanager.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class EncryptionHelper {
    private static final String AES_MODE = "AES/ECB/PKCS5Padding"; // Thay đổi chế độ mã hóa sang ECB với padding
    private static final int KEY_SIZE = 128; // Độ dài khóa AES
    private static final String PREFS_NAME = "secure_prefs"; // Tên file SharedPreferences
    private static final String AES_KEY = "aes_key"; // Key để lưu trữ khóa AES trong SharedPreferences


    public static String generateSecretKey() throws Exception {

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(KEY_SIZE); // Đặt kích thước khóa (128-bit)

        // Sinh khóa AES
        SecretKey secretKey = keyGen.generateKey();

        // Mã hóa khóa thành chuỗi Base64 để dễ lưu trữ hoặc truyền đi
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
    public static String getOrCreateKey(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = sharedPreferences.getString(AES_KEY, null); // Lấy khóa từ SharedPreferences

        if (key == null) { // Nếu khóa không tồn tại
            try {
                key = generateSecretKey(); // Tạo khóa mới
                sharedPreferences.edit().putString(AES_KEY, key).apply(); // Lưu khóa vào SharedPreferences
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return key;
    }

    public static String encrypt(String plainText, String base64SecretKey) throws Exception {
        // Giải mã khóa từ Base64 thành dạng nhị phân
        byte[] decodedKey = Base64.getDecoder().decode(base64SecretKey);

        // Tạo SecretKey từ dữ liệu nhị phân
        SecretKeySpec secretKey = new SecretKeySpec(decodedKey, "AES");

        // Thiết lập Cipher ở chế độ mã hóa
        @SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // Mã hóa dữ liệu
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());

        // Chuyển dữ liệu mã hóa thành Base64
        return Base64.getEncoder().encodeToString(encryptedBytes);

    }


    public static String decrypt(String encryptedText, String base64SecretKey) throws Exception {
        try {
            // Giải mã khóa từ Base64 thành dạng nhị phân
            byte[] decodedKey = Base64.getDecoder().decode(base64SecretKey);

            // Tạo SecretKey từ dữ liệu nhị phân
            SecretKeySpec secretKey = new SecretKeySpec(decodedKey, "AES");

            // Thiết lập Cipher ở chế độ giải mã
            @SuppressLint("GetInstance") Cipher cipher = Cipher.getInstance(AES_MODE);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // Giải mã Base64 thành nhị phân
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);

            // Giải mã bằng AES
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);

            // Chuyển byte thành chuỗi văn bản gốc
            return new String(decryptedBytes);
        } catch (NoSuchAlgorithmException e) {
            Log.e("MyTag", "Error during decryption", e);
            throw e;
        }

    }

}
