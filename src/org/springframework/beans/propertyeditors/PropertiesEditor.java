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

package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Editor for java.util.Properties objects. Handles conversion from String
 * to Properties object. Also handles Map to Properties conversion, for
 * populating a Properties object via XML "map" entries.
 *
 * <p>This editor must be registered before it will be available. Standard
 * editors in this package are automatically registered by BeanWrapperImpl.
 *
 * <p>The required format is defined in java.util.Properties documentation.
 * Each property must be on a new line.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.beans.BeanWrapperImpl
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
	 * Convert Map into Properties.
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
	 * Remove comment lines. We shouldn't need to do this, according to
	 * java.util.Properties documentation, but if we don't we end up with
	 * properties like "#this=is a comment" if we have whitespace before
	 * the comment marker.
	 */
	private void dropComments(Properties props) {
		Iterator keys = props.keySet().iterator();
		List commentKeys = new ArrayList();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			// A comment line starts with one of our comment markers
			if (key.length() > 0 && COMMENT_MARKERS.indexOf(key.charAt(0)) != -1) {
				// We can't actually remove it as we'll get a 
				// concurrent modification exception with the iterator
				commentKeys.add(key);
			}
		}
		for (Iterator it = commentKeys.iterator(); it.hasNext();) {
			String key = (String) it.next();
			props.remove(key);
		}
	}

}
