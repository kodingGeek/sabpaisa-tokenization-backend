package com.sabpaisa.tokenization.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@Slf4j
public class AesGcmEncryptionService {
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // in bits
    private static final int IV_LENGTH = 12; // in bytes
    private static final int SALT_LENGTH = 16; // in bytes
    
    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKey masterKey;
    
    public AesGcmEncryptionService(@Value("${app.encryption.master-key}") String masterKeyBase64) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(masterKeyBase64);
            this.masterKey = new SecretKeySpec(decodedKey, "AES");
            log.info("AES GCM encryption service initialized");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize encryption service", e);
        }
    }
    
    /**
     * Encrypts the given plaintext using AES-256-GCM
     * @param plaintext The data to encrypt
     * @param associatedData Optional additional authenticated data (AAD)
     * @return Base64 encoded encrypted data with IV and salt prepended
     */
    public String encrypt(String plaintext, String associatedData) {
        try {
            // Generate random IV
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            // Generate random salt for key derivation
            byte[] salt = new byte[SALT_LENGTH];
            secureRandom.nextBytes(salt);
            
            // Derive encryption key from master key and salt
            SecretKey encryptionKey = deriveKey(masterKey, salt);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, gcmSpec);
            
            // Add associated data if provided
            if (associatedData != null) {
                cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
            }
            
            // Encrypt the plaintext
            byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plaintextBytes);
            
            // Combine salt + iv + ciphertext
            ByteBuffer byteBuffer = ByteBuffer.allocate(salt.length + iv.length + ciphertext.length);
            byteBuffer.put(salt);
            byteBuffer.put(iv);
            byteBuffer.put(ciphertext);
            
            // Return Base64 encoded result
            return Base64.getEncoder().encodeToString(byteBuffer.array());
            
        } catch (Exception e) {
            log.error("Encryption failed: ", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    /**
     * Decrypts the given ciphertext using AES-256-GCM
     * @param encryptedData Base64 encoded encrypted data with IV and salt prepended
     * @param associatedData Optional additional authenticated data (AAD) - must match encryption AAD
     * @return Decrypted plaintext
     */
    public String decrypt(String encryptedData, String associatedData) {
        try {
            // Decode from Base64
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            
            // Extract components
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedData);
            
            byte[] salt = new byte[SALT_LENGTH];
            byteBuffer.get(salt);
            
            byte[] iv = new byte[IV_LENGTH];
            byteBuffer.get(iv);
            
            byte[] ciphertext = new byte[byteBuffer.remaining()];
            byteBuffer.get(ciphertext);
            
            // Derive decryption key from master key and salt
            SecretKey decryptionKey = deriveKey(masterKey, salt);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, decryptionKey, gcmSpec);
            
            // Add associated data if provided
            if (associatedData != null) {
                cipher.updateAAD(associatedData.getBytes(StandardCharsets.UTF_8));
            }
            
            // Decrypt
            byte[] plaintextBytes = cipher.doFinal(ciphertext);
            
            return new String(plaintextBytes, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Decryption failed: ", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
    
    /**
     * Encrypts an object after serializing it to JSON
     */
    public String encryptObject(Object object, String associatedData) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = mapper.writeValueAsString(object);
            return encrypt(json, associatedData);
        } catch (Exception e) {
            log.error("Object encryption failed: ", e);
            throw new RuntimeException("Object encryption failed", e);
        }
    }
    
    /**
     * Decrypts and deserializes an object from encrypted JSON
     */
    public <T> T decryptObject(String encryptedData, String associatedData, Class<T> clazz) {
        try {
            String json = decrypt(encryptedData, associatedData);
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Object decryption failed: ", e);
            throw new RuntimeException("Object decryption failed", e);
        }
    }
    
    /**
     * Derives a key from master key and salt using HKDF-like approach
     */
    private SecretKey deriveKey(SecretKey masterKey, byte[] salt) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec saltKey = new SecretKeySpec(salt, "HmacSHA256");
        mac.init(saltKey);
        byte[] derivedKeyBytes = mac.doFinal(masterKey.getEncoded());
        return new SecretKeySpec(derivedKeyBytes, "AES");
    }
    
    /**
     * Generates a new AES-256 key
     */
    public static String generateNewKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256, new SecureRandom());
            SecretKey key = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(key.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate key", e);
        }
    }
    
    /**
     * Validates encrypted data integrity without decrypting
     */
    public boolean validateEncryptedData(String encryptedData) {
        try {
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            // Check minimum length: salt + iv + tag
            return decodedData.length >= SALT_LENGTH + IV_LENGTH + (GCM_TAG_LENGTH / 8);
        } catch (Exception e) {
            return false;
        }
    }
}