package com.raffa064.deepnote.updates;

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
}
