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

package org.springframework.beans.factory;

/**
 * Exception thrown when a bean depends on other beans or simple properties that were not
 * specified in the bean factory definition, although dependency checking was enabled.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since September 3, 2003
 */
public class UnsatisfiedDependencyException extends BeanCreationException {

	public UnsatisfiedDependencyException(
			String resourceDescription, String beanName, int ctorArgIndex, Class ctorArgType, String msg) {
		super(resourceDescription, beanName,
				"Unsatisfied dependency expressed through constructor argument with index " +
				ctorArgIndex + " of type [" + ctorArgType.getName() + "]" +
				(msg != null ? ": " + msg : ""));
	}

	public UnsatisfiedDependencyException(
			String resourceDescription, String beanName, String propertyName, String msg) {
		super(resourceDescription, beanName,
				"Unsatisfied dependency expressed through bean property '" + propertyName + "'" +
				(msg != null ? ": " + msg : ""));
	}

}
