package com.sarltokyo.androidgmailservice;

/**
 * LicenseActivity.java
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
import java.io.InputStream;
import java.io.OutputStream;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class LicenseActivity extends Activity {
	private final static String TAG = "LicenseActivity";

	//　初期化
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
        	raw2file(this, R.raw.license, "license.html");
        } catch (IOException e) {
        }

        WebView webView = new WebView(this);
        setContentView(webView);

        webView.loadUrl("content://com.sarltokyo.androidgmailservice/license.html");
	}

	private void raw2file(Context context,
			int resID, String fileName)
			throws IOException {
		InputStream in = context.getResources().openRawResource(resID);
		in2file(context, in, fileName);
	}

	private void in2file(Context context,
			InputStream in, String fileName)
			throws IOException {
		int size;
		byte[] w = new byte[1024];
		OutputStream out = null;
		try {
			out = context.openFileOutput(fileName,
					Context.MODE_WORLD_READABLE);
			while (true) {
				size = in.read(w);
				if (size <= 0) break;
				out.write(w, 0, size);
			}
			out.close();
			in.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException");
			try {
				if (in != null) in.close();
				if (out != null) out.close();
			} catch (IOException e2) {
			}
			throw e;
		}
	}
}
