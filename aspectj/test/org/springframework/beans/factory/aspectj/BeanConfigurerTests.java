/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.beans.factory.aspectj;

import junit.framework.TestCase;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.Autowire;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Adrian Colyer
 * @author Rod Johnson
 */
public class BeanConfigurerTests extends TestCase {

	@Override
	protected void setUp() throws Exception {
		new ClassPathXmlApplicationContext("org/springframework/beans/factory/aspectj/beanConfigurerTests.xml");
	}

	public void testConfigurableWithExplicitBeanName() {
		System.out.println("Before ShouldBeConfiguredBySpring constructor");
		ShouldBeConfiguredBySpring myObject = new ShouldBeConfiguredBySpring();
		System.out.println("After ShouldBeConfiguredBySpring constructor");
		assertEquals("Rod", myObject.getName());
	}

	public void testWithoutAnnotation() {
		ShouldNotBeConfiguredBySpring myObject = new ShouldNotBeConfiguredBySpring();
		assertNull("Name should not have been set", myObject.getName());
	}

	public void testConfigurableWithImplicitBeanName() {
		ShouldBeConfiguredBySpringUsingTypeNameAsBeanName myObject =
			new ShouldBeConfiguredBySpringUsingTypeNameAsBeanName();
		assertEquals("Rob", myObject.getName());
	}

	public void testConfigurableUsingAutowireByType() {
		ShouldBeConfiguredBySpringUsingAutowireByType myObject =
			new ShouldBeConfiguredBySpringUsingAutowireByType();
		assertNotNull(myObject.getFriend());
		assertEquals("Ramnivas", myObject.getFriend().getName());
	}

	public void testConfigurableUsingAutowireByName() {
		ValidAutowireByName myObject = new ValidAutowireByName();
		assertNotNull(myObject.getRamnivas());
		assertEquals("Ramnivas", myObject.getRamnivas().getName());
	}

	public void testInvalidAutowireByName() {
		try {
			new InvalidAutowireByName();
			fail("Autowire by name cannot work");
		}
		catch (UnsatisfiedDependencyException ex) {
			// Ok
		}
	}

	public void testNewAspectAppliesToArbitraryNonAnnotatedPojo() {
		ArbitraryExistingPojo aep = new ArbitraryExistingPojo();
		assertNotNull(aep.friend);
		assertEquals("Ramnivas", aep.friend.getName());
	}

	public void testNewAspectThatWasNotAddedToSpringContainer() {
		try{
			new ClassThatWillNotActuallyBeWired();
		}
		catch (IllegalStateException ex) {
			assertTrue(ex.getMessage().indexOf("BeanFactory") != -1);
		}
	}


	@Configurable("beanOne")
	private static class ShouldBeConfiguredBySpring {

		private String name;

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}


	private static class ShouldNotBeConfiguredBySpring {

		private String name;

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}


	@Configurable
	private static class ShouldBeConfiguredBySpringUsingTypeNameAsBeanName {

		private String name;

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}


	@Configurable(autowire=Autowire.BY_TYPE)
	private static class ShouldBeConfiguredBySpringUsingAutowireByType {

		private TestBean friend;

		public TestBean getFriend() {
			return friend;
		}

		public void setFriend(TestBean friend) {
			this.friend = friend;
		}
	}


	@Configurable(autowire=Autowire.BY_NAME)
	private static class ValidAutowireByName {

		private TestBean friend;

		public TestBean getRamnivas() {
			return friend;
		}

		public void setRamnivas(TestBean friend) {
			this.friend = friend;
		}
	}


	@Configurable(autowire=Autowire.BY_NAME, dependencyCheck=true)
	private static class InvalidAutowireByName {

		private TestBean friend;

		public TestBean getFriend() {
			return friend;
		}

		public void setFriend(TestBean friend) {
			this.friend = friend;
		}
	}


	private static class ArbitraryExistingPojo {

		private TestBean friend;

		public void setFriend(TestBean f) {
			this.friend = f;
		}
	}


	@Aspect
	private static class WireArbitraryExistingPojo extends AbstractBeanConfigurer {

		@Pointcut("initialization(ArbitraryExistingPojo.new(..)) && this(beanInstance)")
		protected void beanCreation(Object beanInstance){

		}
	}


	@Aspect
	private static class AspectThatWillNotBeUsed extends AbstractBeanConfigurer {

		@Pointcut("initialization(ClassThatWillNotActuallyBeWired.new(..)) && this(beanInstance)")
		protected void beanCreation(Object beanInstance){
		}
	}


	private static class ClassThatWillNotActuallyBeWired {

	}

}
