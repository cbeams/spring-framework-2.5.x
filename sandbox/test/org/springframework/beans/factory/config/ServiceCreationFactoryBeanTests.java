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

package org.springframework.beans.factory.config;

import junit.framework.TestCase;

import org.springframework.beans.FatalBeanException;

/**
 * @author Rob Harrop
 */
public class ServiceCreationFactoryBeanTests extends TestCase {

	public void testSingleMethodCreationInterface() throws Exception {
		SingleMethodCreationInterface iface = (SingleMethodCreationInterface) getServiceCreator(SingleMethodCreationInterface.class);

		String name = "Rob Harrop";

		TestService ts = iface.createTestService(name);
		assertNotNull("TestService should not be null", ts);
		assertEquals("Incorrect name in TestService bean", name, ts.getName());
	}

	public void testTwoMethodCreationInterface() throws Exception {
		TwoMethodCreationInterface iface = (TwoMethodCreationInterface)getServiceCreator(TwoMethodCreationInterface.class);

		String name = "Rob Harrop";
		int age = 23;

		TestService one = iface.createTestService(name);
		assertEquals("Name is incorrect", name, one.getName());
		assertEquals("Age is incorrect", Integer.MIN_VALUE, one.getAge());

		TestService two = iface.createTestService(name, age);
		assertEquals("Name is incorrect", name, two.getName());
		assertEquals("Age is incorrect", age, two.getAge());
	}

	public void testWithMixedServiceTypes() throws Exception {
    MixedTypeInterface iface = (MixedTypeInterface)getServiceCreator(MixedTypeInterface.class);

		String name = "Rob Harrop";
		int age = 23;

		TestService testService = iface.createTestService(name, age);
		assertEquals("Name is incorrect", name, testService.getName());
		assertEquals("Age is incorrect", age, testService.getAge());

		String summary = "Name: " + name + ", age: " + age;
		FooService fooService = iface.createFooService(name, age);
		assertEquals("Summary is incorrect", summary, fooService.getSummary());
	}

	public void testWithIncompatibleServiceImplementationType() throws Exception {
		ServiceCreationFactoryBean scfb = new ServiceCreationFactoryBean();
		scfb.setServiceCreatorInterface(SingleMethodCreationInterface.class);
		scfb.setServiceImplementationType(String.class);

		try {
			scfb.afterPropertiesSet();
			fail("Incompatible service implementation type should raise a FatalBeanException");
		}
		catch (FatalBeanException ex) {
			assertTrue(true);
		}
	}

	public void testWithNoServiceCreatorInterface() throws Exception {
		ServiceCreationFactoryBean scfb = new ServiceCreationFactoryBean();
		scfb.setServiceImplementationType(TestServiceBean.class);

		try {
			scfb.afterPropertiesSet();
			fail("No service creator interface should raise a FatalBeanException");
		}
		catch (FatalBeanException ex) {
			assertTrue(true);
		}
	}

	public void testWithClassAsServiceCreatorInterface() throws Exception {
		ServiceCreationFactoryBean scfb = new ServiceCreationFactoryBean();

		try {
			scfb.setServiceCreatorInterface(TestServiceBean.class);
			fail("Using a class for service creator interface should raise a FatalBeanException");
		}
		catch (FatalBeanException ex) {
			assertTrue(true);
		}
	}

	public void testWithNoServiceImplementationType() throws Exception {
		ServiceCreationFactoryBean scfb = new ServiceCreationFactoryBean();
		scfb.setServiceCreatorInterface(SingleMethodCreationInterface.class);

		try {
			scfb.afterPropertiesSet();
			fail("No service implementation type should raise a FatalBeanException");
		}
		catch (FatalBeanException ex) {
			assertTrue(true);
		}
	}

	public void testWithInterfaceAsServiceImplementationType() throws Exception {
		ServiceCreationFactoryBean scfb = new ServiceCreationFactoryBean();

		try {
			scfb.setServiceImplementationType(SingleMethodCreationInterface.class);
			fail("Using an interface for service implementation type should raise a FatalBeanException");
		}
		catch (FatalBeanException ex) {
			assertTrue(true);
		}
	}

	private Object getServiceCreator(Class serviceCreatorInterface) throws Exception {
		ServiceCreationFactoryBean scfb = new ServiceCreationFactoryBean();
		scfb.setServiceCreatorInterface(serviceCreatorInterface);
		scfb.setServiceImplementationType(TestServiceBean.class);
		scfb.afterPropertiesSet();

		return scfb.getObject();
	}

	public static interface SingleMethodCreationInterface {

		TestService createTestService(String name);
	}

	public static interface TwoMethodCreationInterface {

		TestService createTestService(String name);

		TestService createTestService(String name, int age);
	}

	public static interface MixedTypeInterface {
		TestService createTestService(String name, int age);

		FooService createFooService(String name, int age);
	}

	public static interface TestService {

		String getName();

		int getAge();
	}

	public static interface FooService {

		String getSummary();
	}

	public static class TestServiceBean implements TestService, FooService {

		private String name;

		private int age = Integer.MIN_VALUE;

		public TestServiceBean(String name) {
			this.name = name;
		}

		public TestServiceBean(String name, int age) {
			this.name = name;
			this.age = age;
		}

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}

		public String getSummary() {
			return "Name: " + name + ", age: " + age;
		}
	}
}
