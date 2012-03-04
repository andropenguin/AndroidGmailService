package com.sarltokyo.androidgmailservice;

/**
 * AndroidGmailSender.java
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

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import android.util.Log;

public class AndroidGmailSender extends Authenticator {
	private static final String HOST = "smtp.gmail.com";
	private static final String PORT = "465";
	private static final String SPORT = PORT;
	private static final String PROTOCOL = "smtps";
	private static final String TAG = "AndroidGmailSender";

	private Session session;
	private Transport transport;
	private String port;
	private String sport;
	private String host;

	private String user;
	private String pass;


	public AndroidGmailSender() throws NoSuchProviderException {
		this.host = HOST;
		this.port = PORT;
		this.sport = SPORT;

		this.user = "";
		this.pass = "";
		this.session = Session.getInstance(new Properties(), null);
		this.transport = session.getTransport(PROTOCOL);
	}

	public AndroidGmailSender(String user, String pass) throws NoSuchProviderException {
		this();
		this.user = user;
		this.pass = pass;
	}

	public synchronized void connect(String host, int port, String user, String pass)
			throws MessagingException {
		this.transport.connect(host, port, user, pass);
	}

	public synchronized void connect(String host) throws MessagingException {
		connect(host, -1, this.user, this.pass);
	}

	public synchronized void connect(String host, int port)	throws MessagingException {
		connect(host, port, this.user, this.pass);
	}

	public synchronized void disconnect() {
		try {
			this.transport.close();
		} catch (MessagingException e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	public MimeMessage createMessage() {
		return new MimeMessage(this.session);
	}

	public static void setHeaders(MimeMessage msg, String to, String from)
			throws MessagingException, AddressException {
		msg.setFrom(null);
		msg.addFrom(InternetAddress.parse(from, true));
		msg.setRecipients(MimeMessage.RecipientType.TO,
				InternetAddress.parse(to, true));
		msg.setHeader("X-Mailer", "javamail-android Sender");
	}

	/*
	 * necessary to check if valid
	 */
	public static void setHeaders(MimeMessage msg, String[] to, String from)
			throws MessagingException, AddressException {
		msg.setFrom(null);
		msg.addFrom(InternetAddress.parse(from, true));
		String addresslist = "";
		for (int i = 0; i < to.length; i++) {
			addresslist += to[i] + ",";
		}
		msg.setRecipients(MimeMessage.RecipientType.TO,
				InternetAddress.parse(addresslist, true));
		msg.setHeader("X-Mailer", "javamail-android Sender");
	}

	public synchronized void send(MimeMessage message, Address[] envelopeTo)
			throws MessagingException {
		message.setSentDate(new Date());
		setProperties();
		// use From address as Message-ID
		this.session.getProperties().put("mail.from",
				((InternetAddress)message.getFrom()[0]).getAddress());
		message.saveChanges();
		this.transport.sendMessage(message, envelopeTo);
	}

	public synchronized void send(MimeMessage message)
			throws MessagingException {
		send(message, message.getAllRecipients());
	}

	public synchronized void send(String subject, String body, String to, String from)
			throws MessagingException {
		MimeMessage msg = createMessage();
		setHeaders(msg, to, from);
		// Japanese only!!
		msg.setSubject(subject, "ISO-2022-JP");
		msg.setText(body, "ISO-2022-JP");
		send(msg);
	}

	public synchronized void send(String subject, String body, String[] to, String from)
			throws MessagingException {
		MimeMessage msg = createMessage();
		setHeaders(msg, to, from);
		// Japanese only!!
		msg.setSubject(subject, "ISO-2022-JP");
		msg.setText(body, "ISO-2022-JP");
		send(msg);
	}

	public synchronized void send(MimeMessage message, String envelopeTo, String  envelopFrom)
			throws AddressException, MessagingException {
		this.session.getProperties().put("mail.smtp.from", envelopFrom);
		send(message, InternetAddress.parse(envelopeTo, true));
		this.session.getProperties().remove("mail.smtp.from");
	}

	public synchronized void send(MimeMessage message, String[] envelopeTo, String envelopeFrom)
			throws AddressException, MessagingException {
		String addresslist = "";
		for (int i = 0; i < envelopeTo.length; i++) {
			addresslist += envelopeTo[i] + ",";
		}
		this.session.getProperties().put("mail.smtp.from", envelopeFrom);
		send(message, InternetAddress.parse(addresslist, true));
		this.session.getProperties().remove("mail.smtp.from");
	}

	public MimeMultipart createAttachment(String body, String filename)
			throws MessagingException, IOException {
		MimeBodyPart textPart = new MimeBodyPart();
		// Japanese only!!
		textPart.setText(body, "ISO-2022-JP");
		MimeBodyPart filePart = new MimeBodyPart();
		filePart.setDataHandler(new DataHandler(new FileDataSource(filename)));
		// as default, Content-Disposion: is attachment.
		filePart.setFileName(filename);

		// as default, multipart/mixed
		MimeMultipart mp = new MimeMultipart();
		mp.addBodyPart(textPart);
		mp.addBodyPart(filePart);

		return mp;
	}

	public MimeMultipart createAttachment(String body, String[] filename)
			throws MessagingException, IOException {
		MimeBodyPart textPart = new MimeBodyPart();
		// Japanese only!!
		textPart.setText(body, "ISO-2022-JP");
		MimeBodyPart filePart = new MimeBodyPart();
		for (int i = 0; i < filename.length; i++) {
			filePart.setDataHandler(new DataHandler(new FileDataSource(filename[i])));
			// as default, Content-Disposion: is attachment.
			filePart.setFileName(filename[i]);
		}

		// as default, multipart/mixed
		MimeMultipart mp = new MimeMultipart();
		mp.addBodyPart(textPart);
		mp.addBodyPart(filePart);

		return mp;
	}

	private Properties setProperties() {
		Properties props = new Properties();

		// necessary to send mail from Android application
//		props.put("mail.smtp.host", this.host);
//		props.put("mail.smtp.port", this.port);
//		props.put("mail.smtp.socketFactory.port", this.sport);
//		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//		props.put("mail.smtp.socketFactory.fallback", "false");
		props.put("mail.smtp.host", this.host);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", this.port);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");

		return props;
	}
}
