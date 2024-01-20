package com.raffa064.deepnote.updates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    public static void deleteDir(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				deleteDir(f);
			}
		}

		file.delete();
	}

	public static void writeFile(String content, File file) throws FileNotFoundException, IOException {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(content.getBytes());
		}
	}
}
