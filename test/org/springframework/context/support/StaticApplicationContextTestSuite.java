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
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.RuntimeBeanReference;
import org.springframework.context.ACATest;
import org.springframework.context.AbstractApplicationContextTests;
import org.springframework.context.ApplicationContext;
import org.springframework.context.BeanThatListens;

/**
 * Classname doesn't match XXXXTestSuite pattern, so as to avoid
 * being invoked by Ant JUnit run, as it's abstract
 * @author Rod Johnson
 * @version $Revision: 1.3 $
 */
public class StaticApplicationContextTestSuite extends AbstractApplicationContextTests {

	protected StaticApplicationContext sac;

	/** Run for each test */
	protected ApplicationContext createContext() throws Exception {
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
		pvs.addPropertyValue("beanNames", "autoProxyTest,singletonFac*");
		List interceptors = new ManagedList();
		interceptors.add(new RuntimeBeanReference("testInterceptorForCreator"));
		pvs.addPropertyValue("interceptors", interceptors);
		sac.registerSingleton("beanNameAutoProxyCreator", BeanNameAutoProxyCreator.class, pvs);
		sac.registerSingleton("beanThatListens", BeanThatListens.class, new MutablePropertyValues());
		sac.registerSingleton("aca", ACATest.class, new MutablePropertyValues());
		sac.registerPrototype("aca-prototype", ACATest.class, new MutablePropertyValues());
		LBIInit.createTestBeans(sac.defaultBeanFactory);
		sac.rebuild();

		StaticMessageSource sacMessageSource = (StaticMessageSource) sac.getBean("messageSource");
		sacMessageSource.addMessage("code2", Locale.getDefault(), "message2");

		return sac;
	}

	/** Overridden */
	public void testCount() throws Exception {
		assertCount(17);
	}

	public void testBeanPostProcessors() {
		String[] beanNames = getListableBeanFactory().getBeanDefinitionNames();
		for (int i = 0; i < beanNames.length; i++) {
			if (beanNames[i].equals("autoProxyTest")) {
				Object bean = getListableBeanFactory().getBean(beanNames[i]);
				assertTrue("J2SE proxy for bean '" + beanNames[i] + "': " + bean.getClass().getName(),
				           Proxy.isProxyClass(bean.getClass()));
			}
			else if (beanNames[i].equals("singletonFactory")) {
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
		TestInterceptor ti = (TestInterceptor) getListableBeanFactory().getBean("testInterceptorForCreator");
		assertEquals(2, ti.nrOfInvocations);
	}


	public static class TestAutoProxyCreator extends AbstractAutoProxyCreator {

		public TestAutoProxyCreator() {
			setProxyInterfacesOnly(false);
			setOrder(0);
		}

		protected boolean isBeanToProxy(Object bean, String name, RootBeanDefinition definition) {
			return (!(bean instanceof StaticMessageSource));
		}
	}


	public static class TestInterceptor implements MethodInterceptor {

		public int nrOfInvocations = 0;

		public Object invoke(MethodInvocation methodInvocation) throws Throwable {
			nrOfInvocations++;
			return methodInvocation.proceed();
		}
	}

}
