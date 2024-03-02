package com.raffa064.deepnote;

import android.app.Activity;
import com.raffa064.deepnote.activities.MainActivity;
import com.raffa064.deepnote.updates.Update;
import com.raffa064.deepnote.updates.UpdateHandler;
import com.raffa064.deepnote.utils.ViewUtils;

public class DeepNoteUpdateHandler extends UpdateHandler {
	private boolean forceReload;
	private MainActivity activity;

	public DeepNoteUpdateHandler(MainActivity activity) {
		super(activity.getSharedPreferences("auto-update", Activity.MODE_PRIVATE), activity.getFilesDir());
		this.activity = activity;
	}

	public void setForceReload(boolean forceReload) {
		this.forceReload = forceReload;
	}

	public boolean isForceReload() {
		return forceReload;
	}
	
	@Override
	public void updateFound(Update update) {
		ViewUtils.showToast(activity, "Installing update...");
	}

	@Override
	public void updateNotFound() {
	}

	@Override
	public void updateError(int errCode, Throwable throwable) {
		if (forceReload) {
			forceReload = false;

			ErrorHandler.showErrorMessage(activity, "Update error", throwable);
		}
	}

	@Override
	public void updateFinished() {
		if (forceReload) {
			forceReload = false;
			activity.loadApp();
			return;
		}

		activity.askToRestart();
	}
}
