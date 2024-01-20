package com.raffa064.deepnote.updates;

/*
	Store data from a remote file that needs to be downloaded
*/

public class UpdateFile {
	public String url;  // remote url
	public String path; // local path
	public String type; // dir|file

	public UpdateFile(String url, String path, String type) {
		this.url = url;
		this.path = path;
		this.type = type;
	}
}
