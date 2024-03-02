package com.raffa064.deepnote;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AlertDialog;
import android.webkit.JavascriptInterface;
import com.raffa064.deepnote.activities.MainActivity;
import com.raffa064.deepnote.updates.UpdateHandler;
import com.raffa064.deepnote.utils.FileUtils;
import com.raffa064.deepnote.utils.NetUtils;
import com.raffa064.deepnote.utils.ViewUtils;
import java.io.File;
import org.json.JSONObject;

public class DeepNoteJavascriptInterface {
	private MainActivity activity;
	private UpdateHandler updateHandler;

	public DeepNoteJavascriptInterface(MainActivity activity) {
		this.activity = activity;
		this.updateHandler = activity.updateHandler;
	}
	
    @JavascriptInterface
	public void forceUpdate() {
		if (NetUtils.hasInternetConnection(activity)) {
			boolean sucess = updateHandler.forceUpdate();

			if (!sucess) {
				ViewUtils.showToast(activity, "Already updating...");
			}

			return;
		}

		ViewUtils.showToast(activity, "No internet connection!");
	}

	@JavascriptInterface
	public void showFolderContents() {
		File latestDir = activity.getLatestDir();
		String folderContents = FileUtils.getFileTree(latestDir);

		new AlertDialog.Builder(activity)
			.setTitle("Folder Contents")
			.setMessage(folderContents)
			.setNegativeButton("Close", null)
			.create()
			.show();
	}

	@JavascriptInterface
	public String getCommitInfo() {
		return updateHandler.getLocalCommit().toJSON();
	}

	@JavascriptInterface
	public String getInfo() {
		try {
			PackageManager packageManager = activity.getPackageManager();
			Context applicationContext = activity.getApplicationContext();
			String packageName = applicationContext.getPackageName();
			PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
			String pkgName = packageInfo.packageName;
			int versionCode = packageInfo.versionCode;
			String versionName = packageInfo.versionName;

			JSONObject json = new JSONObject();
			json.put("package", pkgName);
			json.put("versionCode", versionCode);
			json.put("versionName", versionName);

			return json.toString();
		} catch (Exception e) {
			return null;
		}
	}

	@JavascriptInterface
	public void clearCache() {
		new Thread() {
			@Override
			public void run() {
				activity.clearWebviewCache();
				ViewUtils.showToast(activity, "Cache cleanned!");
			}
		}.start();
	}
}
