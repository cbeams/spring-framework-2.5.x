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

import java.io.InputStream;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

/**
 * Convenience extension of DefaultListableBeanFactory that reads bean definitions
 * from an XML document. Delegates to XmlBeanDefinitionReader underneath; effectively
 * equivalent to using an XmlBeanDefinitionReader with a DefaultListableBeanFactory.
 *
 * <p>The structure, element and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary
 * to produce this format). "beans" doesn't need to be the root element of the XML
 * document: This class will parse all bean definition elements in the XML file.
 *
 * <p>This class registers each bean definition with the DefaultListableBeanFactory
 * superclass, and relies on the latter's implementation of the BeanFactory interface.
 * It supports singletons, prototypes, and references to either of these kinds of bean.
 * See "spring-beans.dtd" for details on options and configuration style.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 15 April 2001
 */
public class XmlBeanFactory extends DefaultListableBeanFactory {

	private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);

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
	 * Create a new XmlBeanFactory with the given InputStream,
	 * which must be parsable using DOM.
	 * <p>It's preferable to use a Resource argument instead of an
	 * InputStream, to retain location information. This constructor
	 * is mainly kept for backwards compatibility.
	 * @param is XML InputStream to load bean definitions from
	 * @throws BeansException in case of loading or parsing errors
	 * @see #XmlBeanFactory(Resource)
	 */
	public XmlBeanFactory(InputStream is) throws BeansException {
		this(new InputStreamResource(is), null);
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
