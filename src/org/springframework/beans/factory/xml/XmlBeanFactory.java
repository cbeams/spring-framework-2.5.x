/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory.xml;

import java.io.InputStream;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistryLocation;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 * Convenience extension of DefaultListableBeanFactory that reads bean definitions from
 * an XML document. Delegates to DefaultXmlBeanDefinitionReader underneath; effectively
 * equivalent to using a DefaultXmlBeanDefinitionReader for a DefaultListableBeanFactory.
 *
 * <p>The structure, element and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary
 * to produce this format). "beans" doesn't need to be the root element of the XML
 * document: This class will parse all bean definition elements in the XML file.
 *
 * <p>This class registers each bean definition with the DefaultListableBeanFactory
 * superclass, and relies on the latter's implementation of the BeanFactory
 * interface. It supports singletons, prototypes and references to either of
 * these kinds of bean.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 15 April 2001
 * @version $Id: XmlBeanFactory.java,v 1.22 2003-12-19 15:49:58 johnsonr Exp $
 */
public class XmlBeanFactory extends DefaultListableBeanFactory {

	private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);

	/**
	 * Create new XmlBeanFactory using java.io to read the XML document
	 * with the given file name.
	 * @param fileName name of the file containing the XML document
	 */
	public XmlBeanFactory(String fileName) throws BeansException {
		this(fileName, null);
	}

	/**
	 * Create new XmlBeanFactory using java.io to read the XML document
	 * with the given file name.
	 * @param fileName name of the file containing the XML document
	 * @param parentBeanFactory parent bean factory
	 */
	public XmlBeanFactory(String fileName, BeanFactory parentBeanFactory) throws BeansException {
		super(parentBeanFactory);
		this.reader.loadBeanDefinitions(fileName);
	}

	/**
	 * Create a new XmlBeanFactory with the given input stream,
	 * which must be parsable using DOM.
	 * @param is InputStream containing XML
	 * @throws BeansException
	 * @param location location of the resource
	 */
	public XmlBeanFactory(InputStream is, BeanDefinitionRegistryLocation location) throws BeansException {
		this(is, null, location);
	}
	
	/**
	 * Convenient method for testing: doesn't require location info
	 */
	XmlBeanFactory(InputStream is) {
		this(is, null, null);
	}

	/**
	 * Create a new XmlBeanFactory with the given input stream,
	 * which must be parsable using DOM.
	 * @param is InputStream containing XML
	 * @param parentBeanFactory parent bean factory
	 * @param location location information, for display on parse errors
	 * @throws BeansException
	 */
	public XmlBeanFactory(InputStream is, BeanFactory parentBeanFactory, BeanDefinitionRegistryLocation location) throws BeansException {
		super(parentBeanFactory);
		this.reader.loadBeanDefinitions(is, location);
	}

}
