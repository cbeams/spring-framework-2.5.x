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

package org.springframework.aop.framework.autoproxy;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.support.AopUtils;
import org.springframework.aop.TargetSource;
import org.springframework.beans.ITestBean;
import org.springframework.beans.IndexedTestBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.DummyFactory;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ACATest;
import org.springframework.context.BeanThatListens;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.context.support.StaticMessageSource;

/**
 * @author Juergen Hoeller
 * @since 09.12.2003
 */
public class AutoProxyCreatorTestSuite extends TestCase {

	protected StaticApplicationContext sac;

	protected void setUp() throws Exception {
		StaticApplicationContext parent = new StaticApplicationContext();
		Map m = new HashMap();
		m.put("name", "Roderick");
		parent.registerPrototype("rod", TestBean.class, new MutablePropertyValues(m));
		m.put("name", "Albert");
		parent.registerPrototype("father", TestBean.class, new MutablePropertyValues(m));
		parent.refresh();

		StaticMessageSource parentMessageSource = (StaticMessageSource) parent.getBean("messageSource");
		parentMessageSource.addMessage("code1", Locale.getDefault(), "message1");

		this.sac = new StaticApplicationContext(parent);

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("singleton", "false");
		sac.registerSingleton("prototypeFactory", DummyFactory.class, pvs);

		sac.registerSingleton("testAutoProxyCreator", TestAutoProxyCreator.class, new MutablePropertyValues());

		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, RootBeanDefinition.AUTOWIRE_BY_TYPE);
		RootBeanDefinition innerBean = new RootBeanDefinition(TestBean.class, null);
		bd.getPropertyValues().addPropertyValue("spouse", new BeanDefinitionHolder(innerBean, "innerBean"));
		sac.getDefaultListableBeanFactory().registerBeanDefinition("autoProxyTest", bd);

		sac.registerSingleton("autoProxyTest2", IndexedTestBean.class, new MutablePropertyValues());
		sac.registerSingleton("testInterceptorForCreator", TestInterceptor.class, new MutablePropertyValues());

		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("beanNames", "autoProxyTest,autoProxyTest2,prototypeFac*,innerBean");
		List interceptors = new LinkedList();
		interceptors.add("testInterceptorForCreator");
		pvs.addPropertyValue("interceptorNames", interceptors);
		sac.registerSingleton("beanNameAutoProxyCreator", BeanNameAutoProxyCreator.class, pvs);

		sac.registerSingleton("beanThatListens", BeanThatListens.class, new MutablePropertyValues());
		sac.registerSingleton("aca", ACATest.class, new MutablePropertyValues());
		sac.registerPrototype("aca-prototype", ACATest.class, new MutablePropertyValues());

		sac.refresh();

		StaticMessageSource sacMessageSource = (StaticMessageSource) sac.getBean("messageSource");
		sacMessageSource.addMessage("code2", Locale.getDefault(), "message2");
	}

	public void testBeanPostProcessors() {
		assertEquals(sac.getBean("autoProxyTest2"), ((ITestBean) sac.getBean("autoProxyTest")).getNestedIndexedBean());
		String[] beanNames = sac.getBeanDefinitionNames();
		for (int i = 0; i < beanNames.length; i++) {
			if (beanNames[i].equals("autoProxyTest")) {
				Object bean = sac.getBean(beanNames[i]);
				assertTrue("J2SE proxy for bean '" + beanNames[i] + "': " + bean.getClass().getName(),
						Proxy.isProxyClass(bean.getClass()));
			}
			else if (beanNames[i].equals("prototypeFactory")) {
				Object bean = sac.getBean("&" + beanNames[i]);
				assertTrue("J2SE proxy for bean '" + beanNames[i] + "': " + bean.getClass().getName(),
						Proxy.isProxyClass(bean.getClass()));
			}
			else if (!beanNames[i].equals("messageSource") && !beanNames[i].endsWith("Creator")) {
				Object bean = sac.getBean(beanNames[i]);
				assertTrue("Enhanced bean class for bean '" + beanNames[i] + "': " + bean.getClass().getName(),
						AopUtils.isCglibProxy(bean));
			}
		}
		ACATest aca = (ACATest) sac.getBean("aca");
		aca.getApplicationContext();
		aca.getApplicationContext();
		ACATest acaPr = (ACATest) sac.getBean("aca-prototype");
		acaPr.getApplicationContext();
		TestInterceptor ti = (TestInterceptor) sac.getBean("testInterceptorForCreator");
		assertEquals(19, ti.nrOfInvocations);
		TestAutoProxyCreator tapc = (TestAutoProxyCreator) sac.getBean("testAutoProxyCreator");
		assertEquals(3, tapc.testInterceptor.nrOfInvocations);
	}


	public static class TestAutoProxyCreator extends AbstractAutoProxyCreator {

		public TestInterceptor testInterceptor = new TestInterceptor();

		public TestAutoProxyCreator() {
			setProxyTargetClass(true);
			setOrder(0);
		}

		protected Object[] getAdvicesAndAdvisorsForBean(Object bean, String name, TargetSource customTargetSource) {
			if (bean instanceof StaticMessageSource || bean instanceof IndexedTestBean)
				return DO_NOT_PROXY;
			else if (name.startsWith("aca"))
				return new Object[] {testInterceptor};
			else
				return PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS;
		}
	}


	public static class TestInterceptor implements MethodInterceptor {

		public int nrOfInvocations = 0;

		public Object invoke(MethodInvocation methodInvocation) throws Throwable {
			if (!methodInvocation.getMethod().getName().equals("finalize")) {
				nrOfInvocations++;
			}
			return methodInvocation.proceed();
		}
	}

}
