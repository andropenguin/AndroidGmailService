package com.sarltokyo.androidgmailservice;

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
