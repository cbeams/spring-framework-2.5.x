/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.aop.aspectj;

import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.TypePatternMatcher;

import org.springframework.aop.ClassFilter;

/**
 * Spring AOP ClassFilter implementation using AspectJ type matching.
 *
 * @author Rod Johnson
 * @since 2.0
 */
public class TypePatternClassFilter implements ClassFilter {
	
	private String typePattern;
	
	private TypePatternMatcher aspectJTypePatternMatcher;


	/**
	 * JavaBean constructor. Be sure to set the
	 * typePattern property.
	 */
	public TypePatternClassFilter() {
	}
	
	/**
	 * Create a fully configured TypePatternClassFilter using the  
	 * given typePattern
	 * @param typePattern type pattern that AspectJ weaver should parse
	 */
	public TypePatternClassFilter(String typePattern) {
		setTypePattern(typePattern);
	}


	/**
	 * Set the AspectJ type pattern to match. Examples include
	 * <code>
	 * org.springframework.beans.*
	 * </code>
	 * This will match any class or interface in the given package, and
	 * <br>
	 * <code>
	 * org.springframework.beans.ITestBean+
	 * </code>
	 * This will match the ITestBean interface and any class that implements it.
	 * <br>These conventions are established by AspectJ, not Spring AOP.
	 * @param typePattern
	 */
	public void setTypePattern(String typePattern) {
		this.typePattern = typePattern;
		this.aspectJTypePatternMatcher =
				PointcutParser.getPointcutParserSupportingAllPrimitivesAndUsingContextClassloaderForResolution().
				parseTypePattern(typePattern);
	}

	public String getTypePattern() {
		return typePattern;
	}


	public boolean matches(Class clazz) {
		return this.aspectJTypePatternMatcher.matches(clazz);
	}

}
