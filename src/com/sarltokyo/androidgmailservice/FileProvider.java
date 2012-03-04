package com.sarltokyo.androidgmailservice;

/**
 * FileProvider.java
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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class FileProvider extends ContentProvider {

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {
		File file = new File(URI.create("file:///data/data/" +
				AndroidGmailBase.PACKAGE_NAME + "/files/" +
				uri.getLastPathSegment()));
		ParcelFileDescriptor parcel =
			ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
		return parcel;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}
}
