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
import java.io.UnsupportedEncodingException;
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
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;

/**
 * MultipartResolver implementation for
 * <a href="http://jakarta.apache.org/commons/fileupload">Jakarta Commons FileUpload</a>.
 *
 * <p>Provides maxUploadSize, maxInMemorySize, and defaultEncoding settings as
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
public class CommonsMultipartResolver implements MultipartResolver, ServletContextAware {

	protected final Log logger = LogFactory.getLog(getClass());

	private DiskFileUpload fileUpload;

	private String defaultEncoding = WebUtils.DEFAULT_CHARACTER_ENCODING;

	private File uploadTempDir;


	/**
	 * Constructor for use as bean. Determines the servlet container's
	 * temporary directory via the ServletContext passed in as through the
	 * ServletContextAware interface (typically by a WebApplicationContext).
	 * @see #setServletContext
	 * @see org.springframework.web.context.ServletContextAware
	 * @see org.springframework.web.context.WebApplicationContext
	 */
	public CommonsMultipartResolver() {
		this.fileUpload = newFileUpload();
	}

	/**
	 * Constructor for standalone usage. Determines the servlet container's
	 * temporary directory via the given ServletContext.
	 * @param servletContext the ServletContext to use
	 */
	public CommonsMultipartResolver(ServletContext servletContext) {
		this();
		setServletContext(servletContext);
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
	 * Set the default character encoding to use for parsing requests,
	 * to be applied to headers of individual parts and to form fields.
	 * Default is ISO-8859-1, according to the Servlet spec.
	 * <p>If the request specifies a character encoding itself, the request
	 * encoding will override this setting. This also allows for generically
	 * overriding the character encoding in a filter that invokes the
	 * ServletRequest.setCharacterEncoding method.
	 * @param defaultEncoding the character encoding to use
	 * @see #determineEncoding
	 * @see javax.servlet.ServletRequest#getCharacterEncoding
	 * @see javax.servlet.ServletRequest#setCharacterEncoding
	 * @see WebUtils#DEFAULT_CHARACTER_ENCODING
	 * @see org.apache.commons.fileupload.FileUploadBase#setHeaderEncoding
	 */
	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
		this.fileUpload.setHeaderEncoding(defaultEncoding);
	}

	/**
	 * Set the temporary directory where uploaded files get stored.
	 * Default is the servlet container's temporary directory for the web application.
	 * @see org.springframework.web.util.WebUtils#TEMP_DIR_CONTEXT_ATTRIBUTE
	 */
	public void setUploadTempDir(Resource uploadTempDir) throws IOException {
		if (!uploadTempDir.exists() && !uploadTempDir.getFile().mkdirs()) {
			throw new IllegalArgumentException("Given uploadTempDir [" + uploadTempDir + "] could not be created");
		}
		this.uploadTempDir = uploadTempDir.getFile();
		this.fileUpload.setRepositoryPath(uploadTempDir.getFile().getAbsolutePath());
	}

	public void setServletContext(ServletContext servletContext) {
		if (this.uploadTempDir == null) {
			this.fileUpload.setRepositoryPath(WebUtils.getTempDir(servletContext).getAbsolutePath());
		}
	}


	public boolean isMultipart(HttpServletRequest request) {
		return FileUploadBase.isMultipartContent(request);
	}

	public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
		DiskFileUpload fileUpload = this.fileUpload;
		String enc = determineEncoding(request);

		// use prototype FileUpload instance if the request specifies
		// its own encoding that does not match the default encoding
		if (!enc.equals(this.defaultEncoding)) {
			fileUpload = new DiskFileUpload();
			fileUpload.setSizeMax(this.fileUpload.getSizeMax());
			fileUpload.setSizeThreshold(this.fileUpload.getSizeThreshold());
			fileUpload.setRepositoryPath(this.fileUpload.getRepositoryPath());
			fileUpload.setHeaderEncoding(enc);
		}

		try {
			List fileItems = fileUpload.parseRequest(request);
			Map parameters = new HashMap();
			Map multipartFiles = new HashMap();
			for (Iterator it = fileItems.iterator(); it.hasNext();) {
				FileItem fileItem = (FileItem) it.next();
				if (fileItem.isFormField()) {
					String value = null;
					try {
						value = fileItem.getString(enc);
					}
					catch (UnsupportedEncodingException ex) {
						logger.warn("Could not decode multipart item '" + fileItem.getFieldName() +
						    "' with encoding '" + enc + "': using platform default");
						value = fileItem.getString();
					}
					String[] curParam = (String[]) parameters.get(fileItem.getFieldName());
					if (curParam == null) {
						// simple form field
						parameters.put(fileItem.getFieldName(), new String[] { value });
					}
					else {
						// array of simple form fields
						String[] newParam = StringUtils.addStringToArray(curParam, value);
						parameters.put(fileItem.getFieldName(), newParam);
					}
				}
				else {
					// multipart file field
					CommonsMultipartFile file = new CommonsMultipartFile(fileItem);
					multipartFiles.put(file.getName(), file);
					if (logger.isDebugEnabled()) {
						logger.debug("Found multipart file [" + file.getName() + "] of size " + file.getSize() +
						    " bytes with original filename [" + file.getOriginalFilename() + "], stored " +
						    file.getStorageDescription());
					}
				}
			}
			return new DefaultMultipartHttpServletRequest(request, multipartFiles, parameters);
		}
		catch (FileUploadBase.SizeLimitExceededException ex) {
			throw new MaxUploadSizeExceededException(this.fileUpload.getSizeMax(), ex);
		}
		catch (FileUploadException ex) {
			throw new MultipartException("Could not parse multipart request", ex);
		}
	}

	/**
	 * Determine the encoding for the given request.
	 * Can be overridden in subclasses.
	 * <p>The default implementation checks the request encoding,
	 * falling back to the default encoding specified for this resolver.
	 * @param request current HTTP request
	 * @return the encoding for the request (never null)
	 * @see javax.servlet.ServletRequest#getCharacterEncoding
	 * @see #setDefaultEncoding
	 */
	protected String determineEncoding(HttpServletRequest request) {
		String enc = request.getCharacterEncoding();
		if (enc == null) {
			enc = this.defaultEncoding;
		}
		return enc;
	}

	public void cleanupMultipart(MultipartHttpServletRequest request) {
		Map multipartFiles = request.getFileMap();
		for (Iterator i = multipartFiles.keySet().iterator(); i.hasNext();) {
			String name = (String) i.next();
			CommonsMultipartFile file = (CommonsMultipartFile) multipartFiles.get(name);
			logger.debug("Cleaning up multipart file [" + file.getName() + "] with original filename [" +
			    file.getOriginalFilename() + "], stored " + file.getStorageDescription());
			file.getFileItem().delete();
		}
	}

}
