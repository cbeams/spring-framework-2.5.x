package org.springframework.context.support;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.aop.framework.InvokerInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.LBIInit;
import org.springframework.beans.factory.support.BeanPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ACATest;
import org.springframework.context.AbstractApplicationContextTests;
import org.springframework.context.ApplicationContext;
import org.springframework.context.BeanThatListens;

/**
 * Classname doesn't match XXXXTestSuite pattern, so as to avoid
 * being invoked by Ant JUnit run, as it's abstract
 * @author Rod Johnson
 * @version $Revision: 1.2 $
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
		sac.registerSingleton("testBeanProcessor", TestBeanPostProcessor.class, new MutablePropertyValues());
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

	public void testCustomBeanPostProcessor() {
		String[] beanNames = getListableBeanFactory().getBeanDefinitionNames();
		for (int i = 0; i < beanNames.length; i++) {
			if (beanNames[i].toLowerCase().indexOf("factory") == -1 && !beanNames[i].equals("messageSource") &&
			    !beanNames[i].equals("typeMismatch") && !beanNames[i].equals("testBeanProcessor")) {
				Object bean = getListableBeanFactory().getBean(beanNames[i]);
				assertTrue("Enhanced bean class for bean '" + beanNames[i] + "': " + bean.getClass().getName(),
				           bean.getClass().getName().indexOf("EnhancedByCGLIB") != -1);
			}
		}
	}


	public static class TestBeanPostProcessor implements BeanPostProcessor {

		public Object postProcessBean(Object bean, String name, RootBeanDefinition definition) {
			if (!(bean instanceof StaticMessageSource)) {
				ProxyFactory proxyFactory = new ProxyFactory();
				proxyFactory.addInterceptor(new InvokerInterceptor(bean));
				return proxyFactory.getProxy();
			}
			else {
				return bean;
			}
		}
	}

}
