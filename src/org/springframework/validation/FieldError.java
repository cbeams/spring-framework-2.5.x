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

package org.springframework.validation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class that encapsulates a field error, i.e. a reason for rejecting
 * a specific field value.
 *
 * <p>A field error gets created with a single code but uses 3 codes for
 * message resolution, in the following order:
 * <ul>
 * <li>1.: code + "." + object name + "." + field
 * <li>2.: code + "." + field
 * <li>3.: code
 * </ul>
 *
 * <p>E.g. in case of code "typeMismatch", field "age", object name "user":
 * <ul>
 * <li>1. try "typeMismatch.user.age"
 * <li>2. try "typeMismatch.age"
 * <li>3. try "typeMismatch"
 * </ul>
 *
 * <p>Thus, this resolution algorithm can be leveraged for example to show
 * specific messages for binding errors like "required" and "typeMismatch":
 * <ul>
 * <li>at the object + field level ("age" field, but only on "user");
 * <li>field level (all "age" fields, no matter which object name);
 * <li>or general level (all fields, on any object).
 * </ul>
 *
 * <p>In case of array, List or Map properties, both codes for specific
 * elements and for the whole collection are generated. Assuming a field
 * "name" of an array "groups" in object "user":
 * <ul>
 * <li>1. try "typeMismatch.user.groups[0].name"
 * <li>2. try "typeMismatch.user.groups.name"
 * <li>3. try "typeMismatch.groups[0].name"
 * <li>4. try "typeMismatch.groups.name"
 * <li>5. try "typeMismatch.name"
 * <li>6. try "typeMismatch"
 * </ul>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 10.03.2003
 */
public class FieldError extends ObjectError {

	public static final String CODE_SEPARATOR = ".";

	private final String field;

	private final Object rejectedValue;

	private boolean bindingFailure;

	/**
	 * Create a new FieldError instance, building the default code list
	 * based on a single given code.
	 * <p>See class javadoc for details on the generated codes.
	 * @see org.springframework.context.MessageSourceResolvable#getCodes
	 */
	public FieldError(String objectName, String field, Object rejectedValue, boolean bindingFailure,
	                  String code, Object[] args, String defaultMessage) {
		this(objectName, field, rejectedValue, bindingFailure,
				 buildCodeList(code, objectName, field), args, defaultMessage);
	}

	/**
	 * Create a new FieldError instance, using multiple codes.
	 * @see org.springframework.context.MessageSourceResolvable#getCodes
	 */
	protected FieldError(String objectName, String field, Object rejectedValue, boolean bindingFailure,
	                     String[] codes, Object[] args, String defaultMessage) {
		super(objectName, codes, args, defaultMessage);
		this.field = field;
		this.rejectedValue = rejectedValue;
		this.bindingFailure = bindingFailure;
	}

	public String getField() {
		return field;
	}

	public Object getRejectedValue() {
		return rejectedValue;
	}

	public boolean isBindingFailure() {
		return bindingFailure;
	}

	public String toString() {
		return "FieldError occurred in object [" + getObjectName() + "] on [" +
				this.field + "]: rejectedValue [" + this.rejectedValue + "]; " + resolvableToString();
	}

	/**
	 * Build the code list for the given code and field: an object/field-specific code,
	 * a field-specific code, a plain error code. Arrays, Lists and Maps are resolved
	 * both for specific elements and the whole collection.
	 * <p>See class javadoc for details on the generated codes.
	 * @return the list of codes
	 */
	private static String[] buildCodeList(String code, String objectName, String field) {
		List codeList = new ArrayList();
		List fieldList = new ArrayList();
		buildFieldList(field, fieldList);
		for (Iterator it = fieldList.iterator(); it.hasNext();) {
			String fieldInList = (String) it.next();
			codeList.add(code + CODE_SEPARATOR + objectName + CODE_SEPARATOR + fieldInList);
		}
		int dotIndex = field.lastIndexOf('.');
		if (dotIndex != -1) {
			buildFieldList(field.substring(dotIndex + 1), fieldList);
		}
		for (Iterator it = fieldList.iterator(); it.hasNext();) {
			String fieldInList = (String) it.next();
			codeList.add(code + CODE_SEPARATOR + fieldInList);
		}
		codeList.add(code);
		return (String[]) codeList.toArray(new String[codeList.size()]);
	}

	/**
	 * Add both keyed and non-keyed entries for the given field to the field list.
	 */
	private static void buildFieldList(String field, List fieldList) {
		fieldList.add(field);
		String plainField = field;
		int keyIndex = plainField.lastIndexOf('[');
		while (keyIndex != -1) {
			int endKeyIndex = plainField.indexOf(']', keyIndex);
			if (endKeyIndex != -1) {
				plainField = plainField.substring(0, keyIndex) + plainField.substring(endKeyIndex + 1);
				fieldList.add(plainField);
				keyIndex = plainField.lastIndexOf('[');
			}
			else {
				keyIndex = -1;
			}
		}
	}

}
