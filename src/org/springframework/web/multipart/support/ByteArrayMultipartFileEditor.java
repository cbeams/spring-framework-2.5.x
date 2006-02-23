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

package org.springframework.web.multipart.support;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.propertyeditors.ByteArrayPropertyEditor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Custom PropertyEditor for converting MultipartFiles to byte arrays.
 *
 * @author Juergen Hoeller
 * @since 13.10.2003
 */
public class ByteArrayMultipartFileEditor extends ByteArrayPropertyEditor {

	/** Static to avoid creating a new logger every time */
	private static final Log logger = LogFactory.getLog(ByteArrayMultipartFileEditor.class);


	public void setValue(Object value) {
		if (value instanceof MultipartFile) {
			MultipartFile multipartFile = (MultipartFile) value;
			try {
				super.setValue(multipartFile.getBytes());
			}
			catch (IOException ex) {
				logger.warn("Cannot read contents of multipart file", ex);
				throw new IllegalArgumentException("Cannot read contents of multipart file: " + ex.getMessage());
			}
		}
		else if (value instanceof byte[]) {
			super.setValue(value);
		}
		else {
			super.setValue(value != null ? value.toString().getBytes() : null);
		}
	}

	public String getAsText() {
		byte[] value = (byte[]) getValue();
		return (value != null ? new String(value) : "");
	}

}
