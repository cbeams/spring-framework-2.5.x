package org.springframework.web.multipart.commons;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

/**
 * MultipartResolver implementation for
 * <a href="http://jakarta.apache.org/commons/fileupload">Jakarta Commons FileUpload</a>.
 *
 * <p>Provides maximumFileSize, maximumInMemorySize, and headerEncoding settings as
 * bean properties; see respective DiskFileUpload properties (sizeMax, sizeThreshold,
 * headerEncoding) for details in terms of defaults and accepted values.
 *
 * <p>Saves temporary files to the servlet container's temporary directory.
 * Needs to be initialized <i>either</i> by an application context <i>or</i>
 * via the constructor that takes a ServletContext (for standalone usage).
 *
 * @author Trevor D. Cook
 * @author Juergen Hoeller
 * @since 29-Sep-2003
 * @see #CommonsMultipartResolver(ServletContext)
 * @see CommonsMultipartFile
 * @see org.apache.commons.fileupload.DiskFileUpload
 */
public class CommonsMultipartResolver extends WebApplicationObjectSupport implements MultipartResolver {

	protected final Log logger = LogFactory.getLog(getClass());

	private DiskFileUpload fileUpload;

	private File uploadTempDir;

	/**
	 * Constructor for use as bean in an application context.
	 * Determines the servlet container's temporary directory via the application context.
	 */
	public CommonsMultipartResolver() {
		this.fileUpload = newFileUpload();
	}

	/**
	 * Constructor for standalone usage.
	 * Determines the servlet container's temporary directory via the given ServletContext.
	 */
	public CommonsMultipartResolver(ServletContext servletContext) {
		this();
		this.fileUpload.setRepositoryPath(WebUtils.getTempDir(servletContext).getAbsolutePath());
	}

	/**
	 * Initialize the underlying org.apache.commons.fileupload.DiskFileUpload instance.
	 * Can be overridden to use a custom subclass, e.g. for testing purposes.
	 * @return the new DiskFileUpload instance
	 */
	protected DiskFileUpload newFileUpload() {
		return new DiskFileUpload();
	}

	/**
	 * Return the underlying org.apache.commons.fileupload.DiskFileUpload instance.
	 * There is hardly any need to access this.
	 * @return the underlying DiskFileUpload instance
	 */
	public DiskFileUpload getFileUpload() {
		return fileUpload;
	}

	/**
	 * Set the maximum allowed size (in bytes) before uploads are refused.
	 * -1 indicates no limit (the default).
	 * @param maxUploadSize the maximum upload size allowed
	 * @see org.apache.commons.fileupload.FileUploadBase#setSizeMax
	 */
	public void setMaxUploadSize(long maxUploadSize) {
		this.fileUpload.setSizeMax(maxUploadSize);
	}

	/**
	 * Set the maximum allowed size (in bytes) before uploads are written to disk.
	 * Uploaded files will still be received past this amount, but they will not be
	 * stored in memory. Default is 10240, according to Commons FileUpload.
	 * @param maxInMemorySize the maximum in memory size allowed
	 * @see org.apache.commons.fileupload.DiskFileUpload#setSizeThreshold
	 */
	public void setMaxInMemorySize(int maxInMemorySize) {
		this.fileUpload.setSizeThreshold(maxInMemorySize);
	}

	/**
	 * The character encoding to be used when reading the headers of individual parts.
	 * When not specified, or <code>null</code>, the platform default encoding is used.
	 * @param headerEncoding the character encoding to use
	 * @see org.apache.commons.fileupload.FileUploadBase#setHeaderEncoding
	 */
	public void setHeaderEncoding(String headerEncoding) {
		this.fileUpload.setHeaderEncoding(headerEncoding);
	}

	/**
	 * Set the temporary directory where uploaded files get stored.
	 * Default is the servlet container's temporary directory for the web application.
	 * @see org.springframework.web.util.WebUtils#TEMP_DIR_CONTEXT_ATTRIBUTE
	 */
	public void setUploadTempDir(Resource uploadTempDir) throws IOException {
		if (!uploadTempDir.exists() && !uploadTempDir.getFile().mkdirs()) {
			throw new IllegalArgumentException("Given uploadTempDir [" + uploadTempDir +
																				 "] could not be created");
		}
		this.fileUpload.setRepositoryPath(uploadTempDir.getFile().getAbsolutePath());
		this.uploadTempDir = uploadTempDir.getFile();
	}

	protected void initApplicationContext() {
		if (this.uploadTempDir == null) {
			this.fileUpload.setRepositoryPath(getTempDir().getAbsolutePath());
		}
	}


	public boolean isMultipart(HttpServletRequest request) {
		return FileUploadBase.isMultipartContent(request);
	}

	public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
		try {
			List fileItems = this.fileUpload.parseRequest(request);
			Map parameters = new HashMap();
			Map multipartFiles = new HashMap();
			for (Iterator i = fileItems.iterator(); i.hasNext();) {
				FileItem fileItem = (FileItem) i.next();
				if (fileItem.isFormField()) {
					String[] curParam = (String[]) parameters.get(fileItem.getFieldName());
					if (curParam == null) {
						// simple form field
						parameters.put(fileItem.getFieldName(), new String[] { fileItem.getString() });
					}
					else {
						// array of simple form fields
						String[] newParam = StringUtils.addStringToArray(curParam, fileItem.getString());
						parameters.put(fileItem.getFieldName(), newParam);
					}
				}
				else {
					// multipart file field
					CommonsMultipartFile file = new CommonsMultipartFile(fileItem);
					multipartFiles.put(file.getName(), file);
					if (logger.isDebugEnabled()) {
						logger.debug("Found multipart file [" + file.getName() + "] of size " + file.getSize() +
						             " bytes with original file name [" + file.getOriginalFileName() +
						             "], stored " + file.getStorageDescription());
					}
				}
			}
			return new DefaultMultipartHttpServletRequest(request, multipartFiles, parameters);
		}
		catch (FileUploadException ex) {
			throw new MultipartException("Could not parse multipart request", ex);
		}
	}

	public void cleanupMultipart(MultipartHttpServletRequest request) {
		Map multipartFiles = request.getFileMap();
		for (Iterator i = multipartFiles.keySet().iterator(); i.hasNext();) {
			String name = (String) i.next();
			CommonsMultipartFile file = (CommonsMultipartFile) multipartFiles.get(name);
			logger.debug("Cleaning up multipart file [" + file.getName() + "] with original file name [" +
			             file.getOriginalFileName() + "], stored " + file.getStorageDescription());
			file.getFileItem().delete();
		}
	}

}
