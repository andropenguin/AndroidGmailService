package com.sarltokyo.androidgmailservice;

/**
 * AndroidGmailService.java
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

//import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.util.Log;

public class AndroidGmailService extends Service {

	private static final String TAG = "AndroidGmailService";
	private String user;
	private SecretKey key;
	private String encryptedPW;
	private String password;
	private SecretKey syskey;

	private static final String HOST = "smtp.gmail.com";
	private static final String PROTOCOL = "imaps";
	private Message[] m;
	private AndroidGmailSender s;
	private AndroidGmailRetriever r;
	private static final String PREF_TMP = "AndroidGmailServiceTmp";
	private static final String PREF = "AndroidGmailService";

	private static final int SYSPW_OK = 0;
	private static final int RANDOM_OK = 1;
	private static final int SYSPW_ERROR = -1;
	private static final int RANDOM_ERROR = -2;


	@Override
	public IBinder onBind(Intent intent) {
		return mInterfaceImpl;
	}

	private AndroidGmailServiceInterface.Stub mInterfaceImpl =
		new AndroidGmailServiceInterface.Stub() {

		@Override
		public int sendSetting(String syspw, int index) throws RemoteException {
			int msgNumber = sendSettingConc(syspw, index);
			return msgNumber;
		}

		@Override
		public int send1(String to, String from, String subject, String body, long random, int index)
		throws RemoteException {
			int returnCode = verifyRandom(random, index);
			if (returnCode < 0)
				return returnCode;
			int errorCode = readPreferenceConc(index);
			if (errorCode < 0) {
				return errorCode;
			}
			int msgNumber = sendMail(AndroidGmailService.this.user, AndroidGmailService.this.password, new String[]{to}, from, subject, body, index);
			return msgNumber;
		}

		@Override
		public int send2(String[] to, String from, String subject, String body, long random, int index)
		throws RemoteException {
			int returnCode = verifyRandom(random, index);
			if (returnCode < 0)
				return returnCode;
			int errorCode = readPreferenceConc(index);
			if (errorCode < 0) {
				return errorCode;
			}
			int msgNumber = sendMail(AndroidGmailService.this.user, AndroidGmailService.this.password, to, from, subject, body, index);
			return msgNumber;
		}

		@Override
		public int send3(String to, String from, String subject, String body,
				String filename, long random, int index) throws RemoteException {
			int returnCode = verifyRandom(random, index);
			if (returnCode < 0)
				return returnCode;
			int errorCode = readPreferenceConc(index);
			if (errorCode < 0) {
				return errorCode;
			}
			int msgNumber = sendMailWithFile(AndroidGmailService.this.user, AndroidGmailService.this.password, new String[]{to}, from, subject, body, new String[]{filename}, index);
			return msgNumber;
		}

		@Override
		public int send4(String[] to, String from, String subject, String body,
				String filename, long random, int index) throws RemoteException {
			int returnCode = verifyRandom(random, index);
			if (returnCode < 0)
				return returnCode;
			int errorCode = readPreferenceConc(index);
			if (errorCode < 0) {
				return errorCode;
			}
			int msgNumber = sendMailWithFile(AndroidGmailService.this.user, AndroidGmailService.this.password, to, from, subject, body, new String[]{filename}, index);
			return msgNumber;
		}

		@Override
		public int send5(String[] to, String from, String subject, String body,
				String[] filename, long random, int index) throws RemoteException {
			int returnCode = verifyRandom(random, index);
			if (returnCode < 0)
				return returnCode;
			int errorCode = readPreferenceConc(index);
			if (errorCode < 0) {
				return errorCode;
			}
			int msgNumber = sendMailWithFile(AndroidGmailService.this.user, AndroidGmailService.this.password, to, from, subject, body, filename, index);
			return msgNumber;
		}


		@Override
		public int retrieve1(int msgNumber, long random, int index) throws RemoteException {
			int returnCode = verifyRandom(random, index);
			if (returnCode < 0)
				return returnCode;
			int errorCode = readPreferenceConc(index);
			if (errorCode < 0) {
				return errorCode;
			}
			return retrieveConc1(msgNumber, index);
		}

		@Override
		public int retrieve2(int msgNumber, long random, int index, String[] message) throws RemoteException {
			int returnCode = verifyRandom(random, index);
			if (returnCode < 0)
				return returnCode;
			int errorCode = readPreferenceConc(index);
			if (errorCode < 0) {
				return errorCode;
			}
			return retrieveConc2(msgNumber, index, message);
		}

		@Override
		public int delete(int msgNumber, long random, int index) throws RemoteException {
			int returnCode = verifyRandom(random, index);
			if (returnCode < 0)
				return returnCode;
			int errorCode = readPreferenceConc(index);
			if (errorCode < 0) {
				return errorCode;
			}
			return deleteConc(msgNumber, index);
		}

		@Override
		public int writeTmpPreferences(String user, String password)
		throws RemoteException {
			// get SharedPreferences object
			SharedPreferences pref = getSharedPreferences(
					PREF_TMP, MODE_PRIVATE);
			// write to Preferences
			SharedPreferences.Editor editor = pref.edit();
			editor.putString("user", user);
			editor.putString("password", password);
			editor.commit();
			return 0;
		}

		@Override
		public int writePreferences(String user, String password, long random, int index)
		throws RemoteException {
//			OutputStream out = null;
			
			SecretKey key = null;
			try {
				key = generateKey(getApplicationContext(), index);
			} catch (InvalidKeySpecException e) {
				Log.e(TAG, e.getMessage(), e);
				return AndroidGmailBase.ERROR_CODE_INVALIDKEYEXCEPTION;
			} catch (NoSuchAlgorithmException e) {
				Log.e(TAG, e.getMessage(), e);
				return AndroidGmailBase.ERROR_CODE_NOSUCHALGOLITHMEXCEPTION;
			}

			try {
				AndroidGmailService.this.encryptedPW = Crypto.encrypt(key, password);
			} catch(Exception e) {
				Log.e(TAG, e.getMessage(), e);
				return AndroidGmailBase.ERROR_CODE_EXCEPTION;
			}

			// get SharedPreferences object
			SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);
			// write to Preferences
			SharedPreferences.Editor editor = pref.edit();
			editor.putString("user" + index, user);
			editor.putString("encryptedPW" + index, AndroidGmailService.this.encryptedPW);
			editor.putString("random" + index, Long.toString(random));
			editor.commit();

			return 0;
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "onDestroy");
	}

	private void readTmpPreferences() {
		// get SharedPreferences object
		SharedPreferences pref = getSharedPreferences(PREF_TMP, MODE_PRIVATE);
		// read preferences
		this.user = pref.getString("user", "");
		this.password = pref.getString("password", "");
	}

	private int readPreferenceConc(int index) {
		
		try {
			key = generateKey(getApplicationContext(), index);
		} catch (InvalidKeySpecException e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_INVALIDKEYEXCEPTION;
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_NOSUCHALGOLITHMEXCEPTION;
		}

		// get SharedPreferences object
		SharedPreferences pref = getSharedPreferences(PREF, MODE_PRIVATE);

		// read preferences
		this.user = pref.getString("user" + index, "");
		this.encryptedPW = pref.getString("encryptedPW" + index, "");

		try {
			this.password = Crypto.decrypt(this.key, this.encryptedPW);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_EXCEPTION;
		}

		return 0;
	}

	private void removeTmpPreferences() {
		SharedPreferences pref = getSharedPreferences(PREF_TMP, MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.remove("user");
		editor.remove("password");
		editor.commit();
	}

	private int sendSettingConc(String syspw, int index) {
		String givenEncryptedSyspw = "";
		// encrypt the give system password
		try {
			givenEncryptedSyspw = getEncryptedSysPW(getApplicationContext(), syspw);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_EXCEPTION;
		}

		// read the encryped system password from SharedPreference
		SharedPreferences pref = getSharedPreferences("SysPW", MODE_PRIVATE);
		String savedEncryptedSysPW = pref.getString("syspw", "");

		// compare
		if (givenEncryptedSyspw.length() == 0 ||
				savedEncryptedSysPW.length() == 0 ||
				!givenEncryptedSyspw.equals(savedEncryptedSysPW)) {
			return AndroidGmailBase.ERROR_CODE_SYSTEMPW_NOT_SAME;
		}

		readTmpPreferences();

		String from = user + "@gmail.com";
		String to = user + "@gmail.com";

		Log.i(TAG, "AndroidGmailSender");

		String subject = "account setting test";
		String body = subject;

		int msgNumber = sendMail(user, password, new String[]{to}, from, subject, body, index);

		removeTmpPreferences();

		return msgNumber;

	}

	private int sendMail(String user, String password, String[] to, String from, String subject, String body, int index) {

		// read number of messages in mailbox
		int msgNumberPre = getMessageNumber(user, password, index);
		if (msgNumberPre < 0) {
			// Exception
			return msgNumberPre;
		}

		Log.i(TAG, "sendMail");

		try {
			this.s = new AndroidGmailSender(user, password);
		} catch (NoSuchProviderException e) {
			Log.e(TAG, "user or password is wrong.");
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_USER_OR_PASSWORD_WRONG;
		}

		MimeMessage msg = s.createMessage();
		try {
			Log.i(TAG, "setHeaders");
			AndroidGmailSender.setHeaders(msg, to, from);
		} catch (AddressException e) {
			Log.e(TAG, "address is wrong.");
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_ADDRESS_WRONG;
		} catch (MessagingException e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		try {
			Log.i(TAG, "setSubject");
			msg.setSubject(subject);
		} catch (MessagingException e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		Log.i(TAG, "setContents");
		try {
			msg.setText(body, "ISO-2022-JP");  // only Japanese! fix me
		} catch (MessagingException e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		try {
			Log.i(TAG, "saveChanges");
			msg.saveChanges();
		} catch (MessagingException e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		try {
			Log.i(TAG, "connect");
			this.s.connect(HOST);
		} catch (MessagingException e) {
			Log.e(TAG, "cannot connect to " + HOST);
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_CONNECTION_FAIL;
		}

		try {
			Log.i(TAG, "send");
			this.s.send(msg);
		} catch (MessagingException e) {
			Log.e(TAG, "cannot send mail.");
			Log.e(TAG, e.getMessage(), e);
			this.s.disconnect();
			return AndroidGmailBase.ERROR_CODE_SEND_FAIL;
		}

		this.s.disconnect();

		// read number of messages in mailbox
		int msgNumber = getMessageNumber(user, password, index);

		if (msgNumber < 0) {
			// can send mail, but cannot read number of messages
			return msgNumberPre + 1;
		} else {
			// can read number of messages
			return msgNumber;
		}
	}


	private int sendMailWithFile(String user, String password, String[] to, String from, String subject, String body, String[] filename, int index) {

		// read number of messages in mailbox
		int msgNumberPre = getMessageNumber(user, password, index);

		if (msgNumberPre < 0) {
			// Exception
			return msgNumberPre;
		}
		
		try {
			this.s = new AndroidGmailSender(user, password);
		} catch (NoSuchProviderException e) {
			Log.e(TAG, "user or password is wrong");
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_USER_OR_PASSWORD_WRONG;
		}

		Log.i(TAG, "sendMailWithFile");

		MimeMessage msg = this.s.createMessage();
		try {
			Log.i(TAG, "setHeaders");
			AndroidGmailSender.setHeaders(msg, to, from);
		} catch (AddressException e) {
			Log.e(TAG, "address is wrong");
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_ADDRESS_WRONG;
		} catch (MessagingException e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		try {
			Log.i(TAG, "setSubject");
			msg.setSubject(subject);
		} catch (MessagingException e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		try {
			Log.i(TAG, "setContents");
			msg.setContent(this.s.createAttachment(body, filename));
		} catch (IOException e) {
			Log.e(TAG, "file does not exist or cannot attach file.");
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_ATTACHEMENT_FAIL;
		} catch (MessagingException e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		try {
			Log.i(TAG, "saveChages");
			msg.saveChanges();
		} catch (MessagingException e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		try {
			Log.i(TAG, "connect");
			s.connect(HOST);
		} catch (MessagingException e) {
			Log.e(TAG, "cannot connect to " + HOST);
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_CONNECTION_FAIL;
		}

		try {
			Log.i(TAG, "send");
			s.send(msg);
		} catch (MessagingException e) {
			Log.e(TAG, "cannot send mail.");
			Log.e(TAG, e.getMessage(), e);
			s.disconnect();
			return AndroidGmailBase.ERROR_CODE_CONNECTION_FAIL;
		}

		this.s.disconnect();

		// read number of messages in mailbox
		int msgNumber = getMessageNumber(user, password, index);

		if (msgNumber < 0) {
			// can send mail, but cannot read number of messages
			return msgNumberPre + 1;
		} else {
			// can read number of messages
			return msgNumber;
		}
	}

	private int retrieveConc1(int msgNumber, int index) {

		try {
			this.r = new AndroidGmailRetriever(PROTOCOL);
		} catch (NoSuchProviderException e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_NOSUCHPROVIDEREXCEPTION;
		}

		try {
			this.r.connect(HOST, this.user, this.password);
		} catch (MessagingException e) {
			Log.e(TAG, "user or password is wrong.");
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_USER_OR_PASSWORD_WRONG;
		}

		try {
			this.m = this.r.get();
		} catch (MessagingException e) {
			Log.e(TAG, e.getMessage(), e);
			try {
				this.r.disconnect();
			} catch (MessagingException e2) {
				Log.e(TAG, e2.getMessage(), e2);
				return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
			}
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		// extract the attached file.
		try {
			AndroidGmailRetriever.dumpPart(this.m[msgNumber], false, true);
		} catch (IndexOutOfBoundsException e) {
			Log.e(TAG, "no such message number exists");
			Log.e(TAG, e.getMessage(), e);
			try {
				this.r.disconnect();
			} catch (MessagingException e2) {
				Log.e(TAG, e2.getMessage(), e2);
				return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			try {
				this.r.disconnect();
			} catch (MessagingException e2) {
				Log.e(TAG, e2.getMessage(), e2);
				return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
			}
		}

		Log.i(TAG, "file downloaded");

		try {
			this.r.disconnect();
		} catch (MessagingException e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		return 0;
	}


	private int retrieveConc2(int msgNumber, int index, String[] message) {

		try {
			this.r = new AndroidGmailRetriever(PROTOCOL);
		} catch (NoSuchProviderException e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_NOSUCHPROVIDEREXCEPTION;
		}

		try {
			this.r.connect(HOST, this.user, this.password);
		} catch (MessagingException e) {
			Log.e(TAG, "user or password is wrong.");
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_USER_OR_PASSWORD_WRONG;
		}

		try {
			this.m = this.r.get();
		} catch (MessagingException e) {
			Log.e(TAG, e.getMessage(), e);
			try {
				this.r.disconnect();
			} catch (MessagingException e2) {
				Log.e(TAG, e2.getMessage(), e2);
				return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
			}
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		try {
			message[0] = m[msgNumber].getSubject();
			message[1] = ((InternetAddress)m[msgNumber].getFrom()[0]).getAddress();
			// fix me: extract body from message
		} catch (MessagingException e) {
			Log.e(TAG, e.getMessage(), e);
			try {
				this.r.disconnect();
			} catch (MessagingException e2) {
				Log.e(TAG, e2.getMessage(), e2);
				return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
			}
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		// extract the attached file.
		try {
			AndroidGmailRetriever.dumpPart(this.m[msgNumber], false, true);
		} catch (IndexOutOfBoundsException e) {
			Log.e(TAG, "no such message number exists");
			Log.e(TAG, e.getMessage(), e);
			try {
				this.r.disconnect();
			} catch (MessagingException e2) {
				Log.e(TAG, e2.getMessage(), e2);
				return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			try {
				this.r.disconnect();
			} catch (MessagingException e2) {
				Log.e(TAG, e2.getMessage(), e2);
				return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
			}
		}

		Log.i(TAG, "file downloaded");

		try {
			this.r.disconnect();
		} catch (MessagingException e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		return 0;
	}

	private int deleteConc(int msgNumber, int index) {
		try {
			this.r = new AndroidGmailRetriever(PROTOCOL);
		} catch (NoSuchProviderException e) {
			Log.e(TAG, "NoSuchProviderException");
			return AndroidGmailBase.ERROR_CODE_NOSUCHPROVIDEREXCEPTION;
		}

		// set the mail folder writable
		this.r.setWritable(true);

		try {
			this.r.connect(HOST, this.user, this.password);
		} catch (MessagingException e) {
			Log.e(TAG, "user or password is wrong.");
			return AndroidGmailBase.ERROR_CODE_USER_OR_PASSWORD_WRONG;
		}

		try {
			this.m = this.r.get();
			Log.i(TAG, "get");
		} catch (MessagingException e) {
			Log.e(TAG, "MessagingException");
			try {
				this.r.disconnect();
			} catch (MessagingException e2) {
				Log.e(TAG, "MessagingException");
				return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
			}
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		if (msgNumber < 0 || msgNumber >= m.length) {
			Log.e(TAG, "no such msgNumber message");
			try {
				this.r.disconnect();
			} catch (MessagingException e) {
				Log.e(TAG, "MessagingException");
				return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
			}
			return AndroidGmailBase.ERROR_CODE_INDEXOUTOFBOUNDEXCEPTION;
		}

		// delete a mail
		try {
			this.m[msgNumber].setFlag(Flags.Flag.DELETED, true);
			Log.i(TAG, "setFlag");
		} catch (MessagingException e) {
			Log.e(TAG, "MessagingException");
			try {
				this.r.disconnect();
			} catch (MessagingException e2) {
				Log.e(TAG, "MessagingException");
				return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
			}
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		try {
			this.r.disconnect();
		} catch (MessagingException e) {
			Log.e(TAG, "MessagingException");
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		return 0;
	}

	private int getMessageNumber(String user, String password, int index) {
		AndroidGmailRetriever r;
		try {
			this.r = new AndroidGmailRetriever(PROTOCOL);
		} catch (NoSuchProviderException e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_NOSUCHPROVIDEREXCEPTION;
		}

		try {
			this.r.connect(HOST, user, password);
		} catch (MessagingException e) {
			Log.e(TAG, "user or password is wrong");
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_USER_OR_PASSWORD_WRONG;
		}

		try {
			this.m = this.r.get();
		} catch (MessagingException e) {
			Log.e(TAG, e.getMessage(), e);
			try {
				this.r.disconnect();
			} catch (MessagingException e2) {
				return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
			}
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		int msgNumber = this.m.length - 1;
		Log.e(TAG, "msgNumber = " + msgNumber);

		try {
			this.r.disconnect();
		} catch (MessagingException e2) {
			return AndroidGmailBase.ERROR_CODE_MESSAGING_EXCEPTION;
		}

		return msgNumber;
	}

	public int verify(String syspw) {
		String encryptedSysPW = "";
		String savedEncryptedSysPW = "";
		// encrypt syspw
		try {
			encryptedSysPW = getEncryptedSysPW(getApplicationContext(), syspw);
		} catch (Exception e)  {
			Log.e(TAG, e.getMessage(), e);
			return SYSPW_ERROR;
		}
		// read encryptdPW from SharedPreference
		SharedPreferences pref = getSharedPreferences("AndroidGmail", MODE_PRIVATE);
		savedEncryptedSysPW = pref.getString("sysPW", "");

		// compare
		if (encryptedSysPW.equals(savedEncryptedSysPW)) {
			return SYSPW_OK;
		} else {
			return SYSPW_ERROR;
		}
	}

	public int verifyRandom(long random, int index) {
		long savedRandom = 0L;
		// read SharedPreference
		SharedPreferences pref = getSharedPreferences("AndroidGmailService", MODE_PRIVATE);
		String savedRandomStr = pref.getString("random" + index, "");
		try {
			savedRandom = Long.parseLong(savedRandomStr);
		} catch (NumberFormatException e) {
			Log.e(TAG, e.getMessage(), e);
			return AndroidGmailBase.ERROR_CODE_EXCEPTION;
		}
		if (random == savedRandom) {
			return RANDOM_OK;
		} else {
			return AndroidGmailBase.ERROR_CODE_SYSTEMPW_NOT_SAME;
		}
	}

    public String getEncryptedSysPW(Context context,String syspw) throws Exception {
    	syskey = generateKey(context);
    	String encryptedSysPW = Crypto.encrypt(syskey, syspw);
    	return encryptedSysPW;
    }
    
    public char[] generatePassword(Context context) {
    	String orignal_id = getOriginalID(context);
    	
    	// get the package name of the application
    	String packageName = context.getPackageName();
    	
    	// generate the password for Key
    	String password = orignal_id + packageName;
    	
    	return password.toCharArray();
    }
    
    public char[] generatePassword(Context context, int index) {
    	String orignal_id = getOriginalID(context);
    	
    	// get the package name of the application
    	String packageName = context.getPackageName();
    	
    	// generate the password for Key
    	String password = orignal_id + packageName + index;
    	
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
    
    public SecretKey generateKey(Context context, int index)
    		throws NoSuchAlgorithmException, InvalidKeySpecException {
    	// get password to generate Key
    	char[] password = generatePassword(context, index);
    	
    	// generate Key
    	KeySpec keySpec = new PBEKeySpec(password, SALT, 1024, 256);
    	SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWITHSHAAND256BITAES-CBC-BC");
    	SecretKey secretKey = factory.generateSecret(keySpec);
    	
    	return secretKey;
    	
    }
    
    public String getOriginalID(Context context) {
    	SharedPreferences pref = context.getSharedPreferences("id", MODE_PRIVATE);
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
