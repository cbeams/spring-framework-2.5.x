/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.context.support;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.context.ACATest;
import org.springframework.context.AbstractApplicationContextTests;
import org.springframework.context.BeanThatListens;
import org.springframework.context.config.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

/**
 * Tests for static application context.
 * @author Rod Johnson
 * @version $Id: StaticApplicationContextTestSuite.java,v 1.24 2004-01-14 07:38:00 jhoeller Exp $
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
		parent.refresh();

		StaticMessageSource parentMessageSource = (StaticMessageSource) parent.getBean("messageSource");
		parentMessageSource.addMessage("code1", Locale.getDefault(), "message1");

		this.sac = new StaticApplicationContext(parent);
		sac.addListener(listener);
		sac.registerSingleton("beanThatListens", BeanThatListens.class, new MutablePropertyValues());
		sac.registerSingleton("aca", ACATest.class, new MutablePropertyValues());
		sac.registerPrototype("aca-prototype", ACATest.class, new MutablePropertyValues());
		PropertiesBeanDefinitionReader reader = new PropertiesBeanDefinitionReader(sac.getDefaultListableBeanFactory());
		reader.loadBeanDefinitions(new ClassPathResource("testBeans.properties", getClass()));
		sac.refresh();

		StaticMessageSource sacMessageSource = (StaticMessageSource) sac.getBean("messageSource");
		sacMessageSource.addMessage("code2", Locale.getDefault(), "message2");

		return sac;
	}

	/** Overridden */
	public void testCount() {
		assertCount(16);
	}

}
