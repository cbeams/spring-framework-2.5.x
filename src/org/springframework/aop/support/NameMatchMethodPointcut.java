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

package org.springframework.aop.support;

import java.lang.reflect.Method;

/**
 * Pointcut bean for simple method name matches,
 * as alternative to regexp patterns.
 * @author Juergen Hoeller
 * @since 11.02.2004
 * @see #isMatch
 */
public class NameMatchMethodPointcut extends StaticMethodMatcherPointcut {

	private String[] mappedNames = new String[0];

	/**
	 * Convenience method when we have only a single method name
	 * to match. Use either this method or setMappedNames(), not both.
	 * @see #setMappedNames
	 */
	public void setMappedName(String mappedName) {
		this.mappedNames = new String[] {mappedName};
	}

	/**
	 * Set the method names defining methods to match.
	 * Matching will be the union of all these; if any match,
	 * the pointcut matches.
	 */
	public void setMappedNames(String[] mappedNames) {
		this.mappedNames = mappedNames;
	}

	public boolean matches(Method m, Class targetClass) {
		for (int i = 0; i<this.mappedNames.length; i++) {
			String mappedName = this.mappedNames[i];
			if (mappedName.equals(m.getName()) || isMatch(m.getName(), mappedName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return if the given method name matches the mapped name.
	 * The default implementation checks for "xxx*" and "*xxx" matches.
	 * Can be overridden in subclasses.
	 * @param methodName the method name of the class
	 * @param mappedName the name in the descriptor
	 * @return if the names match
	 */
	protected boolean isMatch(String methodName, String mappedName) {
		return (mappedName.endsWith("*") && methodName.startsWith(mappedName.substring(0, mappedName.length() - 1))) ||
				(mappedName.startsWith("*") && methodName.endsWith(mappedName.substring(1, mappedName.length())));
	}

}
