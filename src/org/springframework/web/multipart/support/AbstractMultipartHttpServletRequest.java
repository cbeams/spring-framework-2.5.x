package org.springframework.web.multipart.support;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Abstract base implementation of the MultipartHttpServletRequest interface.
 * Provides management of pre-generated MultipartFile instances.
 * @author Juergen Hoeller
 * @since 06.10.2003
 */
public abstract class AbstractMultipartHttpServletRequest extends HttpServletRequestWrapper
    implements MultipartHttpServletRequest {

	protected final Log logger = LogFactory.getLog(getClass());

	private Map multipartFiles;

	protected AbstractMultipartHttpServletRequest(HttpServletRequest request) {
		super(request);
	}

	protected void setMultipartFiles(Map multipartFiles) {
		this.multipartFiles = multipartFiles;
	}

	public Iterator getFileNames() {
		return this.multipartFiles.keySet().iterator();
	}

	public MultipartFile getFile(String name) {
		return (MultipartFile) this.multipartFiles.get(name);
	}

	public Map getFileMap() {
		return this.multipartFiles;
	}

}
