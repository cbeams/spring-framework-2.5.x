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

package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Editor for <code>java.util.Properties</code> objects.
 * Handles conversion from String to Properties object.
 * Also handles Map to Properties conversion, for populating
 * a Properties object via XML "map" entries.
 *
 * <p>The required format is defined in <code>java.util.Properties</code>
 * documentation. Each property must be on a new line.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see java.util.Properties#load
 */
public class PropertiesEditor extends PropertyEditorSupport {
	
	/**
	 * Any of these characters, if they're first after whitespace or first
	 * on a line, mean that the line is a comment and should be ignored.
	 */
	public final static String COMMENT_MARKERS = "#!";

	/**
	 * Convert String into Properties.
	 */
	public void setAsText(String text) throws IllegalArgumentException {
		if (text == null) {
			throw new IllegalArgumentException("Cannot set Properties to null");
		}
		Properties props = new Properties();
		try {
			props.load(new ByteArrayInputStream(text.getBytes()));
			dropComments(props);
		}
		catch (IOException ex) {
			// shouldn't happen
			throw new IllegalArgumentException("Failed to parse [" + text + "] into Properties");
		}
		setValue(props);
	}

	/**
	 * Take Properties as-is; convert <code>java.util.Map</code> into Properties.
	 */
	public void setValue(Object value) {
		if (!(value instanceof Properties) && value instanceof Map) {
			Properties props = new Properties();
			props.putAll((Map) value);
			super.setValue(props);
		}
		else {
			super.setValue(value);
		}
	}

	/**
	 * Remove comment lines, even if they contain whitespace before the
	 * comment marker. This happens automatically on JDK >= 1.4, but we
	 * need to do this manually on JDK 1.3.
	 */
	private void dropComments(Properties props) {
		Iterator keys = props.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			// A comment line starts with one of our comment markers
			if (key.length() > 0 && COMMENT_MARKERS.indexOf(key.charAt(0)) != -1) {
				keys.remove();
			}
		}
	}

}
