package org.springframework.web.multipart.cos;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.oreilly.servlet.MultipartRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.util.WebUtils;

/**
 * MultipartResolver implementation for Jason Hunter's
 * <a href="http://www.servlets.com/cos">COS (com.oreilly.servlet)</a>.
 * Works with a COS MultipartRequest underneath.
 *
 * <p>Provides maxUploadSize and headerEncoding settings as bean properties;
 * see respective MultipartRequest constructor parameters for details.
 * Default maximum file size is unlimited; default encoding is the platform's default.
 *
 * @author Juergen Hoeller
 * @since 06.10.2003
 * @see CosMultipartHttpServletRequest
 * @see com.oreilly.servlet.MultipartRequest
 */
public class CosMultipartResolver implements MultipartResolver, ServletContextAware {

	public static final String MULTIPART_CONTENT_TYPE = "multipart/form-data";


	protected final Log logger = LogFactory.getLog(getClass());

	private int maxUploadSize = Integer.MAX_VALUE;

	private String headerEncoding;

	private File uploadTempDir;


	/**
	 * Constructor for use as bean. Determines the servlet container's
	 * temporary directory via the ServletContext passed in as through the
	 * ServletContextAware interface (typically by a WebApplicationContext).
	 * @see #setServletContext
	 * @see org.springframework.web.context.ServletContextAware
	 * @see org.springframework.web.context.WebApplicationContext
	 */
	public CosMultipartResolver() {
	}

	/**
	 * Constructor for standalone usage. Determines the servlet container's
	 * temporary directory via the given ServletContext.
	 * @param servletContext the ServletContext to use
	 */
	public CosMultipartResolver(ServletContext servletContext) {
		this.uploadTempDir = WebUtils.getTempDir(servletContext);
	}

	/**
	 * Set the maximum allowed file size (in bytes) before uploads are refused.
	 * -1 indicates no limit (the default).
	 * @param maxUploadSize the maximum file size allowed
	 */
	public void setMaxUploadSize(int maxUploadSize) {
		this.maxUploadSize = maxUploadSize;
	}

	protected int getMaxUploadSize() {
		return maxUploadSize;
	}

	/**
	 * Set the character encoding to be used when reading the headers of individual parts.
	 * When not specified, or <code>null</code>, the platform default encoding is used.
	 * @param headerEncoding the character encoding to use
	 * @see org.apache.commons.fileupload.FileUploadBase#setHeaderEncoding
	 */
	public void setHeaderEncoding(String headerEncoding) {
		this.headerEncoding = headerEncoding;
	}

	protected String getHeaderEncoding() {
		return headerEncoding;
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
		this.uploadTempDir = uploadTempDir.getFile();
	}

	protected File getUploadTempDir() {
		return uploadTempDir;
	}

	public void setServletContext(ServletContext servletContext) {
		if (this.uploadTempDir == null) {
			this.uploadTempDir = WebUtils.getTempDir(servletContext);
		}
	}


	public boolean isMultipart(HttpServletRequest request) {
		return request.getContentType() != null && request.getContentType().startsWith(MULTIPART_CONTENT_TYPE);
	}

	public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
		try {
			MultipartRequest multipartRequest = newMultipartRequest(request);
			if (logger.isDebugEnabled()) {
				Enumeration fileNames = multipartRequest.getFileNames();
				while (fileNames.hasMoreElements()) {
					String fileName = (String) fileNames.nextElement();
					File file = multipartRequest.getFile(fileName);
					logger.debug("Found multipart file '" + fileName + "' of size " + (file != null ? file.length() : 0) +
											 " bytes with original file name [" + multipartRequest.getOriginalFileName(fileName) +
											 "], " + (file != null ? "stored at [" + file.getAbsolutePath() + "]" : "empty"));
				}
			}
			return new CosMultipartHttpServletRequest(request, multipartRequest);
		}
		catch (IOException ex) {
			throw new MultipartException("Could not parse multipart request", ex);
		}
	}

	/**
	 * Create a com.oreilly.servlet.MultipartRequest for the given HTTP request.
	 * Can be overridden to use a custom subclass, e.g. for testing purposes.
	 * @param request current HTTP request
	 * @return the new MultipartRequest
	 * @throws IOException if thrown by the MultipartRequest constructor
	 */
	protected MultipartRequest newMultipartRequest(HttpServletRequest request) throws IOException {
		String tempPath = this.uploadTempDir.getAbsolutePath();
		return this.headerEncoding != null ?
		    new MultipartRequest(request, tempPath, this.maxUploadSize, this.headerEncoding) :
				new MultipartRequest(request, tempPath, this.maxUploadSize);
	}

	public void cleanupMultipart(MultipartHttpServletRequest request) {
		MultipartRequest multipartRequest = ((CosMultipartHttpServletRequest) request).getMultipartRequest();
		Enumeration fileNames = multipartRequest.getFileNames();
		while (fileNames.hasMoreElements()) {
			String fileName = (String) fileNames.nextElement();
			File file = multipartRequest.getFile(fileName);
			if (file != null) {
				if (file.exists()) {
					if (file.delete()) {
						if (logger.isDebugEnabled()) {
						logger.debug("Cleaned up multipart file '" + fileName + "' with original file name [" +
												 multipartRequest.getOriginalFileName(fileName) +
												 "], stored at [" + file.getAbsolutePath() + "]");
						}
					}
					else {
						logger.warn("Could not delete multipart file '" + fileName + "' with original file name [" +
						            multipartRequest.getOriginalFileName(fileName) +
						            "], stored at [" + file.getAbsolutePath() + "]");
					}
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug("Multipart file '" + fileName + "' with original file name [" +
												 multipartRequest.getOriginalFileName(fileName) +
												 "] has already been moved - no cleanup necessary");
					}
				}
			}
		}
	}

}
