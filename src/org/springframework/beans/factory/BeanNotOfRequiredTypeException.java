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

import org.springframework.beans.BeansException;

/**
 * Thrown when a bean doesn't match the required type.
 * @author Rod Johnson
 */
public class BeanNotOfRequiredTypeException extends BeansException {

	/** The name of the instance that was of the wrong type */
	private String name;

	/** The required type */
	private Class requiredType;

	/** The offending instance */
	private Object actualInstance;

	/**
	 * Create a new <code>BeanNotOfRequiredTypeException</code>.
	 * @param name the name of the bean requested
	 * @param requiredType required type
	 * @param actualInstance the instance actually returned, whose
	 * class did not match the expected type.
	 */
	public BeanNotOfRequiredTypeException(String name, Class requiredType, Object actualInstance) {
		super("Bean named '" + name + "' must be of type [" + requiredType.getName() +
				"], but was actually of type [" + actualInstance.getClass().getName() + "]");
		this.name = name;
		this.requiredType = requiredType;
		this.actualInstance = actualInstance;
	}

	public String getBeanName() {
		return name;
	}

	public Class getRequiredType() {
		return requiredType;
	}

	public Class getActualType() {
		return actualInstance.getClass();
	}

	public Object getActualInstance() {
		return actualInstance;
	}

}


