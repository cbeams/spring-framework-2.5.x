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

package org.springframework.beans.factory.config;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.StaticApplicationContext;

/**
 * @author Juergen Hoeller
 * @since 02.10.2003
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
		ac.refresh();
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
		pvs.addPropertyValue("name", "name${var}${var}${");
		pvs.addPropertyValue("spouse", new RuntimeBeanReference("${ref}"));
		ac.registerSingleton("tb1", TestBean.class, pvs);

		ConstructorArgumentValues cas = new ConstructorArgumentValues();
		cas.addIndexedArgumentValue(1, "${age}");
		cas.addGenericArgumentValue("${var}name${age}");

		pvs = new MutablePropertyValues();
		List friends = new ManagedList();
		friends.add("na${age}me");
		friends.add(new RuntimeBeanReference("${ref}"));
		pvs.addPropertyValue("friends", friends);

		Set someSet = new ManagedSet();
		someSet.add("na${age}me");
		someSet.add(new RuntimeBeanReference("${ref}"));
		pvs.addPropertyValue("someSet", someSet);

		Map someMap = new ManagedMap();
		someMap.put("key1", new RuntimeBeanReference("${ref}"));
		someMap.put("key2", "${age}name");
		MutablePropertyValues innerPvs = new MutablePropertyValues();
		innerPvs.addPropertyValue("touchy", "${os.name}");
		someMap.put("key3", new RootBeanDefinition(TestBean.class, innerPvs));
		MutablePropertyValues innerPvs2 = new MutablePropertyValues(innerPvs);
		someMap.put("${key4}", new BeanDefinitionHolder(new ChildBeanDefinition("tb1", innerPvs2), "child"));
		pvs.addPropertyValue("someMap", someMap);

		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, cas, pvs);
		ac.getDefaultListableBeanFactory().registerBeanDefinition("tb2", bd);

		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("properties", "age=98\nvar=${m}var\nref=tb2\nm=my\nkey4=mykey4");
		ac.registerSingleton("configurer", PropertyPlaceholderConfigurer.class, pvs);
		ac.refresh();

		TestBean tb1 = (TestBean) ac.getBean("tb1");
		TestBean tb2 = (TestBean) ac.getBean("tb2");
		assertEquals(98, tb1.getAge());
		assertEquals(98, tb2.getAge());
		assertEquals("namemyvarmyvar${", tb1.getName());
		assertEquals("myvarname98", tb2.getName());
		assertEquals(tb2, tb1.getSpouse());
		assertEquals(2, tb2.getFriends().size());
		assertEquals("na98me", tb2.getFriends().iterator().next());
		assertEquals(tb2, ((List) tb2.getFriends()).get(1));
		assertEquals(2, tb2.getSomeSet().size());
		assertTrue(tb2.getSomeSet().contains("na98me"));
		assertTrue(tb2.getSomeSet().contains(tb2));
		assertEquals(4, tb2.getSomeMap().size());
		assertEquals(tb2, tb2.getSomeMap().get("key1"));
		assertEquals("98name", tb2.getSomeMap().get("key2"));
		TestBean inner1 = (TestBean) tb2.getSomeMap().get("key3");
		TestBean inner2 = (TestBean) tb2.getSomeMap().get("mykey4");
		assertEquals(0, inner1.getAge());
		assertEquals(null, inner1.getName());
		assertEquals(System.getProperty("os.name"), inner1.getTouchy());
		assertEquals(98, inner2.getAge());
		assertEquals("namemyvarmyvar${", inner2.getName());
		assertEquals(System.getProperty("os.name"), inner2.getTouchy());
	}

	public void testPropertyPlaceholderConfigurerWithSystemPropertyFallback() {
		StaticApplicationContext ac = new StaticApplicationContext();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("touchy", "${os.name}");
		ac.registerSingleton("tb", TestBean.class, pvs);
		pvs = new MutablePropertyValues();
		ac.registerSingleton("configurer", PropertyPlaceholderConfigurer.class, pvs);
		ac.refresh();
		TestBean tb = (TestBean) ac.getBean("tb");
		assertEquals(System.getProperty("os.name"), tb.getTouchy());
	}

	public void testPropertyPlaceholderConfigurerWithSystemPropertyNotUsed() {
		StaticApplicationContext ac = new StaticApplicationContext();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("touchy", "${os.name}");
		ac.registerSingleton("tb", TestBean.class, pvs);
		pvs = new MutablePropertyValues();
		Properties props = new Properties();
		props.put("os.name", "myos");
		pvs.addPropertyValue("properties", props);
		ac.registerSingleton("configurer", PropertyPlaceholderConfigurer.class, pvs);
		ac.refresh();
		TestBean tb = (TestBean) ac.getBean("tb");
		assertEquals("myos", tb.getTouchy());
	}

	public void testPropertyPlaceholderConfigurerWithOverridingSystemProperty() {
		StaticApplicationContext ac = new StaticApplicationContext();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("touchy", "${os.name}");
		ac.registerSingleton("tb", TestBean.class, pvs);
		pvs = new MutablePropertyValues();
		Properties props = new Properties();
		props.put("os.name", "myos");
		pvs.addPropertyValue("properties", props);
		pvs.addPropertyValue("systemPropertiesModeName", "SYSTEM_PROPERTIES_MODE_OVERRIDE");
		ac.registerSingleton("configurer", PropertyPlaceholderConfigurer.class, pvs);
		ac.refresh();
		TestBean tb = (TestBean) ac.getBean("tb");
		assertEquals(System.getProperty("os.name"), tb.getTouchy());
	}

	public void testPropertyPlaceholderConfigurerWithUnresolvableSystemProperty() {
		StaticApplicationContext ac = new StaticApplicationContext();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("touchy", "${user.dir}");
		ac.registerSingleton("tb", TestBean.class, pvs);
		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("systemPropertiesModeName", "SYSTEM_PROPERTIES_MODE_NEVER");
		ac.registerSingleton("configurer", PropertyPlaceholderConfigurer.class, pvs);
		try {
			ac.refresh();
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (BeanDefinitionStoreException ex) {
			// expected
			assertTrue(ex.getMessage().indexOf("user.dir") != -1);
		}
	}

	public void testPropertyPlaceholderConfigurerWithUnresolvablePlaceholder() {
		StaticApplicationContext ac = new StaticApplicationContext();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("name", "${ref}");
		ac.registerSingleton("tb", TestBean.class, pvs);
		ac.registerSingleton("configurer", PropertyPlaceholderConfigurer.class, null);
		try {
			ac.refresh();
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (BeanDefinitionStoreException ex) {
			// expected
			assertTrue(ex.getMessage().indexOf("ref") != -1);
		}
	}

	public void testPropertyPlaceholderConfigurerWithIgnoreUnresolvablePlaceholder() {
		StaticApplicationContext ac = new StaticApplicationContext();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("name", "${ref}");
		ac.registerSingleton("tb", TestBean.class, pvs);
		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("ignoreUnresolvablePlaceholders", Boolean.TRUE);
		ac.registerSingleton("configurer", PropertyPlaceholderConfigurer.class, pvs);
		try {
			ac.refresh();
			TestBean tb = (TestBean) ac.getBean("tb");
			assertEquals("${ref}", tb.getName());
		}
		catch (BeanDefinitionStoreException ex) {
			fail("Should not have thrown BeanDefinitionStoreException");
		}
	}

	public void testPropertyPlaceholderConfigurerWithSystemPropertyInLocation() {
		StaticApplicationContext ac = new StaticApplicationContext();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("spouse", new RuntimeBeanReference("${ref}"));
		ac.registerSingleton("tb", TestBean.class, pvs);
		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("location", "${user.dir}/test");
		ac.registerSingleton("configurer", PropertyPlaceholderConfigurer.class, pvs);
		try {
			ac.refresh();
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (BeanInitializationException ex) {
			// expected
			assertTrue(ex.getCause() instanceof FileNotFoundException);
			// slight hack for Linux/Unix systems
			String userDir = System.getProperty("user.dir");
			if (userDir.startsWith("/"))
				userDir = userDir.substring(1);
			assertTrue(ex.getMessage().indexOf(userDir) != -1);
		}
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
			ac.refresh();
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (BeanDefinitionStoreException ex) {
			// expected
		}
	}

	public void testPropertyPlaceholderConfigurerWithDefaultProperties() {
		StaticApplicationContext ac = new StaticApplicationContext();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("touchy", "${test}");
		ac.registerSingleton("tb", TestBean.class, pvs);
		pvs = new MutablePropertyValues();
		Properties props = new Properties();
		props.put("test", "mytest");
		pvs.addPropertyValue("properties", new Properties(props));
		ac.registerSingleton("configurer", PropertyPlaceholderConfigurer.class, pvs);
		ac.refresh();
		TestBean tb = (TestBean) ac.getBean("tb");
		assertEquals("mytest", tb.getTouchy());
	}

	public void testPreferencesPlaceholderConfigurer() {
		StaticApplicationContext ac = new StaticApplicationContext();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("name", "${myName}");
		pvs.addPropertyValue("age", "${myAge}");
		pvs.addPropertyValue("touchy", "${myTouchy}");
		ac.registerSingleton("tb", TestBean.class, pvs);
		pvs = new MutablePropertyValues();
		Properties props = new Properties();
		props.put("myAge", "99");
		pvs.addPropertyValue("properties", props);
		ac.registerSingleton("configurer", PreferencesPlaceholderConfigurer.class, pvs);
		Preferences.systemRoot().put("myName", "myNameValue");
		Preferences.systemRoot().put("myTouchy", "myTouchyValue");
		Preferences.userRoot().put("myTouchy", "myOtherTouchyValue");
		ac.refresh();
		TestBean tb = (TestBean) ac.getBean("tb");
		assertEquals("myNameValue", tb.getName());
		assertEquals(99, tb.getAge());
		assertEquals("myOtherTouchyValue", tb.getTouchy());
		Preferences.userRoot().remove("myTouchy");
		Preferences.systemRoot().remove("myTouchy");
		Preferences.systemRoot().remove("myName");
	}

	public void testPreferencesPlaceholderConfigurerWithCustomTreePaths() {
		StaticApplicationContext ac = new StaticApplicationContext();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("name", "${myName}");
		pvs.addPropertyValue("age", "${myAge}");
		pvs.addPropertyValue("touchy", "${myTouchy}");
		ac.registerSingleton("tb", TestBean.class, pvs);
		pvs = new MutablePropertyValues();
		Properties props = new Properties();
		props.put("myAge", "99");
		pvs.addPropertyValue("properties", props);
		pvs.addPropertyValue("systemTreePath", "mySystemPath");
		pvs.addPropertyValue("userTreePath", "myUserPath");
		ac.registerSingleton("configurer", PreferencesPlaceholderConfigurer.class, pvs);
		Preferences.systemRoot().node("mySystemPath").put("myName", "myNameValue");
		Preferences.systemRoot().node("mySystemPath").put("myTouchy", "myTouchyValue");
		Preferences.userRoot().node("myUserPath").put("myTouchy", "myOtherTouchyValue");
		ac.refresh();
		TestBean tb = (TestBean) ac.getBean("tb");
		assertEquals("myNameValue", tb.getName());
		assertEquals(99, tb.getAge());
		assertEquals("myOtherTouchyValue", tb.getTouchy());
		Preferences.userRoot().node("myUserPath").remove("myTouchy");
		Preferences.systemRoot().node("mySystemPath").remove("myTouchy");
		Preferences.systemRoot().node("mySystemPath").remove("myName");
	}

}
