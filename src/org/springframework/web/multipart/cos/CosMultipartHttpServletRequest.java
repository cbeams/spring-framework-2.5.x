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

package org.springframework.web.multipart.cos;

import java.io.ByteArrayInputStream;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.AbstractMultipartHttpServletRequest;

/**
 * MultipartHttpServletRequest implementation for Jason Hunter's COS.
 * Wraps a COS MultipartRequest with Spring MultipartFile instances.
 *
 * <p>Not intended for direct application usage. Application code can cast
 * to this implementation to access the underlying COS MultipartRequest,
 * if it ever needs to.
 *
 * @author Juergen Hoeller
 * @since 06.10.2003
 * @see CosMultipartResolver
 * @see com.oreilly.servlet.MultipartRequest
 */
public class CosMultipartHttpServletRequest extends AbstractMultipartHttpServletRequest {

	private final MultipartRequest multipartRequest;

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

		public boolean isEmpty() {
			return (multipartRequest.getFile(this.name) == null);
		}

		public String getOriginalFilename() {
			return multipartRequest.getOriginalFileName(this.name);
		}

		public String getContentType() {
			return multipartRequest.getContentType(this.name);
		}

		public long getSize() {
			File file = multipartRequest.getFile(this.name);
			return (file != null ? file.length() : 0);
		}

		public byte[] getBytes() throws IOException {
			File file = multipartRequest.getFile(this.name);
			return (file != null ? FileCopyUtils.copyToByteArray(file) : new byte[0]);
		}

		public InputStream getInputStream() throws IOException {
			File file = multipartRequest.getFile(this.name);
			return (file != null ? (InputStream) new FileInputStream(file) : new ByteArrayInputStream(new byte[0]));
		}

		public void transferTo(File dest) throws IOException, IllegalStateException {
			File tempFile = multipartRequest.getFile(this.name);
			if (tempFile != null) {
				if (!tempFile.exists()) {
					throw new IllegalStateException("File has already been moved - cannot be transferred again");
				}
				if (dest.exists() && !dest.delete()) {
					throw new IOException("Destination file [" + dest.getAbsolutePath() +
					                      "] already exists and could not be deleted");
				}
				if (tempFile.renameTo(dest)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Multipart file [" + getName() + "] with original file name [" +
												 getOriginalFilename() + "], stored at [" + tempFile.getAbsolutePath() +
												 "]: moved to [" + dest.getAbsolutePath() + "]");
					}
				}
				else {
					FileCopyUtils.copy(tempFile, dest);
					if (logger.isDebugEnabled()) {
						logger.debug("Multipart file [" + getName() + "] with original file name [" +
												 getOriginalFilename() + "], stored at [" + tempFile.getAbsolutePath() +
												 "]: copied to [" + dest.getAbsolutePath() + "]");
					}
				}
			}
			else {
				dest.createNewFile();
			}
		}
	}

}
