/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory.xml;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import junit.framework.TestCase;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.ITestBean;
import org.springframework.beans.MethodInvocationException;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.DummyFactory;
import org.springframework.beans.factory.HasMap;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.LifecycleBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;

/**
 * @author Rod Johnson
 * @version $Id: XmlBeanFactoryTestSuite.java,v 1.20 2003-11-22 15:53:50 johnsonr Exp $
 */
public class XmlBeanFactoryTestSuite extends TestCase {

	/** Uses a separate factory */
	public void testRefToSeparatePrototypeInstances() throws Exception {
		InputStream is = getClass().getResourceAsStream("reftypes.xml");
		XmlBeanFactory xbf = new XmlBeanFactory();
		xbf.setValidating(false);
		xbf.loadBeanDefinitions(is);
		assertTrue("6 beans in reftypes, not " + xbf.getBeanDefinitionCount(), xbf.getBeanDefinitionCount() == 6);
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
		InputStream is = getClass().getResourceAsStream("reftypes.xml");
		XmlBeanFactory xbf = new XmlBeanFactory();
		xbf.setValidating(false);
		xbf.loadBeanDefinitions(is);
		assertTrue("6 beans in reftypes, not " + xbf.getBeanDefinitionCount(), xbf.getBeanDefinitionCount() == 6);
		TestBean jen = (TestBean) xbf.getBean("jenny");
		TestBean dave = (TestBean) xbf.getBean("david");
		TestBean jenks = (TestBean) xbf.getBean("jenks");
		ITestBean davesJen = dave.getSpouse();
		ITestBean jenksJen = jenks.getSpouse();
		assertTrue("1 jen instance", davesJen == jenksJen);
		assertTrue("1 jen instance", davesJen == jen);
	}

	public void testSingletonInheritanceFromParentFactorySingleton() throws Exception {
		InputStream pis = getClass().getResourceAsStream("parent.xml");
		XmlBeanFactory parent = new XmlBeanFactory(pis);
		InputStream is = getClass().getResourceAsStream("child.xml");
		XmlBeanFactory child = new XmlBeanFactory(is, parent);
		TestBean inherits = (TestBean) child.getBean("inheritsFromParentFactory");
		// Name property value is overriden
		assertTrue(inherits.getName().equals("override"));
		// Age property is inherited from bean in parent factory
		assertTrue(inherits.getAge() == 1);
		TestBean inherits2 = (TestBean) child.getBean("inheritsFromParentFactory");
		assertTrue(inherits2 == inherits);
	}

