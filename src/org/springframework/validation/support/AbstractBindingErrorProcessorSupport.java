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
package org.springframework.validation.support;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.Errors;

/**
 * Supporting functionality for binding error processors
 * @see org.springframework.validation.DefaultPropertyAccessExceptionProcessor
 * @see org.springframework.validation.DefaultRequiredFieldErrorProcessor
 * 
 * @author Alef Arendsen
 */
public abstract class AbstractBindingErrorProcessorSupport {

    /**
	 * Return FieldError arguments for a binding error on the given field.
	 * Invoked for each missing required fields and each type mismatch.
	 * <p>Default implementation returns a DefaultMessageSourceResolvable
	 * with "objectName.field" and "field" as codes.
	 * @param field the field that caused the binding error
	 * @return the Object array that represents the FieldError arguments
	 * @see org.springframework.validation.FieldError#getArguments
	 * @see org.springframework.context.support.DefaultMessageSourceResolvable
	 */
	protected Object[] getArgumentsForBindingError(String objectName, String field) {
		return new Object[] {
				new DefaultMessageSourceResolvable(new String[] {objectName + Errors.NESTED_PATH_SEPARATOR + field, field},
				null, field)
		};
	}
}
