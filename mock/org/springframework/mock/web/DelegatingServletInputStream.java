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

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

/**
 * Delegating implementation of ServletInputStream.
 *
 * <p>Used by MockHttpServletRequest; typically not
 * necessary for testing application controllers.
 *
 * @author Juergen Hoeller
 * @since 27.04.2004
 * @see org.springframework.mock.web.MockHttpServletRequest
 */
public class DelegatingServletInputStream extends ServletInputStream {

	private final InputStream sourceStream;

	/**
	 * Create a new DelegatingServletInputStream.
	 * @param sourceStream the sourceStream InputStream
	 */
	public DelegatingServletInputStream(InputStream sourceStream) {
		this.sourceStream = sourceStream;
	}

	public InputStream getSourceStream() {
		return sourceStream;
	}

	public int read() throws IOException {
		return this.sourceStream.read();
	}

	public void close() throws IOException {
		super.close();
		this.sourceStream.close();
	}

}
