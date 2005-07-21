package org.springframework.beans.factory.xml;

import junit.framework.TestCase;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Rob Harrop
 */
public class BeanNameGenerationTests extends TestCase {

	private DefaultListableBeanFactory beanFactory;

	public void setUp() {
		this.beanFactory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this.beanFactory);
		reader.setValidating(false);
		reader.loadBeanDefinitions(new ClassPathResource("beanNameGeneration.xml", getClass()));
	}

	public void testNaming() {
		String className = GeneratedNameBean.class.getName();

		GeneratedNameBean topLevel1 = (GeneratedNameBean) beanFactory.getBean(className);
		assertNotNull(topLevel1);

		String targetName = className + BeanDefinitionReaderUtils.GENERATED_BEAN_NAME_SEPARATOR + 1;
		GeneratedNameBean topLevel2 = (GeneratedNameBean) beanFactory.getBean(targetName);
		assertNotNull(topLevel2);

		GeneratedNameBean child1 = topLevel1.getChild();
		assertNotNull(child1.getBeanName());
		assertTrue(child1.getBeanName().startsWith(className));

		GeneratedNameBean child2 = topLevel2.getChild();
		assertNotNull(child2.getBeanName());
		assertTrue(child2.getBeanName().startsWith(className));

		assertFalse(child1.getBeanName().equals(child2.getBeanName()));
	}
}
