package org.springframework.beans.factory.xml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.ITestBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.AbstractListableBeanFactoryTests;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DummyFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.ClasspathBeanDefinitionRegistryLocation;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author Juergen Hoeller
 * @since 09.11.2003
 */
public class XmlListableBeanFactoryTests extends AbstractListableBeanFactoryTests {

	private DefaultListableBeanFactory parent;

	private XmlBeanFactory factory;

	protected void setUp() {
		parent = new DefaultListableBeanFactory();
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
		this.factory = new XmlBeanFactory(is, parent, new ClasspathBeanDefinitionRegistryLocation("test.xml"));
		this.factory.addBeanPostProcessor(new BeanPostProcessor() {
			public Object postProcessBean(Object bean, String name) throws BeansException {
				if (bean instanceof TestBean) {
					((TestBean) bean).setPostProcessed(true);
				}
				if (bean instanceof DummyFactory) {
					((DummyFactory) bean).setPostProcessed(true);
				}
				return bean;
			}
		});
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

	public void testBeanPostProcessor() throws Exception {
		TestBean kerry = (TestBean) getBeanFactory().getBean("kerry");
		TestBean kathy = (TestBean) getBeanFactory().getBean("kathy");
		DummyFactory factory = (DummyFactory) getBeanFactory().getBean("&singletonFactory");
		TestBean factoryCreated = (TestBean) getBeanFactory().getBean("singletonFactory");
		assertTrue(kerry.isPostProcessed());
		assertTrue(kathy.isPostProcessed());
		assertTrue(factory.isPostProcessed());
		assertTrue(factoryCreated.isPostProcessed());
	}

}
