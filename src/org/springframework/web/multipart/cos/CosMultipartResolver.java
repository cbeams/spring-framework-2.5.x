package org.springframework.web.multipart.cos;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.oreilly.servlet.MultipartRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.util.WebUtils;

/**
 * MultipartResolver implementation for Jason Hunter's
 * <a href="http://www.servlets.com/cos">COS (com.oreilly.servlet)</a>.
 * Works with a COS MultipartRequest underneath.
 *
 * <p>Provides maximumFileSize and headerEncoding settings as bean properties;
 * see respective MultipartRequest constructor parameters for details.
 * Default maximum file size is unlimited; default encoding is the platform's default.
 *
 * @author Juergen Hoeller
 * @since 06.10.2003
 * @see CosMultipartHttpServletRequest
 * @see com.oreilly.servlet.MultipartRequest
 */
public class CosMultipartResolver extends WebApplicationObjectSupport implements MultipartResolver {

	public static final String MULTIPART_CONTENT_TYPE = "multipart/form-data";

	protected final Log logger = LogFactory.getLog(getClass());

	private int maximumFileSize = Integer.MAX_VALUE;

	private String headerEncoding;

	private String uploadTempDir;

	/**
	 * Constructor for use as bean in an application context.
	 * Determines the servlet container's temporary directory via the application context.
	 */
	public CosMultipartResolver() {
	}

	/**
	 * Constructor for standalone usage.
	 * Determines the servlet container's temporary directory via the given ServletContext.
	 */
	public CosMultipartResolver(ServletContext servletContext) {
		this.uploadTempDir = WebUtils.getTempDir(servletContext).getAbsolutePath();
	}

	/**
	 * Set the maximum allowed file size (in bytes) before uploads are refused.
	 * -1 indicates no limit (the default).
	 * @param maximumFileSize the maximum file size allowed
	 * @see org.apache.commons.fileupload.FileUploadBase#setSizeMax
	 */
	public void setMaximumFileSize(int maximumFileSize) {
		this.maximumFileSize = maximumFileSize;
	}

	protected int getMaximumFileSize() {
		return maximumFileSize;
	}

	/**
	 * The character encoding to be used when reading the headers of individual parts.
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

	protected String getUploadTempDir() {
		return uploadTempDir;
	}

	protected void initApplicationContext() {
		this.uploadTempDir = getTempDir().getAbsolutePath();
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
					String msg = "Found multipart file [" + fileName + "] of size " + file.length() +
											 " bytes with original file name [" + multipartRequest.getOriginalFileName(fileName) +
											 "], stored at [" + file.getAbsolutePath() + "]";
					logger.debug(msg);
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
		return this.headerEncoding != null ?
		    new MultipartRequest(request, getUploadTempDir(), getMaximumFileSize(), getHeaderEncoding()) :
				new MultipartRequest(request, getUploadTempDir(), getMaximumFileSize());
	}

	public void cleanupMultipart(MultipartHttpServletRequest request) {
		MultipartRequest multipartRequest = ((CosMultipartHttpServletRequest) request).getMultipartRequest();
		Enumeration fileNames = multipartRequest.getFileNames();
		while (fileNames.hasMoreElements()) {
			String fileName = (String) fileNames.nextElement();
			File file = multipartRequest.getFile(fileName);
			if (file.exists()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Cleaning up multipart file [" + fileName + "] with original file name [" +
											 multipartRequest.getOriginalFileName(fileName) +
											 "], stored at [" + file.getAbsolutePath() + "]");
				}
				file.delete();
			}
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Multipart file [" + fileName + "] with original file name [" +
											 multipartRequest.getOriginalFileName(fileName) +
											 "] has already been moved -- no cleanup necessary");
				}
			}
		}
	}

}
