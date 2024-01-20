package com.raffa064.deepnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;

public class ErrorHandler {
    public static void showErrorMessage(final Activity activity, final Throwable throwable) {
        activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				String errorMessage = throwable.getMessage();
				String stackTrace = Log.getStackTraceString(throwable);

				StringBuilder messageBuilder = new StringBuilder();
				messageBuilder.append("Error: ").append(errorMessage).append("\n\n");
				messageBuilder.append("StackTrace:\n").append(stackTrace);

				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setTitle("Error");
				builder.setMessage(messageBuilder.toString());
				builder.setPositiveButton("OK", null);

				AlertDialog alertDialog = builder.create();
				alertDialog.show();
			}
		});
    }
}

