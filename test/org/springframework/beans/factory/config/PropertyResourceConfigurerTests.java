package org.springframework.beans.factory.config;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

import junit.framework.TestCase;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.RootBeanDefinition;
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
		pvs.addPropertyValue("name", "name${var}${");
		pvs.addPropertyValue("touchy", "${os.name}");
		pvs.addPropertyValue("spouse", new RuntimeBeanReference("${ref}"));
		ac.registerSingleton("tb1", TestBean.class, pvs);
		ConstructorArgumentValues cas = new ConstructorArgumentValues();
		cas.addIndexedArgumentValue(1, "${age}");
		cas.addGenericArgumentValue("${var}name${age}");
		List friends = new ManagedList();
		friends.add("na${age}me");
		friends.add(new RuntimeBeanReference("${ref}"));
		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("friends", friends);
		Map someMap = new ManagedMap();
		someMap.put("key1", new RuntimeBeanReference("${ref}"));
		someMap.put("key2", "${age}name");
		pvs.addPropertyValue("someMap", someMap);
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, cas, pvs);
		ac.getDefaultListableBeanFactory().registerBeanDefinition("tb2", bd);
		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("properties", "age=98\nvar=${m}var\nref=tb2\nm=my");
		ac.registerSingleton("configurer", PropertyPlaceholderConfigurer.class, pvs);
		ac.refresh();
		TestBean tb1 = (TestBean) ac.getBean("tb1");
		TestBean tb2 = (TestBean) ac.getBean("tb2");
		assertEquals(98, tb1.getAge());
		assertEquals(98, tb2.getAge());
		assertEquals("namemyvar${", tb1.getName());
		assertEquals("myvarname98", tb2.getName());
		assertEquals(tb2, tb1.getSpouse());
		assertEquals(2, tb2.getFriends().size());
		assertEquals("na98me", tb2.getFriends().iterator().next());
		assertEquals(tb2, ((List) tb2.getFriends()).get(1));
		assertEquals(2, tb2.getSomeMap().size());
		assertEquals(tb2, tb2.getSomeMap().get("key1"));
		assertEquals("98name", tb2.getSomeMap().get("key2"));
		assertEquals(System.getProperty("os.name"), tb1.getTouchy());
	}

	public void testPropertyPlaceholderConfigurerWithSystemPropertyOnly() {
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

	public void testPropertyPlaceholderConfigurerWithUnresolvableSystemProperty() {
		StaticApplicationContext ac = new StaticApplicationContext();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("touchy", "${user.dir}");
		ac.registerSingleton("tb", TestBean.class, pvs);
		pvs = new MutablePropertyValues();
		pvs.addPropertyValue("checkSystemProperties", "false");
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
		pvs.addPropertyValue("spouse", new RuntimeBeanReference("${ref}"));
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
			assertTrue(ex.getMessage().indexOf(System.getProperty("user.dir")) != -1);
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
