package org.springframework.aop.config;

import junit.framework.TestCase;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Rob Harrop
 */
public class TopLevelAopTagTests extends TestCase {

	public void testParse() throws Exception {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
		reader.loadBeanDefinitions(new ClassPathResource("topLevelAop.xml", getClass()));

		assertTrue(beanFactory.containsBeanDefinition("testPointcut"));
	}
}
