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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import junit.framework.TestCase;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import org.springframework.beans.TestBean;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Adrian Colyer
 * @author Rod Johnson
 * @author Ramnivas Laddad
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

	public void testInjectionOnDeserialization() throws Exception {
		ShouldBeConfiguredBySpring domainObject = new ShouldBeConfiguredBySpring();
		domainObject.setName("Anonymous");
		ShouldBeConfiguredBySpring deserializedDomainObject =
				serializeAndDeserialize(domainObject);
		assertEquals("Dependency injected on deserialization","Rod",deserializedDomainObject.getName());
	}
	
	public void testInjectionOnDeserializationForClassesThatContainsPublicReadResolve() throws Exception {
		ShouldBeConfiguredBySpringContainsPublicReadResolve domainObject = new ShouldBeConfiguredBySpringContainsPublicReadResolve();
		domainObject.setName("Anonymous");
		ShouldBeConfiguredBySpringContainsPublicReadResolve deserializedDomainObject =
				serializeAndDeserialize(domainObject);
		assertEquals("Dependency injected on deserialization","Rod",deserializedDomainObject.getName());
		assertEquals("User readResolve should take precedence", 1, deserializedDomainObject.readResolveInvocationCount);
	}

	// See ShouldBeConfiguredBySpringContainsPrivateReadResolve
//	public void testInjectionOnDeserializationForClassesThatContainsPrivateReadResolve() throws Exception {
//		ShouldBeConfiguredBySpringContainsPrivateReadResolve domainObject = new ShouldBeConfiguredBySpringContainsPrivateReadResolve();
//		domainObject.setName("Anonymous");
//		ShouldBeConfiguredBySpringContainsPrivateReadResolve deserializedDomainObject =
//				serializeAndDeserialize(domainObject);
//		assertEquals("Dependency injected on deserialization","Rod",deserializedDomainObject.getName());
//	}
	
	public void testNonInjectionOnDeserializationForSerializedButNotConfigured() throws Exception {
		SerializableThatShouldNotBeConfiguredBySpring domainObject = new SerializableThatShouldNotBeConfiguredBySpring();
		domainObject.setName("Anonymous");
		SerializableThatShouldNotBeConfiguredBySpring deserializedDomainObject =
				serializeAndDeserialize(domainObject);
		assertEquals("Dependency injected on deserialization","Anonymous",deserializedDomainObject.getName());
	}
	
	
	public void testSubBeanConfiguredOnlyOnce() throws Exception {
		SubBean subBean = new SubBean();
		assertEquals("Property injected more than once", 1, subBean.setterCount);
	}

	public void testSubSerializableBeanConfiguredOnlyOnce() throws Exception {
		SubSerializableBean subBean = new SubSerializableBean();
		assertEquals("Property injected more than once", 1, subBean.setterCount);
		subBean.setterCount = 0;

		SubSerializableBean deserializedSubBean = serializeAndDeserialize(subBean);
		assertEquals("Property injected more than once", 1, deserializedSubBean.setterCount);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T serializeAndDeserialize(T serializable) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(serializable);
		oos.close();
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		return (T)ois.readObject();
	}
	
	@Configurable("beanOne")
	private static class ShouldBeConfiguredBySpring implements Serializable {

		private String name;

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}

	@Configurable("beanOne")
	private static class ShouldBeConfiguredBySpringContainsPublicReadResolve implements Serializable {

		private String name;
		
		private int readResolveInvocationCount = 0;

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
		
		public Object readResolve() throws ObjectStreamException {
			readResolveInvocationCount++;
			return this;
		}
	}

// 	Won't work until we use hasmethod() experimental pointcut in AspectJ.
//	@Configurable("beanOne")
//	private static class ShouldBeConfiguredBySpringContainsPrivateReadResolve implements Serializable {
//
//		private String name;
//
//		public void setName(String name) {
//			this.name = name;
//		}
//
//		public String getName() {
//			return this.name;
//		}
//		
//		private Object readResolve() throws ObjectStreamException {
//			return this;
//		}
//	}

	private static class ShouldNotBeConfiguredBySpring {

		private String name;

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}

	private static class SerializableThatShouldNotBeConfiguredBySpring implements Serializable {

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

		private TestBean friend = null;

		public TestBean getFriend() {
			return friend;
		}

		public void setFriend(TestBean friend) {
			this.friend = friend;
		}
	}


	@Configurable(autowire=Autowire.BY_NAME)
	private static class ValidAutowireByName {

		private TestBean friend = null;

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


	@Configurable
	private static class BaseBean {
		public int setterCount;

		private String name;

		public void setName(String name) {
			this.name = name;
			setterCount++;
		}
	}

	private static class SubBean extends BaseBean {
	} 
	
	@Configurable
	private static class BaseSerializableBean implements Serializable {
		public int setterCount;

		private String name;

		public void setName(String name) {
			this.name = name;
			setterCount++;
		}
	}

	private static class SubSerializableBean extends BaseSerializableBean {
	} 

	@Aspect
	private static class WireArbitraryExistingPojo extends AbstractBeanConfigurerAspect {

		@Pointcut("initialization(ArbitraryExistingPojo.new(..)) && this(beanInstance)")
		protected void beanCreation(Object beanInstance){

		}
	}


	@Aspect
	private static class AspectThatWillNotBeUsed extends AbstractBeanConfigurerAspect {

		@Pointcut("initialization(ClassThatWillNotActuallyBeWired.new(..)) && this(beanInstance)")
		protected void beanCreation(Object beanInstance){
		}
	}

	private static class ClassThatWillNotActuallyBeWired {

	}

}
