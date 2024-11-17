package com.google.credentialmanager.sample;
import android.annotation.SuppressLint;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Base64;

public class EncryptionHelper {
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private static final String KEY_ALIAS = "NoteAppKey";
    private static final int IV_SIZE = 12;  // Initialization Vector (IV) size
    private static final int TAG_LENGTH = 128;

    public EncryptionHelper() throws Exception {
        if (!keyExists()) {
            generateKey();
        }
    }

    private void generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());
        keyGenerator.generateKey();
    }

    private boolean keyExists() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        return keyStore.containsAlias(KEY_ALIAS);
    }

    @SuppressLint("NewApi")
    public String encrypt(String plaintext) throws Exception {
        // Load the key from Android Keystore
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);

        // Initialize the cipher with AES-GCM mode and let it generate an IV
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        // Get the automatically generated IV
        byte[] iv = cipher.getIV();

        // Encrypt the plaintext
        byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // Concatenate IV and encrypted data
        byte[] ivAndEncryptedData = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, ivAndEncryptedData, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, ivAndEncryptedData, iv.length, encryptedBytes.length);

        // Encode the concatenated IV and encrypted data to Base64 for storage
        return Base64.getEncoder().encodeToString(ivAndEncryptedData);
    }

    @SuppressLint("NewApi")
    public String decrypt(String ciphertext) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);

        Cipher cipher = Cipher.getInstance(AES_MODE);
        byte[] ivAndEncryptedData = Base64.getDecoder().decode(ciphertext);
        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(ivAndEncryptedData, 0, iv, 0, IV_SIZE);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

        byte[] encryptedData = new byte[ivAndEncryptedData.length - IV_SIZE];
        System.arraycopy(ivAndEncryptedData, IV_SIZE, encryptedData, 0, encryptedData.length);

        return new String(cipher.doFinal(encryptedData));
    }
}
