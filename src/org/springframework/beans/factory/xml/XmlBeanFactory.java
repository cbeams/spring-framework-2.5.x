/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory.xml;

import java.io.InputStream;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;

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
 * @version $Id: XmlBeanFactory.java,v 1.23 2003-12-30 00:15:37 jhoeller Exp $
 */
public class XmlBeanFactory extends DefaultListableBeanFactory {

	private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);

	public XmlBeanFactory(InputStream is) throws BeansException {
		this(new InputStreamResource(is, "(no description"));
	}

	/**
	 * Create a new XmlBeanFactory with the given resource,
	 * which must be parsable using DOM.
	 * @param resource XML resource to load bean definitions from
	 * @throws BeansException in case of loading or parsing errors
	 */
	public XmlBeanFactory(Resource resource) throws BeansException {
		this(resource, null);
	}
	
	/**
	 * Create a new XmlBeanFactory with the given input stream,
	 * which must be parsable using DOM.
	 * @param resource XML resource to load bean definitions from
	 * @param parentBeanFactory parent bean factory
	 * @throws BeansException in case of loading or parsing errors
	 */
	public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
		super(parentBeanFactory);
		this.reader.loadBeanDefinitions(resource);
	}

}
