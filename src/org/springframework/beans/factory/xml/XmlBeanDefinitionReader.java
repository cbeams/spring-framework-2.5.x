/*
 * Copyright 2002-2005 the original author or authors.
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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.io.Resource;
import org.springframework.util.xml.SimpleSaxErrorHandler;

/**
 * Bean definition reader for XML bean definitions. Delegates the actual XML
 * parsing to an implementation of the XmlBeanDefinitionParser interface.
 * Typically applied to a DefaultListableBeanFactory.
 *
 * <p>This class loads a DOM document and applies the bean definition parser to it.
 * The parser will register each bean definition with the given bean factory,
 * relying on the latter's implementation of the BeanDefinitionRegistry interface.

 * @author Juergen Hoeller
 * @since 26.11.2003
 * @see #setParserClass
 * @see XmlBeanDefinitionParser
 * @see DefaultXmlBeanDefinitionParser
 * @see org.springframework.beans.factory.support.BeanDefinitionRegistry
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 */
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

	private boolean validating = true;

	private boolean namespaceAware = false;

	private ErrorHandler errorHandler = new SimpleSaxErrorHandler(logger);

	private EntityResolver entityResolver = null;

	private Class parserClass = DefaultXmlBeanDefinitionParser.class;


	/**
	 * Create new XmlBeanDefinitionReader for the given bean factory.
	 */
	public XmlBeanDefinitionReader(BeanDefinitionRegistry beanFactory) {
		super(beanFactory);

		// Determine EntityResolver to use.
		if (getResourceLoader() != null) {
			this.entityResolver = new ResourceEntityResolver(getResourceLoader());
		}
		else {
			this.entityResolver = new BeansDtdResolver();
		}
	}

	/**
	 * Set if the XML parser should validate the document and thus enforce a DTD.
	 * Default is "true".
	 */
	public void setValidating(boolean validating) {
		this.validating = validating;
	}

	/**
	 * Set whether or not the XML parser should be XML namespace aware.
	 * Default is "false".
	 */
	public void setNamespaceAware(boolean namespaceAware) {
		this.namespaceAware = namespaceAware;
	}

	/**
	 * Set an implementation of the <code>org.xml.sax.ErrorHandler</code>
	 * interface for custom handling of XML parsing errors and warnings.
	 * <p>If not set, a default SimpleSaxErrorHandler is used that simply
	 * logs warnings using the logger instance of the view class,
	 * and rethrows errors to discontinue the XML transformation.
	 * @see org.springframework.util.xml.SimpleSaxErrorHandler
	 */
 	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	/**
	 * Set a SAX entity resolver to be used for parsing. By default,
	 * BeansDtdResolver will be used. Can be overridden for custom entity
	 * resolution, for example relative to some specific base path.
	 * @see org.springframework.beans.factory.xml.BeansDtdResolver
	 */
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	/**
	 * Set the XmlBeanDefinitionParser implementation to use,
	 * responsible for the actual parsing of XML bean definitions.
	 * Default is DefaultXmlBeanDefinitionParser.
	 * @see XmlBeanDefinitionParser
	 * @see DefaultXmlBeanDefinitionParser
	 */
	public void setParserClass(Class parserClass) {
		if (this.parserClass == null || !XmlBeanDefinitionParser.class.isAssignableFrom(parserClass)) {
			throw new IllegalArgumentException("parserClass must be an XmlBeanDefinitionParser");
		}
		this.parserClass = parserClass;
	}


	/**
	 * Load bean definitions from the specified XML file.
	 * @param resource the resource descriptor for the XML file
	 * @return the number of bean definitions found
	 * @throws BeanDefinitionStoreException in case of loading or parsing errors
	 */
	public int loadBeanDefinitions(Resource resource) throws BeansException {
		if (resource == null) {
			throw new BeanDefinitionStoreException("resource cannot be <code>null</code>: expected an XML file");
		}

		InputStream is = null;
		try {
			DocumentBuilderFactory factory = createDocumentBuilderFactory();
			if (logger.isDebugEnabled()) {
				logger.debug("Using JAXP implementation [" + factory + "]");
			}
			DocumentBuilder builder = createDocumentBuilder(factory);

			if (logger.isInfoEnabled()) {
				logger.info("Loading XML bean definitions from " + resource + "");
			}
			is = resource.getInputStream();
			Document doc = builder.parse(is);

			return registerBeanDefinitions(doc, resource);
		}
		catch (ParserConfigurationException ex) {
			throw new BeanDefinitionStoreException("Parser configuration exception parsing XML from " + resource, ex);
		}
		catch (SAXParseException ex) {
			throw new BeanDefinitionStoreException(
			    "Line " + ex.getLineNumber() + " in XML document from " + resource + " is invalid", ex);
		}
		catch (SAXException ex) {
			throw new BeanDefinitionStoreException("XML document from " + resource + " is invalid", ex);
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("IOException parsing XML document from " + resource, ex);
		}
		finally {
			if (is != null) {
				try {
					is.close();
				}
				catch (IOException ex) {
					logger.warn("Could not close InputStream", ex);
				}
			}
		}
	}

	/**
	 * Create a JAXP DocumentBuilderFactory that this bean definition reader
	 * will use for parsing XML documents. Can be overridden in subclasses,
	 * adding further initialization of the factory.
	 * @return the JAXP DocumentBuilderFactory
	 * @throws ParserConfigurationException if thrown by JAXP methods
	 */
	protected DocumentBuilderFactory createDocumentBuilderFactory()
			throws ParserConfigurationException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(this.validating);
		factory.setNamespaceAware(this.namespaceAware);
		return factory;
	}

	/**
	 * Create a JAXP DocumentBuilder that this bean definition reader
	 * will use for parsing XML documents. Can be overridden in subclasses,
	 * adding further initialization of the builder.
	 * @param factory the JAXP DocumentBuilderFactory that the
	 * DocumentBuilder should be created with
	 * @return the JAXP DocumentBuilder
	 * @throws ParserConfigurationException if thrown by JAXP methods
	 */
	protected DocumentBuilder createDocumentBuilder(DocumentBuilderFactory factory)
			throws ParserConfigurationException {

		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		if (this.errorHandler != null) {
			docBuilder.setErrorHandler(this.errorHandler);
		}
		if (this.entityResolver != null) {
			docBuilder.setEntityResolver(this.entityResolver);
		}
		return docBuilder;
	}


	/**
	 * Register the bean definitions contained in the given DOM document.
	 * Called by <code>loadBeanDefinitions</code>.
	 * <p>Creates a new instance of the parser class and invokes
	 * <code>registerBeanDefinitions</code> on it.
	 * @param doc the DOM document
	 * @param resource the resource descriptor (for context information)
	 * @return the number of bean definitions found
	 * @throws BeansException in case of parser instantiation failure
	 * @throws BeanDefinitionStoreException in case of parsing errors
	 * @see #loadBeanDefinitions
	 * @see #setParserClass
	 * @see XmlBeanDefinitionParser#registerBeanDefinitions
	 */
	public int registerBeanDefinitions(Document doc, Resource resource) throws BeansException {
		XmlBeanDefinitionParser parser =
				(XmlBeanDefinitionParser) BeanUtils.instantiateClass(this.parserClass);
		return parser.registerBeanDefinitions(this, doc, resource);
	}

}
