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

package org.springframework.aop.beans;

import junit.framework.TestCase;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.StaticApplicationContext;

/**
 * Test for ServiceLocatorProxyCreator
 * 
 * @author Colin Sampaleanu
 */
public class ServiceLocatorProxyCreatorTest extends TestCase {

	public void testNoArgeGetter() {
		StaticApplicationContext ctx = new StaticApplicationContext();

		ctx.registerSingleton("testbean", TestService.class, new MutablePropertyValues());
		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.addPropertyValue(new PropertyValue("serviceLocatorInterface",
				TestServiceLocator.class));
		ctx.registerSingleton("factory", ServiceLocatorProxyCreator.class, mpv);
		ctx.refresh();
		TestServiceLocator factory = (TestServiceLocator) ctx.getBean("factory");
		TestService tetBean = factory.getTestService();
	}
	
	public void testErrorOnTooManyOrTooFew() {
		StaticApplicationContext ctx = new StaticApplicationContext();

		ctx.registerSingleton("testbean", TestService.class, new MutablePropertyValues());
		ctx.registerSingleton("testbean2", TestService.class, new MutablePropertyValues());
		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.addPropertyValue(new PropertyValue("serviceLocatorInterface",
				TestServiceLocator.class));
		ctx.registerSingleton("factory", ServiceLocatorProxyCreator.class, mpv);
		mpv = new MutablePropertyValues();
		mpv.addPropertyValue(new PropertyValue("serviceLocatorInterface",
				TestServiceLocator2.class));
		ctx.registerSingleton("factory2", ServiceLocatorProxyCreator.class, mpv);
		mpv = new MutablePropertyValues();
		mpv.addPropertyValue(new PropertyValue("serviceLocatorInterface",
				TestService2Locator.class));
		ctx.registerSingleton("factory3", ServiceLocatorProxyCreator.class, mpv);
		ctx.refresh();
		TestServiceLocator factory = (TestServiceLocator) ctx.getBean("factory");
		try {
			TestService testService = factory.getTestService();
			fail("should fail on more than one matching type");
		}
		catch (NoSuchBeanDefinitionException e) {
			// expected
		}
		TestServiceLocator2 factory2 = (TestServiceLocator2) ctx.getBean("factory2");
		try {
			TestService testService = factory2.getTestService(null);
			fail("should fail on more than one matching type");
		}
		catch (NoSuchBeanDefinitionException e) {
			// expected
		}
		TestService2Locator factory3 = (TestService2Locator) ctx.getBean("factory3");
		try {
			TestService2 testService2 = factory3.getTestService();
			fail("should fail on no matching types");
		}
		catch (NoSuchBeanDefinitionException e) {
			// expected
		}
	}
	
	public void testStringArgGetterWithNullId() {
		StaticApplicationContext ctx = new StaticApplicationContext();

		ctx.registerSingleton("testbean", TestService.class, new MutablePropertyValues());
		MutablePropertyValues mpv = new MutablePropertyValues();
		mpv.addPropertyValue(new PropertyValue("serviceLocatorInterface",
				TestServiceLocator2.class));
		ctx.registerSingleton("factory", ServiceLocatorProxyCreator.class, mpv);
		ctx.refresh();
		TestServiceLocator2 factory = (TestServiceLocator2) ctx.getBean("factory");
		TestService tetBean = factory.getTestService(null);
	}

	public static class TestService {
	}

	public static class TestService2 {
	}

	public static interface TestServiceLocator {
		TestService getTestService();
	}
	public static interface TestServiceLocator2 {
		TestService getTestService(String id);
	}

	public static interface TestService2Locator {
		TestService2 getTestService();
	}	
}
