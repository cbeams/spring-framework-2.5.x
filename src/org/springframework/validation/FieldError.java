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
 * Class that encapsulates a field error, i.e. a reason for rejecting
 * a specific field value.
 *
 * <p>See DefaultMessageCodesResolver javadoc for details on how a message
 * code list is built for a FieldError.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 10.03.2003
 * @see DefaultMessageCodesResolver
 */
public class FieldError extends ObjectError {

	private final String field;

	private final Object rejectedValue;

	private final boolean bindingFailure;

	/**
	 * Create a new FieldError instance.
	 * @param objectName the name of the affected object
	 * @param field the affected field of the object
	 * @param rejectedValue the rejected field value
	 * @param bindingFailure whether this error represents a binding failure
	 * (like a type mismatch); else, it is a validation failure
	 * @param codes the codes to be used to resolve this message
	 * @param arguments the array of arguments to be used to resolve this message
	 * @param defaultMessage the default message to be used to resolve this message
	 */
	public FieldError(String objectName, String field, Object rejectedValue, boolean bindingFailure,
										String[] codes, Object[] arguments, String defaultMessage) {
		super(objectName, codes, arguments, defaultMessage);
		this.field = field;
		this.rejectedValue = rejectedValue;
		this.bindingFailure = bindingFailure;
	}

	/**
	 * Return the affected field of the object.
	 */
	public String getField() {
		return field;
	}

	/**
	 * Return the rejected field value.
	 */
	public Object getRejectedValue() {
		return rejectedValue;
	}

	/**
	 * Return whether this error represents a binding failure
	 * (like a type mismatch); else, it is a validation failure.
	 */
	public boolean isBindingFailure() {
		return bindingFailure;
	}

	public String toString() {
		return "Field error in object '" + getObjectName() + "' on field '" + this.field +
				"': rejectedValue=[" + this.rejectedValue + "]; " + resolvableToString();
	}

}
