package com.raffa064.deepnote;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.raffa064.deepnote.activities.DebugActivity;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import android.os.Process;

public class Applications extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		
		final UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable exception) {
				String stackTrace = getStackTrace(exception);
				
				Intent intent = new Intent(getApplicationContext(), DebugActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				intent.putExtra(DebugActivity.EXTRA_ERROR, stackTrace);
				
				PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 11111, intent, PendingIntent.FLAG_ONE_SHOT);
				AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pendingIntent);
				
				Process.killProcess(Process.myPid());
				System.exit(2);
				
				defaultUncaughtExceptionHandler.uncaughtException(thread, exception);
			}
		});
	}
	
	private String getStackTrace(Throwable th){
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		
		Throwable cause = th;
		while(cause != null){
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		
		final String stacktraceAsString = result.toString();
		printWriter.close();
		
		return stacktraceAsString;
	}
}
