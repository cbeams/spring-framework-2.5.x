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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.script.AbstractScriptFactoryTests;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author Rod Johnson
 */
public class BshScriptFactoryContextTests extends AbstractScriptFactoryTests {
	
	private static final String SIMPLE_XML = "/org/springframework/beans/factory/script/bsh/simple.xml";
	
	protected void setUp() {
		applicationContext = new ClassPathXmlApplicationContext(SIMPLE_XML);
	}
	
//	public void testStringPropertySingleton() {
//		System.err.println("beanshell only provides dynamic proxies, so we can't populate properties unless they're on interfaces");
//	}
	
	public void testInterfacesRequired() {
		try {
			applicationContext.getBean("noInterfaces");
			fail("Interfaces are required");
		}
		catch (BeansException ex) {
			ex.printStackTrace();
		}
	}

}
