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
 * @author Keith Donald
 */
public class CodedEnumEditor extends PropertyEditorSupport {

	private Locale locale = Locale.getDefault();

	private CodedEnumResolver enumResolver = StaticCodedEnumResolver.instance();

	private boolean allowsEmpty = true;

	private Class type;

	public CodedEnumEditor() {
	}

	public CodedEnumEditor(Class type) {
		setType(type);
	}

	public CodedEnumEditor(Class type, CodedEnumResolver enumResolver) {
		setType(type);
		setEnumResolver(enumResolver);
	}

	public void setType(Class type) {
		this.type = type;
	}

	/**
	 * Set the resolver to used to lookup enums.
	 *
	 * @param resolver the coded enum resolver
	 */
	public void setEnumResolver(CodedEnumResolver resolver) {
		Assert.notNull(resolver, "The enum resolver is required");
		this.enumResolver = resolver;
	}

	public void setAllowsEmpty(boolean allowsEmpty) {
		this.allowsEmpty = allowsEmpty;
	}

	/**
	 * Sets the locale to use when resolving enums.
	 *
	 * @param locale the locale
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	private Locale getLocale() {
		return locale;
	}

	public void setAsText(String encodedCode) throws IllegalArgumentException {
		if (!StringUtils.hasText(encodedCode)) {
			Assert.isTrue(allowsEmpty, "This property editor does not allow empty encoded enum code values");
			setValue(null);
			return;
		}

		String type;
		Comparable code;

		if (this.type == null) {
			String[] keyParts = StringUtils.delimitedListToStringArray(encodedCode, ".");
			Assert.isTrue(keyParts.length == 2, "Enum string key must in the format '<type>.<code>'");
			type = keyParts[0];
			code = getCodeFromString(keyParts[1]);
		}
		else {
			type = this.type.getName();
			if (ShortCodedEnum.class.isAssignableFrom(this.type)) {
				try {
					code = Short.valueOf(encodedCode);
				}
				catch (NumberFormatException e) {
					IllegalArgumentException iae = new IllegalArgumentException("The encoded enum argument '"
							+ encodedCode + "' could not be converted to a Short, and the enum type is ShortCoded.");
					iae.initCause(e);
					throw iae;
				}
			}
			else if (LetterCodedEnum.class.isAssignableFrom(this.type)) {
				Assert.isTrue(encodedCode.length() == 1,
						"Character letter codes should have length == 1, this one has '" + encodedCode + "' of length "
						+ encodedCode.length());
				char c = encodedCode.charAt(0);
				Assert.isTrue(Character.isLetter(c), "Character code '" + encodedCode + "' is not a letter");
				code = new Character(c);
			}
			else {
				code = encodedCode;
			}
		}
		CodedEnum ce = this.enumResolver.getEnum(type, code, getLocale());
		if (!allowsEmpty) {
			Assert.notNull(ce, "The encoded code '" + encodedCode + "' did not map to a valid enum instance for type "
					+ type);
			if (this.type != null) {
				Assert.isInstanceOf(this.type, ce);
			}
		}
		setValue(ce);
	}

	private Comparable getCodeFromString(String strCode) {
		if (strCode.length() == 1) {
			char c = strCode.charAt(0);
			if (Character.isLetter(c)) {
				return new Character(c);
			}
			else if (Character.isDigit(c)) {
				return new Short((short) c);
			}
			else {
				throw new IllegalArgumentException("Invalid enum code '" + strCode + "'");
			}
		}
		else {
			try {
				return new Short(strCode);
			}
			catch (NumberFormatException e) {
				return strCode;
			}
		}
	}

	public String getAsText() {
		CodedEnum ce = (CodedEnum) getValue();
		return (ce != null ? ce.getLabel() : "");
	}

}
