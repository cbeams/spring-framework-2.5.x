

package org.springframework.beans.factory;

import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.springframework.beans.BeansException;
import org.springframework.beans.ITestBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.DependenciesBean;

/**
 * This largely tests Properties population:
 * ListableBeanFactoryTestSuite tests basic functionality
 * @author Rod Johnson
 * @version $RevisionId$
 */
public class DefaultListableBeanFactoryTestSuite extends TestCase {

	public void testUnreferencedSingletonWasInstantiated() {
		KnowsIfInstantiated.clearInstantiationRecord();
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("x1.class", KnowsIfInstantiated.class.getName());
		assertTrue("singleton not instantiated", !KnowsIfInstantiated.wasInstantiated());
		lbf.registerBeanDefinitions(p);
		lbf.preInstantiateSingletons();
		assertTrue("singleton was instantiated", KnowsIfInstantiated.wasInstantiated());
	}

	public void testLazyInitialization() {
		KnowsIfInstantiated.clearInstantiationRecord();
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("x1.class", KnowsIfInstantiated.class.getName());
		p.setProperty("x1.(lazy-init)", "true");
		assertTrue("singleton not instantiated", !KnowsIfInstantiated.wasInstantiated());
		lbf.registerBeanDefinitions(p);
		assertTrue("singleton not instantiated", !KnowsIfInstantiated.wasInstantiated());
		lbf.preInstantiateSingletons();
		assertTrue("singleton not instantiated", !KnowsIfInstantiated.wasInstantiated());
		lbf.getBean("x1");
		assertTrue("singleton was instantiated", KnowsIfInstantiated.wasInstantiated());
	}

