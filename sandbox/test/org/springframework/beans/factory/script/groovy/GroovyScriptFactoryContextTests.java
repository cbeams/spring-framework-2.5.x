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

package org.springframework.beans.factory.script.groovy;

import groovy.lang.GroovyObject;

import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.dynamic.DynamicObject;
import org.springframework.beans.factory.script.AbstractScriptFactoryTests;
import org.springframework.beans.factory.script.DynamicScript;
import org.springframework.beans.factory.script.Hello;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author Rod Johnson
 */
public class GroovyScriptFactoryContextTests extends AbstractScriptFactoryTests {
	
	private static final String SIMPLE_XML = "/org/springframework/beans/factory/script/groovy/simple.xml";
	
	protected void setUp() {
		applicationContext = new ClassPathXmlApplicationContext(SIMPLE_XML);
	}
	
	public void testPrototypesAreDistinct() {
		
		Hello hello1 = (Hello) applicationContext.getBean("propertyPrototype");
		assertEquals("propertyPrototype", hello1.sayHello());
		
		Hello hello2 = (Hello) applicationContext.getBean("propertyPrototype");
		assertEquals("propertyPrototype", hello1.sayHello());
		assertNotSame(hello1, hello2);
		
		// Change property on 1 shouldn't affect 2
		String newName = "Gordon";
		((GroovyObject) hello1).setProperty("message", newName);
		
		// Refresh would zap that property however...
		
		assertEquals(newName, hello1.sayHello());
		assertEquals("propertyPrototype", hello2.sayHello());
	}
	
	public void testStringPropertySingleton() {
		Hello hello = (Hello) applicationContext.getBean("propertySingleton");
		Advised a = (Advised) hello;
		System.err.println(a.toProxyConfigString());
		assertEquals("hello world property", hello.sayHello());
		System.out.println(((DynamicScript) hello).getResourceString() );
	}
	
	public void testCanCastToGroovyObject() {		
		GroovyObject groovyObj = (GroovyObject) applicationContext.getBean("propertySingleton");
		GroovyObject groovyObj2 = (GroovyObject) applicationContext.getBean("propertyPrototype");
	}
	
	public void testDependencyOnReloadedGroovyBean() throws Throwable {
		
		Hello delegatingHello = (Hello) applicationContext.getBean("dependsOnProperty");
		assertEquals("hello world property", delegatingHello.sayHello());
		
		DynamicObject script = (DynamicObject) applicationContext.getBean("propertySingleton");
		
		//System.out.println("AOP CONFIG=" + ((Advised) script).toProxyConfigString());
		
		assertEquals(1, script.getLoadCount());
		script.refresh();
		assertEquals(2, script.getLoadCount());
		script.refresh();
		assertEquals(3, script.getLoadCount());
		
		// Reference still works, and target returns the same object
		
		assertEquals("hello world property", delegatingHello.sayHello());
		
		assertFalse(script.isModified());
		
		// Give reloading a chance
		// We need to change this file while the test is running :-)
		//Thread.sleep(30 * 1000);
		
		// This assertion only works if the file is changed
		// and reloaded
		//assertTrue("Reloaded in background thread", 3 < script.getLoads());
		
		//assertEquals("hello world property2", delegatingHello.sayHello());
	}

}
