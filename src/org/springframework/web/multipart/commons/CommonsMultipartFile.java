/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.web.multipart.commons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.DefaultFileItem;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.multipart.MultipartFile;

/**
 * MultipartFile implementation for Jakarta Commons FileUpload.
 * @author Trevor D. Cook
 * @author Juergen Hoeller
 * @since 29-Sep-2003
 * @see org.springframework.web.multipart.commons.CommonsMultipartResolver
 */
public class CommonsMultipartFile implements MultipartFile {

	protected final Log logger = LogFactory.getLog(getClass());

	private final FileItem fileItem;

	/**
	 * Create an instance wrapping the given FileItem.
	 * @param fileItem the FileItem to wrap
	 */
	protected CommonsMultipartFile(FileItem fileItem) {
		this.fileItem = fileItem;
	}

	/**
	 * Return the underlying org.apache.commons.fileupload.FileItem instance.
	 * There is hardly any need to access this.
	 */
	public FileItem getFileItem() {
		return fileItem;
	}

	public String getName() {
		return this.fileItem.getFieldName();
	}

	public boolean isEmpty() {
		return (this.fileItem.getName() == null || this.fileItem.getName().length() == 0);
	}

	public String getOriginalFilename() {
		return (!isEmpty() ? new File(this.fileItem.getName()).getName() : null);
	}

	public String getContentType() {
		return (!isEmpty() ? this.fileItem.getContentType() : null);
	}

	public long getSize() {
		return this.fileItem.getSize();
	}

	public byte[] getBytes() {
		return this.fileItem.get();
	}

	public InputStream getInputStream() throws IOException {
		return this.fileItem.getInputStream();
	}

	public void transferTo(File dest) throws IOException, IllegalStateException {
		if (dest.exists() && !dest.delete()) {
			throw new IOException("Destination file [" + dest.getAbsolutePath() +
			                      "] already exists and could not be deleted");
		}
		try {
			this.fileItem.write(dest);
			if (logger.isDebugEnabled()) {
				String action = "transferred";
				if (this.fileItem instanceof DefaultFileItem) {
					action = ((DefaultFileItem) this.fileItem).getStoreLocation().exists() ? "copied" : "moved";
				}
				logger.debug("Multipart file [" + getName() + "] with original file name [" +
										 getOriginalFilename() + "], stored " + getStorageDescription() + ": " +
				             action + " to [" + dest.getAbsolutePath() + "]");
			}
		}
		catch (FileUploadException ex) {
			throw new IllegalStateException(ex.getMessage());
		}
		catch (IOException ex) {
			throw ex;
		}
		catch (Exception ex) {
			logger.error("Could not transfer to file", ex);
			throw new IOException("Could not transfer to file: " + ex.getMessage());
		}
	}

	protected String getStorageDescription() {
		if (this.fileItem.isInMemory()) {
			return "in memory";
		}
		else if (this.fileItem instanceof DefaultFileItem) {
			return "at [" + ((DefaultFileItem) this.fileItem).getStoreLocation().getAbsolutePath() + "]";
		}
		else {
			return "at disk";
		}
	}

}