	public void testFactoryBeanDidNotCreatePrototype() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("x1.class", DummyFactory.class.getName());
		// Reset static state
		DummyFactory.reset();
		p.setProperty("x1.singleton", "false");
		assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
		lbf.registerBeanDefinitions(p);
		assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
		lbf.preInstantiateSingletons();
		assertTrue("prototype not instantiated", !DummyFactory.wasPrototypeCreated());
		lbf.getBean("x1");
		assertTrue("prototype was instantiated", DummyFactory.wasPrototypeCreated());
	}

	public void testEmpty() {
		ListableBeanFactory lbf = new DefaultListableBeanFactory();
		assertTrue("No beans defined --> array != null", lbf.getBeanDefinitionNames() != null);
		assertTrue("No beans defined after no arg constructor", lbf.getBeanDefinitionNames().length == 0);
		assertTrue("No beans defined after no arg constructor", lbf.getBeanDefinitionCount() == 0);
	}

	public void testEmptyPropertiesPopulation() throws BeansException {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		lbf.registerBeanDefinitions(p);
		assertTrue("No beans defined after ignorable invalid", lbf.getBeanDefinitionCount() == 0);
	}

	public void testHarmlessIgnorableRubbish() throws BeansException {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("foo", "bar");
		p.setProperty("qwert", "er");
		lbf.registerBeanDefinitions(p, "test");
		assertTrue("No beans defined after harmless ignorable rubbish", lbf.getBeanDefinitionCount() == 0);
	}

	public void testPropertiesPopulationWithNullPrefix() throws Exception {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("test.class", "org.springframework.beans.TestBean");
		p.setProperty("test.name", "Tony");
		p.setProperty("test.age", "48");
		//p.setProperty("
		int count = lbf.registerBeanDefinitions(p);
		assertTrue("1 beans registered, not " + count, count == 1);
		testSingleTestBean(lbf);
	}

	public void testPropertiesPopulationWithPrefix() throws Exception {
		String PREFIX = "beans.";
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty(PREFIX + "test.class", "org.springframework.beans.TestBean");
		p.setProperty(PREFIX + "test.name", "Tony");
		p.setProperty(PREFIX + "test.age", "48");
		//p.setProperty("
		int count = lbf.registerBeanDefinitions(p, PREFIX);
		assertTrue("1 beans registered, not " + count, count == 1);
		testSingleTestBean(lbf);
	}

	public void testSimpleReference() throws Exception {
		String PREFIX = "beans.";
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();

		p.setProperty(PREFIX + "rod.class", "org.springframework.beans.TestBean");
		p.setProperty(PREFIX + "rod.name", "Rod");

		p.setProperty(PREFIX + "kerry.class", "org.springframework.beans.TestBean");
		p.setProperty(PREFIX + "kerry.class", "org.springframework.beans.TestBean");
		p.setProperty(PREFIX + "kerry.name", "Kerry");
		p.setProperty(PREFIX + "kerry.age", "35");
		p.setProperty(PREFIX + "kerry.spouse(ref)", "rod");
		//p.setProperty("
		int count = lbf.registerBeanDefinitions(p, PREFIX);
		assertTrue("2 beans registered, not " + count, count == 2);

		TestBean kerry = (TestBean) lbf.getBean("kerry", TestBean.class);
		assertTrue("Kerry name is Kerry", "Kerry".equals(kerry.getName()));
		ITestBean spouse = kerry.getSpouse();
		assertTrue("Kerry spouse is non null", spouse != null);
		assertTrue("Kerry spouse name is Rod", "Rod".equals(spouse.getName()));
	}

	public void testUnresolvedReference() throws Exception {
		String PREFIX = "beans.";
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();

		//p.setProperty(PREFIX + "rod.class", "org.springframework.beans.TestBean");
		//p.setProperty(PREFIX + "rod.name", "Rod");

		try {
			p.setProperty(PREFIX + "kerry.class", "org.springframework.beans.TestBean");
			p.setProperty(PREFIX + "kerry.class", "org.springframework.beans.TestBean");
			p.setProperty(PREFIX + "kerry.name", "Kerry");
			p.setProperty(PREFIX + "kerry.age", "35");
			p.setProperty(PREFIX + "kerry.spouse(ref)", "rod");

			lbf.registerBeanDefinitions(p, PREFIX);

			Object kerry = lbf.getBean("kerry");
			fail ("Unresolved reference should have been detected");
		}
		catch (BeansException ex) {
			// cool
		}
	}

	public void testPrototype() throws Exception {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("kerry.class", "org.springframework.beans.TestBean");
		p.setProperty("kerry.age", "35");
		lbf.registerBeanDefinitions(p);
		TestBean kerry1 = (TestBean) lbf.getBean("kerry");
		TestBean kerry2 = (TestBean) lbf.getBean("kerry");
		assertTrue("Non null", kerry1 != null);
		assertTrue("Singletons equal", kerry1 == kerry2);

		lbf = new DefaultListableBeanFactory();
		p = new Properties();
		p.setProperty("kerry.class", "org.springframework.beans.TestBean");
		p.setProperty("kerry.(singleton)", "false");
		p.setProperty("kerry.age", "35");
		lbf.registerBeanDefinitions(p);
		kerry1 = (TestBean) lbf.getBean("kerry");
		kerry2 = (TestBean) lbf.getBean("kerry");
		assertTrue("Non null", kerry1 != null);
		assertTrue("Prototypes NOT equal", kerry1 != kerry2);

		lbf = new DefaultListableBeanFactory();
		p = new Properties();
		p.setProperty("kerry.class", "org.springframework.beans.TestBean");
		p.setProperty("kerry.(singleton)", "true");
		p.setProperty("kerry.age", "35");
		lbf.registerBeanDefinitions(p);
		kerry1 = (TestBean) lbf.getBean("kerry");
		kerry2 = (TestBean) lbf.getBean("kerry");
		assertTrue("Non null", kerry1 != null);
		assertTrue("Specified singletons equal", kerry1 == kerry2);
	}

	public void testPrototypeExtendsPrototype() throws Exception {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("wife.class", "org.springframework.beans.TestBean");
		p.setProperty("wife.name", "kerry");

		p.setProperty("kerry.parent", "wife");
		p.setProperty("kerry.age", "35");
		lbf.registerBeanDefinitions(p);
		TestBean kerry1 = (TestBean) lbf.getBean("kerry");
		TestBean kerry2 = (TestBean) lbf.getBean("kerry");
		assertEquals("kerry", kerry1.getName());
		assertTrue("Non null", kerry1 != null);
		assertTrue("Singletons equal", kerry1 == kerry2);

		lbf = new DefaultListableBeanFactory();
		p = new Properties();
		p.setProperty("wife.class", "org.springframework.beans.TestBean");
		p.setProperty("wife.name", "kerry");
		p.setProperty("wife.(singleton)", "false");
		p.setProperty("kerry.parent", "wife");
		p.setProperty("kerry.(singleton)", "false");
		p.setProperty("kerry.age", "35");
		lbf.registerBeanDefinitions(p);
		assertFalse(lbf.isSingleton("kerry"));
		kerry1 = (TestBean) lbf.getBean("kerry");
		kerry2 = (TestBean) lbf.getBean("kerry");
		assertTrue("Non null", kerry1 != null);
		assertTrue("Prototypes NOT equal", kerry1 != kerry2);

		lbf = new DefaultListableBeanFactory();
		p = new Properties();
		p.setProperty("kerry.class", "org.springframework.beans.TestBean");
		p.setProperty("kerry.(singleton)", "true");
		p.setProperty("kerry.age", "35");
		lbf.registerBeanDefinitions(p);
		kerry1 = (TestBean) lbf.getBean("kerry");
		kerry2 = (TestBean) lbf.getBean("kerry");
		assertTrue("Non null", kerry1 != null);
		assertTrue("Specified singletons equal", kerry1 == kerry2);
	}

	/*
	public void testInvalidBeanDefinition() throws Exception {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("test.class", "org.springframework.beans.TestBean");
		p.setProperty("test.name", "Tony");
		p.setProperty("test.age", "48");
		//p.setProperty("
		int count = lbf.registerBeanDefinitions(p);
		assertTrue("1 beans registered", count == 1);
		testSingleTestBean(lbf);
	}
	*/

	public void testNameAlreadyBound() throws Exception {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("kerry.class", "org.springframework.beans.TestBean");
		p.setProperty("kerry.age", "35");
		lbf.registerBeanDefinitions(p);
		try {
			lbf.registerBeanDefinitions(p);
		}
		catch (BeanDefinitionStoreException ex) {
			// expected
		}
	}

	private void testSingleTestBean(ListableBeanFactory lbf) throws BeansException {
		assertTrue("1 beans defined", lbf.getBeanDefinitionCount() == 1);
		String[] names = lbf.getBeanDefinitionNames();
		assertTrue("Array length == 1", names.length == 1);
		assertTrue("0th element == test", names[0].equals("test"));
		TestBean tb = (TestBean) lbf.getBean("test");
		assertTrue("Test is non null", tb != null);
		assertTrue("Test bean name is Tony", "Tony".equals(tb.getName()));
		assertTrue("Test bean age is 48", tb.getAge() == 48);
	}

	public void testBeanReferenceWithNewSyntax() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("r.class", TestBean.class.getName());
		p.setProperty("r.name", "rod");
		p.setProperty("k.class", TestBean.class.getName());
		p.setProperty("k.name", "kerry");
		p.setProperty("k.spouse", "*r");
		lbf.registerBeanDefinitions(p);
		TestBean k = (TestBean) lbf.getBean("k");
		TestBean r = (TestBean) lbf.getBean("r");
		assertTrue(k.getSpouse() == r);
	}

	public void testCanEscapeBeanReferenceSyntax() {
		String name = "*name";
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("r.class", TestBean.class.getName());
		p.setProperty("r.name", "*" + name);
		lbf.registerBeanDefinitions(p);
		TestBean r = (TestBean) lbf.getBean("r");
		assertTrue(r.getName().equals(name));
	}

	public void testRegisterExistingSingletonWithReference() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		Properties p = new Properties();
		p.setProperty("test.class", "org.springframework.beans.TestBean");
		p.setProperty("test.name", "Tony");
		p.setProperty("test.age", "48");
		p.setProperty("test.spouse(ref)", "singletonObject");
		lbf.registerBeanDefinitions(p);
		Object singletonObject = new TestBean();
		lbf.registerSingleton("singletonObject", singletonObject);
		assertTrue(lbf.isSingleton("singletonObject"));
		TestBean test = (TestBean) lbf.getBean("test");
		assertEquals(singletonObject, lbf.getBean("singletonObject"));
		assertEquals(singletonObject, test.getSpouse());
		Map beansOfType = lbf.getBeansOfType(TestBean.class, false, true);
		assertEquals(2, beansOfType.size());
		assertTrue(beansOfType.containsValue(test));
		assertTrue(beansOfType.containsValue(singletonObject));
	}

	public void testRegisterExistingSingletonWithAutowire() {
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("name", "Tony");
		pvs.addPropertyValue("age", "48");
		RootBeanDefinition bd = new RootBeanDefinition(DependenciesBean.class, pvs, true,
		                                               RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS,
		                                               RootBeanDefinition.AUTOWIRE_BY_TYPE);
		lbf.registerBeanDefinition("test", bd);
		Object singletonObject = new TestBean();
		lbf.registerSingleton("singletonObject", singletonObject);
		assertTrue(lbf.containsBean("singletonObject"));
		assertTrue(lbf.isSingleton("singletonObject"));
		assertEquals(2, lbf.getBeanDefinitionNames().length);
		assertEquals("singletonObject", lbf.getBeanDefinitionNames()[0]);
		assertEquals(1, lbf.getBeanDefinitionNames(ITestBean.class).length);
		assertEquals("singletonObject", lbf.getBeanDefinitionNames(ITestBean.class)[0]);
		assertEquals(1, lbf.getBeanDefinitionNames(TestBean.class).length);
		assertEquals("singletonObject", lbf.getBeanDefinitionNames(TestBean.class)[0]);
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
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (BeanDefinitionStoreException ex) {
			// expected
		}
	}

}
