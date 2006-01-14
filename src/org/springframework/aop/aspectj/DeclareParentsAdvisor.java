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

import java.lang.reflect.Field;

import org.aopalliance.aop.Advice;
import org.aopalliance.aop.AspectException;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.support.ClassFilters;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.aop.support.TypePatternClassFilter;

/**
 * Introduction advisor delegating to the given object.
 * Implements AspectJ annotation-style behaviour for the DeclareParents
 * annotation.
 * @author Rod Johnson
 * @since 2.0
 */
public class DeclareParentsAdvisor implements IntroductionAdvisor {

	private final Class introducedInterface;

	private final ClassFilter typePatternClassFilter;
	
	private final Advice advice;
	
	/**
	 * Create a new advisor for this DeclareParents field
	 * @param introductionField static field defining the introduction 
	 * @param typePattern type pattern the introduction is restricted to
	 * @param defaultImpl default implementation class
	 */
	public DeclareParentsAdvisor(Field introductionField, String typePattern, Class defaultImpl) {
		this.introducedInterface = introductionField.getType();
		ClassFilter typePatternFilter = new TypePatternClassFilter(typePattern);
		// Excludes methods implemented
		ClassFilter exclusion = new ClassFilter() {
			public boolean matches(Class clazz) {
				return !(introducedInterface.isAssignableFrom(clazz));
			}
		};
		this.typePatternClassFilter = ClassFilters.intersection(typePatternFilter, exclusion);

		// Try to instantiate a mixin instance and do delegation
		try {
			Object newIntroductionInstanceToUse = defaultImpl.newInstance();
			this.advice = new DelegatingIntroductionInterceptor(newIntroductionInstanceToUse);
		} 
		catch (IllegalArgumentException ex) {
			throw new AspectException("Cannot evaluate static introduction field " + introductionField, ex);
		} 
		catch (IllegalAccessException ex) {
			throw new AspectException("Cannot evaluate static introduction field " + introductionField, ex);
		} 
		catch (InstantiationException ex) {
			throw new AspectException("Cannot instantiate class determined from static introduction field " + introductionField, ex);
		}
	}
	
	public ClassFilter getClassFilter() {
		return typePatternClassFilter;
	}

	public void validateInterfaces() throws IllegalArgumentException {			
		// Do nothing
	}

	public boolean isPerInstance() {
		return true;
	}

	public Advice getAdvice() {
		return advice;
	}

	public Class[] getInterfaces() {
		return new Class[] { introducedInterface };
	}
}