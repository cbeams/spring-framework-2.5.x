/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.beans.factory.xml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.context.support.StaticApplicationContext;

/**
 * @author Mark Fisher
 */
public class QualifierAnnotationTests extends TestCase {

	private static final String CONFIG_LOCATION =
			"classpath:org/springframework/beans/factory/xml/qualifierAnnotationTests.xml";


	public void testNonQualifiedFieldFails() {
		StaticApplicationContext context = new StaticApplicationContext();
		BeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
		reader.loadBeanDefinitions(CONFIG_LOCATION);
		context.registerSingleton("testBean", NonQualifiedTestBean.class);
		try {
			context.refresh();
			fail("should have thrown a BeanCreationException");
		}
		catch (BeanCreationException e) {
			assertTrue(e.getMessage().contains("found 4"));
		}
	}

	public void testQualifiedByBeanName() {
		StaticApplicationContext context = new StaticApplicationContext();
		BeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
		reader.loadBeanDefinitions(CONFIG_LOCATION);
		context.registerSingleton("testBean", QualifiedByBeanNameTestBean.class);
		context.refresh();
		QualifiedByBeanNameTestBean testBean = (QualifiedByBeanNameTestBean) context.getBean("testBean");
		Person person = testBean.getLarry();
		assertEquals("Larry", person.getName());
	}

	public void testQualifiedByValue() {
		StaticApplicationContext context = new StaticApplicationContext();
		BeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
		reader.loadBeanDefinitions(CONFIG_LOCATION);
		context.registerSingleton("testBean", QualifiedByValueTestBean.class);
		context.refresh();
		QualifiedByValueTestBean testBean = (QualifiedByValueTestBean) context.getBean("testBean");
		Person person = testBean.getCurly();
		assertEquals("Curly", person.getName());
	}

	public void testQualifiedByAttributesFailsWithoutCustomQualifierRegistered() {
		StaticApplicationContext context = new StaticApplicationContext();
		BeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
		reader.loadBeanDefinitions(CONFIG_LOCATION);
		context.registerSingleton("testBean", QualifiedByAttributesTestBean.class);
		try {
			context.refresh();
			fail("should have thrown a BeanCreationException");
		}
		catch (BeanCreationException e) {
			assertTrue(e.getMessage().contains("found 4"));
		}
	}

	public void testQualifiedByAttributesWithCustomQualifierRegistered() {
		StaticApplicationContext context = new StaticApplicationContext();
		BeanDefinitionReader reader = new XmlBeanDefinitionReader(context);
		reader.loadBeanDefinitions(CONFIG_LOCATION);
		context.getDefaultListableBeanFactory().registerQualifierType(MultipleAttributeQualifier.class);
		context.registerSingleton("testBean", QualifiedByAttributesTestBean.class);
		context.refresh();
		QualifiedByAttributesTestBean testBean = (QualifiedByAttributesTestBean) context.getBean("testBean");
		Person moeSenior = testBean.getMoeSenior();
		Person moeJunior = testBean.getMoeJunior();
		assertEquals("Moe Sr.", moeSenior.getName());
		assertEquals("Moe Jr.", moeJunior.getName());
	}


	private static class NonQualifiedTestBean {

		@Autowired
		private Person anonymous;

		public Person getAnonymous() {
			return anonymous;
		}

	}


	private static class QualifiedByBeanNameTestBean {

		@Autowired @Qualifier("larry")
		private Person larry;

		public Person getLarry() {
			return larry;
		}

	}


	private static class QualifiedByValueTestBean {

		@Autowired @SimpleValueQualifier("curly")
		private Person curly;

		public Person getCurly() {
			return curly;
		}

	}


	private static class QualifiedByAttributesTestBean {

		@Autowired @MultipleAttributeQualifier(name="moe", age=42)
		private Person moeSenior;

		@Autowired @MultipleAttributeQualifier(name="moe", age=15)
		private Person moeJunior;

		public Person getMoeSenior() {
			return moeSenior;
		}

		public Person getMoeJunior() {
			return moeJunior;
		}

	}


	private static class Person {

		private String name;

		private int age;


		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

	}


	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@Qualifier
	public @interface SimpleValueQualifier {

		String value() default "";

	}


	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface MultipleAttributeQualifier {

		String name();

		int age();

	}

}
