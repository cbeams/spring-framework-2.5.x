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

package org.springframework.beans.factory.support;

import org.springframework.beans.MutablePropertyValues;

/**
 * Bean definition for beans whose class is defined by their ancestry. PropertyValues
 * defined by the parent will also be "inherited", although it's possible to override
 * them by redefining them in the property values associated with the child.
 * @author Rod Johnson
 * @version $Revision: 1.9 $
 */
public class ChildBeanDefinition extends AbstractBeanDefinition {

	private String parentName;

	/**
	 * Create a new ChildBeanDefinition for the given parent.
	 */
	public ChildBeanDefinition(String parentName, MutablePropertyValues pvs) {
		super(pvs);
		this.parentName = parentName;
	}

	/**
	 * Return the name of the parent bean definition in the bean factory.
	 */
	public String getParentName() {
		return parentName;
	}

	public void validate() throws BeanDefinitionValidationException {
		super.validate();
		if (this.parentName == null) {
			throw new BeanDefinitionValidationException("parentName must be set in ChildBeanDefinition");
		}
	}

	public String toString() {
		return "Child bean with parent '" + getParentName() + "' defined in " + getResourceDescription();
	}

}
