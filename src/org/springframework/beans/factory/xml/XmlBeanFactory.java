/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory.xml;

import java.io.InputStream;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
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
 * @version $Id: XmlBeanFactory.java,v 1.20 2003-11-28 16:51:09 jhoeller Exp $
 */
public class XmlBeanFactory extends DefaultListableBeanFactory {

	private final DefaultXmlBeanDefinitionReader reader = new DefaultXmlBeanDefinitionReader(this);

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
	 */
	public XmlBeanFactory(InputStream is) throws BeansException {
		this(is, null);
	}

	/**
	 * Create a new XmlBeanFactory with the given input stream,
	 * which must be parsable using DOM.
	 * @param is InputStream containing XML
	 * @param parentBeanFactory parent bean factory
	 * @throws BeansException
	 */
	public XmlBeanFactory(InputStream is, BeanFactory parentBeanFactory) throws BeansException {
		super(parentBeanFactory);
		this.reader.loadBeanDefinitions(is);
	}

}
