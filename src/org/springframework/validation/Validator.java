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
 * Interface to be implemented by objects that can validate
 * application-specific objects. This enables validation to
 * be decoupled from the interface and placed in business objects.
 * @author Rod Johnson
 */
public interface Validator {
	
	/**
	 * Return whether or not this object can validate objects
	 * of the given class.
	 */
	boolean supports(Class clazz);
	
	/**
	 * Validate an object, which must be of a class for which
	 * the supports() method returned true.
	 * @param obj  Populated object to validate
	 * @param errors  Errors object we're building. May contain
	 * errors for this field relating to types.
	 */
	void validate(Object obj, Errors errors);

}
