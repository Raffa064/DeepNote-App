package com.raffa064.deepnote.updates;

import org.json.JSONException;
import org.json.JSONObject;

/*
	Used to tore commit metadata
*/

public class Commit {
	public String author;  // Who create this commit
	public String sha;     // Commit id/hash
	public String message; // Commit message

	public Commit(String author, String sha, String message) {
		this.author = author;
		this.sha = sha;
		this.message = message;
	}

	public String toJSON() {
		try {
			JSONObject json = new JSONObject();
			json.put("author", author);
			json.put("sha", sha);
			json.put("message", message);

			return json.toString();
		} catch (JSONException e) {
			return null;
		}
	}
}
