package org.springframework.aop.framework.support;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.DummyFactory;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RuntimeBeanReference;
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
		sac.registerSingleton("autoProxyTest", TestBean.class, new MutablePropertyValues());
		sac.registerSingleton("testInterceptorForCreator", TestInterceptor.class, new MutablePropertyValues());
		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("beanNames", "autoProxyTest,prototypeFac*");
		List interceptors = new ManagedList();
		interceptors.add(new RuntimeBeanReference("testInterceptorForCreator"));
		pvs.addPropertyValue("interceptors", interceptors);
		sac.registerSingleton("beanNameAutoProxyCreator", BeanNameAutoProxyCreator.class, pvs);
		sac.registerSingleton("beanThatListens", BeanThatListens.class, new MutablePropertyValues());
		sac.registerSingleton("aca", ACATest.class, new MutablePropertyValues());
		sac.registerPrototype("aca-prototype", ACATest.class, new MutablePropertyValues());
		sac.refresh();

		StaticMessageSource sacMessageSource = (StaticMessageSource) sac.getBean("messageSource");
		sacMessageSource.addMessage("code2", Locale.getDefault(), "message2");
	}

	public void testBeanPostProcessors() {
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
				           bean.getClass().getName().indexOf("EnhancedByCGLIB") != -1);
			}
		}
		ACATest aca = (ACATest) sac.getBean("aca");
		aca.getApplicationContext();
		aca.getApplicationContext();
		ACATest acaPr = (ACATest) sac.getBean("aca-prototype");
		acaPr.getApplicationContext();
		TestInterceptor ti = (TestInterceptor) sac.getBean("testInterceptorForCreator");
		assertEquals(1, ti.nrOfInvocations);
		TestAutoProxyCreator tapc = (TestAutoProxyCreator) sac.getBean("testAutoProxyCreator");
		assertEquals(3, tapc.testInterceptor.nrOfInvocations);
	}


	public static class TestAutoProxyCreator extends AbstractAutoProxyCreator {

		public TestInterceptor testInterceptor = new TestInterceptor();

		public TestAutoProxyCreator() {
			setProxyTargetClass(true);
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
