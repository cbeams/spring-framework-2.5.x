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

package org.springframework.beans.factory.xml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.springframework.beans.DerivedTestBean;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.ITestBean;
import org.springframework.beans.IndexedTestBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.ResourceTestBean;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.DummyFactory;
import org.springframework.beans.factory.HasMap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.beans.factory.config.SetFactoryBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.MethodReplacer;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.SerializationTestUtils;
import org.springframework.util.StopWatch;

/**
 * @author Juergen Hoeller
 * @author Rod Johnson
 */
public class XmlBeanFactoryTestSuite extends TestCase {

	public void testDescriptionButNoProperties() throws Exception {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(false);
		reader.loadBeanDefinitions(new ClassPathResource("collections.xml", getClass()));
		TestBean validEmpty = (TestBean) xbf.getBean("validEmptyWithDescription");
		assertEquals(0, validEmpty.getAge());
	}

	public void testRefToSeparatePrototypeInstances() throws Exception {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(false);
		reader.loadBeanDefinitions(new ClassPathResource("reftypes.xml", getClass()));
		assertTrue("7 beans in reftypes, not " + xbf.getBeanDefinitionCount(), xbf.getBeanDefinitionCount() == 7);
		TestBean emma = (TestBean) xbf.getBean("emma");
		TestBean georgia = (TestBean) xbf.getBean("georgia");
		ITestBean emmasJenks = emma.getSpouse();
		ITestBean georgiasJenks = georgia.getSpouse();
		assertTrue("Emma and georgia think they have a different boyfriend",emmasJenks != georgiasJenks);
		assertTrue("Emmas jenks has right name", emmasJenks.getName().equals("Andrew"));
		assertTrue("Emmas doesn't equal new ref", emmasJenks != xbf.getBean("jenks"));
		assertTrue("Georgias jenks has right name", emmasJenks.getName().equals("Andrew"));
		assertTrue("They are object equal", emmasJenks.equals(georgiasJenks));
		assertTrue("They object equal direct ref", emmasJenks.equals(xbf.getBean("jenks")));
	}

	public void testRefToSingleton() throws Exception {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(false);
		reader.loadBeanDefinitions(new ClassPathResource("reftypes.xml", getClass()));
		assertTrue("7 beans in reftypes, not " + xbf.getBeanDefinitionCount(), xbf.getBeanDefinitionCount() == 7);
		TestBean jen = (TestBean) xbf.getBean("jenny");
		TestBean dave = (TestBean) xbf.getBean("david");
		TestBean jenks = (TestBean) xbf.getBean("jenks");
		ITestBean davesJen = dave.getSpouse();
		ITestBean jenksJen = jenks.getSpouse();
		assertTrue("1 jen instance", davesJen == jenksJen);
		assertTrue("1 jen instance", davesJen == jen);
	}

	public void testInnerBeans() {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(false);
		reader.loadBeanDefinitions(new ClassPathResource("reftypes.xml", getClass()));
		TestBean hasInnerBeans = (TestBean) xbf.getBean("hasInnerBeans");
		assertEquals(5, hasInnerBeans.getAge());
		assertNotNull(hasInnerBeans.getSpouse());
		assertEquals("inner1", hasInnerBeans.getSpouse().getName());
		assertEquals(6, hasInnerBeans.getSpouse().getAge());
		assertNotNull(hasInnerBeans.getFriends());
		List friends = (List) hasInnerBeans.getFriends();
		assertEquals(2, friends.size());
		DerivedTestBean inner2 = (DerivedTestBean) friends.get(0);
		assertEquals("inner2", inner2.getName());
		assertEquals("innerBean", inner2.getBeanName());
		assertFalse(xbf.containsBean("innerBean"));
		assertEquals(7, inner2.getAge());
		TestBean innerFactory = (TestBean) friends.get(1);
		assertEquals(DummyFactory.SINGLETON_NAME, innerFactory.getName());
		assertNotNull(hasInnerBeans.getSomeMap());
		assertFalse(hasInnerBeans.getSomeMap().isEmpty());
		TestBean inner3 = (TestBean) hasInnerBeans.getSomeMap().get("someKey");
		assertEquals("inner3", inner3.getName());
		assertEquals(8, inner3.getAge());
		xbf.destroySingletons();
		assertTrue(inner2.wasDestroyed());
		assertTrue(innerFactory.getName() == null);
	}

	public void testSingletonInheritanceFromParentFactorySingleton() throws Exception {
		XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
		XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
		TestBean inherits = (TestBean) child.getBean("inheritsFromParentFactory");
		// Name property value is overridden
		assertTrue(inherits.getName().equals("override"));
		// Age property is inherited from bean in parent factory
		assertTrue(inherits.getAge() == 1);
		TestBean inherits2 = (TestBean) child.getBean("inheritsFromParentFactory");
		assertTrue(inherits2 == inherits);
	}

	public void testInheritanceWithDifferentClass() throws Exception {
		XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
		XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
		DerivedTestBean inherits = (DerivedTestBean) child.getBean("inheritsWithDifferentClass");
		// Name property value is overridden
		assertTrue(inherits.getName().equals("override"));
		// Age property is inherited from bean in parent factory
		assertTrue(inherits.getAge() == 1);
		assertTrue(inherits.wasInitialized());
	}

	public void testInheritanceWithClass() throws Exception {
		XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
		XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
		DerivedTestBean inherits = (DerivedTestBean) child.getBean("inheritsWithClass");
		// Name property value is overridden
		assertTrue(inherits.getName().equals("override"));
		// Age property is inherited from bean in parent factory
		assertTrue(inherits.getAge() == 1);
		assertTrue(inherits.wasInitialized());
	}

	public void testPrototypeInheritanceFromParentFactoryPrototype() throws Exception {
		XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
		XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
		TestBean inherits = (TestBean) child.getBean("prototypeInheritsFromParentFactoryPrototype");
		// Name property value is overridden
		assertTrue(inherits.getName().equals("prototype-override"));
		// Age property is inherited from bean in parent factory
		assertTrue(inherits.getAge() == 2);
		TestBean inherits2 = (TestBean) child.getBean("prototypeInheritsFromParentFactoryPrototype");
		assertFalse(inherits2 == inherits);
		inherits2.setAge(13);
		assertTrue(inherits2.getAge() == 13);
		// Shouldn't have changed first instance
		assertTrue(inherits.getAge() == 2);
	}

	public void testPrototypeInheritanceFromParentFactorySingleton() throws Exception {
		XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
		XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
		TestBean inherits = (TestBean) child.getBean("protoypeInheritsFromParentFactorySingleton");
		// Name property value is overridden
		assertTrue(inherits.getName().equals("prototypeOverridesInheritedSingleton"));
		// Age property is inherited from bean in parent factory
		assertTrue(inherits.getAge() == 1);
		TestBean inherits2 = (TestBean) child.getBean("protoypeInheritsFromParentFactorySingleton");
		assertFalse(inherits2 == inherits);
		inherits2.setAge(13);
		assertTrue(inherits2.getAge() == 13);
		// Shouldn't have changed first instance
		assertTrue(inherits.getAge() == 1);
	}

	public void testAbstractParentBeans() {
		XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
		parent.preInstantiateSingletons();
		assertTrue(parent.isSingleton("inheritedTestBeanWithoutClass"));

		// abstract beans should not match
		Map tbs = parent.getBeansOfType(TestBean.class, true, true);
		assertEquals(1, tbs.size());
		assertEquals("inheritedTestBeanPrototype", tbs.keySet().iterator().next());

		// abstract bean should throw exception on creation attempt
		try {
			parent.getBean("inheritedTestBeanWithoutClass");
			fail("Should have thrown BeanIsAbstractException");
		}
		catch (BeanIsAbstractException ex) {
			// expected
		}

		// non-abstract bean should work, even if it serves as parent
		assertTrue(parent.getBean("inheritedTestBeanPrototype") instanceof TestBean);
	}