	public void testPrototypeInheritanceFromParentFactoryPrototype() throws Exception {
		InputStream pis = getClass().getResourceAsStream("parent.xml");
		XmlBeanFactory parent = new XmlBeanFactory(pis);
		InputStream is = getClass().getResourceAsStream("child.xml");
		XmlBeanFactory child = new XmlBeanFactory(is, parent);
		TestBean inherits = (TestBean) child.getBean("prototypeInheritsFromParentFactoryPrototype");
		// Name property value is overriden
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
		InputStream pis = getClass().getResourceAsStream("parent.xml");
		XmlBeanFactory parent = new XmlBeanFactory(pis);
		InputStream is = getClass().getResourceAsStream("child.xml");
		XmlBeanFactory child = new XmlBeanFactory(is, parent);
		TestBean inherits = (TestBean) child.getBean("protoypeInheritsFromParentFactorySingleton");
		// Name property value is overriden
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
	
	public void testDependenciesMaterializeThis() throws Exception {
		InputStream pis = getClass().getResourceAsStream("dependenciesMaterializeThis.xml");
		XmlBeanFactory bf = new XmlBeanFactory(pis);
		DummyBoImpl bos = (DummyBoImpl) bf.getBean("boSingleton");
		DummyBoImpl bop = (DummyBoImpl) bf.getBean("boPrototype");
		assertNotSame(bos, bop);
		assertEquals(bos.dao, bop.dao);
	}

	/**
	 * Check that a prototype can't inherit from a bogus parent.
	 * If a singleton does this the factory will fail to load.
	 * @throws Exception
	 */
	public void testBogusParentageFromParentFactory() throws Exception {
		InputStream pis = getClass().getResourceAsStream("parent.xml");
		XmlBeanFactory parent = new XmlBeanFactory(pis);
		InputStream is = getClass().getResourceAsStream("child.xml");
		XmlBeanFactory child = new XmlBeanFactory(is, parent);
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
		InputStream pis = getClass().getResourceAsStream("parent.xml");
		XmlBeanFactory parent = new XmlBeanFactory(pis);
		InputStream is = getClass().getResourceAsStream("child.xml");
		XmlBeanFactory child = new XmlBeanFactory(is, parent);
		TestBean inherits = (TestBean) child.getBean("singletonInheritsFromParentFactoryPrototype");
		// Name property value is overriden
		assertTrue(inherits.getName().equals("prototype-override"));
		// Age property is inherited from bean in parent factory
		assertTrue(inherits.getAge() == 2);
		TestBean inherits2 = (TestBean) child.getBean("singletonInheritsFromParentFactoryPrototype");
		assertTrue(inherits2 == inherits);
	}

	public void testCircularReferences() {
		InputStream is = getClass().getResourceAsStream("reftypes.xml");
		XmlBeanFactory xbf = new XmlBeanFactory();
		xbf.setValidating(false);
		xbf.loadBeanDefinitions(is);
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
			friends.get(0).equals(jen));
		assertTrue(friends.get(1).equals(dave));
		// Should be ordered
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
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		//assertTrue("5 beans in reftypes, not " + xbf.getBeanDefinitionCount(), xbf.getBeanDefinitionCount() == 5);
		MixedCollectionBean jumble = (MixedCollectionBean) xbf.getBean("jumble");
		assertEquals(1, MixedCollectionBean.nrOfInstances);
		TestBean dave = (TestBean) xbf.getBean("david");
		assertTrue("Expected 3 elements, not " + jumble.getJumble().size(),
				jumble.getJumble().size() == 3);
		List l = (List) jumble.getJumble();
		assertTrue(l.get(0).equals(xbf.getBean("david")));
		assertTrue(l.get(1).equals("literal"));
		assertTrue(l.get(2).equals(xbf.getBean("jenny")));
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
		assertEquals("noname", tb4.getName());
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
		assertTrue(hasMap.getMap().size() == 2);
		assertTrue(hasMap.getMap().get("foo").equals("bar"));
		assertTrue(hasMap.getMap().get("fi").equals("fum"));
	}

	public void testMapWithLiteralsAndReferences() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		HasMap hasMap = (HasMap) xbf.getBean("mixedMap");
		assertTrue(hasMap.getMap().size() == 2);
		assertTrue(hasMap.getMap().get("foo").equals("bar"));
		TestBean jenny = (TestBean) xbf.getBean("jenny");
		assertTrue(hasMap.getMap().get("jenny").equals(jenny));
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
		assertTrue(l.size() == 2);
		assertTrue(l.get(0).equals("zero"));
		assertTrue("List element 1 should be equal to jenny bean, not " + l.get(1),
			l.get(1).equals(jenny));

