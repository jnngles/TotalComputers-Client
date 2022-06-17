package com.jnngl.client;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class Encryption {

    /* ============== RSA ============== */

    public PublicKey publicKey;

    public void decodePublicKeyRSA(byte[] encoded)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(encoded);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        publicKey = factory.generatePublic(encodedKeySpec);
    }

    public byte[] encryptRSA(byte[] data)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
                    IllegalBlockSizeException, BadPaddingException {
        Cipher encrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        encrypt.init(Cipher.ENCRYPT_MODE, publicKey);
        return encrypt.doFinal(data);
    }

    /* ============== AES ============== */

    public SecretKey aes;

    public void generateAES() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128);
        aes = generator.generateKey();
    }

    public byte[] encryptAES(byte[] data)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, aes, new IvParameterSpec(aes.getEncoded()));
        return cipher.doFinal(data);
    }

    public byte[] decryptAES(byte[] encrypted)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, aes, new IvParameterSpec(aes.getEncoded()));
        return cipher.doFinal(encrypted);
    }

}
