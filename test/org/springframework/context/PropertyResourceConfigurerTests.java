package org.springframework.context;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RuntimeBeanReference;
import org.springframework.beans.factory.support.ManagedMap;
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
		pvs.addPropertyValue("spouse", new RuntimeBeanReference("${ref}"));
		ac.registerSingleton("tb1", TestBean.class, pvs);
		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("age", "${age}");
		pvs.addPropertyValue("name", "name${age}");
		List friends = new ManagedList();
		friends.add("na${age}me");
		friends.add(new RuntimeBeanReference("${ref}"));
		pvs.addPropertyValue("friends", friends);
		Map someMap = new ManagedMap();
		someMap.put("key1", new RuntimeBeanReference("${ref}"));
		someMap.put("key2", "${age}name");
		pvs.addPropertyValue("someMap", someMap);
		ac.registerSingleton("tb2", TestBean.class, pvs);
		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("properties", "age=99\nvar=${m}var\nref=tb2\nm=my");
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
		assertEquals(tb2, tb1.getSpouse());
		assertEquals(2, tb2.getFriends().size());
		assertEquals("na98me", tb2.getFriends().iterator().next());
		assertEquals(tb2, ((List) tb2.getFriends()).get(1));
		assertEquals(2, tb2.getSomeMap().size());
		assertEquals(tb2, tb2.getSomeMap().get("key1"));
		assertEquals("98name", tb2.getSomeMap().get("key2"));
	}

	public void testPropertyPlaceholderConfigurerWithCircularReference() {
		StaticApplicationContext ac = new StaticApplicationContext();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("age", "${age}");
		pvs.addPropertyValue("name", "name${var}");
		pvs.addPropertyValue("spouse", new RuntimeBeanReference("${ref}"));
		ac.registerSingleton("tb1", TestBean.class, pvs);
		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("age", "${age}");
		pvs.addPropertyValue("name", "name${age}");
		ac.registerSingleton("tb2", TestBean.class, pvs);
		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("properties", "age=99\nvar=${m}var\nref=tb2\nm=${var}");
		ac.registerSingleton("configurer1", PropertyPlaceholderConfigurer.class, pvs);
		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("properties", "age=98");
		pvs.addPropertyValue("order", "0");
		ac.registerSingleton("configurer2", PropertyPlaceholderConfigurer.class, pvs);
		try {
			ac.rebuild();
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (BeanDefinitionStoreException ex) {
			// expected
		}
	}

}
