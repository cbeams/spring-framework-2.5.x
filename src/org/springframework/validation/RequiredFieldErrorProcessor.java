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
 * Processes required field errors, produces by the databinder. The error processor is pluggable so you
 * can treat errors differently if you want to. A default implementation is provided that uses a special
 * constant to resolve one or more message codes.
 * @see DataBinder#setRequiredFieldErrorProcessor(RequiredFieldErrorProcessor)
 * @see PropertyAccessExceptionProcessor
 * @author Alef Arendsen
 */
public interface RequiredFieldErrorProcessor {

    /**
     * Processes the required field error using the given BindException
     * @param field the field that was missing during binding
     * @param errors the BindException to add the errors to (you have to add the error--either a FieldError or
     * an ObjectError or a combination of both--yourself).
     */
    void processRequiredFieldError(String field, BindException errors);
}
