/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.beans.factory;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.springframework.aop.interceptor.SideEffectBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.ITestBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.NestedTestBean;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ChildBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ConstructorDependenciesBean;
import org.springframework.beans.factory.xml.DependenciesBean;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.test.AssertThrows;

/**
 * Tests properties population and autowire behavior.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rick Evans
 */
public class DefaultListableBeanFactoryTests extends TestCase {

	public void testUnreferencedSingletonWasInstantiated() {
		KnowsIfInstantiated.clearInstantiationRecord();
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("x1.(class)", KnowsIfInstantiated.class.getName());
		assertTrue("singleton not instantiated", !KnowsIfInstantiated.wasInstantiated());
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		lbf.preInstantiateSingletons();
		assertTrue("singleton was instantiated", KnowsIfInstantiated.wasInstantiated());
	}

	public void testLazyInitialization() {
		KnowsIfInstantiated.clearInstantiationRecord();
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("x1.(class)", KnowsIfInstantiated.class.getName());
		p.setProperty("x1.(lazy-init)", "true");
		assertTrue("singleton not instantiated", !KnowsIfInstantiated.wasInstantiated());
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		assertTrue("singleton not instantiated", !KnowsIfInstantiated.wasInstantiated());
		lbf.preInstantiateSingletons();

		assertTrue("singleton not instantiated", !KnowsIfInstantiated.wasInstantiated());
		lbf.getBean("x1");
		assertTrue("singleton was instantiated", KnowsIfInstantiated.wasInstantiated());
	}

	public void testFactoryBeanDidNotCreatePrototype() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("x1.(class)", DummyFactory.class.getName());
		// Reset static state
		DummyFactory.reset();
		p.setProperty("x1.singleton", "false");
		assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
		assertEquals(TestBean.class, lbf.getType("x1"));
		lbf.preInstantiateSingletons();

		assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
		lbf.getBean("x1");
		assertEquals(TestBean.class, lbf.getType("x1"));
		assertTrue("prototype was instantiated", DummyFactory.wasPrototypeCreated());
	}

	public void testEmpty() {
		ListableBeanFactory lbf = new DefaultListableBeanFactory();
		assertTrue("No beans defined --> array != null", lbf.getBeanDefinitionNames() != null);
		assertTrue("No beans defined after no arg constructor", lbf.getBeanDefinitionNames().length == 0);
		assertTrue("No beans defined after no arg constructor", lbf.getBeanDefinitionCount() == 0);
	}

	public void testEmptyPropertiesPopulation() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		assertTrue("No beans defined after ignorable invalid", lbf.getBeanDefinitionCount() == 0);
	}

	public void testHarmlessIgnorableRubbish() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("foo", "bar");
		p.setProperty("qwert", "er");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p, "test");
		assertTrue("No beans defined after harmless ignorable rubbish", lbf.getBeanDefinitionCount() == 0);
	}

	public void testPropertiesPopulationWithNullPrefix() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("test.(class)", "org.springframework.beans.TestBean");
		p.setProperty("test.name", "Tony");
		p.setProperty("test.age", "48");
		//p.setProperty("
		int count = (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		assertTrue("1 beans registered, not " + count, count == 1);
		testSingleTestBean(lbf);
	}

	public void testPropertiesPopulationWithPrefix() {
		String PREFIX = "beans.";
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty(PREFIX + "test.(class)", "org.springframework.beans.TestBean");
		p.setProperty(PREFIX + "test.name", "Tony");
		p.setProperty(PREFIX + "test.age", "48");
		//p.setProperty("
		int count = (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p, PREFIX);
		assertTrue("1 beans registered, not " + count, count == 1);
		testSingleTestBean(lbf);
	}

	public void testSimpleReference() {
		String PREFIX = "beans.";
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();

		p.setProperty(PREFIX + "rod.(class)", "org.springframework.beans.TestBean");
		p.setProperty(PREFIX + "rod.name", "Rod");

		p.setProperty(PREFIX + "kerry.(class)", "org.springframework.beans.TestBean");
		p.setProperty(PREFIX + "kerry.name", "Kerry");
		p.setProperty(PREFIX + "kerry.age", "35");
		p.setProperty(PREFIX + "kerry.spouse(ref)", "rod");

		int count = (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p, PREFIX);
		assertTrue("2 beans registered, not " + count, count == 2);

		TestBean kerry = (TestBean) lbf.getBean("kerry", TestBean.class);
		assertTrue("Kerry name is Kerry", "Kerry".equals(kerry.getName()));
		ITestBean spouse = kerry.getSpouse();
		assertTrue("Kerry spouse is non null", spouse != null);
		assertTrue("Kerry spouse name is Rod", "Rod".equals(spouse.getName()));
	}

	public void testPropertiesWithDotsInKey() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();

		p.setProperty("tb.(class)", "org.springframework.beans.TestBean");
		p.setProperty("tb.someMap[my.key]", "my.value");

		int count = (new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		assertTrue("1 beans registered, not " + count, count == 1);
		assertEquals(1, lbf.getBeanDefinitionCount());

		TestBean tb = (TestBean) lbf.getBean("tb", TestBean.class);
		assertEquals("my.value", tb.getSomeMap().get("my.key"));
	}

	public void testUnresolvedReference() {
		String PREFIX = "beans.";
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();

		try {
			p.setProperty(PREFIX + "kerry.(class)", "org.springframework.beans.TestBean");
			p.setProperty(PREFIX + "kerry.name", "Kerry");
			p.setProperty(PREFIX + "kerry.age", "35");
			p.setProperty(PREFIX + "kerry.spouse(ref)", "rod");

			(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p, PREFIX);

			lbf.getBean("kerry");
			fail("Unresolved reference should have been detected");
		}
		catch (BeansException ex) {
			// cool
		}
	}

	public void testSelfReference() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("spouse", new RuntimeBeanReference("self"));
		lbf.registerBeanDefinition("self", new RootBeanDefinition(TestBean.class, pvs));
		TestBean self = (TestBean) lbf.getBean("self");
		assertEquals(self, self.getSpouse());
	}

	public void testPrototype() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("kerry.(class)", "org.springframework.beans.TestBean");
		p.setProperty("kerry.age", "35");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		TestBean kerry1 = (TestBean) lbf.getBean("kerry");
		TestBean kerry2 = (TestBean) lbf.getBean("kerry");
		assertTrue("Non null", kerry1 != null);
		assertTrue("Singletons equal", kerry1 == kerry2);

		lbf = new DefaultListableBeanFactory();
		p = new Properties();
		p.setProperty("kerry.(class)", "org.springframework.beans.TestBean");
		p.setProperty("kerry.(singleton)", "false");
		p.setProperty("kerry.age", "35");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		kerry1 = (TestBean) lbf.getBean("kerry");
		kerry2 = (TestBean) lbf.getBean("kerry");
		assertTrue("Non null", kerry1 != null);
		assertTrue("Prototypes NOT equal", kerry1 != kerry2);

		lbf = new DefaultListableBeanFactory();
		p = new Properties();
		p.setProperty("kerry.(class)", "org.springframework.beans.TestBean");
		p.setProperty("kerry.(singleton)", "true");
		p.setProperty("kerry.age", "35");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		kerry1 = (TestBean) lbf.getBean("kerry");
		kerry2 = (TestBean) lbf.getBean("kerry");
		assertTrue("Non null", kerry1 != null);
		assertTrue("Specified singletons equal", kerry1 == kerry2);
	}

	public void testPrototypeExtendsPrototype() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("wife.(class)", "org.springframework.beans.TestBean");
		p.setProperty("wife.name", "kerry");

		p.setProperty("kerry.(parent)", "wife");
		p.setProperty("kerry.age", "35");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		TestBean kerry1 = (TestBean) lbf.getBean("kerry");
		TestBean kerry2 = (TestBean) lbf.getBean("kerry");
		assertEquals("kerry", kerry1.getName());
		assertNotNull("Non null", kerry1);
		assertTrue("Singletons equal", kerry1 == kerry2);

		lbf = new DefaultListableBeanFactory();
		p = new Properties();
		p.setProperty("wife.(class)", "org.springframework.beans.TestBean");
		p.setProperty("wife.name", "kerry");
		p.setProperty("wife.(singleton)", "false");
		p.setProperty("kerry.(parent)", "wife");
		p.setProperty("kerry.(singleton)", "false");
		p.setProperty("kerry.age", "35");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		assertFalse(lbf.isSingleton("kerry"));
		kerry1 = (TestBean) lbf.getBean("kerry");
		kerry2 = (TestBean) lbf.getBean("kerry");
		assertTrue("Non null", kerry1 != null);
		assertTrue("Prototypes NOT equal", kerry1 != kerry2);

		lbf = new DefaultListableBeanFactory();
		p = new Properties();
		p.setProperty("kerry.(class)", "org.springframework.beans.TestBean");
		p.setProperty("kerry.(singleton)", "true");
		p.setProperty("kerry.age", "35");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		kerry1 = (TestBean) lbf.getBean("kerry");
		kerry2 = (TestBean) lbf.getBean("kerry");
		assertTrue("Non null", kerry1 != null);
		assertTrue("Specified singletons equal", kerry1 == kerry2);
	}

	public void testCanReferenceParentBeanFromChildViaAlias() {
		final String PARENTS_ALIAS = "alias";
		final String EXPECTED_NAME = "Juergen";
		final int EXPECTED_AGE = 41;

		RootBeanDefinition parentDefinition = new RootBeanDefinition(TestBean.class);
		parentDefinition.setAbstract(true);
		parentDefinition.getPropertyValues().addPropertyValue("name", EXPECTED_NAME);
		parentDefinition.getPropertyValues().addPropertyValue("age", new Integer(EXPECTED_AGE));

		ChildBeanDefinition childDefinition = new ChildBeanDefinition(PARENTS_ALIAS);

		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		factory.registerBeanDefinition("parent", parentDefinition);
		factory.registerBeanDefinition("child", childDefinition);
		factory.registerAlias("parent", PARENTS_ALIAS);

		TestBean child = (TestBean) factory.getBean("child");
		assertEquals(EXPECTED_NAME, child.getName());
		assertEquals(EXPECTED_AGE, child.getAge());

		assertEquals("Use cached merged bean definition",
				factory.getMergedBeanDefinition("child"), factory.getMergedBeanDefinition("child"));
	}

	public void testNameAlreadyBound() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("kerry.(class)", "org.springframework.beans.TestBean");
		p.setProperty("kerry.age", "35");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		try {
			(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		}
		catch (BeanDefinitionStoreException ex) {
			assertEquals("kerry", ex.getBeanName());
			// expected
		}
	}

	private void testSingleTestBean(ListableBeanFactory lbf) {
		assertTrue("1 beans defined", lbf.getBeanDefinitionCount() == 1);
		String[] names = lbf.getBeanDefinitionNames();
		assertTrue("Array length == 1", names.length == 1);
		assertTrue("0th element == test", names[0].equals("test"));
		TestBean tb = (TestBean) lbf.getBean("test");
		assertTrue("Test is non null", tb != null);
		assertTrue("Test bean name is Tony", "Tony".equals(tb.getName()));
		assertTrue("Test bean age is 48", tb.getAge() == 48);
	}

	public void testBeanDefinitionOverriding() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class));
		lbf.registerBeanDefinition("test", new RootBeanDefinition(NestedTestBean.class));
		assertTrue(lbf.getBean("test") instanceof NestedTestBean);
	}

	public void testBeanDefinitionOverridingWithAlias() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class));
		lbf.registerAlias("test", "testAlias");
		lbf.registerBeanDefinition("test", new RootBeanDefinition(NestedTestBean.class));
		lbf.registerAlias("test", "testAlias");
		assertTrue(lbf.getBean("test") instanceof NestedTestBean);
	}

	public void testBeanDefinitionOverridingNotAllowed() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.setAllowBeanDefinitionOverriding(false);
		lbf.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class));
		try {
			lbf.registerBeanDefinition("test", new RootBeanDefinition(NestedTestBean.class));
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (BeanDefinitionStoreException ex) {
			assertEquals("test", ex.getBeanName());
			// expected
		}
	}

	public void testBeanReferenceWithNewSyntax() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("r.(class)", TestBean.class.getName());
		p.setProperty("r.name", "rod");
		p.setProperty("k.(class)", TestBean.class.getName());
		p.setProperty("k.name", "kerry");
		p.setProperty("k.spouse", "*r");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		TestBean k = (TestBean) lbf.getBean("k");
		TestBean r = (TestBean) lbf.getBean("r");
		assertTrue(k.getSpouse() == r);
	}

	public void testCanEscapeBeanReferenceSyntax() {
		String name = "*name";
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("r.(class)", TestBean.class.getName());
		p.setProperty("r.name", "*" + name);
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		TestBean r = (TestBean) lbf.getBean("r");
		assertTrue(r.getName().equals(name));
	}

	public void testCustomEditor() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		NumberFormat nf = NumberFormat.getInstance(Locale.UK);
		lbf.registerCustomEditor(Float.class, new CustomNumberEditor(Float.class, nf, true));
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("myFloat", "1.1");
		lbf.registerBeanDefinition("testBean", new RootBeanDefinition(TestBean.class, pvs));
		TestBean testBean = (TestBean) lbf.getBean("testBean");
		//System.out.println(testBean.getMyFloat().floatValue());
		assertTrue(testBean.getMyFloat().floatValue() == 1.1f);
	}

	public void testRegisterExistingSingletonWithReference() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("test.(class)", "org.springframework.beans.TestBean");
		p.setProperty("test.name", "Tony");
		p.setProperty("test.age", "48");
		p.setProperty("test.spouse(ref)", "singletonObject");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		Object singletonObject = new TestBean();
		lbf.registerSingleton("singletonObject", singletonObject);

		assertTrue(lbf.isSingleton("singletonObject"));
		assertEquals(TestBean.class, lbf.getType("singletonObject"));
		TestBean test = (TestBean) lbf.getBean("test");
		assertEquals(singletonObject, lbf.getBean("singletonObject"));
		assertEquals(singletonObject, test.getSpouse());

		Map beansOfType = lbf.getBeansOfType(TestBean.class, false, true);
		assertEquals(2, beansOfType.size());
		assertTrue(beansOfType.containsValue(test));
		assertTrue(beansOfType.containsValue(singletonObject));

		beansOfType = lbf.getBeansOfType(null, false, true);
		assertEquals(2, beansOfType.size());
	}

	public void testRegisterExistingSingletonWithNameOverriding() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("test.(class)", "org.springframework.beans.TestBean");
		p.setProperty("test.name", "Tony");
		p.setProperty("test.age", "48");
		p.setProperty("test.spouse(ref)", "singletonObject");
		p.setProperty("singletonObject.(class)", "org.springframework.beans.factory.config.PropertiesFactoryBean");
		(new PropertiesBeanDefinitionReader(lbf)).registerBeanDefinitions(p);
		Object singletonObject = new TestBean();
		lbf.registerSingleton("singletonObject", singletonObject);
		lbf.preInstantiateSingletons();

		assertTrue(lbf.isSingleton("singletonObject"));
		assertEquals(TestBean.class, lbf.getType("singletonObject"));
		TestBean test = (TestBean) lbf.getBean("test");
		assertEquals(singletonObject, lbf.getBean("singletonObject"));
		assertEquals(singletonObject, test.getSpouse());

		Map beansOfType = lbf.getBeansOfType(TestBean.class, false, true);
		assertEquals(2, beansOfType.size());
		assertTrue(beansOfType.containsValue(test));
		assertTrue(beansOfType.containsValue(singletonObject));

		beansOfType = lbf.getBeansOfType(null, false, true);
		assertEquals(2, beansOfType.size());
	}

	public void testRegisterExistingSingletonWithAutowire() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("name", "Tony");
		pvs.addPropertyValue("age", "48");
		RootBeanDefinition bd = new RootBeanDefinition(DependenciesBean.class, pvs, true);
		bd.setDependencyCheck(RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
		bd.setAutowireMode(RootBeanDefinition.AUTOWIRE_BY_TYPE);
		lbf.registerBeanDefinition("test", bd);
		Object singletonObject = new TestBean();
		lbf.registerSingleton("singletonObject", singletonObject);

		assertTrue(lbf.containsBean("singletonObject"));
		assertTrue(lbf.isSingleton("singletonObject"));
		assertEquals(TestBean.class, lbf.getType("singletonObject"));
		assertEquals(0, lbf.getAliases("singletonObject").length);
		DependenciesBean test = (DependenciesBean) lbf.getBean("test");
		assertEquals(singletonObject, lbf.getBean("singletonObject"));
		assertEquals(singletonObject, test.getSpouse());
	}

	public void testRegisterExistingSingletonWithAlreadyBound() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Object singletonObject = new TestBean();
		lbf.registerSingleton("singletonObject", singletonObject);
		try {
			lbf.registerSingleton("singletonObject", singletonObject);
			fail("Should have thrown IllegalStateException");
		}
		catch (IllegalStateException ex) {
			// expected
		}
	}

	public void testAutowireWithNoDependencies() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
		lbf.registerBeanDefinition("rod", bd);
		assertEquals(1, lbf.getBeanDefinitionCount());
		Object registered = lbf.autowire(NoDependencies.class, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, false);
		assertEquals(1, lbf.getBeanDefinitionCount());
		assertTrue(registered instanceof NoDependencies);
	}

	public void testAutowireWithSatisfiedJavaBeanDependency() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("name", "Rod");
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, pvs);
		lbf.registerBeanDefinition("rod", bd);
		assertEquals(1, lbf.getBeanDefinitionCount());
		// Depends on age, name and spouse (TestBean)
		Object registered = lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, true);
		assertEquals(1, lbf.getBeanDefinitionCount());
		DependenciesBean kerry = (DependenciesBean) registered;
		TestBean rod = (TestBean) lbf.getBean("rod");
		assertSame(rod, kerry.getSpouse());
	}

	public void testAutowireWithSatisfiedConstructorDependency() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("name", "Rod");
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, pvs);
		lbf.registerBeanDefinition("rod", bd);
		assertEquals(1, lbf.getBeanDefinitionCount());
		Object registered = lbf.autowire(ConstructorDependency.class, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, false);
		assertEquals(1, lbf.getBeanDefinitionCount());
		ConstructorDependency kerry = (ConstructorDependency) registered;
		TestBean rod = (TestBean) lbf.getBean("rod");
		assertSame(rod, kerry.spouse);
	}

	public void testAutowireWithTwoMatchesForConstructorDependency() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
		lbf.registerBeanDefinition("rod", bd);
		RootBeanDefinition bd2 = new RootBeanDefinition(TestBean.class);
		lbf.registerBeanDefinition("rod2", bd2);
		try {
			lbf.autowire(ConstructorDependency.class, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, false);
			fail("Should have thrown UnsatisfiedDependencyException");
		}
		catch (UnsatisfiedDependencyException ex) {
			// expected
			assertTrue(ex.getMessage().indexOf("rod") != -1);
			assertTrue(ex.getMessage().indexOf("rod2") != -1);
		}
	}

	public void testAutowireWithUnsatisfiedConstructorDependency() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "Rod"));
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, pvs);
		lbf.registerBeanDefinition("rod", bd);
		assertEquals(1, lbf.getBeanDefinitionCount());
		try {
			lbf.autowire(UnsatisfiedConstructorDependency.class, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, true);
			fail("Should have unsatisfied constructor dependency on SideEffectBean");
		}
		catch (UnsatisfiedDependencyException ex) {
			// Ok
		}
	}

	public void testAutowireConstructor() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
		lbf.registerBeanDefinition("spouse", bd);
		ConstructorDependenciesBean bean = (ConstructorDependenciesBean)
				lbf.autowire(ConstructorDependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, true);
		Object spouse = lbf.getBean("spouse");
		assertTrue(bean.getSpouse1() == spouse);
		assertTrue(BeanFactoryUtils.beanOfType(lbf, TestBean.class) == spouse);
	}

	public void testAutowireBeanByName() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
		lbf.registerBeanDefinition("spouse", bd);
		DependenciesBean bean = (DependenciesBean)
				lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
		TestBean spouse = (TestBean) lbf.getBean("spouse");
		assertEquals(spouse, bean.getSpouse());
		assertTrue(BeanFactoryUtils.beanOfType(lbf, TestBean.class) == spouse);
	}

	public void testAutowireBeanByNameWithDependencyCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
		lbf.registerBeanDefinition("spous", bd);
		try {
			lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
			fail("Should have thrown UnsatisfiedDependencyException");
		}
		catch (UnsatisfiedDependencyException ex) {
			// expected
		}
	}

	public void testAutowireBeanByNameWithNoDependencyCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
		lbf.registerBeanDefinition("spous", bd);
		DependenciesBean bean = (DependenciesBean)
				lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
		assertNull(bean.getSpouse());
	}

	public void testAutowireBeanByType() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
		lbf.registerBeanDefinition("test", bd);
		DependenciesBean bean = (DependenciesBean)
				lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
		TestBean test = (TestBean) lbf.getBean("test");
		assertEquals(test, bean.getSpouse());
	}

	public void testAutowireBeanByTypeWithTwoMatches() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
		RootBeanDefinition bd2 = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
		lbf.registerBeanDefinition("test", bd);
		lbf.registerBeanDefinition("test2", bd2);
		try {
			lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
			fail("Should have thrown UnsatisfiedDependencyException");
		}
		catch (UnsatisfiedDependencyException ex) {
			// expected
			assertTrue(ex.getMessage().indexOf("test") != -1);
			assertTrue(ex.getMessage().indexOf("test2") != -1);
		}
	}

	public void testAutowireBeanByTypeWithDependencyCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		try {
			lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
			fail("Should have thrown UnsatisfiedDependencyException");
		}
		catch (UnsatisfiedDependencyException ex) {
			// expected
		}
	}

	public void testAutowireBeanByTypeWithNoDependencyCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		DependenciesBean bean = (DependenciesBean)
				lbf.autowire(DependenciesBean.class, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		assertNull(bean.getSpouse());
	}

	public void testAutowireExistingBeanByName() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
		lbf.registerBeanDefinition("spouse", bd);
		DependenciesBean existingBean = new DependenciesBean();
		lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
		TestBean spouse = (TestBean) lbf.getBean("spouse");
		assertEquals(existingBean.getSpouse(), spouse);
		assertSame(spouse, BeanFactoryUtils.beanOfType(lbf, TestBean.class));
	}

	public void testAutowireExistingBeanByNameWithDependencyCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
		lbf.registerBeanDefinition("spous", bd);
		DependenciesBean existingBean = new DependenciesBean();
		try {
			lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, true);
			fail("Should have thrown UnsatisfiedDependencyException");
		}
		catch (UnsatisfiedDependencyException ex) {
			// expected
		}
	}

	public void testAutowireExistingBeanByNameWithNoDependencyCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
		lbf.registerBeanDefinition("spous", bd);
		DependenciesBean existingBean = new DependenciesBean();
		lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false);
		assertNull(existingBean.getSpouse());
	}

	public void testAutowireExistingBeanByType() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
		lbf.registerBeanDefinition("test", bd);
		DependenciesBean existingBean = new DependenciesBean();
		lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
		TestBean test = (TestBean) lbf.getBean("test");
		assertEquals(existingBean.getSpouse(), test);
	}

	public void testAutowireExistingBeanByTypeWithDependencyCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		DependenciesBean existingBean = new DependenciesBean();
		try {
			lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, true);
			fail("Should have thrown UnsatisfiedDependencyException");
		}
		catch (UnsatisfiedDependencyException expected) {
		}
	}

	public void testAutowireExistingBeanByTypeWithNoDependencyCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		DependenciesBean existingBean = new DependenciesBean();
		lbf.autowireBeanProperties(existingBean, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
		assertNull(existingBean.getSpouse());
	}

	public void testInvalidAutowireMode() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		try {
			lbf.autowireBeanProperties(new TestBean(), 0, false);
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException expected) {
		}
	}

	public void testApplyBeanPropertyValues() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("age", "99");
		lbf.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class, pvs));
		TestBean tb = new TestBean();
		assertEquals(0, tb.getAge());
		lbf.applyBeanPropertyValues(tb, "test");
		assertEquals(99, tb.getAge());
	}

	public void testApplyBeanPropertyValuesWithIncompleteDefinition() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("age", "99");
		lbf.registerBeanDefinition("test", new RootBeanDefinition(null, pvs));
		TestBean tb = new TestBean();
		assertEquals(0, tb.getAge());
		lbf.applyBeanPropertyValues(tb, "test");
		assertEquals(99, tb.getAge());
		assertNull(tb.getBeanFactory());
		assertNull(tb.getSpouse());
	}

	public void testConfigureBean() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("age", "99");
		lbf.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class, pvs));
		TestBean tb = new TestBean();
		assertEquals(0, tb.getAge());
		lbf.configureBean(tb, "test");
		assertEquals(99, tb.getAge());
		assertSame(lbf, tb.getBeanFactory());
		assertNull(tb.getSpouse());
	}

	public void testConfigureBeanWithAutowiring() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, new MutablePropertyValues());
		lbf.registerBeanDefinition("spouse", bd);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("age", "99");
		lbf.registerBeanDefinition("test", new RootBeanDefinition(TestBean.class, RootBeanDefinition.AUTOWIRE_BY_NAME));
		TestBean tb = new TestBean();
		lbf.configureBean(tb, "test");
		assertSame(lbf, tb.getBeanFactory());
		TestBean spouse = (TestBean) lbf.getBean("spouse");
		assertEquals(spouse, tb.getSpouse());
	}


	public void testExtensiveCircularReference() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		for (int i = 0; i < 1000; i++) {
			MutablePropertyValues pvs = new MutablePropertyValues();
			pvs.addPropertyValue(new PropertyValue("spouse", new RuntimeBeanReference("bean" + (i < 99 ? i + 1 : 0))));
			RootBeanDefinition bd = new RootBeanDefinition(TestBean.class, pvs);
			lbf.registerBeanDefinition("bean" + i, bd);
		}
		lbf.preInstantiateSingletons();
		for (int i = 0; i < 1000; i++) {
			TestBean bean = (TestBean) lbf.getBean("bean" + i);
			TestBean otherBean = (TestBean) lbf.getBean("bean" + (i < 99 ? i + 1 : 0));
			assertTrue(bean.getSpouse() == otherBean);
		}
	}

	public void testCircularReferenceThroughAutowiring() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(ConstructorDependencyBean.class, RootBeanDefinition.AUTOWIRE_CONSTRUCTOR));
		try {
			lbf.preInstantiateSingletons();
			fail("Should have thrown UnsatisfiedDependencyException");
		}
		catch (UnsatisfiedDependencyException expected) {
		}
	}

	public void testCircularReferenceThroughFactoryBeanAutowiring() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(ConstructorDependencyFactoryBean.class, RootBeanDefinition.AUTOWIRE_CONSTRUCTOR));
		try {
			lbf.preInstantiateSingletons();
			fail("Should have thrown UnsatisfiedDependencyException");
		}
		catch (UnsatisfiedDependencyException expected) {
		}
	}

	public void testCircularReferenceThroughFactoryBeanTypeCheck() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(ConstructorDependencyFactoryBean.class, RootBeanDefinition.AUTOWIRE_CONSTRUCTOR));
		try {
			lbf.getBeansOfType(String.class);
			fail("Should have thrown UnsatisfiedDependencyException");
		}
		catch (UnsatisfiedDependencyException expected) {
		}
	}

	public void testAvoidCircularReferenceThroughAutowiring() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(ConstructorDependencyFactoryBean.class, RootBeanDefinition.AUTOWIRE_CONSTRUCTOR));
		lbf.registerBeanDefinition("string",
				new RootBeanDefinition(String.class, RootBeanDefinition.AUTOWIRE_CONSTRUCTOR));
		lbf.preInstantiateSingletons();
	}

	public void testBeanDefinitionWithInterface() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(ITestBean.class));
		try {
			lbf.getBean("test");
			fail("Should have thrown BeanCreationException");
		}
		catch (BeanCreationException ex) {
			assertEquals("test", ex.getBeanName());
			assertTrue(ex.getMessage().toLowerCase().indexOf("interface") != -1);
		}
	}

	public void testBeanDefinitionWithAbstractClass() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(AbstractBeanFactory.class));
		try {
			lbf.getBean("test");
			fail("Should have thrown BeanCreationException");
		}
		catch (BeanCreationException ex) {
			assertEquals("test", ex.getBeanName());
			assertTrue(ex.getMessage().toLowerCase().indexOf("abstract") != -1);
		}
	}

	public void testPrototypeFactoryBeanNotEagerlyCalled() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test", new RootBeanDefinition(FactoryBeanThatShouldntBeCalled.class));
		lbf.preInstantiateSingletons();
	}

	public void testPrototypeFactoryBeanNotEagerlyCalledInCaseOfBeanClassName() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf.registerBeanDefinition("test",
				new RootBeanDefinition(FactoryBeanThatShouldntBeCalled.class.getName(), null, null));
		lbf.preInstantiateSingletons();
	}

	public void testBeanPostProcessorWithWrappedObjectAndDisposableBean() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(BeanWithDisposableBean.class);
		lbf.registerBeanDefinition("test", bd);
		lbf.addBeanPostProcessor(new BeanPostProcessor() {
			public Object postProcessBeforeInitialization(Object bean, String beanName) {
				return new TestBean();
			}
			public Object postProcessAfterInitialization(Object bean, String beanName) {
				return bean;
			}
		});
		BeanWithDisposableBean.closed = false;
		lbf.preInstantiateSingletons();
		lbf.destroySingletons();
		assertTrue("Destroy method invoked", BeanWithDisposableBean.closed);
	}

	public void testBeanPostProcessorWithWrappedObjectAndDestroyMethod() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(BeanWithDestroyMethod.class);
		bd.setDestroyMethodName("close");
		lbf.registerBeanDefinition("test", bd);
		lbf.addBeanPostProcessor(new BeanPostProcessor() {
			public Object postProcessBeforeInitialization(Object bean, String beanName) {
				return new TestBean();
			}

			public Object postProcessAfterInitialization(Object bean, String beanName) {
				return bean;
			}
		});
		BeanWithDestroyMethod.closed = false;
		lbf.preInstantiateSingletons();
		lbf.destroySingletons();
		assertTrue("Destroy method invoked", BeanWithDestroyMethod.closed);
	}

	public void testFindTypeOfSingletonFactoryMethodOnBeanInstance() {
		findTypeOfPrototypeFactoryMethodOnBeanInstance(true);
	}

	public void testFindTypeOfPrototypeFactoryMethodOnBeanInstance() {
		findTypeOfPrototypeFactoryMethodOnBeanInstance(false);
	}

	/**
	 * @param singleton whether the bean created from the factory method on
	 * the bean instance should be a singleton or prototype. This flag is
	 * used to allow checking of the new ability in 1.2.4 to determine the type
	 * of a prototype created from invoking a factory method on a bean instance
	 * in the factory.
	 */
	private void findTypeOfPrototypeFactoryMethodOnBeanInstance(boolean singleton) {
		String expectedNameFromProperties = "tony";
		String expectedNameFromArgs = "gordon";

		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition instanceFactoryDefinition = new RootBeanDefinition(BeanWithFactoryMethod.class, true);
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("name", expectedNameFromProperties);
		instanceFactoryDefinition.setPropertyValues(pvs);
		lbf.registerBeanDefinition("factoryBeanInstance", instanceFactoryDefinition);

		RootBeanDefinition factoryMethodDefinitionWithProperties = new RootBeanDefinition();
		factoryMethodDefinitionWithProperties.setFactoryBeanName("factoryBeanInstance");
		factoryMethodDefinitionWithProperties.setFactoryMethodName("create");
		factoryMethodDefinitionWithProperties.setSingleton(singleton);
		lbf.registerBeanDefinition("fmWithProperties", factoryMethodDefinitionWithProperties);

		RootBeanDefinition factoryMethodDefinitionGeneric = new RootBeanDefinition();
		factoryMethodDefinitionGeneric.setFactoryBeanName("factoryBeanInstance");
		factoryMethodDefinitionGeneric.setFactoryMethodName("createGeneric");
		factoryMethodDefinitionGeneric.setSingleton(singleton);
		lbf.registerBeanDefinition("fmGeneric", factoryMethodDefinitionGeneric);

		RootBeanDefinition factoryMethodDefinitionWithArgs = new RootBeanDefinition();
		factoryMethodDefinitionWithArgs.setFactoryBeanName("factoryBeanInstance");
		factoryMethodDefinitionWithArgs.setFactoryMethodName("createWithArgs");
		ConstructorArgumentValues cvals = new ConstructorArgumentValues();
		cvals.addGenericArgumentValue(expectedNameFromArgs);
		factoryMethodDefinitionWithArgs.setConstructorArgumentValues(cvals);
		factoryMethodDefinitionWithArgs.setSingleton(singleton);
		lbf.registerBeanDefinition("fmWithArgs", factoryMethodDefinitionWithArgs);

		assertEquals(4, lbf.getBeanDefinitionCount());
		List tbNames = Arrays.asList(lbf.getBeanNamesForType(TestBean.class));
		assertTrue(tbNames.contains("fmWithProperties"));
		assertTrue(tbNames.contains("fmWithArgs"));
		assertEquals(2, tbNames.size());

		TestBean tb = (TestBean) lbf.getBean("fmWithProperties");
		TestBean second = (TestBean) lbf.getBean("fmWithProperties");
		if (singleton) {
			assertSame(tb, second);
		}
		else {
			assertNotSame(tb, second);
		}
		assertEquals(expectedNameFromProperties, tb.getName());

		tb = (TestBean) lbf.getBean("fmGeneric");
		second = (TestBean) lbf.getBean("fmGeneric");
		if (singleton) {
			assertSame(tb, second);
		}
		else {
			assertNotSame(tb, second);
		}
		assertEquals(expectedNameFromProperties, tb.getName());

		TestBean tb2 = (TestBean) lbf.getBean("fmWithArgs");
		second = (TestBean) lbf.getBean("fmWithArgs");
		if (singleton) {
			assertSame(tb2, second);
		}
		else {
			assertNotSame(tb2, second);
		}
		assertEquals(expectedNameFromArgs, tb2.getName());
	}

	public void testFieldSettingWithInstantiationAwarePostProcessorNoShortCircuit() {
		testFieldSettingWithInstantiationAwarePostProcessor(false);
	}

	public void testFieldSettingWithInstantiationAwarePostProcessorWithShortCircuit() {
		testFieldSettingWithInstantiationAwarePostProcessor(true);
	}

	public void testScopingBeanToUnregisteredScopeResultsInAnException() throws Exception {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(TestBean.class);
		AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
		beanDefinition.setScope("he put himself so low could hardly look me in the face");

		final DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		factory.registerBeanDefinition("testBean", beanDefinition);
		new AssertThrows(IllegalStateException.class) {
			public void test() throws Exception {
				factory.getBean("testBean");
			}
		}.runTest();
	}

	public void testExplicitScopeInheritanceForChildBeanDefinitions() throws Exception {
		String theChildScope = "bonanza!";

		RootBeanDefinition parent = new RootBeanDefinition();
		parent.setSingleton(false);
		
		AbstractBeanDefinition child = BeanDefinitionBuilder
				.childBeanDefinition("parent").getBeanDefinition();
		child.setBeanClass(TestBean.class);
		child.setScope(theChildScope);
		
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		factory.registerBeanDefinition("parent", parent);
		factory.registerBeanDefinition("child", child);

		AbstractBeanDefinition def = (AbstractBeanDefinition) factory.getBeanDefinition("child");
		assertEquals("Child 'scope' not overriding parent scope (it must).", theChildScope, def.getScope());
	}	

	public void testImplicitScopeInheritanceForChildBeanDefinitions() throws Exception {
		RootBeanDefinition parent = new RootBeanDefinition();
		parent.setScope("bonanza!");
		
		AbstractBeanDefinition child = BeanDefinitionBuilder
				.childBeanDefinition("parent").getBeanDefinition();
		child.setBeanClass(TestBean.class);
		
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		factory.registerBeanDefinition("parent", parent);
		factory.registerBeanDefinition("child", child);

		AbstractBeanDefinition def = (AbstractBeanDefinition) factory.getBeanDefinition("child");
		assertTrue("Child 'scope' not overriding parent scope (it must).", def.isSingleton());
	}


	private void testFieldSettingWithInstantiationAwarePostProcessor(final boolean skipPropertyPopulation) {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		RootBeanDefinition bd = new RootBeanDefinition(TestBean.class);
		int ageSetByPropertyValue = 27;
		bd.getPropertyValues().addPropertyValue(new PropertyValue("age", new Integer(ageSetByPropertyValue)));
		lbf.registerBeanDefinition("test", bd);
		final String nameSetOnField = "nameSetOnField";
		lbf.addBeanPostProcessor(new InstantiationAwareBeanPostProcessorAdapter() {
			public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
				TestBean tb = (TestBean) bean;
				try {
					Field f = TestBean.class.getDeclaredField("name");
					f.setAccessible(true);
					f.set(tb, nameSetOnField);
					return !skipPropertyPopulation;
				}
				catch (Exception ex) {
					fail("Unexpected exception: " + ex);
					// Keep compiler happy about return
					throw new IllegalStateException();
				}
			}
		});
		lbf.preInstantiateSingletons();
		TestBean tb = (TestBean) lbf.getBean("test");
		assertEquals("Name was set on field by IAPP", nameSetOnField, tb.getName());
		if (!skipPropertyPopulation) {
			assertEquals("Property value still set", ageSetByPropertyValue, tb.getAge());
		}
		else {
			assertEquals("Property value was NOT set and still has default value", 0, tb.getAge());
		}
	}


	public static class NoDependencies {

		private NoDependencies() {
		}
	}


	public static class ConstructorDependency {

		public TestBean spouse;

		public ConstructorDependency(TestBean spouse) {
			this.spouse = spouse;
		}

		private ConstructorDependency(TestBean spouse, TestBean otherSpouse) {
			throw new IllegalArgumentException("Should never be called");
		}
	}


	public static class UnsatisfiedConstructorDependency {

		public UnsatisfiedConstructorDependency(TestBean t, SideEffectBean b) {
		}
	}


	public static class ConstructorDependencyBean {

		public ConstructorDependencyBean(ConstructorDependencyBean dependency) {
		}
	}


	public static class ConstructorDependencyFactoryBean implements FactoryBean {

		public ConstructorDependencyFactoryBean(String dependency) {
		}

		public Object getObject() {
			return "test";
		}

		public Class getObjectType() {
			return String.class;
		}

		public boolean isSingleton() {
			return true;
		}
	}


	public static class BeanWithDisposableBean implements DisposableBean {

		private static boolean closed;

		public void destroy() {
			closed = true;
		}
	}


	public static class BeanWithDestroyMethod {

		private static boolean closed;

		public void close() {
			closed = true;
		}
	}


	public static class BeanWithFactoryMethod {

		private String name;

		public void setName(String name) {
			this.name = name;
		}

		public TestBean create() {
			TestBean tb = new TestBean();
			tb.setName(name);
			return tb;
		}

		public TestBean createWithArgs(String arg) {
			TestBean tb = new TestBean();
			tb.setName(arg);
			return tb;
		}

		public Object createGeneric() {
			return create();
		}
	}


	public static class FactoryBeanThatShouldntBeCalled implements FactoryBean {

		public Object getObject() {
			throw new IllegalStateException();
		}

		public Class getObjectType() {
			return null;
		}

		public boolean isSingleton() {
			return false;
		}
	}

}
