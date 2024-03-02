package com.raffa064.deepnote.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.support.v4.content.ContextCompat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetUtils {
    public static String GET(String urlString) throws IOException {
		URL url = new URL(urlString);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");

		InputStreamReader streamReader = new InputStreamReader(url.openStream());
		BufferedReader reader = new BufferedReader(streamReader);

		StringBuilder content = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			content.append(line + "\n");
		}

		reader.close();
		connection.disconnect();

		return content.toString();
	}
	
	public static boolean hasInternetConnection(Activity activity) {
		ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

		boolean phoneIsConnected = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
		boolean appHasPermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED;

		return phoneIsConnected && appHasPermission;
	}
}
