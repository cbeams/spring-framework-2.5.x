/*
 * Copyright 2002-2005 the original author or authors.
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

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.NestedCheckedException;
import org.springframework.core.NestedRuntimeException;

/**
 * @author Colin Sampaleanu
 */
public class ServiceLocatorFactoryBeanTests extends TestCase {

	public void testNoArgGetter() {
		StaticApplicationContext ctx = new StaticApplicationContext();
		ctx.registerSingleton("testService", TestService.class, new MutablePropertyValues());
		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.addPropertyValue(new PropertyValue("serviceLocatorInterface", TestServiceLocator.class));
		ctx.registerSingleton("factory", ServiceLocatorFactoryBean.class, mpv);
		ctx.refresh();

		TestServiceLocator factory = (TestServiceLocator) ctx.getBean("factory");
		TestService testBean = factory.getTestService();
	}

	public void testErrorOnTooManyOrTooFew() throws Exception {
		StaticApplicationContext ctx = new StaticApplicationContext();
		ctx.registerSingleton("testService", TestService.class, new MutablePropertyValues());
		ctx.registerSingleton("testServiceInstance2", TestService.class, new MutablePropertyValues());
		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.addPropertyValue(new PropertyValue("serviceLocatorInterface", TestServiceLocator.class));
		ctx.registerSingleton("factory", ServiceLocatorFactoryBean.class, mpv);
		mpv = new MutablePropertyValues();
		mpv.addPropertyValue(new PropertyValue("serviceLocatorInterface", TestServiceLocator2.class));
		ctx.registerSingleton("factory2", ServiceLocatorFactoryBean.class, mpv);
		mpv = new MutablePropertyValues();
		mpv.addPropertyValue(new PropertyValue("serviceLocatorInterface", TestService2Locator.class));
		ctx.registerSingleton("factory3", ServiceLocatorFactoryBean.class, mpv);
		ctx.refresh();

		TestServiceLocator factory = (TestServiceLocator) ctx.getBean("factory");
		try {
			TestService testService = factory.getTestService();
			fail("should fail on more than one matching type");
		}
		catch (NoSuchBeanDefinitionException ex) {
			// expected
		}
		TestServiceLocator2 factory2 = (TestServiceLocator2) ctx.getBean("factory2");
		try {
			TestService testService = factory2.getTestService(null);
			fail("should fail on more than one matching type");
		}
		catch (NoSuchBeanDefinitionException ex) {
			// expected
		}
		TestService2Locator factory3 = (TestService2Locator) ctx.getBean("factory3");
		try {
			TestService2 testService2 = factory3.getTestService();
			fail("should fail on no matching types");
		}
		catch (NoSuchBeanDefinitionException ex) {
			// expected
		}
	}

	public void testErrorOnTooManyOrTooFewWithCustomServiceLocatorException() {
		StaticApplicationContext ctx = new StaticApplicationContext();
		ctx.registerSingleton("testService", TestService.class, new MutablePropertyValues());
		ctx.registerSingleton("testServiceInstance2", TestService.class, new MutablePropertyValues());
		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.addPropertyValue(new PropertyValue("serviceLocatorInterface", TestServiceLocator.class));
		mpv.addPropertyValue(new PropertyValue("serviceLocatorExceptionClass", CustomServiceLocatorException1.class));
		ctx.registerSingleton("factory", ServiceLocatorFactoryBean.class, mpv);
		mpv = new MutablePropertyValues();
		mpv.addPropertyValue(new PropertyValue("serviceLocatorInterface", TestServiceLocator2.class));
		mpv.addPropertyValue(new PropertyValue("serviceLocatorExceptionClass", CustomServiceLocatorException2.class));
		ctx.registerSingleton("factory2", ServiceLocatorFactoryBean.class, mpv);
		mpv = new MutablePropertyValues();
		mpv.addPropertyValue(new PropertyValue("serviceLocatorInterface", TestService2Locator.class));
		mpv.addPropertyValue(new PropertyValue("serviceLocatorExceptionClass", CustomServiceLocatorException3.class));
		ctx.registerSingleton("factory3", ServiceLocatorFactoryBean.class, mpv);
		ctx.refresh();

		TestServiceLocator factory = (TestServiceLocator) ctx.getBean("factory");
		try {
			TestService testService = factory.getTestService();
			fail("should fail on more than one matching type");
		}
		catch (CustomServiceLocatorException1 ex) {
			// expected
			assertTrue(ex.getCause() instanceof NoSuchBeanDefinitionException);
		}
		TestServiceLocator2 factory2 = (TestServiceLocator2) ctx.getBean("factory2");
		try {
			TestService testService = factory2.getTestService(null);
			fail("should fail on more than one matching type");
		}
		catch (CustomServiceLocatorException2 ex) {
			// expected
			assertTrue(ex.getCause() instanceof NoSuchBeanDefinitionException);
		}
		TestService2Locator factory3 = (TestService2Locator) ctx.getBean("factory3");
		try {
			TestService2 testService2 = factory3.getTestService();
			fail("should fail on no matching types");
		}
		catch (CustomServiceLocatorException3 ex) {
			// expected
		}
	}

