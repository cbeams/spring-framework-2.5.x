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

package org.springframework.enums.support;

import org.springframework.enums.AbstractCodedEnum;
import org.springframework.util.Assert;

/**
 * @author Keith Donald
 */
public class GenericLabeledCodedEnum extends AbstractCodedEnum {

	private Comparable code;

	private String type;

	protected GenericLabeledCodedEnum() {
	}

	public GenericLabeledCodedEnum(String type, int code, String label) {
		super(label);
		setShortCode((short) code);
		setType(type);
	}

	public GenericLabeledCodedEnum(String type, char code, String label) {
		super(label);
		setLetterCode(code);
		setType(type);
	}

	public GenericLabeledCodedEnum(String type, String code, String label) {
		super(label);
		setStringCode(code);
		setType(type);
	}

	protected void setType(String type) {
		Assert.hasText(type, "Type is required");
		this.type = type;
	}

	public short getShortCode() {
		Assert.state(isShortCoded(), "Not short coded");
		return ((Short) code).shortValue();
	}

	protected void setShortCode(short code) {
		this.code = new Short(code);
	}

	public char getLetterCode() {
		Assert.state(isLetterCoded(), "Not letter coded");
		return ((Character) code).charValue();
	}

	protected void setLetterCode(char code) {
		Assert.isTrue(Character.isLetter(code), "The code " + code + " is invalid; it must be a letter.");
		this.code = new Character((char) code);
	}

	public String getStringCode() {
		Assert.state(isStringCoded(), "Not string coded");
		return (String) code;
	}

	protected void setStringCode(String code) {
		Assert.hasText(code, "The string code is required");
		this.code = code;
	}

	public boolean isShortCoded() {
		return code instanceof Short;
	}

	public boolean isLetterCoded() {
		return code instanceof Character;
	}

	public boolean isStringCoded() {
		return code instanceof String;
	}

	public Comparable getCode() {
		return code;
	}

	public String getType() {
		return type;
	}

}
