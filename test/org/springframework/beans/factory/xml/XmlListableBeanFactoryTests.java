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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.ITestBean;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.TestBean;
import org.springframework.beans.factory.AbstractListableBeanFactoryTests;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DummyFactory;
import org.springframework.beans.factory.LifecycleBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.ClassPathResource;

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
		this.factory = new XmlBeanFactory(new ClassPathResource("test.xml", getClass()), parent);
		this.factory.addBeanPostProcessor(new BeanPostProcessor() {
			public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
				if (bean instanceof TestBean) {
					((TestBean) bean).setPostProcessed(true);
				}
				if (bean instanceof DummyFactory) {
					((DummyFactory) bean).setPostProcessed(true);
				}
				return bean;
			}
			public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
				return bean;
			}
		});
		this.factory.addBeanPostProcessor(new LifecycleBean.PostProcessor());
		this.factory.preInstantiateSingletons();
	}

	protected BeanFactory getBeanFactory() {
		return factory;
	}

	public void testCount() {
		assertCount(14);
	}

	public void testTestBeanCount() {
		assertTestBeanCount(8);
	}

	public void testFactoryNesting() {
		ITestBean father = (ITestBean) getBeanFactory().getBean("father");
		assertTrue("Bean from root context", father != null);

		TestBean rod = (TestBean) getBeanFactory().getBean("rod");
		assertTrue("Bean from child context", "Rod".equals(rod.getName()));
		assertTrue("Bean has external reference", rod.getSpouse() == father);

		rod = (TestBean) parent.getBean("rod");
		assertTrue("Bean from root context", "Roderick".equals(rod.getName()));
	}

	public void testFactoryReferences() {
		DummyReferencer ref = (DummyReferencer) getBeanFactory().getBean("factoryReferencer");
		DummyFactory factory = (DummyFactory) getBeanFactory().getBean("&singletonFactory");
		assertTrue(ref.getTestBean1() == ref.getTestBean2());
		assertTrue(ref.getDummyFactory() == factory);
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

	public void testEmptyValues() {
		TestBean rod = (TestBean) getBeanFactory().getBean("rod");
		TestBean kerry = (TestBean) getBeanFactory().getBean("kerry");
		assertTrue("Touchy is empty", "".equals(rod.getTouchy()));
		assertTrue("Touchy is empty", "".equals(kerry.getTouchy()));
	}

	public void testCommentsAndCdataInValue() {
		TestBean bean = (TestBean) getBeanFactory().getBean("commentsInValue");
		assertEquals("Failed to handle comments and CDATA properly",
								 "this is a <!--comment-->", bean.getName());
	}

}
