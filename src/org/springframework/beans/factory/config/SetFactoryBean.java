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

package org.springframework.beans.factory.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;

/**
 * Simple factory for shared Set instances. Allows for central setup
 * of Sets via the "set" element in XML bean definitions.
 * @author Juergen Hoeller
 * @since 21.01.2003
 */
public class SetFactoryBean extends AbstractFactoryBean {

	private Set sourceSet;

	private Class targetSetClass = HashSet.class;

	/**
	 * Set the source Set, typically populated via XML "set" elements.
	 */
	public void setSourceSet(Set sourceSet) {
		this.sourceSet = sourceSet;
	}

	/**
	 * Set the class to use for the target Set.
	 * Default is <code>java.util.HashSet</code>.
	 * @see java.util.HashSet
	 */
	public void setTargetSetClass(Class targetSetClass) {
		if (targetSetClass == null) {
			throw new IllegalArgumentException("targetSetClass must not be null");
		}
		if (!Set.class.isAssignableFrom(targetSetClass)) {
			throw new IllegalArgumentException("targetSetClass must implement java.util.Set");
		}
		this.targetSetClass = targetSetClass;
	}

	public Class getObjectType() {
		return Set.class;
	}

	protected Object createInstance() {
		if (this.sourceSet == null) {
			throw new IllegalArgumentException("sourceSet is required");
		}
		Set result = (Set) BeanUtils.instantiateClass(this.targetSetClass);
		result.addAll(this.sourceSet);
		return result;
	}

}
