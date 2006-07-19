/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.web.servlet.tags.form;

import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.JspWriter;
import java.io.Writer;
import java.io.Reader;
import java.io.IOException;

/**
 * @author Rob Harrop
 * @since 2.0
*/
class MockBodyContent extends BodyContent {

	private final String mockContent;

	private final Writer realWriter;

	public MockBodyContent(String mockContent, Writer realWriter) {
		this(null, mockContent, realWriter);
	}

	public MockBodyContent(JspWriter jspWriter, String mockContent, Writer realWriter) {
		super(jspWriter);
		this.mockContent = mockContent;
		this.realWriter = realWriter;
	}

	public Reader getReader() {
		throw new UnsupportedOperationException();
	}

	public String getString() {
		return this.mockContent;
	}

	public void writeOut(Writer writer) throws IOException {
		this.realWriter.write(mockContent);
	}

	public void clear() throws IOException {
		throw new UnsupportedOperationException();
	}

	public void clearBuffer() throws IOException {
		throw new UnsupportedOperationException();
	}

	public void close() throws IOException {
		throw new UnsupportedOperationException();
	}

	public int getRemaining() {
		throw new UnsupportedOperationException();
	}

	public void newLine() throws IOException {
		throw new UnsupportedOperationException();
	}

	public void print(boolean b) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void print(char c) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void print(char[] chars) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void print(double v) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void print(float v) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void print(int i) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void print(long l) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void print(Object object) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void print(String string) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void println() throws IOException {
		throw new UnsupportedOperationException();
	}

	public void println(boolean b) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void println(char c) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void println(char[] chars) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void println(double v) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void println(float v) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void println(int i) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void println(long l) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void println(Object object) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void println(String string) throws IOException {
		throw new UnsupportedOperationException();
	}

	public void write(char cbuf[], int off, int len) throws IOException {
		throw new UnsupportedOperationException();
	}
}
