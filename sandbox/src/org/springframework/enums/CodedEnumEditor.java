/*
 * Copyright 2002-2004 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.springframework.enums;

import java.beans.PropertyEditorSupport;
import java.util.Locale;

import org.springframework.enums.support.StaticCodedEnumResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Property Editor converts the string form of a CodedEnum into a CodedEnum
 * instance using a CodedEnumResolver.
 *
 * @author Keith Donald
 */
public class CodedEnumEditor extends PropertyEditorSupport {

	private Locale locale = Locale.getDefault();

	private CodedEnumResolver resolver = StaticCodedEnumResolver.instance();

	/**
	 * Set the resolver to used to lookup enums.
	 *
	 * @param resolver
	 *            the coded enum resolver
	 */
	public void setEnumResolver(CodedEnumResolver resolver) {
		this.resolver = resolver;
	}

	/**
	 * Sets the locale to use when resolving enums.
	 *
	 * @param locale
	 *            the locale
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	private Locale getLocale() {
		return Locale.getDefault();
	}

	public void setAsText(String text) throws IllegalArgumentException {
		String[] keyParts = StringUtils.delimitedListToStringArray(text, ".");
		Assert.isTrue(keyParts.length == 2,
				"Enum string key must in the format '<type>.<code>'");

		Object code;
		String strCode = keyParts[1];
		if (strCode.length() == 1) {
			char c = strCode.charAt(0);
			if (Character.isLetter(c)) {
				code = new Character(c);
			}
			else if (Character.isDigit(c)) {
				code = new Integer(c);
			}
			else {
				throw new IllegalArgumentException("Invalid enum code '"
						+ strCode + "'");
			}
		}
		else {
			try {
				code = new Integer(strCode);
			}
			catch (NumberFormatException e) {
				code = strCode;
			}
		}
		CodedEnum enum = resolver.getEnum(keyParts[0], code, getLocale());
		Assert.notNull(enum, "No enum with string key '" + text
				+ "' was found.");
		setValue(enum);
	}

	public String getAsText() {
		CodedEnum enum = (CodedEnum) getValue();
		return enum.getKey();
	}

}