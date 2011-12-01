package com.sarltokyo.androidgmailservice;

/**
 * AndroidGmailRetriever.java
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
 *  
 *  In this file, the methods dumpPart and dumpEnvelope are modified methods
 *  of the ones in a sample source msgshow.java of sun javamail. The copyright
 *  notice, the list of conditions and the disclaimer are written bellow.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
*/

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.mail.NoSuchProviderException;
import java.util.Date;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.ParseException;
//import android.os.Environment;
import android.util.Log;

public class AndroidGmailRetriever {

	private Store store;
	private Folder currentFolder = null;
	private boolean showStructure = false;
	private boolean saveAttachments = false;
	private boolean writable = false;
	static int attnum = 1;
	private static final String TAG = "AndroidGmailRetriever";
//	private static final File EXTERNAL_SD = Environment.getExternalStorageDirectory();

	public AndroidGmailRetriever(String protocol) throws NoSuchProviderException {
		Session session = Session.getInstance(new Properties(), null);
		store = session.getStore(protocol);
	}

	public synchronized void connect(String host, int port, String user, String pass)
			throws MessagingException {
		store.connect(host, port, user, pass);
	}

	public synchronized void connect(String host, String user, String pass)
			throws MessagingException {
		connect(host, -1, user, pass);
	}

	public synchronized void disconnect()
			throws MessagingException {
		if (currentFolder != null && currentFolder.isOpen()) {
			try {
				currentFolder.close(true);
			} catch (MessagingException e) {
				Log.e(TAG, e.getMessage(), e);
				throw e;
			}
		}
		try {
			store.close();
		} catch (MessagingException e) {
			Log.e(TAG, e.getMessage(), e);
			throw e;
		}
	}

	public synchronized void setWritable(boolean writable) {
		this.writable = writable;
	}

	public synchronized void setCurrentFolder(String name)
			throws MessagingException {
		if (currentFolder != null && currentFolder.isOpen()) {
			currentFolder.close(true);
		}
		currentFolder = store.getFolder(name);
		if (currentFolder == null) {
			throw new FolderNotFoundException();
		}
		if (writable) {
			currentFolder.open(Folder.READ_WRITE);
		} else {
			currentFolder.open(Folder.READ_ONLY);
		}
	}

	public synchronized MimeMessage[]	 get()
			throws MessagingException {
		if (currentFolder == null) {
			setCurrentFolder("INBOX");
		}
		Message[] messages = currentFolder.getMessages();
		MimeMessage[] mimeMessages = new MimeMessage[messages.length];
		for (int i = 0; i < messages.length; i++) {
			mimeMessages[i] = (MimeMessage)messages[i];
		}
		return mimeMessages;
	}

	public synchronized MimeMessage get(int num)
			throws MessagingException {
		if (currentFolder == null) {
			setCurrentFolder("INBOX");
		}
		try {
			return (MimeMessage)currentFolder.getMessage(num);
		} catch (IndexOutOfBoundsException e) {
			throw new MessagingException("Message number out of bounds", e);
		}
	}

	protected void finalize() throws Throwable {
		disconnect();
	}