	public void testDependenciesMaterializeThis() throws Exception {
		InputStream pis = getClass().getResourceAsStream("dependenciesMaterializeThis.xml");
		XmlBeanFactory bf = new XmlBeanFactory(pis);
		DummyBoImpl bos = (DummyBoImpl) bf.getBean("boSingleton");
		DummyBoImpl bop = (DummyBoImpl) bf.getBean("boPrototype");
		assertNotSame(bos, bop);
		assertTrue(bos.dao == bop.dao);
	}

	public void testChildOverridesParentBean() throws Exception {
		XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
		XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
		TestBean inherits = (TestBean) child.getBean("inheritedTestBean");
		// Name property value is overridden
		assertTrue(inherits.getName().equals("overrideParentBean"));
		// Age property is inherited from bean in parent factory
		assertTrue(inherits.getAge() == 1);
		TestBean inherits2 = (TestBean) child.getBean("inheritedTestBean");
		assertTrue(inherits2 == inherits);
	}

	/**
	 * Check that a prototype can't inherit from a bogus parent.
	 * If a singleton does this the factory will fail to load.
	 */
	public void testBogusParentageFromParentFactory() throws Exception {
		XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
		XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
		try {
			TestBean inherits = (TestBean) child.getBean("bogusParent");
			fail();
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Ok
			// Check exception message contains the name
			assertTrue(ex.getMessage().indexOf("bogusParent") != -1);
		}
	}

	/**
	 * Note that prototype/singleton distinction is <b>not</b> inherited.
	 * It's possible for a subclass singleton not to return independent
	 * instances even if derived from a prototype
	 * @throws Exception
	 */
	public void testSingletonInheritsFromParentFactoryPrototype() throws Exception {
		XmlBeanFactory parent = new XmlBeanFactory(new ClassPathResource("parent.xml", getClass()));
		XmlBeanFactory child = new XmlBeanFactory(new ClassPathResource("child.xml", getClass()), parent);
		TestBean inherits = (TestBean) child.getBean("singletonInheritsFromParentFactoryPrototype");
		// Name property value is overriden
		assertTrue(inherits.getName().equals("prototype-override"));
		// Age property is inherited from bean in parent factory
		assertTrue(inherits.getAge() == 2);
		TestBean inherits2 = (TestBean) child.getBean("singletonInheritsFromParentFactoryPrototype");
		assertTrue(inherits2 == inherits);
	}

	public void testCircularReferences() {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(false);
		reader.loadBeanDefinitions(new ClassPathResource("reftypes.xml", getClass()));
		TestBean jenny = (TestBean) xbf.getBean("jenny");
		TestBean david = (TestBean) xbf.getBean("david");
		TestBean ego = (TestBean) xbf.getBean("ego");
		assertTrue("Correct circular reference", jenny.getSpouse() == david);
		assertTrue("Correct circular reference", david.getSpouse() == jenny);
		assertTrue("Correct circular reference", ego.getSpouse() == ego);
	}

	public void testFactoryReferenceCircle() {
		InputStream is = getClass().getResourceAsStream("factoryCircle.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		TestBean tb = (TestBean) xbf.getBean("singletonFactory");
		DummyFactory db = (DummyFactory) xbf.getBean("&singletonFactory");
		assertTrue(tb == db.getOtherTestBean());
	}

	public void testRefSubelement() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		//assertTrue("5 beans in reftypes, not " + xbf.getBeanDefinitionCount(), xbf.getBeanDefinitionCount() == 5);
		TestBean jen = (TestBean) xbf.getBean("jenny");
		TestBean dave = (TestBean) xbf.getBean("david");
		assertTrue(jen.getSpouse() == dave);
	}

