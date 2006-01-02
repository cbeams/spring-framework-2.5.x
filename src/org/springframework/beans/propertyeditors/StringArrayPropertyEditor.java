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

import org.springframework.util.StringUtils;

/**
 * Editor for String arrays. Strings must be in CSV format,
 * with customizable separator.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.util.StringUtils#delimitedListToStringArray
 * @see org.springframework.util.StringUtils#arrayToDelimitedString
 */
public class StringArrayPropertyEditor extends PropertyEditorSupport {

	/**
	 * Default separator for splitting a String: a comma (",")
	 */
	public static final String DEFAULT_SEPARATOR = ",";

	private final String separator;


	/**
	 * Create a new StringArrayPropertyEditor with the default separator:
	 * a comma (",")
	 */
	public StringArrayPropertyEditor() {
		this.separator = DEFAULT_SEPARATOR;
	}

	/**
	 * Create a new StringArrayPropertyEditor with the given separator.
	 * @param separator the separator to use for splitting a String
	 */
	public StringArrayPropertyEditor(String separator) {
		this.separator = separator;
	}


	public void setAsText(String text) throws IllegalArgumentException {
		String[] array = StringUtils.delimitedListToStringArray(text, this.separator);
		setValue(array);
	}

	public String getAsText() {
		String[] array = (String[]) this.getValue();
		return StringUtils.arrayToDelimitedString(array, this.separator);
	}

}
