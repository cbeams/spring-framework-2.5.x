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

package org.springframework.beans.factory.groovy;

import groovy.lang.GroovyObject;
import junit.framework.TestCase;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.groovy.DynamicScript;
import org.springframework.beans.factory.groovy.GroovyFactory;
import org.springframework.beans.factory.groovy.ScriptNotFoundException;

/**
 * 
 * @author Rod Johnson
 * @version $Id: GroovyFactoryTests.java,v 1.2 2004-07-31 08:57:39 johnsonr Exp $
 */
public class GroovyFactoryTests extends TestCase {
	
	private static final String SCRIPT_BASE = "org/springframework/beans/factory/groovy/";
	
	public void testNoScriptFound() {
		try {
			GroovyFactory.staticObject("rubbish");
			fail();
		}
		catch (ScriptNotFoundException ex) {
			// Ok
		}
	}
	
	public void testScriptWithSyntaxErrors() {
		try {
			GroovyFactory.staticObject(SCRIPT_BASE + "Bad.groovy");
			fail();
		}
		catch (CompilationException ex) {
			// Ok
		}
	}
	
	public void testValidScript() {
		GroovyObject groovyObject = GroovyFactory.staticObject(SCRIPT_BASE + "SimpleHello.groovy");
		System.out.println(groovyObject);
	}
	
	public void testNotReloadable() {
		Hello hello = (Hello) GroovyFactory.staticObject(SCRIPT_BASE + "SimpleHello.groovy");
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
