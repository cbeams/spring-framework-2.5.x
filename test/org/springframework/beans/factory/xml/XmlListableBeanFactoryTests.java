package org.springframework.beans.factory.xml;

import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;

import org.springframework.beans.factory.AbstractListableBeanFactoryTests;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.ListableBeanFactoryImpl;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.TestBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.ITestBean;

/**
 * @author Juergen Hoeller
 * @since 09.11.2003
 */
public class XmlListableBeanFactoryTests extends AbstractListableBeanFactoryTests {

	private ListableBeanFactoryImpl parent;

	private XmlBeanFactory factory;

	protected void setUp() {
		parent = new ListableBeanFactoryImpl();
		Map m = new HashMap();
		m.put("name", "Albert");
		parent.registerBeanDefinition("father",
			new RootBeanDefinition(TestBean.class, new MutablePropertyValues(m)));
		m = new HashMap();
		m.put("name", "Roderick");
		parent.registerBeanDefinition("rod",
			new RootBeanDefinition(TestBean.class, new MutablePropertyValues(m)));

		// Load from classpath, NOT a file path
		InputStream is = getClass().getResourceAsStream("test.xml");
		this.factory = new XmlBeanFactory(is, parent);
		this.factory.preInstantiateSingletons();
	}

	protected BeanFactory getBeanFactory() {
		return factory;
	}

	public void testFactoryNesting() {
		ITestBean father = (ITestBean) getBeanFactory().getBean("father");
		assertTrue("Bean from root context", father != null);

		ITestBean rod = (ITestBean) getBeanFactory().getBean("rod");
		assertTrue("Bean from child context", "Rod".equals(rod.getName()));
		assertTrue("Bean has external reference", rod.getSpouse() == father);

		rod = (ITestBean) parent.getBean("rod");
		assertTrue("Bean from root context", "Roderick".equals(rod.getName()));
	}

	public void testFactoryReferences() {
		DummyReferencer ref = (DummyReferencer) getBeanFactory().getBean("factoryReferencer");
		assertTrue(ref.getTestBean1() == ref.getTestBean2());
	}

	public void testPrototypeReferences() {
		// check that not broken by circular reference resolution mechanism
		DummyReferencer ref1 = (DummyReferencer) getBeanFactory().getBean("prototypeReferencer");
		assertTrue("Not referencing same bean twice", ref1.getTestBean1() != ref1.getTestBean2());
		DummyReferencer ref2 = (DummyReferencer) getBeanFactory().getBean("prototypeReferencer");
		assertTrue("Not the same referencer", ref1 != ref2);
		assertTrue("Not referencing same bean twice", ref2.getTestBean1() != ref2.getTestBean2());
		assertTrue("Not referencing same bean twice", ref1.getTestBean1() != ref2.getTestBean1());
		assertTrue("Not referencing same bean twice", ref1.getTestBean2() != ref2.getTestBean2());
		assertTrue("Not referencing same bean twice", ref1.getTestBean1() != ref2.getTestBean2());
	}

}
