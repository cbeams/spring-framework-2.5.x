package org.springframework.web.multipart.cos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.oreilly.servlet.MultipartRequest;

import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.AbstractMultipartHttpServletRequest;

/**
 * MultipartHttpServletRequest implementation for Jason Hunter's COS.
 * Wraps a COS MultipartRequest with Spring MultipartFile instances.
 *
 * <p>Not intended for direct application usage. Application code can cast
 * to this implementation to access the underlying COS MultipartRequest,
 * if it ever neeeds to.
 *
 * @author Juergen Hoeller
 * @since 06.10.2003
 * @see CosMultipartResolver
 * @see com.oreilly.servlet.MultipartRequest
 */
public class CosMultipartHttpServletRequest extends AbstractMultipartHttpServletRequest {

	private MultipartRequest multipartRequest;

	protected CosMultipartHttpServletRequest(HttpServletRequest originalRequest, MultipartRequest multipartRequest) {
		super(originalRequest);
		this.multipartRequest = multipartRequest;
		setMultipartFiles(initFileMap(multipartRequest));
	}

	/**
	 * Return the underlying com.oreilly.servlet.MultipartRequest instance.
	 * There is hardly any need to access this.
	 */
	public MultipartRequest getMultipartRequest() {
		return multipartRequest;
	}

	protected Map initFileMap(MultipartRequest multipartRequest) {
		Map files = new HashMap();
		Enumeration fileNames = multipartRequest.getFileNames();
		while (fileNames.hasMoreElements()) {
			String fileName = (String) fileNames.nextElement();
			files.put(fileName, new CosMultipartFile(fileName));
		}
		return files;
	}

	public Enumeration getParameterNames() {
		return this.multipartRequest.getParameterNames();
	}

	public String getParameter(String name) {
		return this.multipartRequest.getParameter(name);
	}

	public String[] getParameterValues(String name) {
		return this.multipartRequest.getParameterValues(name);
	}

	public Map getParameterMap() {
		Map params = new HashMap();
		Enumeration names = getParameterNames();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			params.put(name, getParameterValues(name));
		}
		return Collections.unmodifiableMap(params);
	}


	private class CosMultipartFile implements MultipartFile {

		private String name;

		private CosMultipartFile(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public String getOriginalFileName() {
			return multipartRequest.getOriginalFileName(this.name);
		}

		public String getContentType() {
			return multipartRequest.getContentType(this.name);
		}

		public long getSize() {
			return multipartRequest.getFile(this.name).length();
		}

		public byte[] getBytes() throws MultipartException {
			try {
				return FileCopyUtils.copyToByteArray(multipartRequest.getFile(this.name));
			}
			catch (IOException ex) {
				throw new MultipartException("Could not read contents", ex);
			}
		}

		public InputStream getInputStream() throws MultipartException {
			try {
				return new FileInputStream(multipartRequest.getFile(this.name));
			}
			catch (IOException ex) {
				throw new MultipartException("Could not read contents", ex);
			}
		}

		public void transferTo(File dest) throws MultipartException, IllegalStateException {
			File temp = multipartRequest.getFile(this.name);
			if (!temp.exists()) {
				throw new IllegalStateException("File has already been moved -- cannot be transferred again");
			}
			else if (temp.renameTo(dest)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Multipart file [" + getName() + "] with original file name [" +
											 getOriginalFileName() + "], stored at [" + temp.getAbsolutePath() +
											 "]: moved to [" + dest.getAbsolutePath() + "]");
				}
			}
			else {
				try {
					FileCopyUtils.copy(temp, dest);
				}
				catch (IOException ex) {
					throw new MultipartException("Could not read contents", ex);
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Multipart file [" + getName() + "] with original file name [" +
											 getOriginalFileName() + "], stored at [" + temp.getAbsolutePath() +
											 "]: copied to [" + dest.getAbsolutePath() + "]");
				}
			}
		}
	}

}
