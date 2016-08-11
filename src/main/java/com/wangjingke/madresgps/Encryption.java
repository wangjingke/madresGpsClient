package com.wangjingke.madresgps;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;


public class Encryption {
    public static String encode(String input) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        String key = "USCmadresEMA2016";
        Key aesKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);

        // get rid of bundle content between {} to reduce output size
        byte[] encrypted = cipher.doFinal(input.replaceAll("\\{.+?\\}", "").getBytes("UTF-8"));
        String encryptedString = Base64.encodeToString(encrypted, Base64.NO_WRAP);
        return encryptedString;
    }
}
