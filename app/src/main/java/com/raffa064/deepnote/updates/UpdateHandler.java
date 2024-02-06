package com.raffa064.deepnote.updates;

import android.content.SharedPreferences;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static com.raffa064.deepnote.updates.NetUtils.*;
import static com.raffa064.deepnote.updates.FileUtils.*;

/*
	Used to find and apply updates
*/

public abstract class UpdateHandler {
    public final String GH_API_REPO_COMMITS = "https://api.github.com/repos/Raffa064/DeepNote/commits/main";
	public final String GH_API_REPO_CONTENT_URL = "https://api.github.com/repos/Raffa064/DeepNote/contents/";
	public final int ERR_IO_EXCEPTION = 0;
	public final int ERR_JSON_EXCEPTION = 1;

	public SharedPreferences sharedPrefs; // Used to store data
	public File filesDir; // android files folder /data/data/<pkgname>/files
	
	public Thread runningInstance;

	public UpdateHandler(SharedPreferences sharedPrefs, File filesDir) {
		this.sharedPrefs = sharedPrefs;
		this.filesDir = filesDir;
	}
	
	// Callbacks
	public abstract void updateFound(Update update);
	public abstract void updateNotFound();
	public abstract void updateError(int errCode, Throwable throwable);
	public abstract void updateFinished();
	
	public Commit getLocalCommit() {
		String author = sharedPrefs.getString("commit-author", "Unknown");
		String sha = sharedPrefs.getString("commit-sha", "Unknown");
		String message = sharedPrefs.getString("commit-message", "Unknown");
		
		Commit commit = new Commit(author, sha, message);
		return commit;
	}

	public void setLocalCommit(Commit commit) {
		sharedPrefs.edit()
			.putString("commit-author", commit.author)
			.putString("commit-sha", commit.sha)
		    .putString("commit-message", commit.message)
			.commit();
			
	}

	public boolean forceUpdate() {
		if (!isRunning()) {
			Commit fakeCommit = new Commit("Unknown", "fakecommit", "This is a fake commit, used to force update");
			setLocalCommit(fakeCommit);
			startUpdateThread();
			
			return true;
		}
		
		return false;
	}
	
	public boolean isRunning() {
		return runningInstance != null && runningInstance.isAlive();
	}
	
	public void startUpdateThread() {
		if (isRunning()) {
			return;
		}
		
		runningInstance = new Thread() {
			@Override
			public void run() {
				doUpdatePipeline();
			}
		};
		
		runningInstance.start();
	}

	public void doUpdatePipeline() {
		try {
			Update update = checkForUpdate(); // Search for updates

			if (update != null) {
				updateFound(update);

				File downloadDir = new File(filesDir, "download");
				File latestDir = new File(filesDir, "latest");

				getUpdateContent(update, "");               // Get files from repo root (metadata only)
				downloadUpdateContent(update, downloadDir); // Download repo files

				if (latestDir.exists()) {
					deleteDir(latestDir);                   // Delete current "latest" dir (It's the folder where the applicaton runs)
				}  

				downloadDir.renameTo(latestDir);            // Rename "download" folder to "latest" (replace)
				setLocalCommit(update.commit);              // Update local commit metadata (used to check for updates)

				updateFinished();
			} else {
				updateNotFound();
			}
		} catch (IOException e) {
			updateError(ERR_IO_EXCEPTION, e);   // Connection/File error
		} catch (JSONException e) {
			updateError(ERR_JSON_EXCEPTION, e); // Server reponse error
		}
	}
	public Update checkForUpdate() throws IOException, JSONException {
		Commit localCommit = getLocalCommit();

		// Server Response
		String responseJson = GET(GH_API_REPO_COMMITS);
		JSONObject json = new JSONObject(responseJson);
		JSONObject commit = json.getJSONObject("commit");
		JSONObject author = commit.getJSONObject("author");

		// Remote commit data
		String commitAuthor = author.getString("name");
		String commitSHA = json.getString("sha");
		String commitMessage = commit.getString("message");

		boolean isNewCommit = !localCommit.sha.equals(commitSHA);
		
		if (isNewCommit) {
			Commit remoteCommit = new Commit(commitAuthor, commitSHA, commitMessage);
			Update update = new Update(remoteCommit);

			return update;
		}

		return null;
	}

	public void getUpdateContent(Update update, String dir) throws IOException, JSONException {
		String jsonReponse = GET(GH_API_REPO_CONTENT_URL + dir);
		JSONArray jsonArray = new JSONArray(jsonReponse);

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject json = jsonArray.getJSONObject(i);

			String url = json.getString("download_url");
			String path = json.getString("path");
			String type = json.getString("type");
			
			UpdateFile file = new UpdateFile(url, path, type);

			if (type.equals("dir")) {
				update.folders.add(file);
				getUpdateContent(update, path); // It's a recursive search/get
				continue;
			}

			update.files.add(file);
		}
	}

	public void downloadUpdateContent(Update update, File downloadDir) throws IOException {
		if (downloadDir.exists()) {
			deleteDir(downloadDir); // Delete trash
		}

		downloadDir.mkdirs();

		for (UpdateFile uFolder : update.folders) {
			// Create repo folders into download dir
			File folder = new File(downloadDir, uFolder.path);
			folder.mkdirs();
		}

		for (UpdateFile uFile : update.files) {
			// Download repo files into downloads dir
			File file = new File(downloadDir, uFile.path);
			file.createNewFile();

			String content = GET(uFile.url);
			writeFile(content, file);
		}
	}
}
