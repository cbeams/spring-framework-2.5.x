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

package org.springframework.beans.factory.script;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.TimeStamped;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.dynamic.DynamicObject;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import junit.framework.TestCase;

/**
 * 
 * @author Rod Johnson
 */
public class AbstractScriptFactoryTests extends TestCase {

	protected ClassPathXmlApplicationContext applicationContext;

	public void testBadSyntax() {
		
		try {
			applicationContext.getBean("bad");
			fail();
		}
		catch (BeanCreationException ex) {
			// Ok
			//ex.printStackTrace();
			//assertTrue(ex.getCause() instanceof CompilationException);
		}
	}

	protected void tearDown() {
		applicationContext.close();	
	}

	public void testSimpleSingleton() {		
		Hello hello = (Hello) applicationContext.getBean("simpleSingleton");
		assertEquals("hello world", hello.sayHello());
		
		Hello hello2 = (Hello) applicationContext.getBean("simpleSingleton");
		assertSame(hello, hello2);
		assertTrue("Is dynamic script", hello instanceof DynamicScript);
	}
	
	public void testTwoInterfaces() {		
		Hello hello = (Hello) applicationContext.getBean("twoInterfaces");
		assertEquals("hello world two interfaces", hello.sayHello());
		
		TimeStamped ts = (TimeStamped) hello;
		assertEquals(1000, ts.getTimeStamp());
		assertTrue("Is dynamic script", hello instanceof DynamicScript);
	}

	public void testInlineScriptDefinition() {
		Hello hello = (Hello) applicationContext.getBean("inline");
		assertEquals("hello world inline", hello.sayHello());
		assertTrue(hello instanceof DynamicObject);
		assertTrue(hello instanceof Script);
		System.err.println(((DynamicScript) hello).getResourceString());
		DynamicObject dobj = (DynamicObject) hello;
		assertFalse("An inline script can't be modified", dobj.isModified());
	}

	public void testStringPropertySingleton() {
		Hello hello = (Hello) applicationContext.getBean("propertySingleton");
		Advised a = (Advised) hello;
		System.err.println(a.toProxyConfigString());
		assertEquals("hello world property", hello.sayHello());
		System.out.println(((DynamicScript) hello).getResourceString() );
	}

}
