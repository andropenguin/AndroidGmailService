package com.sarltokyo.androidgmailservice;

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

public class SysPWSettingActivity extends Activity {

	private EditText syspwsettingPasswordEt;
	private EditText syspwsettingReenterpasswordEt;
	private Button syspwsettingSetBtn;
	private Button syspwsettingPwresettingBtn;
	private final static String TAG = "AndroidGmailActivity";
	private String syspw;
	private String reenteredpw;
	private Key syskey;
	private String encryptedSysPW;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.syspwsetting);

		syspwsettingPasswordEt = (EditText)findViewById(R.id.syspwsettingPasswordEt);
		syspwsettingReenterpasswordEt = (EditText)findViewById(R.id.syspwsettingReenterpasswordEt);
		syspwsettingSetBtn = (Button)findViewById(R.id.syspwsettingSetBtn);
		syspwsettingPwresettingBtn = (Button)findViewById(R.id.syspwsettingPwresettingBtn);
	}

	@Override
	public void onResume() {
		super.onResume();

		SharedPreferences pref = getSharedPreferences("SysPW", MODE_PRIVATE);
		encryptedSysPW = pref.getString("syspw", "");
		if (encryptedSysPW != null && encryptedSysPW.length() > 0) {
			syspwsettingPasswordEt.setEnabled(false);
			syspwsettingReenterpasswordEt.setEnabled(false);
			syspwsettingSetBtn.setEnabled(false);
			syspwsettingPwresettingBtn.setEnabled(true);
		} else {
			syspwsettingPasswordEt.setEnabled(true);
			syspwsettingReenterpasswordEt.setEnabled(true);
			syspwsettingSetBtn.setEnabled(true);
			syspwsettingPwresettingBtn.setEnabled(false);
		}

		syspwsettingSetBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				syspw = syspwsettingPasswordEt.getText().toString();
				reenteredpw = syspwsettingReenterpasswordEt.getText().toString();
				if (syspw != null && syspw.length() > 0
						&& reenteredpw != null && reenteredpw.length() > 0) {
					if (syspw.equals(reenteredpw)) {
						saveSysKey();
						syskey = getSysKey();
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
						Toast toast = Toast.makeText(SysPWSettingActivity.this, getString(R.string.system_password_set),
								Toast.LENGTH_LONG);
						toast.show();
						Intent intent = new Intent(SysPWSettingActivity.this, SysPWSettingActivity.class);
						startActivity(intent);
					} else {
						final AlertDialog dialog = new AlertDialog.Builder(SysPWSettingActivity.this)
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
					final AlertDialog dialog = new AlertDialog.Builder(SysPWSettingActivity.this)
					.setTitle(getString(R.string.error))
					.setMessage(getString(R.string.enter_system_password))
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

		syspwsettingPwresettingBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SysPWSettingActivity.this,
						SysPWResettingActivity.class);
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
