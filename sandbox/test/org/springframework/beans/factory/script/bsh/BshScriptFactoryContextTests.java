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

package org.springframework.beans.factory.script.bsh;

import junit.framework.TestCase;

import org.springframework.beans.factory.dynamic.DynamicObject;
import org.springframework.beans.factory.script.DynamicScript;
import org.springframework.beans.factory.script.Hello;
import org.springframework.beans.factory.script.Script;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author Rod Johnson
 */
public class BshScriptFactoryContextTests extends TestCase {
	
	private static final String SIMPLE_XML = "/org/springframework/beans/factory/script/bsh/simple.xml";
	
//	public void testBadGroovySyntax() {
//		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(SIMPLE_XML);
//		try {
//			ac.getBean("bad");
//			fail();
//		}
//		catch (BeanCreationException ex) {
//			// Ok
//			//ex.printStackTrace();
//			//assertTrue(ex.getCause() instanceof CompilationException);
//		}
//	}
	
	public void testSimple() {
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(SIMPLE_XML);
		Hello hello = (Hello) ac.getBean("simple");
		assertEquals("hello world", hello.sayHello());
		assertTrue(hello instanceof DynamicObject);
		assertTrue(hello instanceof Script);
		System.err.println(((DynamicScript) hello).getResourceString());
	}
	
//	public void testStringProperty() {
//		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(SIMPLE_XML);
//		Hello hello = (Hello) ac.getBean("property");
//		assertEquals("hello world property", hello.sayHello());
//		System.out.println(((DynamicScript) hello).getResourceString() );
//	}
//	
//	public void testDependencyOnReloadedGroovyBean() throws Exception {
//		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(SIMPLE_XML);
//		Hello delegatingHello = (Hello) ac.getBean("dependsOnProperty");
//		assertEquals("hello world property", delegatingHello.sayHello());
//		
//		DynamicScript script = (DynamicScript) ac.getBean("property");
//		assertEquals(1, script.getLoads());
//		script.refresh();
//		assertEquals(2, script.getLoads());
//		
//		// Reference still works
//		assertEquals("hello world property", delegatingHello.sayHello());
//		
//		// Give reloading a chance
//		// We need to change this file while the test is running :-)
//		//Thread.sleep(60 * 1000);
//		
//		// This assertion only works if the file is changed
//		// and reloaded
//		//assertTrue("Reloaded in background thread", 2 < script.getLoads());
//		
//	}

}
