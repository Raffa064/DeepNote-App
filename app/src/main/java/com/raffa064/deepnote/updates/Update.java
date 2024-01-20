package com.raffa064.deepnote.updates;

import java.util.ArrayList;
import java.util.List;

/*
	This class store all metadata from commit and it's files
*/

public class Update {
	public Commit commit;
	public List<UpdateFile> folders = new ArrayList<>();
	public List<UpdateFile> files = new ArrayList<>();

	public Update(Commit commit) {
		this.commit = commit;
	}
}
