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

package org.springframework.beans.factory.dynamic;

import junit.framework.TestCase;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.TestBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author Rod Johnson
 */
public class PropertiesDynamicObjectConverterTests extends TestCase {
	
	private static final String SIMPLE_XML = "/org/springframework/beans/factory/dynamic/refresh.xml";
	
	private ClassPathXmlApplicationContext ac;
	
	protected void setUp() {
		ac = new ClassPathXmlApplicationContext(SIMPLE_XML);
	}
	
	public void testDynamicIsDynamic() {
		TestBean tb = (TestBean) ac.getBean("dynamic1");
		assertTrue(tb instanceof DynamicObject);
		assertEquals("Rod", tb.getName());
		
		PropertiesDynamicObjectConverter pdoc = (PropertiesDynamicObjectConverter) ac.getBean("dynamo");
		pdoc.refresh();
		assertTrue(tb instanceof DynamicObject);
		assertEquals("Rod", tb.getName());
		
		// TODO restore
		//assertEquals(2, ((DynamicObject) tb).getLoads());
	}
	
	public void testStaticIsStatic() {
		TestBean tb = (TestBean) ac.getBean("static1");
		assertFalse(tb instanceof DynamicObject);
		assertFalse(AopUtils.isAopProxy(tb));
	}

}