	public static void dumpPart(Part p, boolean showStructure, boolean saveAttachments) throws Exception {
		if (p instanceof Message)
			dumpEnvelope((Message)p);

		String ct = p.getContentType();
		try {
			pr("CONTENT-TYPE: " + (new ContentType(ct)).toString());
		} catch (ParseException pex) {
			pr("BAD CONTENT-TYPE: " + ct);
		}
		String filename = p.getFileName();
		if (filename != null)
			pr("FILENAME: " + filename);

		/*
		 * Using isMimeType to determine the content type avoids
		 * fetching the actual content data until we need it.
		 */
		if (p.isMimeType("text/plain")) {
			pr("This is plain text");
			pr("---------------------------");
			if (!showStructure && !saveAttachments)
				Log.i(TAG, (String)p.getContent());
		} else if (p.isMimeType("multipart/*")) {
			pr("This is a Multipart");
			pr("---------------------------");
			Multipart mp = (Multipart)p.getContent();
			level++;
			int count = mp.getCount();
			for (int i = 0; i < count; i++)
				dumpPart(mp.getBodyPart(i), showStructure, saveAttachments);
			level--;
		} else if (p.isMimeType("message/rfc822")) {
			pr("This is a Nested Message");
			pr("---------------------------");
			level++;
			dumpPart((Part)p.getContent(), showStructure, saveAttachments);
			level--;
		} else {
			if (!showStructure && !saveAttachments) {
				/*
				 * If we actually want to see the data, and it's not a
				 * MIME type we know, fetch it and check its Java type.
				 */
				Object o = p.getContent();
				if (o instanceof String) {
					pr("This is a string");
					pr("---------------------------");
					Log.i(TAG, (String)o);
				} else if (o instanceof InputStream) {
					pr("This is just an input stream");
					pr("---------------------------");
					InputStream is = (InputStream)o;
					int c;
					while ((c = is.read()) != -1)
						Log.i(TAG, String.valueOf(c));
				} else {
					pr("This is an unknown type");
					pr("---------------------------");
					pr(o.toString());
				}
			} else {
				// just a separator
				pr("---------------------------");
			}
		}

		if (saveAttachments && level != 0 && !p.isMimeType("multipart/*")) {
			String disp = p.getDisposition();
			// many mailers don't include a Content-Disposition
			if (disp == null || disp.equalsIgnoreCase(Part.ATTACHMENT)) {
				if (filename == null)
					filename = "Attachment" + attnum++;
				pr("Saving attachment to file " + filename);
				try {
//					File f = new File(EXTERNAL_SD, filename);
					File f = new File(filename);
					if (f.exists())
						// XXX - could try a series of names
						throw new IOException("file exists");
					((MimeBodyPart)p).saveFile(f);
				} catch (IOException ex) {
					pr("Failed to save attachment: " + ex);
				}
				pr("---------------------------");
			}
		}
	}

	public static void dumpEnvelope(Message m) throws Exception {
		pr("This is the message envelope");
		pr("---------------------------");
		Address[] a;
		// FROM
		if ((a = m.getFrom()) != null) {
			for (int j = 0; j < a.length; j++)
				pr("FROM: " + a[j].toString());
		}

		// TO
		if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
			for (int j = 0; j < a.length; j++) {
				pr("TO: " + a[j].toString());
				InternetAddress ia = (InternetAddress)a[j];
				if (ia.isGroup()) {
					InternetAddress[] aa = ia.getGroup(false);
					for (int k = 0; k < aa.length; k++)
						pr("  GROUP: " + aa[k].toString());
				}
			}
		}

		// SUBJECT
		pr("SUBJECT: " + m.getSubject());

		// DATE
		Date d = m.getSentDate();
		pr("SendDate: " +
				(d != null ? d.toString() : "UNKNOWN"));

		// FLAGS
		Flags flags = m.getFlags();
		StringBuffer sb = new StringBuffer();
		Flags.Flag[] sf = flags.getSystemFlags(); // get the system flags

		boolean first = true;
		for (int i = 0; i < sf.length; i++) {
			String s;
			Flags.Flag f = sf[i];
			if (f == Flags.Flag.ANSWERED)
				s = "\\Answered";
			else if (f == Flags.Flag.DELETED)
				s = "\\Deleted";
			else if (f == Flags.Flag.DRAFT)
				s = "\\Draft";
			else if (f == Flags.Flag.FLAGGED)
				s = "\\Flagged";
			else if (f == Flags.Flag.RECENT)
				s = "\\Recent";
			else if (f == Flags.Flag.SEEN)
				s = "\\Seen";
			else
				continue;	// skip it
			if (first)
				first = false;
			else
				sb.append(' ');
			sb.append(s);
		}

		String[] uf = flags.getUserFlags(); // get the user flag strings
		for (int i = 0; i < uf.length; i++) {
			if (first)
				first = false;
			else
				sb.append(' ');
			sb.append(uf[i]);
		}
		pr("FLAGS: " + sb.toString());

		// X-MAILER
		String[] hdrs = m.getHeader("X-Mailer");
		if (hdrs != null)
			pr("X-Mailer: " + hdrs[0]);
		else
			pr("X-Mailer NOT available");
	}

	static int level = 0;

	public static void pr(String s) {
		Log.i(TAG, s);
	}
}
