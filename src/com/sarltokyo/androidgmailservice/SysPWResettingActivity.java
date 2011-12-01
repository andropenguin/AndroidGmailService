package com.sarltokyo.androidgmailservice;

/**
 * SysPWResettingActivity.java
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
				syskey = getSysKey();
				try {
					encryptedInputOldSyspw = Crypto.encrypt(syskey, inputOldSyspw);
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
						saveSysKey();
						syskey = getSysKey();
						syspw = newSyspw;
						try {
							encryptedSysPW = getEncryptedSysPW(syspw);
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

	public void saveSysKey() {
		InputStream in = null;
		OutputStream out = null;
		boolean existFlag = true;

		try {
			in = openFileInput(AndroidGmailConstant.SYSKEY_FILE);
		} catch (FileNotFoundException e) {
			existFlag = false;
			Log.e(TAG, e.getMessage(), e);
		}

		try {
			if (in != null) in.close();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}

		if (!existFlag) {
			try {
				syskey = Crypto.makeKey(128);
			} catch (NoSuchAlgorithmException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			try {
				out = openFileOutput(AndroidGmailConstant.SYSKEY_FILE, MODE_PRIVATE);
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage(), e);
			}
			try {
				ObjectOutputStream oos = new ObjectOutputStream(out);
				oos.writeObject(syskey);
				oos.flush();
				oos.close();
				out.close();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				try {
					if (out != null) out.close();
				} catch (IOException e2) {
					Log.e(TAG, e2.getMessage(), e2);
				}
			}
		}
	}

    public Key getSysKey() {
    	InputStream in = null;

    	try {
    		in = openFileInput(AndroidGmailConstant.SYSKEY_FILE);
    	} catch (FileNotFoundException e) {
    		Log.e(TAG, e.getMessage(), e);
    	}
    	try {
    		ObjectInputStream ois = new ObjectInputStream(in);
    		syskey = (Key)ois.readObject();
    		ois.close();
    		in.close();
    	} catch (IOException e) {
    		Log.e(TAG, e.getMessage(), e);
    		try {
    			if (in != null) in.close();
    		} catch (IOException e2) {
    			Log.e(TAG, e2.getMessage(), e2);
    		}
    	} catch (ClassNotFoundException e) {
    		Log.e(TAG, e.getMessage(), e);
    		try {
    			if (in != null) in.close();
    		} catch (IOException e2) {
    			Log.e(TAG, e2.getMessage(), e2);
    		}
    	}
    	return syskey;
    }

    public String getEncryptedSysPW(String syspw) throws Exception {
    	syskey = getSysKey();
    	encryptedSysPW = Crypto.encrypt(syskey, syspw);
    	return encryptedSysPW;
    }
}


