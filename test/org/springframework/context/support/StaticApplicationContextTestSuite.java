/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.context.support;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.aop.framework.support.AbstractAutoProxyCreator;
import org.springframework.aop.framework.support.BeanNameAutoProxyCreator;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.LBIInit;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RuntimeBeanReference;
import org.springframework.context.ACATest;
import org.springframework.context.AbstractApplicationContextTests;
import org.springframework.context.BeanThatListens;
import org.springframework.context.config.ConfigurableApplicationContext;

/**
 * Tests for static application context.
 * @author Rod Johnson
 * @version $Id: StaticApplicationContextTestSuite.java,v 1.17 2003-11-22 17:20:29 jhoeller Exp $
 */
public class StaticApplicationContextTestSuite extends AbstractApplicationContextTests {

	protected StaticApplicationContext sac;

	/** Run for each test */
	protected ConfigurableApplicationContext createContext() throws Exception {
		StaticApplicationContext parent = new StaticApplicationContext();
		parent.addListener(parentListener) ;
		Map m = new HashMap();
		m.put("name", "Roderick");
		parent.registerPrototype("rod", TestBean.class, new MutablePropertyValues(m));
		m.put("name", "Albert");
		parent.registerPrototype("father", TestBean.class, new MutablePropertyValues(m));
		parent.rebuild();

		StaticMessageSource parentMessageSource = (StaticMessageSource) parent.getBean("messageSource");
		parentMessageSource.addMessage("code1", Locale.getDefault(), "message1");

		this.sac = new StaticApplicationContext(parent);
		sac.addListener(listener);
		sac.registerSingleton("testAutoProxyCreator", TestAutoProxyCreator.class, new MutablePropertyValues());
		sac.registerSingleton("autoProxyTest", TestBean.class, new MutablePropertyValues());
		sac.registerSingleton("testInterceptorForCreator", TestInterceptor.class, new MutablePropertyValues());
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("beanNames", "autoProxyTest,prototypeFac*");
		List interceptors = new ManagedList();
		interceptors.add(new RuntimeBeanReference("testInterceptorForCreator"));
		pvs.addPropertyValue("interceptors", interceptors);
		sac.registerSingleton("beanNameAutoProxyCreator", BeanNameAutoProxyCreator.class, pvs);
		sac.registerSingleton("beanThatListens", BeanThatListens.class, new MutablePropertyValues());
		sac.registerSingleton("aca", ACATest.class, new MutablePropertyValues());
		sac.registerPrototype("aca-prototype", ACATest.class, new MutablePropertyValues());
		LBIInit.createTestBeans(sac.getListableBeanFactory());
		sac.rebuild();

		StaticMessageSource sacMessageSource = (StaticMessageSource) sac.getBean("messageSource");
		sacMessageSource.addMessage("code2", Locale.getDefault(), "message2");

		return sac;
	}

	/** Overridden */
	public void testCount() {
		assertCount(20);
	}

	/** Overridden */
	public void testTestBeanCount() {
		assertTestBeanCount(8);
	}

	public void testBeanPostProcessors() {
		String[] beanNames = getListableBeanFactory().getBeanDefinitionNames();
		for (int i = 0; i < beanNames.length; i++) {
			if (beanNames[i].equals("autoProxyTest")) {
				Object bean = getListableBeanFactory().getBean(beanNames[i]);
				assertTrue("J2SE proxy for bean '" + beanNames[i] + "': " + bean.getClass().getName(),
				           Proxy.isProxyClass(bean.getClass()));
			}
			else if (beanNames[i].equals("prototypeFactory")) {
				Object bean = getListableBeanFactory().getBean("&" + beanNames[i]);
				assertTrue("J2SE proxy for bean '" + beanNames[i] + "': " + bean.getClass().getName(),
				           Proxy.isProxyClass(bean.getClass()));
			}
			else if (beanNames[i].toLowerCase().indexOf("factory") == -1 && !beanNames[i].equals("messageSource") &&
			    !beanNames[i].equals("typeMismatch") && !beanNames[i].endsWith("Creator")) {
				Object bean = getListableBeanFactory().getBean(beanNames[i]);
				assertTrue("Enhanced bean class for bean '" + beanNames[i] + "': " + bean.getClass().getName(),
				           bean.getClass().getName().indexOf("EnhancedByCGLIB") != -1);
			}
		}
		ACATest aca = (ACATest) getListableBeanFactory().getBean("aca");
		aca.getApplicationContext();
		aca.getApplicationContext();
		ACATest acaPr = (ACATest) getListableBeanFactory().getBean("aca-prototype");
		acaPr.getApplicationContext();
		TestInterceptor ti = (TestInterceptor) getListableBeanFactory().getBean("testInterceptorForCreator");
		assertEquals(1, ti.nrOfInvocations);
		TestAutoProxyCreator tapc = (TestAutoProxyCreator) getListableBeanFactory().getBean("testAutoProxyCreator");
		assertEquals(3, tapc.testInterceptor.nrOfInvocations);
	}


	public static class TestAutoProxyCreator extends AbstractAutoProxyCreator {

		public TestInterceptor testInterceptor = new TestInterceptor();

		public TestAutoProxyCreator() {
			setProxyInterfacesOnly(false);
			setOrder(0);
		}

		protected Object[] getInterceptorsAndAdvisorsForBean(Object bean, String name) {
			if (bean instanceof StaticMessageSource)
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
