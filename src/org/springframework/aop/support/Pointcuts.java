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

import java.io.Serializable;
import java.lang.reflect.Method;

import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;

/**
 * Pointcut constants for matching getters and setters,
 * and static methods useful for manipulating and evaluating pointcuts.
 * These methods are particularly useful for composing pointcuts
 * using the union and intersection methods.
 * @author Rod Johnson
 */
public abstract class Pointcuts {
	
	private static class SetterPointcut extends StaticMethodMatcherPointcut implements Serializable {
		public static SetterPointcut INSTANCE = new SetterPointcut();
		public boolean matches(Method m, Class targetClass) {
			return m.getName().startsWith("set");
		}
		private Object readResolve() {
			return INSTANCE;
		}
	}
	
	private static class GetterPointcut extends StaticMethodMatcherPointcut implements Serializable {
		public static GetterPointcut INSTANCE = new GetterPointcut();
		public boolean matches(Method m, Class targetClass) {
			return m.getName().startsWith("get");
		}
		private Object readResolve() {
			return INSTANCE;
		}
	}
	
	/**
	 * Pointcut matching all setters, in any class
	 */
	public static final Pointcut SETTERS = SetterPointcut.INSTANCE; 
	
	/**
	 * Pointcut matching all getters, in any class
	 */
	public static final Pointcut GETTERS = GetterPointcut.INSTANCE; 
	
	
	public static Pointcut union(Pointcut a, Pointcut b) {
		return new UnionPointcut(a, b);
	}
	
	public static Pointcut intersection(Pointcut a, Pointcut b) {
		return new ComposablePointcut(a.getClassFilter(), a.getMethodMatcher()).intersection(b);
	}
	
	/**
	 * Perform the least expensive check for a match.
	 */
	public static boolean matches(Pointcut pc, Method m, Class targetClass, Object[] arguments) {
		if (pc == Pointcut.TRUE) {
			return true;
		}
			
		if (pc.getClassFilter().matches(targetClass)) {
			// Only check if it gets past first hurdle
			MethodMatcher mm = pc.getMethodMatcher();
			if (mm.matches(m, targetClass)) { 
				// We may need additional runtime (argument) check
				return  mm.isRuntime() ? mm.matches(m, targetClass, arguments) : true;
			}
		}
		return false;
	}

	public static boolean equals(Pointcut a, Pointcut b) {
		return a.getClassFilter() == b.getClassFilter() &&
				a.getMethodMatcher() == b.getMethodMatcher();
	}

}
