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

package org.springframework.web.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Rod Johnson
 * @version $RevisionId$
 */
public class MockHttpServletResponse implements HttpServletResponse {

	private HashMap headers = new HashMap();

	private Locale locale;

	private int bufsize = 12096;

	private int status = HttpServletResponse.SC_OK;

	public String forwarded;
	public String included;
	public String redirected;
	
	public List cookies = new ArrayList(); 

	/** Creates new MockHttpServletResponse */
	public MockHttpServletResponse() {
	}

	public boolean containsHeader(String str) {
		return headers.get(str) != null;
	}

	public String encodeUrl(String str) {
		throw new UnsupportedOperationException("encodeUrl");
	}

	public void setHeader(String str, String str1) {
		headers.put(str, str1);
	}

	public Locale getLocale() {
		return locale;
	}
	
	public javax.servlet.http.Cookie[] getCookies() {
		return (Cookie[]) cookies.toArray(new Cookie[cookies.size()]);
	}

	public void flushBuffer() throws java.io.IOException {
	}

	public void addCookie(javax.servlet.http.Cookie cookie) {
		cookies.add(cookie);
	}

	public void sendError(int param) throws java.io.IOException {
		this.status = param;
	}

	public int getBufferSize() {
		return bufsize;
	}

	public void addDateHeader(String str, long param) {
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setBufferSize(int param) {
		this.bufsize = param;
	}

	public String encodeRedirectURL(String str) {
		throw new UnsupportedOperationException("encodeRedirectUrl");
	}

	public void setStatus(int param, String str) {
		this.status = param;
	}

	public java.io.PrintWriter getWriter() throws java.io.IOException {
		return new java.io.PrintWriter(System.out);
	}

	public boolean isCommitted() {
		return false;
	}

	public String getCharacterEncoding() {
		throw new UnsupportedOperationException("getCharacterEncoding");
	}

	public void setDateHeader(String str, long param) {
		headers.put(str, "" + param);
	}

	public javax.servlet.ServletOutputStream getOutputStream() throws java.io.IOException {
		throw new UnsupportedOperationException("getOutputStream");
	}

	public void addIntHeader(String str, int param) {
	}

	public String encodeRedirectUrl(String str) {
		throw new UnsupportedOperationException("encodeRedirectUrl");
	}

	public void setIntHeader(String str, int param) {
	}

	public void setContentType(String str) {
	}

	public void setContentLength(int param) {
	}

	public String encodeURL(String str) {
		throw new UnsupportedOperationException("encodeUrl");
	}

	public void sendRedirect(String str) throws java.io.IOException {
		redirected = str;
		System.out.println(">>>>>>>>>>>>>>> request.sendRedirect to [" + str + "]");
	}

	public void reset() {
	}

	public void addHeader(String str, String str1) {
		headers.put(str, str1);
	}

	public String getHeader(String str) {
		return (String) headers.get(str);
	}

	public void sendError(int param, String str) throws java.io.IOException {
		this.status = param;
	}

	public void setStatus(int param) {
		this.status = param;
	}

	public void resetBuffer() {
	}


	public int getStatusCode() {
		return status;
	}

}
