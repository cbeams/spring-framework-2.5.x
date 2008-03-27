/*
 * Copyright 2002-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.util;

import java.io.File;
import java.io.IOException;

/**
 * Utility methods for working with the file system.
 *
 * @author Rob Harrop
 * @since 2.5.3
 */
public abstract class FileSystemUtils {

	/**
	 * Delete the supplied {@link File} and, for directories, recursively delete
	 * any nested directories or files.
	 *
	 * @param root the root <code>File</code> to delete.
	 * @return <code>true</code> if the <code>File</code> was deleted,
	 * otherwise <code>false</code>.
	 */
	public static boolean deleteRecursively(File root) {
		if (root.exists()) {
			if (root.isDirectory()) {
				File[] children = root.listFiles();
				for (File file : children) {
					deleteRecursively(file);
				}
			}
			return root.delete();
		}
		return false;
	}

	/**
	 * Recursively copy the contents of <code>src</code> to <code>dest</code>.
	 *
	 * @param src the source file.
	 * @param dest the destination file.
	 * @throws IOException in the case of I/O errors.
	 */
	public static void copyRecursively(File src, File dest) throws IOException {
		dest.mkdir();
		File[] entries = src.listFiles();
		for (File file : entries) {
			File newFile = new File(dest, file.getName());
			if (file.isFile()) {
				newFile.createNewFile();
				FileCopyUtils.copy(file, newFile);
			}
			else {
				copyRecursively(file, newFile);
			}
		}
	}

}
