package com.raffa064.deepnote;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.raffa064.deepnote.updates.Update;
import com.raffa064.deepnote.updates.UpdateHandler;
import java.io.File;
import org.json.JSONObject;

import static com.raffa064.deepnote.updates.UpdateHandler.*;

public class MainActivity extends AppCompatActivity {
	private UpdateHandler updateHandler;
	private WebView webView;
	private boolean forceReload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		setupUpdater();
		setupWebView();
		setupInterface();

		if (savedInstanceState == null) {
			loadApp();
		}
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (webView != null) {
			webView.saveState(outState);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		if (webView != null) {
			webView.restoreState(savedInstanceState);
		}
	}
	
	@JavascriptInterface
	public void forceUpdate() {
		updateHandler.forceUpdate();
	}

	@JavascriptInterface
	public void showFolderContents() {
		File latestDir = new File(getFilesDir(), "latest");
		String folderContents = getFolderContent(latestDir);

		new AlertDialog.Builder(MainActivity.this)
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
			PackageManager packageManager = getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(getApplicationContext().getPackageName(), 0);
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

	private void setupWebView() {
		webView = new WebView(this);
		webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
					super.onReceivedError(view, request, error);

					// Obtém a mensagem de erro completa
					String errorMessage;
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						errorMessage = error.getDescription().toString() + ": " + request.getUrl();
					} else {
						errorMessage = "Erro desconhecido";
					}

					showToast("Error: " + errorMessage);
				}
			});
		webView.addJavascriptInterface(this, "app");

		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setForceDark(WebSettings.FORCE_DARK_OFF);
		settings.setDomStorageEnabled(true);
		settings.setAllowUniversalAccessFromFileURLs(true);
		settings.setAllowFileAccessFromFileURLs(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			WebView.setWebContentsDebuggingEnabled(true);
		}

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
	}

	private void setupUpdater() {
		SharedPreferences prefs = getSharedPreferences("auto-update", MODE_PRIVATE);
		updateHandler = new UpdateHandler(prefs, getFilesDir()) {
			@Override
			public void updateFound(Update update) {
				showToast("Installing update...");
			}

			@Override
			public void updateNotFound() {
			}

			@Override
			public void updateError(int errCode, Throwable throwable) {
				ErrorHandler.showErrorMessage(MainActivity.this, throwable);
			}

			@Override
			public void updateFinished() {
				if (forceReload) {
					forceReload = false;

					runOnUiThread(new Runnable() {
							@Override
							public void run() {
								loadApp();
							}
						});
					return;

				}

				askToRestart();
			}
		};

		updateHandler.start();
	}

	private void setupInterface() {
		setContentView(webView);
	}

	private String getIndentPrefix(int level) {
		String prefix = "";
		for (int i = 0; i < level; i++) {
			prefix += "  ";
		}

		return prefix;
	}

	private String getFolderContent(File dir, String content, int level) {
        content += getIndentPrefix(level) + dir.getName();

		if (dir.isDirectory()) {
			content += "/\n";
			for (File f : dir.listFiles()) {
				content = getFolderContent(f, content, level + 1);
			}
        } else {
			content += "\n";
		}

		return content;
    }

	private String getFolderContent(File dir) {
		return getFolderContent(dir, "", 0);
    }

	private void loadApp() {	
		File latestDir = new File(getFilesDir(), "latest");
		File index = new File(latestDir, "index.html");

		if (index.exists()) {
			webView.clearCache(true);
			webView.clearHistory();
			System.gc();

			webView.loadUrl("file://" + latestDir + "/index.html"); // Fixed to work on dual apps, and second space
			return;
		} 

		webView.loadUrl("file:///android_asset/index.html");
		forceReload = true;
	}

	private void showToast(final String message) {
		runOnUiThread(new Runnable() {
				@Override
				public void run() {
					int length = message.length() < 20 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;

					Toast.makeText(MainActivity.this, message, length).show();			
				}
			});
	}

	private void askToRestart() {
		runOnUiThread(new Runnable() {
				@Override
				public void run() {
					new AlertDialog.Builder(MainActivity.this)
						.setTitle("Update installed")
						.setMessage("The app has been updated, do you need to retart to apply changes.")
						.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface di, int id) {
								loadApp();
							}
						})
						.setNegativeButton("Maybe Later", null)
						.create()
						.show();			
				}
			});
	}
}