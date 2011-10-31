package com.sarltokyo.androidgmailservice;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import android.util.Log;

public class Crypto {
	private static final String TAG = "Crypto";

	public static Key makeKey(int key_bits) throws NoSuchAlgorithmException {
		Key key = null;
		KeyGenerator kg = KeyGenerator.getInstance("AES");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
		kg.init(key_bits, random);
		key = kg.generateKey();
		return key;
	}

	public static String encrypt(Key key, String data) throws Exception {
		try {
			Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
			c.init(Cipher.ENCRYPT_MODE, key);
			byte[] b = c.doFinal(data.getBytes());
			return Base64.encode(b);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			throw new Exception();
		}
	}

	public static String decrypt(Key key, String encryptedData) throws Exception {
		String data = null;
		try {
			Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
			c.init(Cipher.DECRYPT_MODE, key);
			data = new String(c.doFinal(Base64.decode(encryptedData)));
			return data;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			throw new Exception();
		}
	}
}
