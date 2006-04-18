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
 *
 * Created on 14-Apr-2006 by Adrian Colyer
 */
package org.springframework.aop.aspectj;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;


/**
 * See SPR-1682
 * 
 * @author Adrian Colyer
 * @since 2.0
 */
public class SharedPointcutWithArgsMismatch extends AbstractDependencyInjectionSpringContextTests {

	private ToBeAdvised toBeAdvised;
	
	/* (non-Javadoc)
	 * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
	 */
	protected String[] getConfigLocations() {
		return new String[] {
				"org/springframework/aop/aspectj/args-mismatch.xml"
		};
	}
	
	public void setToBeAdvised(ToBeAdvised tba) {
		this.toBeAdvised = tba;
	}

	
	public void testMismatchedArgBinding() {
		this.toBeAdvised.foo("Hello");
	}
	
	public static class ToBeAdvised {
		public void foo(String s) {
			System.out.println(s);
		}
	}
	
	public static class MyAspect {
		
		public void doBefore(int x) {
			System.out.println(x);
		}
		
		public void doBefore(String x) {
			System.out.println(x);
		}
		
	}
	
}
