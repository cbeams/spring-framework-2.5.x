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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.ReaderContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;

/**
 * Default implementation of the XmlBeanDefinitionParser interface.
 * Parses bean definitions according to the "spring-beans" DTD,
 * that is, Spring's default XML bean definition format.
 * <p/>
 * <p>The structure, elements and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary
 * to produce this format). <code>&lt;beans&gt;</code> doesn't need to be the root
 * element of the XML document: This class will parse all bean definition elements
 * in the XML file, not regarding the actual root element.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Erik Wiersma
 * @since 18.12.2003
 */
public class DefaultXmlBeanDefinitionParser implements XmlBeanDefinitionParser {

	public static final String BEANS_NAMESPACE_URI = "http://www.springframework.org/schema/beans";

	public static final String BEAN_NAME_DELIMITERS = ",; ";

	public static final String DEFAULT_LAZY_INIT_ATTRIBUTE = "default-lazy-init";
	public static final String DEFAULT_AUTOWIRE_ATTRIBUTE = "default-autowire";
	public static final String DEFAULT_DEPENDENCY_CHECK_ATTRIBUTE = "default-dependency-check";
	public static final String DEFAULT_INIT_METHOD_ATTRIBUTE = "default-init-method";
	public static final String DEFAULT_DESTROY_METHOD_ATTRIBUTE = "default-destroy-method";
	public static final String DEFAULT_MERGE_ATTRIBUTE = "default-merge";
	public static final String IMPORT_ELEMENT = "import";
	public static final String RESOURCE_ATTRIBUTE = "resource";
	public static final String ALIAS_ELEMENT = "alias";
	public static final String NAME_ATTRIBUTE = "name";
	public static final String ALIAS_ATTRIBUTE = "alias";
	public static final String BEAN_ELEMENT = "bean";


	protected final Log logger = LogFactory.getLog(getClass());

	private NamespaceHandlerResolver namespaceHandlerResolver;

	private ReaderContext readerContext;

	/**
	 * Parses bean definitions according to the "spring-beans" DTD.
	 * <p>Opens a DOM Document; then initializes the default settings
	 * specified at <code>&lt;beans&gt;</code> level; then parses
	 * the contained bean definitions.
	 */
	public int registerBeanDefinitions(Document doc, ReaderContext readerContext) {

		this.readerContext = readerContext;

		logger.debug("Loading bean definitions");
		Element root = doc.getDocumentElement();

		XmlBeanDefinitionParserHelper helper = new XmlBeanDefinitionParserHelper(readerContext);

		initDefaults(root, helper);

		preProcessXml(root);
		int beanDefinitionCount = parseBeanDefinitions(root, helper);
		if (logger.isDebugEnabled()) {
			logger.debug("Found [" + beanDefinitionCount + "] <bean> elements in [" + readerContext.getResource() + "].");
		}
		postProcessXml(root);

		return beanDefinitionCount;
	}

	/**
	 * Return the descriptor for the XML resource that this parser works on.
	 */
	protected final ReaderContext getReaderContext() {
		return this.readerContext;
	}

	/**
	 * Initialize the default lazy-init, autowire and dependency check settings passing them
	 * to the supplied {@link XmlBeanDefinitionParserHelper}.
	 */
	protected void initDefaults(Element root, XmlBeanDefinitionParserHelper helper) {
		helper.setDefaultLazyInit(root.getAttribute(DEFAULT_LAZY_INIT_ATTRIBUTE));
		helper.setDefaultAutowire(root.getAttribute(DEFAULT_AUTOWIRE_ATTRIBUTE));
		helper.setDefaultDependencyCheck(root.getAttribute(DEFAULT_DEPENDENCY_CHECK_ATTRIBUTE));
		if (root.hasAttribute(DEFAULT_INIT_METHOD_ATTRIBUTE)) {
			helper.setDefaultInitMethod(root.getAttribute(DEFAULT_INIT_METHOD_ATTRIBUTE));
		}
		if (root.hasAttribute(DEFAULT_DESTROY_METHOD_ATTRIBUTE)) {
			helper.setDefaultDestroyMethod(root.getAttribute(DEFAULT_DESTROY_METHOD_ATTRIBUTE));
		}
		helper.setDefaultMerge(root.getAttribute(DEFAULT_MERGE_ATTRIBUTE));
	}

	/**
	 * Allow the XML to be extensible by processing any custom element types first,
	 * before we start to process the bean definitions. This method is a natural
	 * extension point for any other custom pre-processing of the XML.
	 * <p>Default implementation is empty. Subclasses can override this method to
	 * convert custom elements into standard Spring bean definitions, for example.
	 * Implementors have access to the parser's bean definition reader and the
	 * underlying XML resource, through the corresponding accessors.
	 * @see #getReaderContext()
	 */
	protected void preProcessXml(Element root) {
	}

