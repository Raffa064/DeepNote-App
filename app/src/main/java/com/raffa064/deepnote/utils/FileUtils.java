package com.raffa064.deepnote.utils;

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
	
	private static String getIndentPrefix(int level) {
		String prefix = "";
		for (int i = 0; i < level; i++) {
			prefix += "  ";
		}

		return prefix;
	}
	
	public static String getFileTree(File dir, String content, int level) {
        content += getIndentPrefix(level) + dir.getName();

		if (dir.isDirectory()) {
			content += "/\n";
			for (File f : dir.listFiles()) {
				content = getFileTree(f, content, level + 1);
			}
        } else {
			content += "\n";
		}

		return content;
    }

	public static String getFileTree(File dir) {
		return getFileTree(dir, "", 0);
    }
}
