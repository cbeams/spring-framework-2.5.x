/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import org.springframework.aop.ClassFilter;


public abstract class ClassFilters {
	
	
	public static ClassFilter union(ClassFilter a, ClassFilter b) {
		return new UnionClassFilter(new ClassFilter[] { a, b } );
	}
	
	public static ClassFilter intersection(ClassFilter a, ClassFilter b) {
		return new IntersectionClassFilter(new ClassFilter[] { a, b } );
	}
	
	
	private static class UnionClassFilter implements ClassFilter {
		
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
	
	private static class IntersectionClassFilter implements ClassFilter {
		
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