	/**
	 * Creates the {@link NamespaceHandlerResolver} used to resolve all {@link NamespaceHandler}
	 * implementations from their corresponding namespace.
	 */
	protected NamespaceHandlerResolver createNamespaceHandlerResolver() {
		ClassLoader classLoader = getReaderContext().getReader().getBeanClassLoader();
		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}
		return new DefaultNamespaceHandlerResolver(classLoader);
	}

	/**
	 * Parse the elements at the root level in the document:
	 * "import", "alias", "bean".
	 * @param root the DOM root element of the document
	 * @return the number of bean definitions found
	 */
	protected int parseBeanDefinitions(Element root, XmlBeanDefinitionParserHelper helper) {
		NodeList nl = root.getChildNodes();
		int beanDefinitionCount = 0;
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				Element ele = (Element) node;
				String namespaceUri = ele.getNamespaceURI();
				if (isDefaultNamespace(namespaceUri)) {
					beanDefinitionCount += parseDefaultElement(ele, helper);
				}
				else {
					beanDefinitionCount += parseCustomElement(ele);
				}
			}
		}
		return beanDefinitionCount;
	}

	private boolean isDefaultNamespace(String namespaceUri) {
		return (!StringUtils.hasLength(namespaceUri) || BEANS_NAMESPACE_URI.equals(namespaceUri));
	}

	private int parseDefaultElement(Element ele, XmlBeanDefinitionParserHelper helper) {
		if (IMPORT_ELEMENT.equals(ele.getNodeName())) {
			importBeanDefinitionResource(ele);
			return 0;
		}
		else if (ALIAS_ELEMENT.equals(ele.getNodeName())) {
			String name = ele.getAttribute(NAME_ATTRIBUTE);
			String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
			getReaderContext().getReader().getBeanFactory().registerAlias(name, alias);
			return 0;
		}
		else if (BEAN_ELEMENT.equals(ele.getNodeName())) {
			BeanDefinitionHolder bdHolder = helper.parseBeanDefinitionElement(ele, false);
			if (bdHolder != null) {
				return decorateAndRegisterBeanDefinition(ele, bdHolder);
			}
			return 0;
		}
		else {
			// non-important element
			return 0;
		}
	}

	private NamespaceHandlerResolver getNamespaceHandlerResolver() {
		if (this.namespaceHandlerResolver == null) {
			this.namespaceHandlerResolver = createNamespaceHandlerResolver();
		}
		return this.namespaceHandlerResolver;
	}

	protected int parseCustomElement(Element ele) {
		String namespaceUri = ele.getNamespaceURI();
		NamespaceHandler handler = getNamespaceHandlerResolver().resolve(namespaceUri);

		if (handler == null) {
			getReaderContext().error("Unable to locate NamespaceHandler for namespace [" + namespaceUri + "].", ele);
			return 0;
		}

		int countBefore = getBeanDefinitionCount();
		BeanDefinitionParser parser = handler.findParserForElement(ele);
		parser.parse(ele, getReaderContext().getReader().getBeanFactory());
		return (getBeanDefinitionCount() - countBefore);
	}

	private int decorateAndRegisterBeanDefinition(Element element, BeanDefinitionHolder definitionHolder) {
		int registeredCount = 1;
		BeanDefinitionHolder finalDefinition = definitionHolder;

		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			String uri = node.getNamespaceURI();
			if (node.getNodeType() == Node.ELEMENT_NODE && !isDefaultNamespace(uri)) {
				Element childElement = (Element) node;
				// A node from a namespace outside of the standard - should map to a decorator.
				NamespaceHandler handler = getNamespaceHandlerResolver().resolve(uri);
				BeanDefinitionDecorator decorator = handler.findDecoratorForElement(childElement);
				int countBefore = getBeanDefinitionCount();
				finalDefinition = decorator.decorate(childElement, finalDefinition, getReaderContext().getReader().getBeanFactory());
				registeredCount += (getBeanDefinitionCount() - countBefore);
			}
		}
		// Register the final decorated instance.
		BeanDefinitionReaderUtils.registerBeanDefinition(finalDefinition, getReaderContext().getReader().getBeanFactory());
		return registeredCount;
	}

	private int getBeanDefinitionCount() {
		return getReaderContext().getReader().getBeanFactory().getBeanDefinitionCount();
	}

	/**
	 * Parse an "import" element and load the bean definitions
	 * from the given resource into the bean factory.
	 */
	protected void importBeanDefinitionResource(Element ele) {
		String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
		// Resolve system properties: e.g. "${user.dir}"
		location = SystemPropertyUtils.resolvePlaceholders(location);

		if (ResourcePatternUtils.isUrl(location)) {
			int importCount = getReaderContext().getReader().loadBeanDefinitions(location);
			if (logger.isDebugEnabled()) {
				logger.debug("Imported " + importCount + " bean definitions from URL location [" + location + "]");
			}
		}
		else {
			// No URL -> considering resource location as relative to the current file.
			try {
				Resource relativeResource = getReaderContext().getResource().createRelative(location);
				int importCount = getReaderContext().getReader().loadBeanDefinitions(relativeResource);
				if (logger.isDebugEnabled()) {
					logger.debug("Imported " + importCount + " bean definitions from relative location [" + location + "]");
				}
			}
			catch (IOException ex) {
				getReaderContext().error("Invalid relative resource location [" + location + "] to import bean definitions from", ele, null, ex);
			}
		}
	}

	/**
	 * Allow the XML to be extensible by processing any custom element types last,
	 * after we finished processing the bean definitions. This method is a natural
	 * extension point for any other custom post-processing of the XML.
	 * <p>Default implementation is empty. Subclasses can override this method to
	 * convert custom elements into standard Spring bean definitions, for example.
	 * Implementors have access to the parser's bean definition reader and the
	 * underlying XML resource, through the corresponding accessors.
	 * @see #getReaderContext()
	 */
	protected void postProcessXml(Element root) {
	}

}
