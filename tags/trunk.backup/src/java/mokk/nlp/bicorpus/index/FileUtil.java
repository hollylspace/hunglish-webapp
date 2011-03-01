package mokk.nlp.bicorpus.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtil {

	public static long getSize(File me) throws IOException{
		FileInputStream fis = null;
        try {
                fis = new FileInputStream(me);
                return fis.getChannel().size();
        } finally {
                fis.close();
        }		
	}
	
	public static void copyFile(File sourceFile, File destFile)
			throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	public static void copyDirectory(File sourceDir, File destDir)
			throws IOException {

		if (!destDir.exists()) {
			destDir.mkdir();
		} else {
			delete(destDir, true);
		}

		File[] children = sourceDir.listFiles();

		for (File sourceChild : children) {
			String name = sourceChild.getName();
			File destChild = new File(destDir, name);
			if (sourceChild.isDirectory()) {
				copyDirectory(sourceChild, destChild);
			} else {
				copyFile(sourceChild, destChild);
			}
		}
	}

	public static void delete(File resource, boolean onlyChilds)
			throws IOException {
		if (resource.isDirectory()) {
			File[] childFiles = resource.listFiles();
			for (File child : childFiles) {
				delete(child, false);
			}

		}
		if (!onlyChilds) {
			resource.delete();
		} 
	}
}
