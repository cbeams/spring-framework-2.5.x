package org.springframework.web.multipart.commons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.DefaultFileItem;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

/**
 * <a href="http://jakarta.apache.org/commons/fileupload">Jakarta Commons FileUpload</a>
 * implementation for MultipartFile.
 * @author Trevor D. Cook
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

	public String getOriginalFileName() {
		return new File(this.fileItem.getName()).getName();
	}

	public String getContentType() {
		return this.fileItem.getContentType();
	}

	public long getSize() {
		return this.fileItem.getSize();
	}

	public byte[] getBytes() {
		return this.fileItem.get();
	}

	public InputStream getInputStream() throws MultipartException {
		try {
			return this.fileItem.getInputStream();
		}
		catch (IOException ex) {
			throw new MultipartException("Could not read contents", ex);
		}
	}

	public void transferTo(File dest) throws MultipartException, IllegalStateException {
		try {
			this.fileItem.write(dest);
			logger.debug("Multipart file [" + getName() + "] with original file name [" +
									 getOriginalFileName() + "], stored " + getStorageDescription() +
									 ": moved to [" + dest.getAbsolutePath() + "]");
		}
		catch (FileUploadException ex) {
			throw new IllegalStateException(ex.getMessage());
		}
		catch (Exception ex) {
			throw new MultipartException("Could not transfer to file", ex);
		}
	}

	protected String getStorageDescription() {
		if (this.fileItem.isInMemory())
			return "in memory";
		else if (this.fileItem instanceof DefaultFileItem)
			return "at [" + ((DefaultFileItem) fileItem).getStoreLocation().getAbsolutePath() + "]";
		else
			return "at disk";
	}

}
