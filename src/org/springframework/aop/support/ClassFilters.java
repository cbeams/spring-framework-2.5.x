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

import org.springframework.aop.ClassFilter;

/**
 * Static methods useful for composing ClassFilters.
 * @author Rod Johnson
 * @since 11-Nov-2003
 */
public abstract class ClassFilters {

	public static ClassFilter union(ClassFilter a, ClassFilter b) {
		return new UnionClassFilter(new ClassFilter[] { a, b } );
	}
	
	public static ClassFilter intersection(ClassFilter a, ClassFilter b) {
		return new IntersectionClassFilter(new ClassFilter[] { a, b } );
	}
	
	
	private static class UnionClassFilter implements ClassFilter, Serializable {
		
		private ClassFilter[] filters;
		
		public UnionClassFilter(ClassFilter[] filters) {
			this.filters = filters;
		}

		public boolean matches(Class clazz) {
			for (int i = 0; i < filters.length; i++) {
				if (filters[i].matches(clazz)) {
					return true;
				}
			}
			return false;
		}
	}
	
	private static class IntersectionClassFilter implements ClassFilter, Serializable {
		
		private ClassFilter[] filters;
		
		public IntersectionClassFilter(ClassFilter[] filters) {
			this.filters = filters;
		}

		public boolean matches(Class clazz) {
			for (int i = 0; i < filters.length; i++) {
				if (!filters[i].matches(clazz)) {
					return false;
				}
			}
			return true;
		}
	}

}
