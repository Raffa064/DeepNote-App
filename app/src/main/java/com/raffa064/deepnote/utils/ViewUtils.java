package com.raffa064.deepnote.utils;

import android.app.Activity;
import android.widget.Toast;

public class ViewUtils {
	public static void showToast(final Activity activity, final String message) {
		activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					int length = message.length() < 20 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;

					Toast.makeText(activity, message, length).show();			
				}
			});
	}
}
