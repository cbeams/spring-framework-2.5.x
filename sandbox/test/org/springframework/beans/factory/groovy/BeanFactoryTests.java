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

import org.springframework.beans.factory.script.DynamicScript;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

/**
 * 
 * @author Rod Johnson
 * @version $Id: BeanFactoryTests.java,v 1.2 2004-08-01 15:42:01 johnsonr Exp $
 */
public class BeanFactoryTests extends TestCase {
	
	private static final String SIMPLE_XML = "/org/springframework/beans/factory/groovy/simple.xml";
	
	// TODO syntax errors test Bad.groovy
	
	public void testSimple() {
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(SIMPLE_XML);
		Hello hello = (Hello) ac.getBean("simple");
		assertEquals("hello world", hello.sayHello());
	}
	
	public void testStringProperty() {
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(SIMPLE_XML);
		Hello hello = (Hello) ac.getBean("property");
		assertEquals("hello world property", hello.sayHello());
	}
	
	public void testDependencyOnReloadedGroovyBean() throws Exception {
		ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext(SIMPLE_XML);
		Hello delegatingHello = (Hello) ac.getBean("dependsOnProperty");
		assertEquals("hello world property", delegatingHello.sayHello());
		
		DynamicScript script = (DynamicScript) ac.getBean("property");
		assertEquals(1, script.getLoads());
		script.refresh();
		assertEquals(2, script.getLoads());
		
		// Reference still works
		assertEquals("hello world property", delegatingHello.sayHello());
		
		// Give reloading a chance
		// We need to change this file while the test is running :-)
		//Thread.sleep(60 * 1000);
		
		// This assertion only works if the file is changed
		// and reloaded
		//assertTrue("Reloaded in background thread", 2 < script.getLoads());
		
	}

}
