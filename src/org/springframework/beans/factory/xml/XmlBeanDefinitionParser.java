package org.springframework.beans.factory.xml;

import org.w3c.dom.Document;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.io.Resource;

/**
 * Strategy interface for parsing XML bean definitions.
 * Used by XmlBeanDefinitionReader for actually parsing a DOM document.
 * @author Juergen Hoeller
 * @since 18.12.2003
 * @see XmlBeanDefinitionReader#setParserClass
 */
public interface XmlBeanDefinitionParser {

	/**
	 * Load bean definitions from the given DOM document,
	 * and register them with the given bean factory.
	 * @param beanFactory the bean factory to register the bean definitions with
	 * @param beanClassLoader class loader to use for bean classes
	 * @param doc the DOM document
	 * @param resource descriptor of the original XML resource
	 * (useful for displaying parse errors)
	 */
	public void loadBeanDefinitions(BeanDefinitionRegistry beanFactory, ClassLoader beanClassLoader,
	                                Document doc, Resource resource) throws BeansException;

}
