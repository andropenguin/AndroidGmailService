package com.sarltokyo.androidgmailservice;

/**
 * Crypto.java
 *
 * Copyright (C) 2011
 * @author OSABE Satoshi, e-mail address: andropenguin@gmail.com
 * @version 1.0
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version with the exception of the file
 *  FlvDownloadServiceInterface.aidl. The license of the
 *  FlvDownloadServiceInterface.aidl is Public domain. About the
 *  exclamation of this license, see http://en.wikipedia.org/wiki/Public_domain.
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FlvDownlaodService.java; see the file COPYING.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 */


import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import android.util.Log;

public class Crypto {
	private static final String TAG = "Crypto";
	
	private static final byte[] IV =
		{ 23, -12, 90, 27, 56, 92, 0, 1, 54, 26, 88, -12, 34, 55, 39, 49};

	public static String encrypt(Key key, String data) throws Exception {
		try {
			Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
			c.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV));
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
			Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
			c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV));
			data = new String(c.doFinal(Base64.decode(encryptedData)));
			return data;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			throw new Exception();
		}
	}
}
