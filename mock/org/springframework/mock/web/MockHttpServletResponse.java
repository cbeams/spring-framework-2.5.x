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

package org.springframework.mock.web;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Mock implementation of the HttpServletResponse interface.
 *
 * <p>Used for testing the web framework; also useful
 * for testing application controllers.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class MockHttpServletResponse implements HttpServletResponse {

	public static final int DEFAULT_SERVER_PORT = 80;


	//---------------------------------------------------------------------
	// ServletResponse properties
	//---------------------------------------------------------------------

	private String characterEncoding;

	private final ByteArrayOutputStream content = new ByteArrayOutputStream();

	private final DelegatingServletOutputStream outputStream = new DelegatingServletOutputStream(this.content);

	private PrintWriter writer;

	private int contentLength;

	private String contentType;

	private int bufferSize = 4096;

	private boolean committed;

	private Locale locale;


	//---------------------------------------------------------------------
	// HttpServletResponse properties
	//---------------------------------------------------------------------

	private final List cookies = new ArrayList();

	private final Hashtable headers = new Hashtable();

	private int status = HttpServletResponse.SC_OK;

	private String redirectedUrl;

	private String forwardedUrl;

	private String includedUrl;


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	public MockHttpServletResponse() {
	}


	//---------------------------------------------------------------------
	// ServletResponse interface
	//---------------------------------------------------------------------

	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	public String getCharacterEncoding() {
		return characterEncoding;
	}

	public ServletOutputStream getOutputStream() {
		return this.outputStream;
	}

	public PrintWriter getWriter() throws UnsupportedEncodingException {
		if (this.writer == null) {
			Writer targetWriter = (this.characterEncoding != null ?
					new OutputStreamWriter(this.content, this.characterEncoding) : new OutputStreamWriter(this.content));
			this.writer = new PrintWriter(targetWriter);
		}
		return writer;
	}

	public byte[] getContentAsByteArray() {
		return this.content.toByteArray();
	}

	public String getContentAsString() throws UnsupportedEncodingException {
		return (this.characterEncoding != null) ?
				this.content.toString(this.characterEncoding) : this.content.toString();
	}

	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	public int getContentLength() {
		return contentLength;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void flushBuffer() {
		if (this.writer != null) {
			this.writer.flush();
		}
	}

	public void resetBuffer() {
		this.content.reset();
	}

	public void setCommitted(boolean committed) {
		this.committed = committed;
	}

	public boolean isCommitted() {
		return committed;
	}

	public void reset() {
		resetBuffer();
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public Locale getLocale() {
		return locale;
	}


	//---------------------------------------------------------------------
	// HttpServletResponse interface
	//---------------------------------------------------------------------

	public void addCookie(Cookie cookie) {
		this.cookies.add(cookie);
	}

	public Cookie[] getCookies() {
		return (Cookie[]) this.cookies.toArray(new Cookie[this.cookies.size()]);
	}

	public boolean containsHeader(String name) {
		return this.headers.contains(name);
	}

	public Object getHeader(String name) {
		return this.headers.get(name);
	}

	public String encodeURL(String url) {
		return url;
	}

	public String encodeRedirectURL(String url) {
		return url;
	}

	public String encodeUrl(String url) {
		return url;
	}

	public String encodeRedirectUrl(String url) {
		return url;
	}

	public void sendError(int param, String message) {
		this.status = param;
	}

	public void sendError(int status) {
		this.status = status;
	}

	public void sendRedirect(String url) {
		this.redirectedUrl = url;
	}

	public String getRedirectedUrl() {
		return redirectedUrl;
	}

	public void setDateHeader(String name, long value) {
		this.headers.put(name, new Long(value));
	}

	public void addDateHeader(String name, long value) {
		this.headers.put(name, new Long(value));
	}

	public void setHeader(String name, String value) {
		this.headers.put(name, value);
	}

	public void addHeader(String name, String value) {
		this.headers.put(name, value);
	}

	public void setIntHeader(String name, int value) {
		this.headers.put(name, new Integer(value));
	}

	public void addIntHeader(String name, int value) {
		this.headers.put(name, new Integer(value));
	}

	public void setStatus(int status, String message) {
		this.status = status;
	}

	public void setStatus(int param) {
		this.status = param;
	}

	public int getStatus() {
		return status;
	}


	//---------------------------------------------------------------------
	// Methods for MockRequestDispatcher
	//---------------------------------------------------------------------

	public void setForwardedUrl(String forwardedUrl) {
		this.forwardedUrl = forwardedUrl;
	}

	public String getForwardedUrl() {
		return forwardedUrl;
	}

	public void setIncludedUrl(String includedUrl) {
		this.includedUrl = includedUrl;
	}

	public String getIncludedUrl() {
		return includedUrl;
	}

}