		// Check nested map
		Map m = (Map) hasMap.getMap().get("map");
		assertNotNull(m);
		assertTrue(m.size() == 2);
		assertTrue(m.get("foo").equals("bar"));
		assertTrue("Map element 'jenny' should be equal to jenny bean, not " + m.get("jenny"),
			l.get(1).equals(jenny));
	}

	public void testEmptyProps() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		HasMap hasMap = (HasMap) xbf.getBean("emptyProps");
		assertTrue(hasMap.getMap().size() == 0);
		assertTrue(hasMap.getMap().size() == 0);
	}

	public void testPopulatedProps() throws Exception {
		InputStream is = getClass().getResourceAsStream("collections.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		HasMap hasMap = (HasMap) xbf.getBean("props");
		assertTrue(hasMap.getMap().size() == 2);
		assertTrue(hasMap.getMap().get("foo").equals("bar"));
		assertTrue(hasMap.getMap().get("2").equals("TWO"));
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
		catch (MethodInvocationException ex) {
			assertTrue(ex.getRootCause() instanceof ServletException);
		}
	}

	public void testNoSuchInitMethod() throws Exception {
		InputStream is = getClass().getResourceAsStream("initializers.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		try {
			xbf.getBean("init-method3");
			fail();
		}
		catch (FatalBeanException ex) {
			// Ok
			// Check message is helpful
			assertTrue(ex.getMessage().indexOf("init") != -1);
			assertTrue(ex.getMessage().indexOf("beans.TestBean") != -1);
		}
	}

	/**
	 * Check that InitializingBean method is called first.
	 * @throws Exception
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

	public void testNoSuchXmlFile() throws Exception {
		String filename = "missing.xml";
		InputStream is = getClass().getResourceAsStream(filename);
		try {
			XmlBeanFactory xbf = new XmlBeanFactory(is);
			fail("Shouldn't create factory from missing XML");
		}
		catch (BeanDefinitionStoreException ex) {
			// Ok
			// TODO Check that the error message includes filename
		}
	}

	public void testInvalidXmlFile() throws Exception {
		String filename = "invalid.xml";
		InputStream is = getClass().getResourceAsStream(filename);
		try {
			XmlBeanFactory xbf = new XmlBeanFactory(is);
			fail("Shouldn't create factory from invalid XML");
		}
		catch (BeanDefinitionStoreException ex) {
			// Ok
			// TODO Check that the error message includes filename
		}
	}

	public void testUnsatisfiedObjectDependencyCheck() throws Exception {
		InputStream is = getClass().getResourceAsStream("unsatisfiedObjectDependencyCheck.xml");

		try {
			XmlBeanFactory xbf = new XmlBeanFactory(is);
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
		InputStream is = getClass().getResourceAsStream("unsatisfiedSimpleDependencyCheck.xml");
		try {
			XmlBeanFactory xbf = new XmlBeanFactory(is);
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
		InputStream is = getClass().getResourceAsStream("satisfiedObjectDependencyCheck.xml");

		XmlBeanFactory xbf = new XmlBeanFactory(is);
		DependenciesBean a = (DependenciesBean) xbf.getBean("a");
		assertNotNull(a.getSpouse());
	}

	public void testSatisfiedSimpleDependencyCheck() throws Exception {
		InputStream is = getClass().getResourceAsStream("satisfiedSimpleDependencyCheck.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		DependenciesBean a = (DependenciesBean) xbf.getBean("a");
		assertEquals(a.getAge(), 33);
	}

	public void testUnsatisfiedAllDependencyCheck() throws Exception {
		InputStream is = getClass().getResourceAsStream("unsatisfiedAllDependencyCheckMissingObjects.xml");
		try {
			XmlBeanFactory xbf = new XmlBeanFactory(is);
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
		InputStream is = getClass().getResourceAsStream("satisfiedAllDependencyCheck.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		DependenciesBean a = (DependenciesBean) xbf.getBean("a");
		assertEquals(a.getAge(), 33);
		assertNotNull(a.getName());
		assertNotNull(a.getSpouse());
	}

	public void testSatisfiedAutowireByType() throws Exception {
		InputStream is = getClass().getResourceAsStream("autowire.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		DependenciesBean rod = (DependenciesBean) xbf.getBean("rod1");
		TestBean kerry = (TestBean) xbf.getBean("spouse");
		// Should have been autowired
		assertEquals(kerry, rod.getSpouse());
	}

	public void testSatisfiedAutowireByName() throws Exception {
		InputStream is = getClass().getResourceAsStream("autowire.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		DependenciesBean rod = (DependenciesBean) xbf.getBean("rod2");
		TestBean kerry = (TestBean) xbf.getBean("spouse");
		// Should have been autowired
		assertEquals(kerry, rod.getSpouse());
	}

	public void testSatisfiedAutowireByConstructor() throws Exception {
		InputStream is = getClass().getResourceAsStream("autowire.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		ConstructorDependenciesBean rod = (ConstructorDependenciesBean) xbf.getBean("rod3");
		TestBean kerry = (TestBean) xbf.getBean("spouse");
		LifecycleBean other = (LifecycleBean) xbf.getBean("other");
		// Should have been autowired
		assertEquals(kerry, rod.getSpouse1());
		assertEquals(kerry, rod.getSpouse2());
		assertEquals(other, rod.getOther());
	}

	public void testExceptionIfNoDefaultConstructor() throws Exception {
		InputStream is = getClass().getResourceAsStream("autowire.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		try {
			ConstructorDependenciesBean rod = (ConstructorDependenciesBean) xbf.getBean("rod4");
			fail("Should not have thrown FatalBeanException");
		}
		catch (FatalBeanException ex) {
			// expected
		}
	}

	public void testAutowireByConstructorWithDefaultConstructor() throws Exception {
		InputStream is = getClass().getResourceAsStream("autowire.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		DependenciesBean rod = (DependenciesBean) xbf.getBean("rod5");
		// Should not have been autowired
		assertNull(rod.getSpouse());
	}

	public void testAutowireByConstructorWithClassPathXmlApplicationContext() throws Exception {
		InputStream is = getClass().getResourceAsStream("autowire.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		BeanFactory appCtx = (BeanFactory) xbf.getBean("childAppCtx");
		assertTrue(appCtx.getBean("rod1") != null);
		assertTrue(appCtx.getBean("dependingBean") != null);
		assertTrue(appCtx.getBean("jenny") != null);
	}

	public void testSatisfiedAutowireByTypeWithDefault() throws Exception {
		InputStream is = getClass().getResourceAsStream("default-autowire.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		DependenciesBean rod = (DependenciesBean) xbf.getBean("rod1");
		// Should have been autowired
		assertNotNull(rod.getSpouse());
		assertTrue(rod.getSpouse().getName().equals("Kerry"));
	}

	public void testSatisfiedAutowireByNameWithDefault() throws Exception {
		InputStream is = getClass().getResourceAsStream("default-autowire.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		DependenciesBean rod = (DependenciesBean) xbf.getBean("rod2");
		// Should have been autowired
		assertNotNull(rod.getSpouse());
		assertTrue(rod.getSpouse().getName().equals("Kerry"));
	}

	public void testSatisfiedGenericConstructorArg() throws Exception {
		InputStream is = getClass().getResourceAsStream("constructor-arg.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		ConstructorDependenciesBean rod = (ConstructorDependenciesBean) xbf.getBean("rod1");
		TestBean kerry = (TestBean) xbf.getBean("kerry2");
		// Should have been autowired
		assertEquals(kerry, rod.getSpouse1());
		assertEquals(kerry, rod.getSpouse2());
		assertEquals(0, rod.getAge());
		assertEquals(null, rod.getName());
	}

	public void testSatisfiedIndexedConstructorArg() throws Exception {
		InputStream is = getClass().getResourceAsStream("constructor-arg.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		ConstructorDependenciesBean rod = (ConstructorDependenciesBean) xbf.getBean("rod2");
		TestBean kerry1 = (TestBean) xbf.getBean("kerry1");
		TestBean kerry2 = (TestBean) xbf.getBean("kerry2");
		// Should have been autowired
		assertEquals(kerry2, rod.getSpouse1());
		assertEquals(kerry1, rod.getSpouse2());
		assertEquals(0, rod.getAge());
		assertEquals(null, rod.getName());
	}

	public void testSatisfiedConstructorArgWithAutowire() throws Exception {
		InputStream is = getClass().getResourceAsStream("constructor-arg.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		ConstructorDependenciesBean rod = (ConstructorDependenciesBean) xbf.getBean("rod3");
		TestBean kerry = (TestBean) xbf.getBean("kerry2");
		LifecycleBean other = (LifecycleBean) xbf.getBean("other");
		// Should have been autowired
		assertEquals(kerry, rod.getSpouse1());
		assertEquals(kerry, rod.getSpouse2());
		assertEquals(other, rod.getOther());
		assertEquals(0, rod.getAge());
		assertEquals(null, rod.getName());
	}

	public void testSatisfiedConstructorArgWithAutowireAndMultipleConstructors() throws Exception {
		InputStream is = getClass().getResourceAsStream("constructor-arg.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		ConstructorDependenciesBean rod = (ConstructorDependenciesBean) xbf.getBean("rod4");
		TestBean kerry = (TestBean) xbf.getBean("kerry2");
		LifecycleBean other = (LifecycleBean) xbf.getBean("other");
		// Should have been autowired
		assertEquals(kerry, rod.getSpouse1());
		assertEquals(kerry, rod.getSpouse2());
		assertEquals(other, rod.getOther());
		assertEquals(0, rod.getAge());
		assertEquals(null, rod.getName());
	}

	public void testSatisfiedConstructorArgWithSimpleValuesAndGreedyConstructor() throws Exception {
		InputStream is = getClass().getResourceAsStream("constructor-arg.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		ConstructorDependenciesBean rod = (ConstructorDependenciesBean) xbf.getBean("rod5");
		TestBean kerry1 = (TestBean) xbf.getBean("kerry1");
		TestBean kerry2 = (TestBean) xbf.getBean("kerry2");
		LifecycleBean other = (LifecycleBean) xbf.getBean("other");
		// Should have been autowired
		assertEquals(kerry2, rod.getSpouse1());
		assertEquals(kerry1, rod.getSpouse2());
		assertEquals(other, rod.getOther());
		assertEquals(99, rod.getAge());
		assertEquals("myname", rod.getName());
	}

	public void testSatisfiedConstructorArgWithSimpleValuesAndHumbleConstructor() throws Exception {
		InputStream is = getClass().getResourceAsStream("constructor-arg.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		ConstructorDependenciesBean rod = (ConstructorDependenciesBean) xbf.getBean("rod6");
		TestBean kerry1 = (TestBean) xbf.getBean("kerry1");
		TestBean kerry2 = (TestBean) xbf.getBean("kerry2");
		LifecycleBean other = (LifecycleBean) xbf.getBean("other");
		// Should have been autowired
		assertEquals(kerry2, rod.getSpouse1());
		assertEquals(kerry1, rod.getSpouse2());
		assertEquals(other, rod.getOther());
		assertEquals(0, rod.getAge());
		assertEquals(null, rod.getName());
	}

	public void testThrowsExceptionOnTooManyArguments() throws Exception {
		InputStream is = getClass().getResourceAsStream("constructor-arg.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		try {
			ConstructorDependenciesBean rod = (ConstructorDependenciesBean) xbf.getBean("rod7");
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (BeanDefinitionStoreException ex) {
			// expected
		}
	}

	public void testThrowsExceptionOnAmbiguousResolution() throws Exception {
		InputStream is = getClass().getResourceAsStream("constructor-arg.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
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
			InputStream is = getClass().getResourceAsStream("invalid-factory.xml");
			XmlBeanFactory xbf = new XmlBeanFactory(is);
			fail("Should have thrown BeanDefinitionStoreException");
		}
		catch (BeanDefinitionStoreException ex) {
			// expected
		}
	}

	public void testDependsOn() {
		InputStream is = getClass().getResourceAsStream("initializers.xml");
		XmlBeanFactory xbf = new XmlBeanFactory(is);
		xbf.preInstantiateSingletons();
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


	public static class PreparingBean1 {

		public static boolean prepared = false;

		public PreparingBean1() {
			prepared = true;
		}
	}


	public static class PreparingBean2 {

		public static boolean prepared = false;

		public PreparingBean2() {
			prepared = true;
		}
	}


	public static class DependingBean {

		public DependingBean() {
			if (!(PreparingBean1.prepared && PreparingBean2.prepared)) {
				throw new IllegalStateException("Need prepared PreparedBeans!");
			}
		}
	}

}
