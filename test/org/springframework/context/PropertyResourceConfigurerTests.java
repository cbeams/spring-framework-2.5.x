package org.springframework.context;

import junit.framework.TestCase;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.context.config.PropertyOverrideConfigurer;
import org.springframework.context.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.StaticApplicationContext;

/**
 * @author Juergen Hoeller
 */
public class PropertyResourceConfigurerTests extends TestCase {

	public void testPropertyOverrideConfigurer() {
		StaticApplicationContext ac = new StaticApplicationContext();
		ac.registerSingleton("tb1", TestBean.class, new MutablePropertyValues());
		ac.registerSingleton("tb2", TestBean.class, new MutablePropertyValues());
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("properties", "tb1.age=99\ntb2.name=test");
		ac.registerSingleton("configurer1", PropertyOverrideConfigurer.class, pvs);
		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("properties", "tb2.age=99\ntb2.name=test2");
		pvs.addPropertyValue("order", "0");
		ac.registerSingleton("configurer2", PropertyOverrideConfigurer.class, pvs);
		ac.rebuild();
		TestBean tb1 = (TestBean) ac.getBean("tb1");
		TestBean tb2 = (TestBean) ac.getBean("tb2");
		assertEquals(99, tb1.getAge());
		assertEquals(99, tb2.getAge());
		assertEquals(null, tb1.getName());
		assertEquals("test", tb2.getName());
	}

	public void testPropertyPlaceholderConfigurer() {
		StaticApplicationContext ac = new StaticApplicationContext();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("age", "${age}");
		pvs.addPropertyValue("name", "name${var}");
		ac.registerSingleton("tb1", TestBean.class, pvs);
		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("age", "${age}");
		pvs.addPropertyValue("name", "name${age}");
		ac.registerSingleton("tb2", TestBean.class, pvs);
		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("properties", "age=99\nvar=myvar");
		ac.registerSingleton("configurer1", PropertyPlaceholderConfigurer.class, pvs);
		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("properties", "age=98");
		pvs.addPropertyValue("order", "0");
		ac.registerSingleton("configurer2", PropertyPlaceholderConfigurer.class, pvs);
		ac.rebuild();
		TestBean tb1 = (TestBean) ac.getBean("tb1");
		TestBean tb2 = (TestBean) ac.getBean("tb2");
		assertEquals(98, tb1.getAge());
		assertEquals(98, tb2.getAge());
		assertEquals("namemyvar", tb1.getName());
		assertEquals("name98", tb2.getName());
	}

}
