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

package org.springframework.beans.groovy;

import groovy.lang.GroovyObject;
import junit.framework.TestCase;

import org.springframework.aop.support.AopUtils;

/**
 * 
 * @author Rod Johnson
 * @version $Id: GroovyFactoryTests.java,v 1.1 2004-07-30 18:42:34 johnsonr Exp $
 */
public class GroovyFactoryTests extends TestCase {
	
	public void testNoScriptFound() {
		try {
			GroovyFactory.staticObject("rubbish");
			fail();
		}
		catch (ScriptNotFoundException ex) {
			// Ok
		}
	}
	
	public void testValidScript() {
		GroovyObject groovyObject = GroovyFactory.staticObject("org/springframework/beans/groovy/SimpleHello.groovy");
		System.out.println(groovyObject);
	}
	
	public void testNotReloadable() {
		Hello hello = (Hello) GroovyFactory.staticObject("org/springframework/beans/groovy/SimpleHello.groovy");
		assertFalse("Doesn't proxy unless dynamic features requested", AopUtils.isCglibProxy(hello));
		assertEquals("hello world", hello.sayHello());
		assertFalse("Doesn't implement DynamicScript unless dynamic features requested", 
				hello instanceof DynamicScript);
	}
	
	/*
	 * Can't test this without a factory
	public void testReloadable() {
		Hello hello = (Hello) GroovyFactory.groovyObject("org/springframework/beans/groovy/SimpleHello.groovy", 25);
		assertTrue(AopUtils.isCglibProxy(hello));
		assertEquals("hello world", hello.sayHello());
		Advised a = (Advised) hello;
		assertTrue(a.getTargetSource() instanceof GroovyTargetSource);
		
		DynamicScript script = (DynamicScript) hello;
		assertEquals(1, script.getLoads());
		script.reload();
		assertEquals(2, script.getLoads());
	}
	*/

}
