package com.sarltokyo.androidgmailservice;

/**
 * SysPWResettingActivity.java
 *
 * Copyright (C) 2012
 * @author OSABE Satoshi, e-mail address: andropenguin@gmail.com
 * @version 1.0.0
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SysPWResettingActivity extends Activity {
	private Button syspwresettingReturnBtn;
	private EditText syspwresettingOldpasswordEt;
	private EditText syspwresettingNewpasswordEt;
	private EditText syspwresettingReenternewpasswordEt;
	private Button syspwresettingSetBtn;

	private String inputOldSyspw;
	private String encryptedInputOldSyspw;
	private String encryptedOldSyspw;
	private String newSyspw;
	private String reenteredNewSyspw;
	private String syspw;
	private Key syskey;
	private String encryptedSysPW;

	private final static String TAG = "SysPWResettingActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.syspwresetting);

		syspwresettingReturnBtn = (Button)findViewById(R.id.syspwresettingReturnBtn);
		syspwresettingOldpasswordEt = (EditText)findViewById(R.id.syspwresettingOldpasswordEt);
		syspwresettingNewpasswordEt = (EditText)findViewById(R.id.syspwresettingNewpasswordEt);
		syspwresettingReenternewpasswordEt = (EditText)findViewById(R.id.syspwresettingReenternewpasswordEt);
		syspwresettingSetBtn = (Button)findViewById(R.id.syspwresettingSetBtn);
	}

	@Override
	public void onResume() {
		super.onResume();

		syspwresettingSetBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				inputOldSyspw = syspwresettingOldpasswordEt.getText().toString();
				if (inputOldSyspw == null || inputOldSyspw.length() == 0) {
					Toast toast = Toast.makeText(SysPWResettingActivity.this,
							getString(R.string.enter_system_password), Toast.LENGTH_LONG);
					toast.show();
					return;
				}

				// encription of input used system password
				try {
					encryptedInputOldSyspw = getEncryptedSysPW(getApplicationContext(), inputOldSyspw);
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
				}
				// get encrypted system password from SharedPrefeence
				SharedPreferences pref2 = getSharedPreferences("SysPW", MODE_PRIVATE);
				encryptedOldSyspw = pref2.getString("syspw", "");
				// check
				if (!encryptedInputOldSyspw.equals(encryptedOldSyspw)) {
					Toast toast = Toast.makeText(SysPWResettingActivity.this, getString(R.string.system_password_is_wrong), Toast.LENGTH_LONG);
					toast.show();
					return;
				}

				newSyspw = syspwresettingNewpasswordEt.getText().toString();
				reenteredNewSyspw = syspwresettingReenternewpasswordEt.getText().toString();

				if (newSyspw != null && newSyspw.length() > 0
						&& reenteredNewSyspw != null && reenteredNewSyspw.length() > 0) {
					if (newSyspw.equals(reenteredNewSyspw))	 {
						syspw = newSyspw;
						try {
							encryptedSysPW = getEncryptedSysPW(getApplicationContext(), syspw);
						} catch (Exception e) {
							Log.e(TAG, e.getMessage(), e);
							return;
						}
						SharedPreferences pref = getSharedPreferences("SysPW", MODE_PRIVATE);
						SharedPreferences.Editor editor = pref.edit();
						editor.putString("syspw", encryptedSysPW);
						editor.commit();
						Toast toast = Toast.makeText(SysPWResettingActivity.this, getString(R.string.system_password_set),
								Toast.LENGTH_LONG);
						toast.show();
						Intent intent = new Intent(SysPWResettingActivity.this, SysPWResettingActivity.class);
						startActivity(intent);
					} else {
						final AlertDialog dialog = new AlertDialog.Builder(SysPWResettingActivity.this)
						.setTitle(getString(R.string.error))
						.setMessage(getString(R.string.system_passwords_not_the_same))
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								return;
							}
						})
						.create();
					dialog.show();
					}
				} else {
					final AlertDialog dialog = new AlertDialog.Builder(SysPWResettingActivity.this)
					.setTitle(getString(R.string.error))
					.setMessage(getString(R.string.enter_new_system_password))
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					})
					.create();
				dialog.show();
				}
			}
		});

		syspwresettingReturnBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SysPWResettingActivity.this,
						SysPWSettingActivity.class);
				startActivity(intent);
			}
		});
	}

    public String getEncryptedSysPW(Context context, String syspw) throws Exception {
    	syskey = generateKey(context);
    	encryptedSysPW = Crypto.encrypt(syskey, syspw);
    	return encryptedSysPW;
    }
    
    public char[] generatePassword(Context context) {
    	String orignal_id = getOriginalID();
    	
    	// get the package name of the application
    	String packageName = context.getPackageName();
    	
    	// generate the password for Key
    	String password = orignal_id + packageName;
    	
    	return password.toCharArray();
    }
    
    private static final byte[] SALT = new byte[] {
    	12, -32, 124, 4, 19, 45, -93, -23, 45, 23,
    	3, 37, -10, 114, 14, 56, 23, 85, 29, 64
    };

    public SecretKey generateKey(Context context)
    		throws NoSuchAlgorithmException, InvalidKeySpecException {
    	// get password to generate Key
    	char[] password = generatePassword(context);
    	
    	// generate Key
    	KeySpec keySpec = new PBEKeySpec(password, SALT, 1024, 256);
    	SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWITHSHAAND256BITAES-CBC-BC");
    	SecretKey secretKey = factory.generateSecret(keySpec);
    	
    	return secretKey;
    	
    }
    
    public String getOriginalID() {
    	SharedPreferences pref = getSharedPreferences("id", MODE_PRIVATE);
    	String original_id = pref.getString("original_id", "");
    	if (original_id.length() == 0) {
    		String android_id = Secure.getString(getContentResolver(),
    				Secure.ANDROID_ID);
    		long now = (long)(new Date().getTime() / 1000);
    		original_id = createDigest(android_id + "_" + now);
    		pref.edit().putString("original_id", original_id).commit();
    	}
    	return original_id;
    }
    
    public String createDigest(String source) {
    	try {
    		MessageDigest md = MessageDigest.getInstance("MD5");

    		byte[] data = source.getBytes();
    		md.update(data);

    		byte[] digest = md.digest();

    		StringBuilder sb = new StringBuilder();
    		for (int i = 0; i < digest.length; i++) {
    			int b = 0xFF & digest[i];
    			if (b < 16) {
    				sb.append("0");
    			}
    			sb.append(Integer.toHexString(b));
    		}
    		return sb.toString();
    	} catch (NoSuchAlgorithmException e) {
    		Log.e(TAG, e.getMessage(), e);
    	}
    	return "";
    }
}