	public void testPropertyWithLiteralValueSubelement() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		TestBean verbose = (TestBean) xbf.getBean("verbose");
		assertTrue(verbose.getName().equals("verbose"));
	}

	public void testPropertyWithIdRefLocalAttrSubelement() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		TestBean verbose = (TestBean) xbf.getBean("verbose2");
		assertTrue(verbose.getName().equals("verbose"));
	}

	public void testPropertyWithIdRefBeanAttrSubelement() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		TestBean verbose = (TestBean) xbf.getBean("verbose3");
		assertTrue(verbose.getName().equals("verbose"));
	}

	public void testRefSubelementsBuildCollection() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		//assertTrue("5 beans in reftypes, not " + xbf.getBeanDefinitionCount(), xbf.getBeanDefinitionCount() == 5);
		TestBean jen = (TestBean) xbf.getBean("jenny");
		TestBean dave = (TestBean) xbf.getBean("david");
		TestBean rod = (TestBean) xbf.getBean("rod");

		// Must be a list to support ordering
		// Our bean doesn't modify the collection:
		// of course it could be a different copy in a real object
		List friends = (List) rod.getFriends();
		assertTrue(friends.size() == 2);

		assertTrue("First friend must be jen, not " + friends.get(0),
			friends.get(0) == jen);
		assertTrue(friends.get(1) == dave);
		// Should be ordered
	}

	public void testRefSubelementsBuildCollectionWithPrototypes() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);

		TestBean jen = (TestBean) xbf.getBean("pJenny");
		TestBean dave = (TestBean) xbf.getBean("pDavid");
		TestBean rod = (TestBean) xbf.getBean("pRod");
		List friends = (List) rod.getFriends();
		assertTrue(friends.size() == 2);
		assertTrue("First friend must be jen, not " + friends.get(0),
			friends.get(0).toString().equals(jen.toString()));
		assertTrue("Jen not same instance", friends.get(0) != jen);
		assertTrue(friends.get(1).toString().equals(dave.toString()));
		assertTrue("Dave not same instance", friends.get(1) != dave);

		TestBean rod2 = (TestBean) xbf.getBean("pRod");
		List friends2 = (List) rod2.getFriends();
		assertTrue(friends2.size() == 2);
		assertTrue("First friend must be jen, not " + friends2.get(0),
			friends2.get(0).toString().equals(jen.toString()));
		assertTrue("Jen not same instance", friends2.get(0) != friends.get(0));
		assertTrue(friends2.get(1).toString().equals(dave.toString()));
		assertTrue("Dave not same instance", friends2.get(1) != friends.get(1));
	}

	public void testRefSubelementsBuildCollectionFromSingleElement() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		//assertTrue("5 beans in reftypes, not " + xbf.getBeanDefinitionCount(), xbf.getBeanDefinitionCount() == 5);
		TestBean loner = (TestBean) xbf.getBean("loner");
		TestBean dave = (TestBean) xbf.getBean("david");
		assertTrue(loner.getFriends().size() == 1);
		assertTrue(loner.getFriends().contains(dave));
	}

	public void testBuildCollectionFromMixtureOfReferencesAndValues() throws Exception {
		// Ensure that a test runner like Eclipse, that keeps the same JVM up,
		// will get fresh static values
		MixedCollectionBean.resetStaticState();
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		//assertTrue("5 beans in reftypes, not " + xbf.getBeanDefinitionCount(), xbf.getBeanDefinitionCount() == 5);
		MixedCollectionBean jumble = (MixedCollectionBean) xbf.getBean("jumble");
		assertEquals(1, MixedCollectionBean.nrOfInstances);
		assertTrue("Expected 3 elements, not " + jumble.getJumble().size(),
				jumble.getJumble().size() == 4);
		List l = (List) jumble.getJumble();
		assertTrue(l.get(0).equals(xbf.getBean("david")));
		assertTrue(l.get(1).equals("literal"));
		assertTrue(l.get(2).equals(xbf.getBean("jenny")));
		assertTrue(l.get(3).equals("rod"));
	}

	/**
	 * Test that properties with name as well as id creating an alias up front.
	 */
	public void testAutoAliasing() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		List beanNames = Arrays.asList(xbf.getBeanDefinitionNames());

		TestBean tb1 = (TestBean) xbf.getBean("aliased");
		TestBean alias1 = (TestBean) xbf.getBean("myalias");
		assertTrue(tb1 == alias1);
		List tb1Aliases = Arrays.asList(xbf.getAliases("aliased"));
		assertEquals(1, tb1Aliases.size());
		assertTrue(tb1Aliases.contains("myalias"));
		assertTrue(beanNames.contains("aliased"));
		assertFalse(beanNames.contains("myalias"));

		TestBean tb2 = (TestBean) xbf.getBean("multiAliased");
		TestBean alias2 = (TestBean) xbf.getBean("alias1");
		TestBean alias3 = (TestBean) xbf.getBean("alias2");
		assertTrue(tb2 == alias2);
		assertTrue(tb2 == alias3);
		List tb2Aliases = Arrays.asList(xbf.getAliases("multiAliased"));
		assertEquals(2, tb2Aliases.size());
		assertTrue(tb2Aliases.contains("alias1"));
		assertTrue(tb2Aliases.contains("alias2"));
		assertTrue(beanNames.contains("multiAliased"));
		assertFalse(beanNames.contains("alias1"));
		assertFalse(beanNames.contains("alias2"));

		TestBean tb3 = (TestBean) xbf.getBean("aliasWithoutId1");
		TestBean alias4 = (TestBean) xbf.getBean("aliasWithoutId2");
		TestBean alias5 = (TestBean) xbf.getBean("aliasWithoutId3");
		assertTrue(tb3 == alias4);
		assertTrue(tb3 == alias5);
		List tb3Aliases = Arrays.asList(xbf.getAliases("aliasWithoutId1"));
		assertEquals(2, tb2Aliases.size());
		assertTrue(tb3Aliases.contains("aliasWithoutId2"));
		assertTrue(tb3Aliases.contains("aliasWithoutId3"));
		assertTrue(beanNames.contains("aliasWithoutId1"));
		assertFalse(beanNames.contains("aliasWithoutId2"));
		assertFalse(beanNames.contains("aliasWithoutId3"));

		TestBean tb4 = (TestBean) xbf.getBean(TestBean.class.getName());
		assertEquals(null, tb4.getName());

		Map drs = xbf.getBeansOfType(DummyReferencer.class, false, false);
		assertEquals(3, drs.size());
		assertTrue(drs.containsKey(DummyReferencer.class.getName()));
		assertTrue(drs.containsKey(DummyReferencer.class.getName() + "#2"));
		assertTrue(drs.containsKey(DummyReferencer.class.getName() + "#3"));
	}

	public void testEmptyMap() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		HasMap hasMap = (HasMap) xbf.getBean("emptyMap");
		assertTrue(hasMap.getMap().size() == 0);
	}

	public void testMapWithLiteralsOnly() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		HasMap hasMap = (HasMap) xbf.getBean("literalMap");
		assertTrue(hasMap.getMap().size() == 3);
		assertTrue(hasMap.getMap().get("foo").equals("bar"));
		assertTrue(hasMap.getMap().get("fi").equals("fum"));
		assertTrue(hasMap.getMap().get("fa") == null);
	}

	public void testMapWithLiteralsAndReferences() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		HasMap hasMap = (HasMap) xbf.getBean("mixedMap");
		assertTrue(hasMap.getMap().size() == 3);
		assertTrue(hasMap.getMap().get("foo").equals("bar"));
		TestBean jenny = (TestBean) xbf.getBean("jenny");
		assertTrue(hasMap.getMap().get("jenny") == jenny);
		assertTrue(hasMap.getMap().get("david").equals("david"));
	}

	public void testMapWithLiteralsAndPrototypeReferences() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);

		TestBean jenny = (TestBean) xbf.getBean("pJenny");
		HasMap hasMap = (HasMap) xbf.getBean("pMixedMap");
		assertTrue(hasMap.getMap().size() == 2);
		assertTrue(hasMap.getMap().get("foo").equals("bar"));
		assertTrue(hasMap.getMap().get("jenny").toString().equals(jenny.toString()));
		assertTrue("Not same instance", hasMap.getMap().get("jenny") != jenny);

		HasMap hasMap2 = (HasMap) xbf.getBean("pMixedMap");
		assertTrue(hasMap2.getMap().size() == 2);
		assertTrue(hasMap2.getMap().get("foo").equals("bar"));
		assertTrue(hasMap2.getMap().get("jenny").toString().equals(jenny.toString()));
		assertTrue("Not same instance", hasMap2.getMap().get("jenny") != hasMap.getMap().get("jenny"));
	}

	public void testMapWithLiteralsReferencesAndList() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		HasMap hasMap = (HasMap) xbf.getBean("mixedMapWithList");
		assertTrue(hasMap.getMap().size() == 4);
		assertTrue(hasMap.getMap().get("foo").equals("bar"));
		TestBean jenny = (TestBean) xbf.getBean("jenny");
		assertTrue(hasMap.getMap().get("jenny").equals(jenny));

		// Check list
		List l = (List) hasMap.getMap().get("list");
		assertNotNull(l);
		assertTrue(l.size() == 4);
		assertTrue(l.get(0).equals("zero"));
		assertTrue(l.get(3) == null);

		// Check nested map in list
		Map m = (Map) l.get(1);
		assertNotNull(m);
		assertTrue(m.size() == 2);
		assertTrue(m.get("fo").equals("bar"));
		assertTrue("Map element 'jenny' should be equal to jenny bean, not " + m.get("jen"),
			m.get("jen").equals(jenny));

		// Check nested list in list
		l = (List) l.get(2);
		assertNotNull(l);
		assertTrue(l.size() == 2);
		assertTrue(l.get(0).equals(jenny));
		assertTrue(l.get(1).equals("ba"));

		// Check nested map
		m = (Map) hasMap.getMap().get("map");
		assertNotNull(m);
		assertTrue(m.size() == 2);
		assertTrue(m.get("foo").equals("bar"));
		assertTrue("Map element 'jenny' should be equal to jenny bean, not " + m.get("jenny"),
			m.get("jenny").equals(jenny));
	}

	public void testEmptySet() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		HasMap hasMap = (HasMap) xbf.getBean("emptySet");
		assertTrue(hasMap.getSet().size() == 0);
	}

	public void testPopulatedSet() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		HasMap hasMap = (HasMap) xbf.getBean("set");
		assertTrue(hasMap.getSet().size() == 3);
		assertTrue(hasMap.getSet().contains("bar"));
		TestBean jenny = (TestBean) xbf.getBean("jenny");
		assertTrue(hasMap.getSet().contains(jenny));
		assertTrue(hasMap.getSet().contains(null));
	}

	public void testEmptyProps() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		HasMap hasMap = (HasMap) xbf.getBean("emptyProps");
		assertTrue(hasMap.getProps().size() == 0);
	}

	public void testPopulatedProps() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		HasMap hasMap = (HasMap) xbf.getBean("props");
		assertTrue(hasMap.getProps().size() == 2);
		assertTrue(hasMap.getProps().get("foo").equals("bar"));
		assertTrue(hasMap.getProps().get("2").equals("TWO"));
	}

	public void testObjectArray() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		HasMap hasMap = (HasMap) xbf.getBean("objectArray");
		assertTrue(hasMap.getObjectArray().length ==2);
		assertTrue(hasMap.getObjectArray()[0].equals("one"));
		assertTrue(hasMap.getObjectArray()[1].equals(xbf.getBean("jenny")));
	}

	public void testClassArray() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		HasMap hasMap = (HasMap) xbf.getBean("classArray");
		assertTrue(hasMap.getClassArray().length ==2);
		assertTrue(hasMap.getClassArray()[0].equals(String.class));
		assertTrue(hasMap.getClassArray()[1].equals(Exception.class));
	}

	/*
	 * TODO address this failure
	 *
	public void testIntegerArray() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		HasMap hasMap = (HasMap) xbf.getBean("integerArray");
		assertTrue(hasMap.getIntegerArray().length == 3);
		assertTrue(hasMap.getIntegerArray()[0].intValue() == 0);
		assertTrue(hasMap.getIntegerArray()[1].intValue() == 1);
		assertTrue(hasMap.getIntegerArray()[2].intValue() == 2);
	}
	*/

	public void testInitMethodIsInvoked() throws Exception {
		InputStream is = getClass().getResourceAsStream("initializers.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		DoubleInitializer in = (DoubleInitializer) xbf.getBean("init-method1");
		// Initializer should have doubled value
		assertEquals(14, in.getNum());
	}

	/**
	 * Test that if a custom initializer throws an exception, it's handled correctly
	 */
	public void testInitMethodThrowsException() {
		InputStream is = getClass().getResourceAsStream("initializers.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		try {
			xbf.getBean("init-method2");
			fail();
		}
		catch (BeanCreationException ex) {
			assertTrue(ex.getCause() instanceof ServletException);
		}
	}

	public void testNoSuchInitMethod() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("initializers.xml", getClass()));
		try {
			xbf.getBean("init-method3");
			fail();
		}
		catch (FatalBeanException ex) {
			// check message is helpful
			assertTrue(ex.getMessage().indexOf("initializers.xml") != -1);
			assertTrue(ex.getMessage().indexOf("init-method3") != -1);
			assertTrue(ex.getMessage().indexOf("init") != -1);
		}
	}

	/**
	 * Check that InitializingBean method is called first.
	 */
	public void testInitializingBeanAndInitMethod() throws Exception {
		InitAndIB.constructed = false;
		InputStream is = getClass().getResourceAsStream("initializers.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		assertFalse(InitAndIB.constructed);
		xbf.preInstantiateSingletons();
		assertFalse(InitAndIB.constructed);
		InitAndIB iib = (InitAndIB) xbf.getBean("init-and-ib");
		assertTrue(InitAndIB.constructed);
		assertTrue(iib.afterPropertiesSetInvoked && iib.initMethodInvoked);
		assertTrue(!iib.destroyed && !iib.customDestroyed);
		xbf.destroySingletons();
		assertTrue(iib.destroyed && iib.customDestroyed);
		xbf.destroySingletons();
		assertTrue(iib.destroyed && iib.customDestroyed);
	}

	/**
	 * Check that InitializingBean method is called first.
	 */
	public void testDefaultLazyInit() throws Exception {
		InitAndIB.constructed = false;
		InputStream is = getClass().getResourceAsStream("default-lazy-init.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		assertFalse(InitAndIB.constructed);
		xbf.preInstantiateSingletons();
		assertTrue(InitAndIB.constructed);
		try {
			xbf.getBean("lazy-and-bad");
		}
		catch (BeanCreationException ex) {
			assertTrue(ex.getCause() instanceof ServletException);
		}
	}

	public void testNoSuchXmlFile() throws Exception {
		try {
			XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("missing.xml", getClass()));
			fail("Shouldn't create factory from missing XML");
		}
		catch (BeanDefinitionStoreException ex) {
			// Ok
			// TODO Check that the error message includes filename
		}
	}

	public void testInvalidXmlFile() throws Exception {
		try {
			XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("invalid.xml", getClass()));
			fail("Shouldn't create factory from invalid XML");
		}
		catch (BeanDefinitionStoreException ex) {
			// Ok
			// TODO Check that the error message includes filename
		}
	}

	public void testUnsatisfiedObjectDependencyCheck() throws Exception {
		try {
			XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("unsatisfiedObjectDependencyCheck.xml", getClass()));
			DependenciesBean a = (DependenciesBean) xbf.getBean("a");
			fail();
		}
		catch (UnsatisfiedDependencyException ex) {
			// Ok
			// What if many dependencies are unsatisfied?
			//assertTrue(ex.getMessage().indexOf("spouse"))
		}
	}

	public void testUnsatisfiedSimpleDependencyCheck() throws Exception {
		try {
			XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("unsatisfiedSimpleDependencyCheck.xml", getClass()));
			DependenciesBean a = (DependenciesBean) xbf.getBean("a");
			fail();
		}
		catch (UnsatisfiedDependencyException ex) {
			// Ok
			// What if many dependencies are unsatisfied?
			//assertTrue(ex.getMessage().indexOf("spouse"))
		}
	}

	public void testSatisfiedObjectDependencyCheck() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("satisfiedObjectDependencyCheck.xml", getClass()));
		DependenciesBean a = (DependenciesBean) xbf.getBean("a");
		assertNotNull(a.getSpouse());
	}

	public void testSatisfiedSimpleDependencyCheck() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("satisfiedSimpleDependencyCheck.xml", getClass()));
		DependenciesBean a = (DependenciesBean) xbf.getBean("a");
		assertEquals(a.getAge(), 33);
	}

	public void testUnsatisfiedAllDependencyCheck() throws Exception {
		try {
			XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("unsatisfiedAllDependencyCheckMissingObjects.xml", getClass()));
			DependenciesBean a = (DependenciesBean) xbf.getBean("a");
			fail();
		}
		catch (UnsatisfiedDependencyException ex) {
			// Ok
			// What if many dependencies are unsatisfied?
			//assertTrue(ex.getMessage().indexOf("spouse"))
		}
	}

	public void testSatisfiedAllDependencyCheck() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("satisfiedAllDependencyCheck.xml", getClass()));
		DependenciesBean a = (DependenciesBean) xbf.getBean("a");
		assertEquals(a.getAge(), 33);
		assertNotNull(a.getName());
		assertNotNull(a.getSpouse());
	}

	public void testAutowire() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("autowire.xml", getClass()));
		TestBean spouse = new TestBean("kerry", 0);
		xbf.registerSingleton("spouse", spouse);
		doTestAutowire(xbf);
	}

	public void testAutowireWithParent() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("autowire.xml", getClass()));
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("name", "kerry");
		lbf.registerBeanDefinition("spouse", new RootBeanDefinition(TestBean.class, pvs));
		xbf.setParentBeanFactory(lbf);
		doTestAutowire(xbf);
	}

	private void doTestAutowire(XmlBeanFactory xbf) throws Exception {
		DependenciesBean rod1 = (DependenciesBean) xbf.getBean("rod1");
		TestBean kerry = (TestBean) xbf.getBean("spouse");
		// should have been autowired
		assertEquals(kerry, rod1.getSpouse());

		DependenciesBean rod1a = (DependenciesBean) xbf.getBean("rod1a");
		// should have been autowired
		assertEquals(kerry, rod1a.getSpouse());

		DependenciesBean rod2 = (DependenciesBean) xbf.getBean("rod2");
		// should have been autowired
		assertEquals(kerry, rod2.getSpouse());

		ConstructorDependenciesBean rod3 = (ConstructorDependenciesBean) xbf.getBean("rod3");
		IndexedTestBean other = (IndexedTestBean) xbf.getBean("other");
		// should have been autowired
		assertEquals(kerry, rod3.getSpouse1());
		assertEquals(kerry, rod3.getSpouse2());
		assertEquals(other, rod3.getOther());

		ConstructorDependenciesBean rod3a = (ConstructorDependenciesBean) xbf.getBean("rod3a");
		// should have been autowired
		assertEquals(kerry, rod3a.getSpouse1());
		assertEquals(kerry, rod3a.getSpouse2());
		assertEquals(other, rod3a.getOther());

		try {
			ConstructorDependenciesBean rod4 = (ConstructorDependenciesBean) xbf.getBean("rod4");
			fail("Should not have thrown FatalBeanException");
		}
		catch (FatalBeanException ex) {
			// expected
		}

		DependenciesBean rod5 = (DependenciesBean) xbf.getBean("rod5");
		// Should not have been autowired
		assertNull(rod5.getSpouse());

		BeanFactory appCtx = (BeanFactory) xbf.getBean("childAppCtx");
		assertTrue(appCtx.containsBean("rod1"));
		assertTrue(appCtx.containsBean("jenny"));
	}

	public void testAutowireWithDefault() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("default-autowire.xml", getClass()));

		DependenciesBean rod1 = (DependenciesBean) xbf.getBean("rod1");
		// should have been autowired
		assertNotNull(rod1.getSpouse());
		assertTrue(rod1.getSpouse().getName().equals("Kerry"));

		DependenciesBean rod2 = (DependenciesBean) xbf.getBean("rod2");
		// should have been autowired
		assertNotNull(rod2.getSpouse());
		assertTrue(rod2.getSpouse().getName().equals("Kerry"));

		try {
			DependenciesBean rod3 = (DependenciesBean) xbf.getBean("rod3");
			fail("Should have thrown UnsatisfiedDependencyException");
		}
		catch (UnsatisfiedDependencyException ex) {
			// expected
		}
	}

	public void testAutowireByConstructor() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("constructor-arg.xml", getClass()));
		ConstructorDependenciesBean rod1 = (ConstructorDependenciesBean) xbf.getBean("rod1");
		TestBean kerry = (TestBean) xbf.getBean("kerry2");
		// should have been autowired
		assertEquals(kerry, rod1.getSpouse1());
		assertEquals(0, rod1.getAge());
		assertEquals(null, rod1.getName());

		ConstructorDependenciesBean rod2 = (ConstructorDependenciesBean) xbf.getBean("rod2");
		TestBean kerry1 = (TestBean) xbf.getBean("kerry1");
		TestBean kerry2 = (TestBean) xbf.getBean("kerry2");
		// should have been autowired
		assertEquals(kerry2, rod2.getSpouse1());
		assertEquals(kerry1, rod2.getSpouse2());
		assertEquals(0, rod2.getAge());
		assertEquals(null, rod2.getName());

		ConstructorDependenciesBean rod = (ConstructorDependenciesBean) xbf.getBean("rod3");
		IndexedTestBean other = (IndexedTestBean) xbf.getBean("other");
		// should have been autowired
		assertEquals(kerry, rod.getSpouse1());
		assertEquals(kerry, rod.getSpouse2());
		assertEquals(other, rod.getOther());
		assertEquals(0, rod.getAge());
		assertEquals(null, rod.getName());

		ConstructorDependenciesBean rod4 = (ConstructorDependenciesBean) xbf.getBean("rod4");
		// should have been autowired
		assertEquals(kerry, rod.getSpouse1());
		assertEquals(kerry, rod.getSpouse2());
		assertEquals(other, rod.getOther());
		assertEquals(0, rod.getAge());
		assertEquals(null, rod.getName());
	}

	public void testAutowireByConstructorWithSimpleValues() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("constructor-arg.xml", getClass()));

		ConstructorDependenciesBean rod5 = (ConstructorDependenciesBean) xbf.getBean("rod5");
		TestBean kerry1 = (TestBean) xbf.getBean("kerry1");
		TestBean kerry2 = (TestBean) xbf.getBean("kerry2");
		IndexedTestBean other = (IndexedTestBean) xbf.getBean("other");
		// should have been autowired
		assertEquals(kerry2, rod5.getSpouse1());
		assertEquals(kerry1, rod5.getSpouse2());
		assertEquals(other, rod5.getOther());
		assertEquals(99, rod5.getAge());
		assertEquals("myname", rod5.getName());

		DerivedConstructorDependenciesBean rod6 = (DerivedConstructorDependenciesBean) xbf.getBean("rod6");
		// should have been autowired
		assertTrue(rod6.initialized);
		assertTrue(!rod6.destroyed);
		assertEquals(kerry2, rod6.getSpouse1());
		assertEquals(kerry1, rod6.getSpouse2());
		assertEquals(other, rod6.getOther());
		assertEquals(0, rod6.getAge());
		assertEquals(null, rod6.getName());

		xbf.destroySingletons();
		assertTrue(rod6.destroyed);
	}

	public void testConstructorArgResolution() {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("constructor-arg.xml", getClass()));
		TestBean kerry1 = (TestBean) xbf.getBean("kerry1");
		TestBean kerry2 = (TestBean) xbf.getBean("kerry2");

		ConstructorDependenciesBean rod9 = (ConstructorDependenciesBean) xbf.getBean("rod9");
		assertEquals(99, rod9.getAge());

		ConstructorDependenciesBean rod10 = (ConstructorDependenciesBean) xbf.getBean("rod10");
		assertEquals(null, rod10.getName());

		ConstructorDependenciesBean rod11 = (ConstructorDependenciesBean) xbf.getBean("rod11");
		assertEquals(kerry2, rod11.getSpouse1());

		ConstructorDependenciesBean rod12 = (ConstructorDependenciesBean) xbf.getBean("rod12");
		assertEquals(kerry1, rod12.getSpouse1());
		assertNull(rod12.getSpouse2());

		ConstructorDependenciesBean rod13 = (ConstructorDependenciesBean) xbf.getBean("rod13");
		assertEquals(kerry1, rod13.getSpouse1());
		assertEquals(kerry2, rod13.getSpouse2());
	}

	public void testConstructorArgWithSingleMatch() {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("constructor-arg.xml", getClass()));
		File file = (File) xbf.getBean("file");
		assertEquals(File.separator + "test", file.getPath());
	}

	public void testThrowsExceptionOnTooManyArguments() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("constructor-arg.xml", getClass()));
		try {
			ConstructorDependenciesBean rod = (ConstructorDependenciesBean) xbf.getBean("rod7");
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (BeanCreationException ex) {
			// expected
		}
	}

	public void testThrowsExceptionOnAmbiguousResolution() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("constructor-arg.xml", getClass()));
		try {
			ConstructorDependenciesBean rod = (ConstructorDependenciesBean) xbf.getBean("rod8");
			fail("Should have thrown UnsatisfiedDependencyException");
		}
		catch (UnsatisfiedDependencyException ex) {
			// expected
		}
	}

	public void testFactoryBeanDefinedAsPrototype()  {
		try {
			XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("invalid-factory.xml", getClass()));
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (BeanDefinitionStoreException ex) {
			// expected
		}
	}

	public void testDependsOn() {
		doTestDependencies("dependencies-dependsOn.xml");
	}

	public void testDependsOnInInnerBean() {
		doTestDependencies("dependencies-dependsOn-inner.xml");
	}

	public void testDependenciesThroughConstructorArguments() {
		doTestDependencies("dependencies-carg.xml");
	}

	public void testDependenciesThroughConstructorArgumentAutowiring() {
		doTestDependencies("dependencies-carg-autowire.xml");
	}

	public void testDependenciesThroughConstructorArgumentsInInnerBean() {
		doTestDependencies("dependencies-carg-inner.xml");
	}

	public void testDependenciesThroughProperties() {
		doTestDependencies("dependencies-prop.xml");
	}

	public void testDependenciesThroughPropertyAutowiringByName() {
		doTestDependencies("dependencies-prop-autowireByName.xml");
	}

	public void testDependenciesThroughPropertyAutowiringByType() {
		doTestDependencies("dependencies-prop-autowireByType.xml");
	}

	public void testDependenciesThroughPropertiesInInnerBean() {
		doTestDependencies("dependencies-prop-inner.xml");
	}

	private void doTestDependencies(String filename) {
		PreparingBean1.prepared = false;
		PreparingBean1.destroyed = false;
		PreparingBean2.prepared = false;
		PreparingBean2.destroyed = false;
		DependingBean.destroyed = false;
		HoldingBean.destroyed = false;
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource(filename, getClass()));
		xbf.preInstantiateSingletons();
		xbf.destroySingletons();
		assertTrue(PreparingBean1.prepared);
		assertTrue(PreparingBean1.destroyed);
		assertTrue(PreparingBean2.prepared);
		assertTrue(PreparingBean2.destroyed);
		assertTrue(DependingBean.destroyed);
		if (xbf.containsBean("holdingBean")) {
			assertTrue(HoldingBean.destroyed);
		}
	}

	public void testClassNotFoundWithDefault() {
		try {
			XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("classNotFound.xml", getClass()));
			// should have thrown BeanDefinitionStoreException
		}
		catch (BeanDefinitionStoreException ex) {
			assertTrue(ex.getCause() instanceof ClassNotFoundException);
			// expected
		}
	}

	public void testClassNotFoundWithNoBeanClassLoader() {
		try {
			DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
			XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(bf);
			reader.setBeanClassLoader(null);
			reader.loadBeanDefinitions(new ClassPathResource("classNotFound.xml", getClass()));
			assertTrue(bf.getBeanDefinition("classNotFound") instanceof RootBeanDefinition);
			assertEquals(((RootBeanDefinition) bf.getBeanDefinition("classNotFound")).getBeanClassName(),
									 "org.springframework.beans.TestBeana");
		}
		catch (BeanDefinitionStoreException ex) {
			fail("Should not have thrown BeanDefinitionStoreException");
		}
	}

	public void testProps() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));

		HasMap hasMap = (HasMap) xbf.getBean("props");
		assertEquals(2, hasMap.getProps().size());
		assertEquals("bar", hasMap.getProps().getProperty("foo"));
		assertEquals("TWO", hasMap.getProps().getProperty("2"));

		HasMap hasMap2 = (HasMap) xbf.getBean("propsViaMap");
		assertEquals(2, hasMap2.getProps().size());
		assertEquals("bar", hasMap2.getProps().getProperty("foo"));
		assertEquals("TWO", hasMap2.getProps().getProperty("2"));
	}

	public void testListFactory() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		List list = (List) xbf.getBean("listFactory");
		assertTrue(list instanceof LinkedList);
		assertTrue(list.size() == 2);
		assertEquals("bar", list.get(0));
		assertEquals("jenny", list.get(1));
	}

	public void testPrototypeListFactory() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		List list = (List) xbf.getBean("pListFactory");
		assertTrue(list instanceof LinkedList);
		assertTrue(list.size() == 2);
		assertEquals("bar", list.get(0));
		assertEquals("jenny", list.get(1));
	}

	public void testSetFactory() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		Set set = (Set) xbf.getBean("setFactory");
		assertTrue(set instanceof TreeSet);
		assertTrue(set.size() == 2);
		assertTrue(set.contains("bar"));
		assertTrue(set.contains("jenny"));
	}

	public void testPrototypeSetFactory() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		Set set = (Set) xbf.getBean("pSetFactory");
		assertTrue(set instanceof TreeSet);
		assertTrue(set.size() == 2);
		assertTrue(set.contains("bar"));
		assertTrue(set.contains("jenny"));
	}

	public void testMapFactory() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		Map map = (Map) xbf.getBean("mapFactory");
		assertTrue(map instanceof TreeMap);
		assertTrue(map.size() == 2);
		assertEquals("bar", map.get("foo"));
		assertEquals("jenny", map.get("jen"));
	}

	public void testPrototypeMapFactory() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		Map map = (Map) xbf.getBean("pMapFactory");
		assertTrue(map instanceof TreeMap);
		assertTrue(map.size() == 2);
		assertEquals("bar", map.get("foo"));
		assertEquals("jenny", map.get("jen"));
	}

	public void testCollectionFactoryDefaults() throws Exception {
		ListFactoryBean listFactory = new ListFactoryBean();
		listFactory.setSourceList(new LinkedList());
		listFactory.afterPropertiesSet();
		assertTrue(listFactory.getObject() instanceof ArrayList);

		SetFactoryBean setFactory = new SetFactoryBean();
		setFactory.setSourceSet(new TreeSet());
		setFactory.afterPropertiesSet();
		assertTrue(setFactory.getObject() instanceof HashSet);

		MapFactoryBean mapFactory = new MapFactoryBean();
		mapFactory.setSourceMap(new TreeMap());
		mapFactory.afterPropertiesSet();
		assertTrue(mapFactory.getObject() instanceof HashMap);
	}

	public void testResourceAndInputStream() throws IOException {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("resource.xml", getClass()));
		// comes from "resourceImport.xml"
		ResourceTestBean resource1 = (ResourceTestBean) xbf.getBean("resource1");
		// comes from "resource.xml"
		ResourceTestBean resource2 = (ResourceTestBean) xbf.getBean("resource2");

		assertTrue(resource1.getResource() instanceof ClassPathResource);
		StringWriter writer = new StringWriter();
		FileCopyUtils.copy(new InputStreamReader(resource1.getResource().getInputStream()), writer);
		assertEquals("test", writer.toString());
		writer = new StringWriter();
		FileCopyUtils.copy(new InputStreamReader(resource1.getInputStream()), writer);
		assertEquals("test", writer.toString());
		writer = new StringWriter();
		FileCopyUtils.copy(new InputStreamReader(resource2.getResource().getInputStream()), writer);
		assertEquals("test", writer.toString());
		writer = new StringWriter();
		FileCopyUtils.copy(new InputStreamReader(resource2.getInputStream()), writer);
		assertEquals("test", writer.toString());
	}

	public void testClassPathResourceWithImport() {
		XmlBeanFactory xbf = new XmlBeanFactory(
				new ClassPathResource("org/springframework/beans/factory/xml/resource.xml"));
		// comes from "resourceImport.xml"
		ResourceTestBean resource1 = (ResourceTestBean) xbf.getBean("resource1");
		// comes from "resource.xml"
		ResourceTestBean resource2 = (ResourceTestBean) xbf.getBean("resource2");
	}

	public void testUrlResourceWithImport() {
		URL url = getClass().getResource("resource.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(new UrlResource(url));
		// comes from "resourceImport.xml"
		ResourceTestBean resource1 = (ResourceTestBean) xbf.getBean("resource1");
		// comes from "resource.xml"
		ResourceTestBean resource2 = (ResourceTestBean) xbf.getBean("resource2");
	}

	public void testFileSystemResourceWithImport() {
		String file = getClass().getResource("resource.xml").getFile();
		XmlBeanFactory xbf = new XmlBeanFactory(new FileSystemResource(file));
		// comes from "resourceImport.xml"
		ResourceTestBean resource1 = (ResourceTestBean) xbf.getBean("resource1");
		// comes from "resource.xml"
		ResourceTestBean resource2 = (ResourceTestBean) xbf.getBean("resource2");
	}

	public void testLookupOverrideMethodsWithSetterInjection() {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(true);
		reader.loadBeanDefinitions(new ClassPathResource("overrides.xml", getClass()));
		
		testLookupOverrideMethodsWithSetterInjection(xbf, "overrideOneMethod", true);
		// Should work identically on subclass definition, in which lookup
		// methods are inherited
		testLookupOverrideMethodsWithSetterInjection(xbf, "overrideInheritedMethod", true);
		
		// Check cost of repeated construction of beans with method overrides
		// Will pick up misuse of CGLIB
		int howmany = 100;
		StopWatch sw = new StopWatch();
		sw.start("Look up " + howmany + " prototype bean instances with method overrides");
		for (int i = 0; i < howmany; i++) {
			testLookupOverrideMethodsWithSetterInjection(xbf, "overrideOnPrototype", false);
		}
		sw.stop();
		System.out.println(sw);
		assertTrue(sw.getTotalTimeMillis() < 1000);
		
		// Now test distinct bean with swapped value in factory, to ensure the two are independent
		OverrideOneMethod swappedOom = (OverrideOneMethod) xbf.getBean("overrideOneMethodSwappedReturnValues");
		
		TestBean tb = swappedOom.getPrototypeDependency();
		assertEquals("David", tb.getName()); 
		tb = swappedOom.protectedOverrideSingleton();
		assertEquals("Jenny", tb.getName()); 
	}
	
	private void testLookupOverrideMethodsWithSetterInjection(BeanFactory xbf, String beanName, boolean singleton) {
		OverrideOneMethod oom = (OverrideOneMethod) xbf.getBean(beanName);
		
		if (singleton) {
			assertSame(oom, xbf.getBean(beanName));
		}
		else {
			assertNotSame(oom, xbf.getBean(beanName));
		}
		
		TestBean jenny1 = oom.getPrototypeDependency();
		assertEquals("Jenny", jenny1.getName()); 
		TestBean jenny2 = oom.getPrototypeDependency();
		assertEquals("Jenny", jenny2.getName());
		assertNotSame(jenny1, jenny2); 
		
		// Check that the bean can invoke the overriden method on itself
		// This differs from Spring's AOP support, which has
		// a distinct notion of a "target" object, meaning that the
		// target needs explicit knowledge of AOP proxying to invoke
		// an advised method on itself
		TestBean jenny3 = oom.invokesOverridenMethodOnSelf();
		assertEquals("Jenny", jenny3.getName());
		assertNotSame(jenny1, jenny3); 
		
		// Now try protected method, and singleton
		TestBean dave1 = oom.protectedOverrideSingleton();
		assertEquals("David", dave1.getName()); 
		TestBean dave2 = oom.protectedOverrideSingleton();
		assertEquals("David", dave2.getName());
		assertSame(dave1, dave2); 
	}
	
	public void testReplaceMethodOverrideWithSetterInjection() {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(true);
		reader.loadBeanDefinitions(new ClassPathResource("delegationOverrides.xml", getClass()));
		
		OverrideOneMethod oom = (OverrideOneMethod) xbf.getBean("overrideOneMethod");
		
		// Same contract as for overrides.xml
		TestBean jenny1 = oom.getPrototypeDependency();
		assertEquals("Jenny", jenny1.getName()); 
		TestBean jenny2 = oom.getPrototypeDependency();
		assertEquals("Jenny", jenny2.getName());
		assertNotSame(jenny1, jenny2); 
		
		// Now try protected method, and singleton
		TestBean dave1 = oom.protectedOverrideSingleton();
		assertEquals("David", dave1.getName()); 
		TestBean dave2 = oom.protectedOverrideSingleton();
		assertEquals("David", dave2.getName());
		assertSame(dave1, dave2); 
		
		// Check unadvised behaviour
		String str = "woierowijeiowiej";
		assertEquals(str, oom.echo(str));
		
		// Now test replace
		String s = "this is not a palindrome";
		String reverse = new StringBuffer(s).reverse().toString();
		assertEquals("Should have overridden to reverse, not echo", reverse, oom.replaceMe(s));
		
		assertEquals("Should have overridden no-arg overloaded replaceMe method to return fixed value",
				FixedMethodReplacer.VALUE, oom.replaceMe());
		
		OverrideOneMethodSubclass ooms = (OverrideOneMethodSubclass) xbf.getBean("replaceVoidMethod");
		DoSomethingReplacer dos = (DoSomethingReplacer) xbf.getBean("doSomethingReplacer");
		assertEquals(null, dos.lastArg);
		String s1 = "";
		String s2 = "foo bar black sheep";
		ooms.doSomething(s1);
		assertEquals(s1, dos.lastArg);
		ooms.doSomething(s2);
		assertEquals(s2, dos.lastArg);
	}
	
	public void testLookupOverrideOneMethodWithConstructorInjection() {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(true);
		reader.loadBeanDefinitions(new ClassPathResource("constructorOverrides.xml", getClass()));
		
		ConstructorInjectedOverrides cio = (ConstructorInjectedOverrides) xbf.getBean("constructorOverrides");
		
		// Check that the setter was invoked...
		// We should be able to combine Constructor and
		// Setter Injection
		assertEquals("Setter string was set", "from property element", cio.getSetterString());
		
		// Jenny is a singleton
		TestBean jenny = (TestBean) xbf.getBean("jenny");
		assertSame(jenny, cio.getTestBean());
		assertSame(jenny, cio.getTestBean());
		FactoryMethods fm1 = cio.createFactoryMethods();
		FactoryMethods fm2 = cio.createFactoryMethods();
		assertNotSame("FactoryMethods reference is to a prototype", fm1, fm2);
		assertSame("The two prototypes hold the same singleton reference",
				fm1.getTestBean(), fm2.getTestBean());
	}
	
	public void testRejectsOverrideOfBogusMethodName() {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(true);
		try {
			reader.loadBeanDefinitions(new ClassPathResource("invalidOverridesNoSuchMethod.xml", getClass()));
			fail("Shouldn't allow override of bogus method");
		}
		catch (BeanDefinitionStoreException ex) {
			// Check that the bogus method name was included in the error message
			assertTrue("Bogus method name correctly reported", ex.getMessage().indexOf("bogusMethod") != -1);
		}
	}
	
	/**
	 * Assert the presence of this bug until we resolve
	 * it
	 * @throws Exception
	 */
	public void testSerializabilityOfMethodReplacer() throws Exception {
		try {
			BUGtestSerializableMethodReplacerAndSuperclass();
			fail();
		}
		catch (AssertionFailedError ex) {
			System.err.println("****** SPR-356: Objects with MethodReplace overrides are not serializable");
		}
	}
	
	public void BUGtestSerializableMethodReplacerAndSuperclass() throws IOException, ClassNotFoundException {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(true);
		reader.loadBeanDefinitions(new ClassPathResource("delegationOverrides.xml", getClass()));
		SerializableMethodReplacerCandidate s = (SerializableMethodReplacerCandidate) xbf.getBean("serializableReplacer");
		String forwards = "this is forwards";
		String backwards = new StringBuffer(forwards).reverse().toString();
		assertEquals(backwards, s.replaceMe(forwards));
		assertTrue(SerializationTestUtils.isSerializable(s));
		s = (SerializableMethodReplacerCandidate) SerializationTestUtils.serializeAndDeserialize(s);
		assertEquals("Method replace still works after serialization and deserialization", backwards, s.replaceMe(forwards));
	}
				
	
	public void testFactoryMethodsSingletonOnTargetClass() {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(true);
		reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
		FactoryMethods fm = (FactoryMethods) xbf.getBean("default");
		assertEquals(0, fm.getNum());
		assertEquals("default", fm.getName());
		assertEquals("defaultInstance", fm.getTestBean().getName());
		assertEquals("setterString", fm.getStringValue());
		
		fm = (FactoryMethods) xbf.getBean("testBeanOnly");
		assertEquals(0, fm.getNum());
		assertEquals("default", fm.getName());
		// This comes from the test bean
		assertEquals("Juergen", fm.getTestBean().getName());
		
		fm = (FactoryMethods) xbf.getBean("full");
		assertEquals(27, fm.getNum());
		assertEquals("gotcha", fm.getName());
		assertEquals("Juergen", fm.getTestBean().getName());
		
		FactoryMethods fm2 = (FactoryMethods) xbf.getBean("full");
		assertSame(fm, fm2);
	}
	
	public void testFactoryMethodsPrototypeOnTargetClass() {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(true);
		reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
		FactoryMethods fm = (FactoryMethods) xbf.getBean("defaultPrototype");
		FactoryMethods fm2 = (FactoryMethods) xbf.getBean("defaultPrototype");
		assertEquals(0, fm.getNum());
		assertEquals("default", fm.getName());
		assertEquals("defaultInstance", fm.getTestBean().getName());
		assertEquals("setterString", fm.getStringValue());
		assertEquals(fm.getNum(), fm2.getNum());
		assertEquals(fm.getStringValue(), fm2.getStringValue());
		// The TestBean is created separately for each bean
		assertNotSame(fm.getTestBean(), fm2.getTestBean());
		assertNotSame(fm, fm2);
		
		fm = (FactoryMethods) xbf.getBean("testBeanOnlyPrototype");
		fm2 = (FactoryMethods) xbf.getBean("testBeanOnlyPrototype");
		assertEquals(0, fm.getNum());
		assertEquals("default", fm.getName());
		// This comes from the test bean
		assertEquals("Juergen", fm.getTestBean().getName());
		assertEquals(fm.getNum(), fm2.getNum());
		assertEquals(fm.getStringValue(), fm2.getStringValue());
		// The TestBean reference is resolved to a prototype in the factory
		assertSame(fm.getTestBean(), fm2.getTestBean());
		assertNotSame(fm, fm2);
		
		fm = (FactoryMethods) xbf.getBean("fullPrototype");
		fm2 = (FactoryMethods) xbf.getBean("fullPrototype");
		assertEquals(27, fm.getNum());
		assertEquals("gotcha", fm.getName());
		assertEquals("Juergen", fm.getTestBean().getName());
		assertEquals(fm.getNum(), fm2.getNum());
		assertEquals(fm.getStringValue(), fm2.getStringValue());
		// The TestBean reference is resolved to a prototype in the factory
		assertSame(fm.getTestBean(), fm2.getTestBean());
		assertNotSame(fm, fm2);
	}
	
	/**
	 * Tests where the static factory method is on a different class
	 *
	 */
	public void testFactoryMethodsOnExternalClass() {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(true);
		reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
		TestBean tb = (TestBean) xbf.getBean("externalFactoryMethodWithoutArgs");
		assertEquals(2, tb.getAge());
		assertEquals("Tristan", tb.getName());
		
		tb = (TestBean) xbf.getBean("externalFactoryMethodWithArgs");
		assertEquals(33, tb.getAge());
		assertEquals("Rod", tb.getName());
	}
	
	public void testInstanceFactoryMethodWithoutArgs() {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(true);
		reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
		FactoryMethods fm = (FactoryMethods) xbf.getBean("instanceFactoryMethodWithoutArgs");
		assertEquals("instanceFactory", fm.getTestBean().getName());
		
		//tb = (TestBean) xbf.getBean("externalFactoryMethodWithArgs");
		//assertEquals(33, tb.getAge());
		//assertEquals("Rod", tb.getName());
	}
	
	public void testFactoryMethodNoMatchingStaticMethod() {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(true);
		reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
		try{
			xbf.getBean("noMatchPrototype");
			fail("No static method matched");
		}
		catch (BeanCreationException ex) {
			// Ok
		}
	}
	
	public void testCanSpecifyFactoryMethodArgumentsOnFactoryMethodPrototype() {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(true);
		reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
		TestBean tbArg = new TestBean();
		tbArg.setName("arg1");
		TestBean tbArg2 = new TestBean();
		tbArg2.setName("arg2");
		FactoryMethods fm1 = (FactoryMethods) xbf.getBean("testBeanOnlyPrototype", new Object[] { tbArg });	
		FactoryMethods fm2 = (FactoryMethods) xbf.getBean("testBeanOnlyPrototype", new Object[] { tbArg2 });	
		
		assertEquals(0, fm1.getNum());
		assertEquals("default", fm1.getName());
		// This comes from the test bean
		assertEquals("arg1", fm1.getTestBean().getName());
		assertEquals("arg2", fm2.getTestBean().getName());
		assertEquals(fm1.getNum(), fm2.getNum());
		assertEquals(fm2.getStringValue(), "testBeanOnlyPrototypeDISetterString");
		assertEquals(fm2.getStringValue(), fm2.getStringValue());
		// The TestBean reference is resolved to a prototype in the factory
		assertSame(fm2.getTestBean(), fm2.getTestBean());
		assertNotSame(fm1, fm2);
	}
	
	public void testCannotSpecifyFactoryMethodArgumentsOnSingleton() {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(true);
		reader.loadBeanDefinitions(new ClassPathResource("factory-methods.xml", getClass()));
		try {
			xbf.getBean("testBeanOnly", new Object[] { new TestBean() });	
			fail("Shouldn't allow args to be passed to a singleton");
		}
		catch (BeanDefinitionStoreException ex) {
			// OK
		}
	}
	
	public void testCannotSpecifyFactoryMethodArgumentsExceptWithFactoryMethod() {
		DefaultListableBeanFactory xbf = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(xbf);
		reader.setValidating(true);
		reader.loadBeanDefinitions(new ClassPathResource("overrides.xml", getClass()));
		try {
			xbf.getBean("overrideOnPrototype", new Object[] { new TestBean() });	
			fail("Shouldn't allow args to be passed to a Setter-Injected object");
		}
		catch (BeanDefinitionStoreException ex) {
			// OK			
		}
	}


	public static class DoSomethingReplacer implements MethodReplacer {

		public Object lastArg;

		public Object reimplement(Object o, Method m, Object[] args) throws Throwable {
			assertEquals(1, args.length);
			assertEquals("doSomething", m.getName());
			lastArg = args[0];
			return null;
		}
	}


	public static class BadInitializer {

		/** Init method */
		public void init2() throws ServletException {
			throw new ServletException();
		}
	}


	public static class DoubleInitializer {

		private int num;

		public int getNum() {
			return num;
		}

		public void setNum(int i) {
			num = i;
		}

		/** Init method */
		public void init() {
			this.num *= 2;
		}
	}


	public static class InitAndIB implements InitializingBean, DisposableBean {

		public static boolean constructed;

		public boolean afterPropertiesSetInvoked, initMethodInvoked, destroyed, customDestroyed;

		public InitAndIB() {
			constructed = true;
		}

		public void afterPropertiesSet() {
			if (this.initMethodInvoked)
				fail();
			this.afterPropertiesSetInvoked = true;
		}

		/** Init method */
		public void customInit() throws ServletException {
			if (!this.afterPropertiesSetInvoked)
				fail();
			this.initMethodInvoked = true;
		}

		public void destroy() {
			if (this.customDestroyed)
				fail();
			if (this.destroyed) {
				throw new IllegalStateException("Already destroyed");
			}
			this.destroyed = true;
		}

		public void customDestroy() {
			if (!this.destroyed)
				fail();
			if (this.customDestroyed) {
				throw new IllegalStateException("Already customDestroyed");
			}
			this.customDestroyed = true;
		}
	}


	public static class PreparingBean1 implements DisposableBean {

		public static boolean prepared = false;
		public static boolean destroyed = false;

		public PreparingBean1() {
			prepared = true;
		}

		public void destroy() {
			destroyed = true;
		}
	}


	public static class PreparingBean2 implements DisposableBean {

		public static boolean prepared = false;
		public static boolean destroyed = false;

		public PreparingBean2() {
			prepared = true;
		}

		public void destroy() {
			destroyed = true;
		}
	}


	public static class DependingBean implements InitializingBean, DisposableBean {

		public static boolean destroyed = false;

		public DependingBean() {
		}

		public DependingBean(PreparingBean1 bean1, PreparingBean2 bean2) {
		}

		public void setBean1(PreparingBean1 bean1) {
		}

		public void setBean2(PreparingBean2 bean2) {
		}

		public void afterPropertiesSet() {
			if (!(PreparingBean1.prepared && PreparingBean2.prepared)) {
				throw new IllegalStateException("Need prepared PreparedBeans!");
			}
		}

		public void destroy() {
			if (PreparingBean1.destroyed || PreparingBean2.destroyed) {
				throw new IllegalStateException("Should not be destroyed before PreparedBeans");
			}
			destroyed = true;
		}
	}


	public static class HoldingBean implements DisposableBean {

		public static boolean destroyed = false;

		public void setDependingBean(DependingBean dependingBean) {
		}

		public void destroy() {
			if (DependingBean.destroyed) {
				throw new IllegalStateException("Should not be destroyed before DependingBean");
			}
			destroyed = true;
		}
	}

}
