/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.mock.web.portlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Locale;

import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;

import org.springframework.web.util.WebUtils;

/**
 * Mock implementation of the RenderResponse interface.
 *
 * @author John A. Lewis
 * @since 2.0
 */
public class MockRenderResponse extends MockPortletResponse implements RenderResponse {

	private String contentType;

	private String namespace = "MockPortlet";

	private String title;

	private String characterEncoding = WebUtils.DEFAULT_CHARACTER_ENCODING;

	private PrintWriter writer;

	private Locale locale = Locale.getDefault();

	private int bufferSize = 4096;

	private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	private boolean committed;

	private String includedUrl;

	
	//---------------------------------------------------------------------
	// RenderResponse methods
	//---------------------------------------------------------------------

	public String getContentType() {
		return this.contentType;
	}

	public PortletURL createRenderURL() {
		PortletURL url = new MockPortletURL("render");
		return url;
	}

	public PortletURL createActionURL() {
		PortletURL url = new MockPortletURL("action");
		return url;
	}

	public String getNamespace() {
		return this.namespace;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getCharacterEncoding() {
		return this.characterEncoding;
	}

	public PrintWriter getWriter() throws UnsupportedEncodingException {
		if (this.writer == null) {
			Writer targetWriter = (this.characterEncoding != null
					? new OutputStreamWriter(this.outputStream, this.characterEncoding)
					: new OutputStreamWriter(this.outputStream));
			this.writer = new PrintWriter(targetWriter);
		}
		return this.writer;
	}

	public Locale getLocale() {
		return this.locale;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public int getBufferSize() {
		return this.bufferSize;
	}

	public void flushBuffer() {
		if (this.writer != null) {
			this.writer.flush();
		}
		if (this.outputStream != null) {
			try {
				this.outputStream.flush();
			}
			catch (IOException ex) {
				throw new IllegalStateException("Could not flush OutputStream: " + ex.getMessage());
			}
		}
		this.committed = true;
	}

	public void resetBuffer() {
		if (this.committed) {
			throw new IllegalStateException("Cannot reset buffer - response is already committed");
		}
		this.outputStream.reset();
	}

	public boolean isCommitted() {
		return this.committed;
	}

	public void reset() {
		resetBuffer();
		this.characterEncoding = null;
		this.contentType = null;
		this.locale = null;
	}

	public OutputStream getPortletOutputStream() throws IOException {
		return this.outputStream;
	}


	//---------------------------------------------------------------------
	// MockRenderReponse methods
	//---------------------------------------------------------------------

	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setCommitted(boolean committed) {
		this.committed = committed;
	}

	public byte[] getContentAsByteArray() {
		flushBuffer();
		return this.outputStream.toByteArray();
	}

	public String getContentAsString() throws UnsupportedEncodingException {
		flushBuffer();
		return (this.characterEncoding != null)
				? this.outputStream.toString(this.characterEncoding)
				: this.outputStream.toString();
	}

	public void setIncludedUrl(String includedUrl) {
		this.includedUrl = includedUrl;
	}

	public String getIncludedUrl() {
		return includedUrl;
	}

}
