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

/**
 * Encapsulates a field error, i.e. a reason for rejecting a
 * specific field value.
 *
 * <p>A field error gets created with a single code but uses
 * 3 codes for message resolution, in the following order:
 * <ul>
 * <li>first: code + "." + object name + "." + field;
 * <li>then: code + "." + field;
 * <li>finally: code.
 * </ul>
 *
 * <p>E.g.: code "typeMismatch", field "age", object name "user":
 * <ul>
 * <li>1. try "typeMismatch.user.age";
 * <li>2. try "typeMismatch.age";
 * <li>3. try "typeMismatch".
 * </ul>
 *
 * <p>Thus, this resolution algorithm can be leveraged for example
 * to show specific messages for binding errors like "required"
 * and "typeMismatch":
 * <ul>
 * <li>at the object + field level ("age" field, but only on "user");
 * <li>field level (all "age" fields, no matter which object name);
 * <li>or general level (all fields, on any object).
 * </ul>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class FieldError extends ObjectError {

	public static final String CODE_SEPARATOR = ".";

	private final String field;

	private final Object rejectedValue;

	private boolean bindingFailure;

	/**
	 * Create a new FieldError instance, using a default code.
	 */
	public FieldError(String objectName, String field, Object rejectedValue, boolean bindingFailure,
	                  String code, Object[] args, String defaultMessage) {
		this(objectName, field, rejectedValue, bindingFailure,
		     new String[] {code + CODE_SEPARATOR + objectName + CODE_SEPARATOR + field,
		                   code + CODE_SEPARATOR + field,
		                   code},
		     args, defaultMessage);
	}

	/**
	 * Create a new FieldError instance, using multiple codes.
	 * <p>This is only meant to be used by subclasses.
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

}
