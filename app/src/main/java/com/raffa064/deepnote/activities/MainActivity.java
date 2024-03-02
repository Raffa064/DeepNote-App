package com.raffa064.deepnote.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.raffa064.deepnote.DeepNoteJavascriptInterface;
import com.raffa064.deepnote.DeepNoteUpdateHandler;
import com.raffa064.deepnote.FilteredWebViewClient;
import com.raffa064.deepnote.utils.NetUtils;
import java.io.File;

public class MainActivity extends AppCompatActivity {
	public DeepNoteUpdateHandler updateHandler;
	public DeepNoteJavascriptInterface jsi;
	public WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		setupUpdateHandler();
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
	
	private void setupUpdateHandler() {
		updateHandler = new DeepNoteUpdateHandler(this);

		if (NetUtils.hasInternetConnection(this)) {
			updateHandler.startUpdateThread();
		}
	}

	private void setupWebView() {
		jsi = new DeepNoteJavascriptInterface(this);
		
		FilteredWebViewClient filteredWebViewClient = new FilteredWebViewClient();
		filteredWebViewClient.addFilter(
			"^http://localhost:\\d{4}/.*e",     // Debug server
			"^file:///android_asset/.*",        // Used for loadscreen
			"^file://" + getLatestDir() + "/.*" // Default work dir
		);

		webView = new WebView(this);
		webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		webView.addJavascriptInterface(jsi, "app");
		webView.setWebViewClient(filteredWebViewClient);

		WebSettings settings = webView.getSettings();
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setJavaScriptEnabled(true);
		settings.setForceDark(WebSettings.FORCE_DARK_OFF);
		settings.setDomStorageEnabled(true);
		settings.setAllowUniversalAccessFromFileURLs(true);
		settings.setAllowFileAccessFromFileURLs(true);

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // http/https access (used for debug)
		}
	}

	private void setupInterface() {
		setContentView(webView);
	}

	public void loadApp() {	
		File latestDir = getLatestDir();
		File index = new File(latestDir, "index.html");

		if (index.exists()) {
			clearWebviewCache();
			webView.loadUrl("file://" + latestDir + "/index.html"); // Fixed to work on dual apps, and second space
			return;
		} 

		webView.loadUrl("file:///android_asset/index.html"); // Loading screen (Appear only at first launch) 
		updateHandler.setForceReload(true); // Force reload when download has been finished 
	}

	public void clearWebviewCache() {
		runOnUiThread(new Runnable() {
				@Override
				public void run() {
					webView.clearCache(true);
					webView.clearHistory();
					System.gc();
				}
			});
	}

	public void askToRestart() {
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
	
	public File getLatestDir() {
		File latestDir = new File(getFilesDir(), "latest");
		return latestDir;
	}
}
