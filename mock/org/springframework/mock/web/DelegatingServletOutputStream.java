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
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;

/**
 * Delegating implementation of ServletOutputStream.
 *
 * <p>Used by MockHttpServletResponse; typically not
 * necessary for testing application controllers.
 *
 * @author Juergen Hoeller
 * @since 27.04.2004
 * @see org.springframework.mock.web.MockHttpServletResponse
 */
public class DelegatingServletOutputStream extends ServletOutputStream {

	private final OutputStream targetStream;

	/**
	 * Create a new DelegatingServletOutputStream.
	 * @param targetStream the target OutputStream
	 */
	public DelegatingServletOutputStream(OutputStream targetStream) {
		this.targetStream = targetStream;
	}

	public void write(int b) throws IOException {
		this.targetStream.write(b);
	}

	public void close() throws IOException {
		super.close();
		this.targetStream.close();
	}

}
