
package org.springframework.beans.factory.config;

import junit.framework.TestCase;

import org.springframework.beans.FatalBeanException;

/**
 * @author robh
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
		scfb.setServiceCreatorInterface(TestServiceBean.class);
		scfb.setServiceImplementationType(TestServiceBean.class);

		try {
			scfb.afterPropertiesSet();
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
		scfb.setServiceCreatorInterface(SingleMethodCreationInterface.class);
		scfb.setServiceImplementationType(SingleMethodCreationInterface.class);

		try {
			scfb.afterPropertiesSet();
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
