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

import org.springframework.beans.PropertyAccessException;
import org.springframework.validation.support.AbstractBindingErrorProcessorSupport;

/**
 * <p>Default implementation of the <code>PropertyAccessExceptionProcessor</code> that processes the exception to
 * create a field error based on the arguments and the <code>MessageCodesResolver</code> set to the <code>Errors</code>
 * object given.</p>
 * <p>Additional implementations can use the convenience methods defined on this class to create different behavior.</p>
 * @see Errors
 * @see MessageCodesResolver
 *
 * @author Alef Arendsen
 */
public class DefaultPropertyAccessExceptionProcessor extends AbstractBindingErrorProcessorSupport implements PropertyAccessExceptionProcessor {

    public void processPropertyAccessException(PropertyAccessException ex, BindException errors) {
        // create field with the exceptions's code, e.g. "typeMismatch"
		String field = ex.getPropertyChangeEvent().getPropertyName();
    		errors.addError(
				new FieldError(errors.getObjectName(), field, ex.getPropertyChangeEvent().getNewValue(), true,
				errors.resolveMessageCodes(ex.getErrorCode(), field),
				getArgumentsForBindingError(errors.getObjectName(), field), ex.getLocalizedMessage()));
    }
}
