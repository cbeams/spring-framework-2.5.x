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

/**
 * Translates <code>PropertyAccessException</code> thrown by the data binder to a <code>FieldError</code>.
 * @see DataBinder#setPropertyAccessExceptionProcessor(PropertyAccessExceptionProcessor)
 * @see DefaultPropertyAccessExceptionProcessor
 * @author Alef Arendsen
 */
public interface PropertyAccessExceptionProcessor {

    /**
     * Translates PropertyAccessExceptions to an appropriate <code>Error</code>. Note that two error types are
     * available, <code>FieldError</code>s and <code>ObjectError</code>s. Usually field errors are created but in
     * certain situations one might want to create a global <code>ObjectError</code>s instead.
     * @param ex the exception to translate
     * @param errors the errors object. Note that you should add the error(s) to the BindException object yourself. This has
     * the additional benefit that you can add more than just one error or maybe even ignore it. The <code>BindException</code>
     * object features convenience utils such as a MessageCodesResolver to resolve message code.
     * @see MessageCodesResolver
     * @see FieldError
     * @see ObjectError
     */
    void processPropertyAccessException(PropertyAccessException ex, BindException errors);
}
