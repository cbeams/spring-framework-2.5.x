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

package org.springframework.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.springframework.beans.TestBean;

import junit.framework.TestCase;

public class LocalVariableTableParameterNameDiscovererTests extends TestCase {

	private LocalVariableTableParameterNameDiscover discoverer;
	
	public void testMethodParameterNameDiscoveryNoArgs() throws NoSuchMethodException  {
		Method getName = TestBean.class.getMethod("getName",new Class[0]);
		String[] names = discoverer.getParameterNames(getName);
		assertNotNull("should find method info",names);
		assertEquals("no argument names",0,names.length);
		
	}
	
	public void testMethodParameterNameDiscoveryWithArgs() throws NoSuchMethodException {
		Method setName = TestBean.class.getMethod("setName",new Class[] {String.class});
		String[] names = discoverer.getParameterNames(setName);
		assertNotNull("should find method info",names);
		assertEquals("one argument",1,names.length);
		assertEquals("name",names[0]);
	}
	
	public void testConsParameterNameDiscoveryNoArgs() throws NoSuchMethodException {
		Constructor noArgsCons = TestBean.class.getConstructor(new Class[0]);
		String[] names = discoverer.getParameterNames(noArgsCons);
		assertNotNull("should find cons info",names);
		assertEquals("no argument names",0,names.length);		
	}
	
	public void testConsParameterNameDiscoveryArgs() throws NoSuchMethodException {
		Constructor twoArgCons = TestBean.class.getConstructor(new Class[]{String.class,int.class});
		String[] names = discoverer.getParameterNames(twoArgCons);
		assertNotNull("should find cons info",names);
		assertEquals("one argument",2,names.length);
		assertEquals("name",names[0]);
		assertEquals("age",names[1]);		
	}

	protected void setUp() throws Exception {
		this.discoverer = new LocalVariableTableParameterNameDiscover();
	}

}
