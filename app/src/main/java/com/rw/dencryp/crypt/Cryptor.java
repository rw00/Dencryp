package com.rw.dencryp.crypt;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Cryptor {
    public static final String CIPHER_ALGORITHM = "AES";
    public static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String CHARSET = "UTF-8";
    private static final String TAG = Cryptor.class.getSimpleName();

    private SecretKeySpec keySpec;
    private IvParameterSpec ivSpec;

    public Cryptor() {
        this(generateSecretKey(), generateInitVector());
    }

    public Cryptor(String secretKey, String initVector) {
        this(getStringBytes(secretKey), getStringBytes(initVector));
    }

    public Cryptor(byte[] secretKey, byte[] initVector) {
        keySpec = new SecretKeySpec(Arrays.copyOf(secretKey, 16), CIPHER_ALGORITHM);
        ivSpec = new IvParameterSpec(Arrays.copyOf(initVector, 16));
    }

    private static byte[] getStringBytes(String s) {
        try {
            return s.getBytes(CHARSET);
        } catch (UnsupportedEncodingException ex) {
            Log.d(TAG, ex.getMessage(), ex);
            return null;
        }
    }

    public static byte[] generateSecretKey() {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance(CIPHER_ALGORITHM);
            keygen.init(128);
            return keygen.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException ex) {
            Log.d(TAG, ex.getMessage(), ex);
            return null;
        }
    }

    public static byte[] generateInitVector() {
        SecureRandom random = new SecureRandom();
        byte initVector[] = new byte[16];
        random.nextBytes(initVector);
        return initVector;
    }

    public String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(value.getBytes(CHARSET));

            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception ex) {
            // ex.printStackTrace();
            return null;
        }
    }

    public String decrypt(String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] data = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT));

            return new String(data);
        } catch (Exception ex) {
            // ex.printStackTrace();
            return null;
        }
    }

    // public static Object convertByteArrayToObject(byte[] objectBytes) throws IOException, ClassNotFoundException {
    // ByteArrayInputStream bytesInStream = new ByteArrayInputStream(objectBytes);
    // ObjectInputStream in = new ObjectInputStream(bytesInStream);
    // Object object = in.readObject();
    // in.close();
    // return object;
    // }
    //
    // private static byte[] convertObjectToByteArray(Object objectData) throws IOException {
    // ByteArrayOutputStream bytesOutStream = new ByteArrayOutputStream();
    // ObjectOutputStream out = new ObjectOutputStream(bytesOutStream);
    // out.writeObject(objectData);
    // out.close();
    // return bytesOutStream.toByteArray();
    // }
}
