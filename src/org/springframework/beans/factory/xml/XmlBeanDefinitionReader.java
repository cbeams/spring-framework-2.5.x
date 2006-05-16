/*
 * Copyright 2002-2006 the original author or authors.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ComponentDefinition;
import org.springframework.beans.factory.support.NullSourceExtractor;
import org.springframework.beans.factory.support.ProblemReporter;
import org.springframework.beans.factory.support.ReaderEventListener;
import org.springframework.beans.factory.support.SourceExtractor;
import org.springframework.core.Constants;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.Assert;
import org.springframework.util.xml.SimpleSaxErrorHandler;

/**
 * Bean definition reader for XML bean definitions. Delegates the actual XML
 * parsing to an implementation of the XmlBeanDefinitionParser interface.
 * Typically applied to a DefaultListableBeanFactory.
 *
 * <p>This class loads a DOM document and applies the bean definition parser to it.
 * The parser will register each bean definition with the given bean factory,
 * relying on the latter's implementation of the BeanDefinitionRegistry interface.
 *
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 26.11.2003
 * @see #setParserClass
 * @see XmlBeanDefinitionParser
 * @see DefaultXmlBeanDefinitionParser
 * @see BeanDefinitionRegistry
 * @see org.springframework.beans.factory.support.DefaultListableBeanFactory
 */
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

	/**
	 * Indicates that the validation should be disabled.
	 */
	public static final int VALIDATION_NONE = 0;

	/**
	 * Indicates that the validation mode should be detected automatically.
	 */
	public static final int VALIDATION_AUTO = 1;

	/**
	 * Indicates that DTD validation should be used.
	 */
	public static final int VALIDATION_DTD = 2;
	/**
	 * Indicates that XSD validation should be used.
	 */
	public static final int VALIDATION_XSD = 3;

	/**
	 * JAXP attribute used to configure the schema language for validation.
	 */
	private static final String SCHEMA_LANGUAGE_ATTRIBUTE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

	/**
	 * JAXP attribute value indicating the XSD schema language.
	 */
	private static final String XSD_SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

	/**
	 * The maximum number of lines the validation autodetection process should peek into
	 * a file looking for the <code>DOCTYPE</code> definition.
	 */
	private static final int MAX_PEEK_LINES = 5;


	/**
	 * {@link Constants} instance for this class.
	 */
	private static final Constants constants = new Constants(XmlBeanDefinitionReader.class);

	/**
	 * Are namespaces important?
	 */
	private boolean namespaceAware = false;

	/**
	 * The current validation mode. Defaults to {@link #VALIDATION_AUTO}.
	 */
	private int validationMode = VALIDATION_AUTO;

	/**
	 * The {@link ErrorHandler} to use when XML parsing errors occur.
	 */
	private ErrorHandler errorHandler = new SimpleSaxErrorHandler(logger);

	/**
	 * The {@link EntityResolver} implementation to use.
	 */
	private EntityResolver entityResolver = null;

	/**
	 * The {@link XmlBeanDefinitionParser} <code>Class</code> to use for parsing. 
	 */
	private Class parserClass = DefaultXmlBeanDefinitionParser.class;

	/**
	 * The {@link ProblemReporter} used to report any errors or warnings during parsing.
	 */
	private ProblemReporter problemReporter = new FailFastProblemReporter();

	/**
	 * The {@link ReaderEventListener} that all component registration events should be sent to.
	 */
	private ReaderEventListener eventListener = new NullReaderEventListener();

	/**
	 * The {@link SourceExtractor} to use when extracting
	 * {@link org.springframework.beans.factory.config.BeanDefinition#getSource() source objects}
	 * from the configuration data.
	 */
	private SourceExtractor sourceExtractor = new NullSourceExtractor();

	/**
	 * The {@link NamespaceHandlerResolver} implementation passed to the {@link XmlBeanDefinitionParser}.
	 */
	private NamespaceHandlerResolver namespaceHandlerResolver;

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
			this.entityResolver = new DelegatingEntityResolver();
		}
	}


	/**
	 * Set whether or not the XML parser should be XML namespace aware.
	 * Default is "false".
	 */
	public void setNamespaceAware(boolean namespaceAware) {
		this.namespaceAware = namespaceAware;
	}

	/**
	 * Sets the validation mode to use. Defaults to {@link #VALIDATION_AUTO}.
	 */
	public void setValidationMode(int validationMode) {
		this.validationMode = validationMode;
	}

	/**
	 * Sets the validation mode to use by name. Defaults to {@link #VALIDATION_AUTO}.
	 */
	public void setValidationModeName(String validationModeName) {
		setValidationMode(constants.asNumber(validationModeName).intValue());
	}

	/**
	 * Specifies which {@link ProblemReporter} to use. Default implementation is
	 * {@link FailFastProblemReporter} which exhibits fail fast behaviour. External tools
	 * can provide an alternative implementation that collates errors and warnings for
	 * display in the tool UI.
	 */
	public void setProblemReporter(ProblemReporter problemReporter) {
		Assert.notNull(problemReporter, "'problemReporter' cannot be null.");
		this.problemReporter = problemReporter;
	}

	/**
	 * Specifies which {@link ReaderEventListener} to use. Default implementation is
	 * NullReaderEventListener which discards every event notification. External tools
	 * can provide an alternative implementation to monitor the components being registered
	 * in the BeanFactory.
	 */
	public void setEventListener(ReaderEventListener eventListener) {
		Assert.notNull(eventListener, "'eventListener' cannot be null.");
		this.eventListener = eventListener;
	}

	/**
	 * Specifies the {@link SourceExtractor} to use. The default implementation is
	 * {@link NullSourceExtractor} which simply returns <code>null</code> as the source object.
	 * This means that during normal runtime execution no additional source metadata is attached
	 * to the bean configuration metadata.
	 */
	public void setSourceExtractor(SourceExtractor sourceExtractor) {
		Assert.notNull(sourceExtractor, "'sourceExtractor' cannot be null.");
		this.sourceExtractor = sourceExtractor;
	}

	/**
	 * Specifies the {@link NamespaceHandlerResolver} to use. If none is specified a default
	 * instance will be created by {@link #createDefaultNamespaceHandlerResolver()}.
	 */
	public void setNamespaceHandlerResolver(NamespaceHandlerResolver namespaceHandlerResolver) {
		this.namespaceHandlerResolver = namespaceHandlerResolver;
	}

	/**
	 * Set an implementation of the <code>org.xml.sax.ErrorHandler</code>
	 * interface for custom handling of XML parsing errors and warnings.
	 * <p>If not set, a default SimpleSaxErrorHandler is used that simply
	 * logs warnings using the logger instance of the view class,
	 * and rethrows errors to discontinue the XML transformation.
	 * @see SimpleSaxErrorHandler
	 */
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	/**
	 * Set a SAX entity resolver to be used for parsing. By default,
	 * BeansDtdResolver will be used. Can be overridden for custom entity
	 * resolution, for example relative to some specific base path.
	 * @see BeansDtdResolver
	 */
	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	/**
	 * Set the XmlBeanDefinitionParser implementation to use,
	 * responsible for the actual parsing of XML bean definitions.
	 * Default is DefaultXmlBeanDefinitionParser.
	 *
	 * @param parserClass the desired XmlBeanDefinitionParser implementation class
	 * @throws IllegalArgumentException if the specified parserClass is
	 *                  either <code>null</code> or is not an implementation of the XmlBeanDefinitionParser interface.
	 * @see XmlBeanDefinitionParser
	 * @see DefaultXmlBeanDefinitionParser
	 */
	public void setParserClass(Class parserClass) {
		if (parserClass == null || !XmlBeanDefinitionParser.class.isAssignableFrom(parserClass)) {
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
		return loadBeanDefinitions(new EncodedResource(resource));
	}

	/**
	 * Load bean definitions from the specified XML file.
	 * @param encodedResource the resource descriptor for the XML file,
	 * allowing to specify an encoding to use for parsing the file
	 * @return the number of bean definitions found
	 * @throws BeanDefinitionStoreException in case of loading or parsing errors
	 */
	public int loadBeanDefinitions(EncodedResource encodedResource) throws BeansException {
		if (encodedResource == null) {
			throw new BeanDefinitionStoreException("Resource cannot be null: expected an XML file");
		}
		if (logger.isInfoEnabled()) {
			logger.info("Loading XML bean definitions from " + encodedResource.getResource());
		}

		try {
			InputStream inputStream = encodedResource.getResource().getInputStream();
			try {
				InputSource inputSource = new InputSource(inputStream);
				if (encodedResource.getEncoding() != null) {
					inputSource.setEncoding(encodedResource.getEncoding());
				}
				return doLoadBeanDefinitions(inputSource, encodedResource.getResource());
			}
			finally {
				inputStream.close();
			}
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException(
					"IOException parsing XML document from " + encodedResource.getResource(), ex);
		}
	}

	/**
	 * Load bean definitions from the specified XML file.
	 * @param inputSource the SAX InputSource to read from
	 * @return the number of bean definitions found
	 * @throws BeanDefinitionStoreException in case of loading or parsing errors
	 */
	public int loadBeanDefinitions(InputSource inputSource) throws BeansException {
		return loadBeanDefinitions(inputSource, "resource loaded through SAX InputSource");
	}

	/**
	 * Load bean definitions from the specified XML file.
	 * @param inputSource the SAX InputSource to read from
	 * @param resourceDescription a description of the resource
	 * (can be <code>null</code> or empty)
	 * @return the number of bean definitions found
	 * @throws BeanDefinitionStoreException in case of loading or parsing errors
	 */
	public int loadBeanDefinitions(InputSource inputSource, String resourceDescription) throws BeansException {
		return doLoadBeanDefinitions(inputSource, new DescriptiveResource(resourceDescription));
	}


	/**
	 * Actually load bean definitions from the specified XML file.
	 * @param inputSource the SAX InputSource to read from
	 * @param resource the resource descriptor for the XML file
	 * @return the number of bean definitions found
	 * @throws BeanDefinitionStoreException in case of loading or parsing errors
	 */
	protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource) throws BeansException {
		try {
			DocumentBuilderFactory factory =
					createDocumentBuilderFactory(resource, getValidationModeForResource(resource));
			if (logger.isDebugEnabled()) {
				logger.debug("Using JAXP provider [" + factory + "]");
			}
			DocumentBuilder builder = createDocumentBuilder(factory);
			Document doc = builder.parse(inputSource);
			return registerBeanDefinitions(doc, resource);
		}
		catch (ParserConfigurationException ex) {
			throw new BeanDefinitionStoreException(
					"Parser configuration exception parsing XML from " + resource, ex);
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
	}


	/**
	 * Gets the validation mode for the specified {@link Resource}. If no explicit
	 * validation mode has been configured then the validation mode is
	 * {@link #detectValidationMode detected}.
	 */
	private int getValidationModeForResource(Resource resource) {
		return (this.validationMode != VALIDATION_AUTO ? this.validationMode : detectValidationMode(resource));
	}

	/**
	 * Detects which kind of validation to perform on the XML file identified
	 * by the supplied {@link Resource}. If the
	 * file has a <code>DOCTYPE</code> definition then DTD validation is used
	 * otherwise XSD validation is assumed.
	 */
	protected int detectValidationMode(Resource resource) {
		if (resource.isOpen()) {
			throw new BeanDefinitionStoreException(
					"Passed-in Resource [" + resource + "] contains an open stream: " +
					"cannot determine validation mode automatically. Either pass in a Resource " +
					"that is able to create fresh streams, or explicitly specify the validationMode " +
					"on your XmlBeanDefinitionReader instance.");
		}

		// Peek into the file to look for DOCTYPE.
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
			boolean isDtdValidated = false;

			for (int x = 0; x < MAX_PEEK_LINES; x++) {
				String line = reader.readLine();
				if (line == null) {
					// End of stream...
					break;
				}
				else if (line.indexOf("DOCTYPE") > -1) {
					isDtdValidated = true;
					break;
				}
			}
			return (isDtdValidated ? VALIDATION_DTD : VALIDATION_XSD);
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException(
					"Unable to determine validation mode for [" + resource + "]: cannot open InputStream. " +
					"Did you attempt to load directly from a SAX InputSource without specifying the " +
					"validationMode on your XmlBeanDefinitionReader instance?", ex);
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException ex) {
					if (logger.isWarnEnabled()) {
						logger.warn("Unable to close BufferedReader for [" + resource + "].", ex);
					}
				}
			}
		}
	}

	/**
	 * Creates the {@link DocumentBuilderFactory} instance.
	 * @param resource the {@link Resource} being parsed.
	 * @param validationMode the resolved validation mode.
	 * Correctly reflects any mode that was detected automatically.
	 */
	protected DocumentBuilderFactory createDocumentBuilderFactory(Resource resource, int validationMode)
			throws ParserConfigurationException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(this.namespaceAware);

		if (validationMode != VALIDATION_NONE) {
			factory.setValidating(true);

			if (validationMode == VALIDATION_XSD) {
				// enforce namespace aware for XSD
				factory.setNamespaceAware(true);
				try {
					factory.setAttribute(SCHEMA_LANGUAGE_ATTRIBUTE, XSD_SCHEMA_LANGUAGE);
				}
				catch (IllegalArgumentException ex) {
					throw new FatalBeanException(
							"Unable to validate using XSD: Your JAXP provider [" +
							factory + "] does not support XML Schema. " +
							"Are you running on Java 1.4 or below with Apache Crimson? " +
							"Upgrade to Apache Xerces (or Java 1.5) for full XSD support.");
				}
			}
		}

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
		XmlBeanDefinitionParser parser = createXmlBeanDefinitionParser();
		int countBefore = this.getBeanFactory().getBeanDefinitionCount();
		parser.registerBeanDefinitions(doc, createReaderContext(resource));
		return this.getBeanFactory().getBeanDefinitionCount() - countBefore;
	}

	/**
	 * Creates the {@link XmlReaderContext} to pass over to the parser.
	 */
	protected XmlReaderContext createReaderContext(Resource resource) {
		NamespaceHandlerResolver resolver = (this.namespaceHandlerResolver == null ? createDefaultNamespaceHandlerResolver() : this.namespaceHandlerResolver);
		return new XmlReaderContext(this, resource, this.problemReporter, this.eventListener, this.sourceExtractor, resolver);
	}

	/**
	 * Creates the default implementation of {@link NamespaceHandlerResolver} used if none is specified.
	 * Default implementation returns an instance of {@link DefaultNamespaceHandlerResolver}.
	 */
	protected NamespaceHandlerResolver createDefaultNamespaceHandlerResolver() {
		ClassLoader classLoader = getBeanClassLoader();
		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}
		return new DefaultNamespaceHandlerResolver(classLoader);
	}

	protected XmlBeanDefinitionParser createXmlBeanDefinitionParser() {
		return (XmlBeanDefinitionParser) BeanUtils.instantiateClass(this.parserClass);
	}


	private static class NullReaderEventListener implements ReaderEventListener {

		public void componentRegistered(ComponentDefinition componentDefinition) {
			// no-op
		}

		public void aliasRegistered(String targetBeanName, String alias) {
			// no-op
		}

		public void importProcessed(String importedResource) {
			// no-op
		}
	}

}
