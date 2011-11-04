package com.sarltokyo.androidgmailservice;

/**
  * AndroidGmailServiceInterface.aidl
  *
  * The license of this file is Public domain. About the exclamation
  *  of this license, see http://en.wikipedia.org/wiki/Public_domain.
*/

interface AndroidGmailServiceInterface {
	// send a mail
	int send1(String to, String from, String subject, String body, long random, int index);
	int send2(in String[] to, String from, String subject, String body, long random, int index);
	int send3(String to, String from, String subject, String body, String filename, long random, int index);
	int send4(in String[] to, String from, String subject, String body, String filename, long random, int index);
	int send5(in String[] to, String from, String subject, String body, in String[] filename, long random, int index);

	// send a setting
	int sendSetting(String syspw, int index);

	// receive a mail
	int retrieve1(int msgNumber, long random, int index);
	int retrieve2(int msgNumber, long random, int index, out String[] message);

	// delete a mail
	int delete(int msgNumber, long random, int index);

	// write preferences of account information temporary
	int writeTmpPreferences(String user, String password);

	// write preferences of account information
	int writePreferences(String user, String password, long random, int index);
}
