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

package org.springframework.aop.framework.autoproxy;

import java.lang.reflect.Proxy;

import junit.framework.TestCase;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.ITestBean;
import org.springframework.beans.IndexedTestBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.DummyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.MessageSource;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.context.support.StaticMessageSource;

/**
 * @author Juergen Hoeller
 * @since 09.12.2003
 */
public class AutoProxyCreatorTests extends TestCase {

	public void testCustomAutoProxyCreator() {
		StaticApplicationContext sac = new StaticApplicationContext();
		sac.registerSingleton("testAutoProxyCreator", TestAutoProxyCreator.class, null);
		sac.registerSingleton("singletonNoInterceptor", TestBean.class, null);
		sac.registerSingleton("singletonToBeProxied", TestBean.class, null);
		sac.registerPrototype("prototypeToBeProxied", TestBean.class, null);
		sac.refresh();

		MessageSource messageSource = (MessageSource) sac.getBean("messageSource");
		ITestBean singletonNoInterceptor = (ITestBean) sac.getBean("singletonNoInterceptor");
		ITestBean singletonToBeProxied = (ITestBean) sac.getBean("singletonToBeProxied");
		ITestBean prototypeToBeProxied = (ITestBean) sac.getBean("prototypeToBeProxied");
		assertFalse(AopUtils.isCglibProxy(messageSource));
		assertTrue(AopUtils.isCglibProxy(singletonNoInterceptor));
		assertTrue(AopUtils.isCglibProxy(singletonToBeProxied));
		assertTrue(AopUtils.isCglibProxy(prototypeToBeProxied));

		TestAutoProxyCreator tapc = (TestAutoProxyCreator) sac.getBean("testAutoProxyCreator");
		assertEquals(0, tapc.testInterceptor.nrOfInvocations);
		singletonNoInterceptor.getName();
		assertEquals(0, tapc.testInterceptor.nrOfInvocations);
		singletonToBeProxied.getAge();
		assertEquals(1, tapc.testInterceptor.nrOfInvocations);
		prototypeToBeProxied.getSpouse();
		assertEquals(2, tapc.testInterceptor.nrOfInvocations);
	}

	public void testBeanNameAutoProxyCreator() {
		StaticApplicationContext sac = new StaticApplicationContext();
		sac.registerSingleton("testInterceptor", TestInterceptor.class, null);

		RootBeanDefinition proxyCreator = new RootBeanDefinition(BeanNameAutoProxyCreator.class, null);
		proxyCreator.getPropertyValues().addPropertyValue("interceptorNames", "testInterceptor");
		proxyCreator.getPropertyValues().addPropertyValue("beanNames", "singletonToBeProxied,innerBean");
		sac.getDefaultListableBeanFactory().registerBeanDefinition("beanNameAutoProxyCreator", proxyCreator);

		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, RootBeanDefinition.AUTOWIRE_BY_TYPE);
		RootBeanDefinition innerBean = new RootBeanDefinition(TestBean.class, null);
		bd.getPropertyValues().addPropertyValue("spouse", new BeanDefinitionHolder(innerBean, "innerBean"));
		sac.getDefaultListableBeanFactory().registerBeanDefinition("singletonToBeProxied", bd);

		sac.registerSingleton("autowiredIndexedTestBean", IndexedTestBean.class, new MutablePropertyValues());

		sac.refresh();

		MessageSource messageSource = (MessageSource) sac.getBean("messageSource");
		ITestBean singletonToBeProxied = (ITestBean) sac.getBean("singletonToBeProxied");
		assertFalse(Proxy.isProxyClass(messageSource.getClass()));
		assertTrue(Proxy.isProxyClass(singletonToBeProxied.getClass()));
		assertTrue(Proxy.isProxyClass(singletonToBeProxied.getSpouse().getClass()));

		// test whether autowiring succeeded with auto proxy creation
		assertEquals(sac.getBean("autowiredIndexedTestBean"), singletonToBeProxied.getNestedIndexedBean());

		TestInterceptor ti = (TestInterceptor) sac.getBean("testInterceptor");
		// already 2: getSpouse + getNestedIndexedBean calls above
		assertEquals(2, ti.nrOfInvocations);
		singletonToBeProxied.getName();
		singletonToBeProxied.getSpouse().getName();
		assertEquals(5, ti.nrOfInvocations);
	}

	public void testAutoProxyCreatorWithFactoryBean() {
		StaticApplicationContext sac = new StaticApplicationContext();
		sac.registerSingleton("testAutoProxyCreator", TestAutoProxyCreator.class, null);

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("singleton", "false");
		sac.registerSingleton("prototypeFactoryToBeProxied", DummyFactory.class, pvs);

		sac.refresh();

		FactoryBean prototypeFactory = (FactoryBean) sac.getBean("&prototypeFactoryToBeProxied");
		assertTrue(AopUtils.isCglibProxy(prototypeFactory));

		TestAutoProxyCreator tapc = (TestAutoProxyCreator) sac.getBean("testAutoProxyCreator");
		tapc.testInterceptor.nrOfInvocations = 0;
		sac.getBean("prototypeFactoryToBeProxied");
		assertEquals(1, tapc.testInterceptor.nrOfInvocations);
	}


	public static class TestAutoProxyCreator extends AbstractAutoProxyCreator {

		public TestInterceptor testInterceptor = new TestInterceptor();

		public TestAutoProxyCreator() {
			setProxyTargetClass(true);
			setOrder(0);
		}

		protected Object[] getAdvicesAndAdvisorsForBean(Class beanClass, String name, TargetSource customTargetSource) {
			if (StaticMessageSource.class.equals(beanClass)) {
				return DO_NOT_PROXY;
			}
			else if (name.endsWith("ToBeProxied")) {
				return new Object[] {this.testInterceptor};
			}
			else {
				return PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS;
			}
		}
	}


	/**
	 * Interceptor that counts the number of non-finalize method calls.
	 */
	public static class TestInterceptor implements MethodInterceptor {

		public int nrOfInvocations = 0;

		public Object invoke(MethodInvocation invocation) throws Throwable {
			if (!invocation.getMethod().getName().equals("finalize")) {
				this.nrOfInvocations++;
			}
			return invocation.proceed();
		}
	}

}