	public void testStringArgGetter() throws Exception {
		StaticApplicationContext ctx = new StaticApplicationContext();
		ctx.registerSingleton("testService", TestService.class, new MutablePropertyValues());
		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.addPropertyValue(new PropertyValue("serviceLocatorInterface", TestServiceLocator2.class));
		ctx.registerSingleton("factory", ServiceLocatorFactoryBean.class, mpv);
		ctx.refresh();

		// test string-arg getter with null id
		TestServiceLocator2 factory = (TestServiceLocator2) ctx.getBean("factory");
		TestService testBean = factory.getTestService(null);
		// now test with explicit id
		testBean = factory.getTestService("testService");
		// now verify failure on bad id
		try {
			testBean = factory.getTestService("bogusTestService");
			fail("illegal operation allowed");
		}
		catch (NoSuchBeanDefinitionException ex) {
			// expected
		}
	}

	public void testCombinedLocatorInterface() {
		StaticApplicationContext ctx = new StaticApplicationContext();
		ctx.registerPrototype("testService", TestService.class, new MutablePropertyValues());
		ctx.registerAlias("testService", "1");
		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.addPropertyValue("serviceLocatorInterface", TestServiceLocator3.class);
		ctx.registerSingleton("factory", ServiceLocatorFactoryBean.class, mpv);
		ctx.refresh();

		TestServiceLocator3 factory = (TestServiceLocator3) ctx.getBean("factory");
		TestService testBean1 = factory.getTestService();
		TestService testBean2 = factory.getTestService("testService");
		TestService testBean3 = factory.getTestService(1);
		TestService testBean4 = factory.someFactoryMethod();
		assertNotSame(testBean1, testBean2);
		assertNotSame(testBean1, testBean3);
		assertNotSame(testBean1, testBean4);
		assertNotSame(testBean2, testBean3);
		assertNotSame(testBean2, testBean4);
		assertNotSame(testBean3, testBean4);

		assertTrue(factory.toString().indexOf("TestServiceLocator3") != -1);
	}

	public void testServiceMappings() {
		StaticApplicationContext ctx = new StaticApplicationContext();
		ctx.registerPrototype("testService1", TestService.class, new MutablePropertyValues());
		ctx.registerPrototype("testService2", ExtendedTestService.class, new MutablePropertyValues());
		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.addPropertyValue("serviceLocatorInterface", TestServiceLocator3.class);
		mpv.addPropertyValue("serviceMappings", "=testService1\n1=testService1\n2=testService2");
		ctx.registerSingleton("factory", ServiceLocatorFactoryBean.class, mpv);
		ctx.refresh();

		TestServiceLocator3 factory = (TestServiceLocator3) ctx.getBean("factory");
		TestService testBean1 = factory.getTestService();
		TestService testBean2 = factory.getTestService("testService1");
		TestService testBean3 = factory.getTestService(1);
		TestService testBean4 = factory.getTestService(2);
		assertNotSame(testBean1, testBean2);
		assertNotSame(testBean1, testBean3);
		assertNotSame(testBean1, testBean4);
		assertNotSame(testBean2, testBean3);
		assertNotSame(testBean2, testBean4);
		assertNotSame(testBean3, testBean4);
		assertFalse(testBean1 instanceof ExtendedTestService);
		assertFalse(testBean2 instanceof ExtendedTestService);
		assertFalse(testBean3 instanceof ExtendedTestService);
		assertTrue(testBean4 instanceof ExtendedTestService);
	}


	public static class TestService {
	}


	public static class ExtendedTestService extends TestService {
	}


	public static class TestService2 {
	}


	public static interface TestServiceLocator {

		TestService getTestService();
	}


	public static interface TestServiceLocator2 {

		TestService getTestService(String id) throws CustomServiceLocatorException2;
	}


	public static interface TestServiceLocator3 {

		TestService getTestService();

		TestService getTestService(String id);

		TestService getTestService(int id);

		TestService someFactoryMethod();
	}


	public static interface TestService2Locator {

		TestService2 getTestService() throws CustomServiceLocatorException3;
	}


	public static class CustomServiceLocatorException1 extends NestedRuntimeException {

		public CustomServiceLocatorException1(String message, Throwable cause) {
			super(message, cause);
		}
	}


	public static class CustomServiceLocatorException2 extends NestedCheckedException {

		public CustomServiceLocatorException2(Throwable cause) {
			super("", cause);
		}
	}


	public static class CustomServiceLocatorException3 extends NestedCheckedException {

		public CustomServiceLocatorException3(String message) {
			super(message);
		}
	}

}
