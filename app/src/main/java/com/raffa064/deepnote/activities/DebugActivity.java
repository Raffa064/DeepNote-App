package com.raffa064.deepnote.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.widget.Toast;

public class DebugActivity extends Activity {
	public static final String EXTRA_ERROR = "error";
	
	String[] exceptionType = {
			"StringIndexOutOfBoundsException",
			"IndexOutOfBoundsException",
			"ArithmeticException",
			"NumberFormatException",
			"ActivityNotFoundException"
	};
	String[] errMessage= {
			"Invalid string operation\n",
			"Invalid list operation\n",
			"Invalid arithmetical operation\n",
			"Invalid toNumber block operation\n",
			"Invalid intent operation"
	};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		String errMsg = "";
		String madeErrMsg = "";
		if(intent != null){
			errMsg = intent.getStringExtra(EXTRA_ERROR);
			String[] spilt = errMsg.split("\n");
			//errMsg = spilt[0];
			try {
				for (int j = 0; j < exceptionType.length; j++) {
					if (spilt[0].contains(exceptionType[j])) {
						madeErrMsg = errMessage[j];
						int addIndex = spilt[0].indexOf(exceptionType[j]) + exceptionType[j].length();
						madeErrMsg += spilt[0].substring(addIndex, spilt[0].length());
						break;
					}
				}
				if(madeErrMsg.isEmpty()) madeErrMsg = errMsg;
			}catch(Exception e){}
		}
		
		final String finalMessage = madeErrMsg;
		
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
		bld.setTitle("An error occured");
		bld.setMessage( madeErrMsg );
		bld.setNeutralButton("End Application", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		bld.setPositiveButton("Copy error", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface di, int which) {
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Activity.CLIPBOARD_SERVICE);
				clipboard.setText(finalMessage);
				Toast.makeText(DebugActivity.this, "Copied!", Toast.LENGTH_SHORT).show();
			}
		});
		bld.create().show();
    }
}
