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

package org.springframework.beans.factory.support;

import java.util.Collections;
import java.util.Properties;

import junit.framework.TestCase;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.NestedTestBean;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;

/**
 * 
 * @author Rod Johnson
 */
public class DependencyInjectionAspectSupportTests extends TestCase {

	public void testNoBeanFactory() {
		DependencyInjectionAspectSupport dias = new DummyDependencyInjectionAspect();
		try {
			dias.afterPropertiesSet();
			fail("Should require arguments");
		}
		catch (IllegalArgumentException ex) {
			System.err.println(ex.getMessage());
		}
	}
	
	public void testNoClassDefinitions() {
		DependencyInjectionAspectSupport dias = new DummyDependencyInjectionAspect();
		try {
			// We do set the bean factory this time
			dias.setBeanFactory(new DefaultListableBeanFactory());
			dias.afterPropertiesSet();
			fail("Should require arguments");
		}
		catch (IllegalArgumentException ex) {
			System.err.println(ex.getMessage());
		}
	}
	
	public void testNoAutowiringInformationForClass() {
		DependencyInjectionAspectSupport dias = new DummyDependencyInjectionAspect();
		String prototypeName = "myTestBeanPrototype";
		try {
			// We do set the bean factory this time
			Properties p = new Properties();
			p.setProperty(TestBean.class.getName(), prototypeName);
			dias.setManagedClassNamesToPrototypeNames(p);
			DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
			RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, 0);
			bd.setSingleton(false);
			bf.registerBeanDefinition(prototypeName, bd);
			dias.setBeanFactory(bf);
			dias.afterPropertiesSet();

			// We ask for a different class
			dias.createAndConfigure(NestedTestBean.class);
		}
		catch (DependencyInjectionAspectSupport.NoAutowiringConfigurationForClassException ex) {
			System.err.println(ex.getMessage());
		}
	}
	
	public void testAutowireClassByTypeAddClass() throws Exception {
	    testAutowireClassByType(0);		
	}
	
	public void testAutowireClassByTypeSetClassList() throws Exception {
	    testAutowireClassByType(1);		
	}
	
	public void testAutowireClassByTypeSetStringList() throws Exception {
	    testAutowireClassByType(2);		
	}
	
	private void testAutowireClassByType(int howConfigure) throws Exception {
		DependencyInjectionAspectSupport dias = new DummyDependencyInjectionAspect();
		switch (howConfigure) {
			case 0 :dias.addAutowireByTypeClass(TestBean.class);
					break;
			  case 1:dias.setAutowireByTypeClasses(Collections.singletonList(TestBean.class));
					break;
			  case 2 :dias.setAutowireByTypeClasses(Collections.singletonList(TestBean.class.getName()));
					break;
			default : fail("Unknown case");
		}
		DefaultListableBeanFactory bf = beanFactoryWithTestBeanSingleton();
		dias.setBeanFactory(bf);
		dias.afterPropertiesSet();

		TestBean tb1 = (TestBean) dias.createAndConfigure(TestBean.class);
		assertSame(tb1.getSpouse(), bf.getBean("kerry"));
		TestBean tb2 = (TestBean) dias.createAndConfigure(TestBean.class);
		assertNotSame(tb1, tb2);
		assertSame(tb2.getSpouse(), bf.getBean("kerry"));
	}
	
	
	public void testDefaultAutowireByType() throws Exception {
		DependencyInjectionAspectSupport dias = new DummyDependencyInjectionAspect();
		dias.setDefaultAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE);
		DefaultListableBeanFactory bf = beanFactoryWithTestBeanSingleton();
		dias.setBeanFactory(bf);
		dias.afterPropertiesSet();

		TestBean tb1 = (TestBean) dias.createAndConfigure(TestBean.class);
		assertSame(tb1.getSpouse(), bf.getBean("kerry"));
		TestBean tb2 = (TestBean) dias.createAndConfigure(TestBean.class);
		assertNotSame(tb1, tb2);
		assertSame(tb2.getSpouse(), bf.getBean("kerry"));
	}
	
	
	public void testAutowireByPrototype() throws Exception {
		DependencyInjectionAspectSupport dias = new DummyDependencyInjectionAspect();
		Properties p = new Properties();
		String protoName = "myTestBean";
		String expectedName = "prototype name property value";
		p.setProperty(TestBean.class.getName(), protoName);
		dias.setManagedClassNamesToPrototypeNames(p);
		
		DefaultListableBeanFactory bf = beanFactoryWithTestBeanSingleton();
		RootBeanDefinition bd = prototypeSpouseBeanDefinition(expectedName);
		bf.registerBeanDefinition(protoName, bd);
		
		dias.setBeanFactory(bf);
		dias.afterPropertiesSet();

		TestBean tb1 = (TestBean) dias.createAndConfigure(TestBean.class);
		assertEquals(expectedName, tb1.getName());
		assertSame(tb1.getSpouse(), bf.getBean("kerry"));
		TestBean tb2 = (TestBean) dias.createAndConfigure(TestBean.class);
		assertEquals(expectedName, tb2.getName());
		assertNotSame(tb1, tb2);
		assertSame(tb2.getSpouse(), bf.getBean("kerry"));
	}
	
	
	/**
	 * @param expectedName
	 * @return
	 */
	public static RootBeanDefinition prototypeSpouseBeanDefinition(String expectedName) {
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, 0);
		bd.setPropertyValues(new MutablePropertyValues().addPropertyValue(new PropertyValue("name", expectedName)).
				addPropertyValue(new PropertyValue("spouse", new RuntimeBeanReference("kerry"))));
		bd.setSingleton(false);
		return bd;
	}

	public static DefaultListableBeanFactory beanFactoryWithTestBeanSingleton() {
		DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, 0);
		bd.setSingleton(true);
		bf.registerBeanDefinition("kerry", bd);
		return bf;
	}
	
	public void testFactoryHasSingletonNotPrototype() {
		DependencyInjectionAspectSupport dias = new DummyDependencyInjectionAspect();
		String prototypeName = "myTestBeanPrototype";
		try {
			// We do set the bean factory this time
			Properties p = new Properties();
			p.setProperty(TestBean.class.getName(), prototypeName);
			dias.setManagedClassNamesToPrototypeNames(p);
			StaticListableBeanFactory bf = new StaticListableBeanFactory();
			bf.addBean(prototypeName, new TestBean());
			dias.setBeanFactory(bf);
			dias.afterPropertiesSet();

			fail("Should have failed as it's a prototype");
		}
		catch (IllegalArgumentException ex) {
			System.err.println(ex.getMessage());
		}
	}
	
	public void testNoBogusPrototypeDefinition() {
		DependencyInjectionAspectSupport dias = new DummyDependencyInjectionAspect();
		try {
			// We do set the bean factory this time
			Properties p = new Properties();
			// But there's no bean definition
			p.setProperty(TestBean.class.getName(), "myTestBeanPrototype");
			dias.setManagedClassNamesToPrototypeNames(p);
			dias.setBeanFactory(new DefaultListableBeanFactory());
			dias.afterPropertiesSet();
			fail("No bean found");
		}
		catch (IllegalArgumentException ex) {
			System.err.println(ex.getMessage());
		}
	}
	
	private class DummyDependencyInjectionAspect extends DependencyInjectionAspectSupport {
		protected void validateProperties() {
		}
	}
}
