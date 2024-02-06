package com.raffa064.deepnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.content.DialogInterface;
import android.content.ClipboardManager;
import android.widget.Toast;

public class ErrorHandler {
    public static void showErrorMessage(final Activity activity, final String title, final Throwable throwable) {
        activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final String errorMessage = throwable.getMessage();
				final String stackTrace = Log.getStackTraceString(throwable);

				StringBuilder messageBuilder = new StringBuilder();
				messageBuilder.append(errorMessage).append("\n\n");
				messageBuilder.append("StackTrace:\n").append(stackTrace);

				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setTitle(title);
				builder.setMessage(messageBuilder.toString());
				builder.setNegativeButton("OK", null);
				builder.setPositiveButton("Copy error", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface di, int which) {
							ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Activity.CLIPBOARD_SERVICE);
							clipboard.setText(title + "\n" + errorMessage + "\n\nStackTrace:\n"+stackTrace);
							Toast.makeText(activity, "Copied!", Toast.LENGTH_SHORT).show();
						}
					});
				
				AlertDialog alertDialog = builder.create();
				alertDialog.show();
			}
		});
    }
}

