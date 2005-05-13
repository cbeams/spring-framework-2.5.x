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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.support.MethodOverrides;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;

/**
 * Default implementation of the XmlBeanDefinitionParser interface.
 * Parses bean definitions according to the "spring-beans" DTD,
 * i.e. Spring's default XML bean definition format.
 *
 * <p>The structure, elements and attribute names of the required XML document
 * are hard-coded in this class. (Of course a transform could be run if necessary
 * to produce this format). "beans" doesn't need to be the root element of the XML
 * document: This class will parse all bean definition elements in the XML file.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 18.12.2003
 */
public class DefaultXmlBeanDefinitionParser implements XmlBeanDefinitionParser {

	public static final String BEAN_NAME_DELIMITERS = ",; ";

	/**
	 * Value of a T/F attribute that represents true.
	 * Anything else represents false. Case seNsItive.
	 */
	public static final String TRUE_VALUE = "true";
	public static final String DEFAULT_VALUE = "default";
	public static final String DESCRIPTION_ELEMENT = "description";

	public static final String AUTOWIRE_BY_NAME_VALUE = "byName";
	public static final String AUTOWIRE_BY_TYPE_VALUE = "byType";
	public static final String AUTOWIRE_CONSTRUCTOR_VALUE = "constructor";
	public static final String AUTOWIRE_AUTODETECT_VALUE = "autodetect";

	public static final String DEPENDENCY_CHECK_ALL_ATTRIBUTE_VALUE = "all";
	public static final String DEPENDENCY_CHECK_SIMPLE_ATTRIBUTE_VALUE = "simple";
	public static final String DEPENDENCY_CHECK_OBJECTS_ATTRIBUTE_VALUE = "objects";

	public static final String DEFAULT_LAZY_INIT_ATTRIBUTE = "default-lazy-init";
	public static final String DEFAULT_DEPENDENCY_CHECK_ATTRIBUTE = "default-dependency-check";
	public static final String DEFAULT_AUTOWIRE_ATTRIBUTE = "default-autowire";

	public static final String IMPORT_ELEMENT = "import";
	public static final String RESOURCE_ATTRIBUTE = "resource";

	public static final String ALIAS_ELEMENT = "alias";
	public static final String NAME_ATTRIBUTE = "name";
	public static final String ALIAS_ATTRIBUTE = "alias";

	public static final String BEAN_ELEMENT = "bean";
	public static final String ID_ATTRIBUTE = "id";
	public static final String PARENT_ATTRIBUTE = "parent";

	public static final String CLASS_ATTRIBUTE = "class";
	public static final String ABSTRACT_ATTRIBUTE = "abstract";
	public static final String SINGLETON_ATTRIBUTE = "singleton";
	public static final String LAZY_INIT_ATTRIBUTE = "lazy-init";
	public static final String AUTOWIRE_ATTRIBUTE = "autowire";
	public static final String DEPENDENCY_CHECK_ATTRIBUTE = "dependency-check";
	public static final String DEPENDS_ON_ATTRIBUTE = "depends-on";
	public static final String INIT_METHOD_ATTRIBUTE = "init-method";
	public static final String DESTROY_METHOD_ATTRIBUTE = "destroy-method";
	public static final String FACTORY_METHOD_ATTRIBUTE = "factory-method";
	public static final String FACTORY_BEAN_ATTRIBUTE = "factory-bean";

	public static final String CONSTRUCTOR_ARG_ELEMENT = "constructor-arg";
	public static final String INDEX_ATTRIBUTE = "index";
	public static final String TYPE_ATTRIBUTE = "type";
	public static final String PROPERTY_ELEMENT = "property";
	public static final String REF_ATTRIBUTE = "ref";
	public static final String VALUE_ATTRIBUTE = "value";
	public static final String LOOKUP_METHOD_ELEMENT = "lookup-method";

	public static final String REPLACED_METHOD_ELEMENT = "replaced-method";
	public static final String REPLACER_ATTRIBUTE = "replacer";
	public static final String ARG_TYPE_ELEMENT = "arg-type";
	public static final String ARG_TYPE_MATCH_ATTRIBUTE = "match";

	public static final String REF_ELEMENT = "ref";
	public static final String IDREF_ELEMENT = "idref";
	public static final String BEAN_REF_ATTRIBUTE = "bean";
	public static final String LOCAL_REF_ATTRIBUTE = "local";
	public static final String PARENT_REF_ATTRIBUTE = "parent";

	public static final String VALUE_ELEMENT = "value";
	public static final String NULL_ELEMENT = "null";
	public static final String LIST_ELEMENT = "list";
	public static final String SET_ELEMENT = "set";
	public static final String MAP_ELEMENT = "map";
	public static final String ENTRY_ELEMENT = "entry";
	public static final String KEY_ELEMENT = "key";
	public static final String KEY_ATTRIBUTE = "key";
	public static final String KEY_REF_ATTRIBUTE = "key-ref";
	public static final String VALUE_REF_ATTRIBUTE = "value-ref";
	public static final String PROPS_ELEMENT = "props";
	public static final String PROP_ELEMENT = "prop";


	protected final Log logger = LogFactory.getLog(getClass());

	private BeanDefinitionReader beanDefinitionReader;

	private Resource resource;

	private String defaultLazyInit;

	private String defaultDependencyCheck;

	private String defaultAutowire;


	public int registerBeanDefinitions(BeanDefinitionReader reader, Document doc, Resource resource)
			throws BeanDefinitionStoreException {

		this.beanDefinitionReader = reader;
		this.resource = resource;

		logger.debug("Loading bean definitions");
		Element root = doc.getDocumentElement();

		this.defaultLazyInit = root.getAttribute(DEFAULT_LAZY_INIT_ATTRIBUTE);
		this.defaultDependencyCheck = root.getAttribute(DEFAULT_DEPENDENCY_CHECK_ATTRIBUTE);
		this.defaultAutowire = root.getAttribute(DEFAULT_AUTOWIRE_ATTRIBUTE);
		if (logger.isDebugEnabled()) {
			logger.debug("Default lazy init '" + this.defaultLazyInit + "'");
			logger.debug("Default dependency check '" + this.defaultDependencyCheck + "'");
			logger.debug("Default autowire '" + this.defaultAutowire + "'");
		}

		int beanDefinitionCount = parseBeanDefinitions(root);
		if (logger.isDebugEnabled()) {
			logger.debug("Found " + beanDefinitionCount + " <bean> elements defining beans");
		}
		return beanDefinitionCount;
	}

	protected final BeanDefinitionReader getBeanDefinitionReader() {
		return beanDefinitionReader;
	}

	protected final Resource getResource() {
		return resource;
	}

	protected final String getDefaultLazyInit() {
		return defaultLazyInit;
	}

	protected final String getDefaultDependencyCheck() {
		return defaultDependencyCheck;
	}

	protected final String getDefaultAutowire() {
		return defaultAutowire;
	}


	/**
	 * Parse the elements at the root level in the document:
	 * "import", "alias", "bean".
	 * @param root the DOM root element of the document
	 * @return the number of bean definitions found
	 */
	protected int parseBeanDefinitions(Element root) throws BeanDefinitionStoreException {
		NodeList nl = root.getChildNodes();
		int beanDefinitionCounter = 0;
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				Element ele = (Element) node;
				if (IMPORT_ELEMENT.equals(node.getNodeName())) {
					importBeanDefinitionResource(ele);
				}
				else if (ALIAS_ELEMENT.equals(node.getNodeName())) {
					String name = ele.getAttribute(NAME_ATTRIBUTE);
					String alias = ele.getAttribute(ALIAS_ATTRIBUTE);
					this.beanDefinitionReader.getBeanFactory().registerAlias(name, alias);
				}
				else if (BEAN_ELEMENT.equals(node.getNodeName())) {
					beanDefinitionCounter++;
					BeanDefinitionHolder bdHolder = parseBeanDefinitionElement(ele);
					BeanDefinitionReaderUtils.registerBeanDefinition(
							bdHolder, this.beanDefinitionReader.getBeanFactory());
				}
			}
		}
		return beanDefinitionCounter;
	}

	/**
	 * Parse an "import" element and load the bean definitions
	 * from the given resource into the bean factory.
	 */
	protected void importBeanDefinitionResource(Element ele) throws BeanDefinitionStoreException {
		String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
		Resource relativeResource = null;
		if (ResourceUtils.isUrl(location)) {
			ResourceLoader resourceLoader = this.beanDefinitionReader.getResourceLoader();
			if (resourceLoader == null) {
				throw new BeanDefinitionStoreException(
						"Cannot import bean definitions from location [" + location + "]: no resource loader available");
			}
			relativeResource = resourceLoader.getResource(location);
		}
		else {
			try {
				relativeResource = this.resource.createRelative(location);
			}
			catch (IOException ex) {
				throw new BeanDefinitionStoreException(
						"Invalid relative resource location [" + location + "] to import bean definitions from", ex);
			}
		}
		this.beanDefinitionReader.loadBeanDefinitions(relativeResource);
	}

	/**
	 * Parse a standard bean definition into a BeanDefinitionHolder,
	 * including bean name and aliases.
	 * <p>Bean elements specify their canonical name as "id" attribute
	 * and their aliases as a delimited "name" attribute.
	 * <p>If no "id" specified, uses the first name in the "name" attribute
	 * as canonical name, registering all others as aliases.
	 */
	protected BeanDefinitionHolder parseBeanDefinitionElement(Element ele) throws BeanDefinitionStoreException {
		String id = ele.getAttribute(ID_ATTRIBUTE);
		String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);

		List aliases = new ArrayList();
		if (StringUtils.hasLength(nameAttr)) {
			String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr, BEAN_NAME_DELIMITERS);
			aliases.addAll(Arrays.asList(nameArr));
		}

		String beanName = id;
		if (!StringUtils.hasText(beanName) && !aliases.isEmpty()) {
			beanName = (String) aliases.remove(0);
			if (logger.isDebugEnabled()) {
				logger.debug("No XML 'id' specified - using '" + beanName +
						"' as bean name and " + aliases + " as aliases");
			}
		}

		BeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName);

		if (!StringUtils.hasText(beanName) && beanDefinition instanceof AbstractBeanDefinition) {
			beanName = BeanDefinitionReaderUtils.generateBeanName(
					(AbstractBeanDefinition) beanDefinition, this.beanDefinitionReader.getBeanFactory());
			if (logger.isDebugEnabled()) {
				logger.debug("Neither XML 'id' nor 'name' specified - " +
						"using generated bean name [" + beanName + "]");
			}
		}

		String[] aliasesArray = (String[]) aliases.toArray(new String[aliases.size()]);
		return new BeanDefinitionHolder(beanDefinition, beanName, aliasesArray);
	}

	/**
	 * Parse the BeanDefinition itself, without regard to name or aliases.
	 */
	protected BeanDefinition parseBeanDefinitionElement(Element ele, String beanName)
			throws BeanDefinitionStoreException {

		String className = null;
		if (ele.hasAttribute(CLASS_ATTRIBUTE)) {
			className = ele.getAttribute(CLASS_ATTRIBUTE);
		}
		String parent = null;
		if (ele.hasAttribute(PARENT_ATTRIBUTE)) {
			parent = ele.getAttribute(PARENT_ATTRIBUTE);
		}

		try {
			ConstructorArgumentValues cargs = parseConstructorArgElements(ele, beanName);
			MutablePropertyValues pvs = parsePropertyElements(ele, beanName);

			AbstractBeanDefinition bd = BeanDefinitionReaderUtils.createBeanDefinition(
					className, parent, cargs, pvs, this.beanDefinitionReader.getBeanClassLoader());

			if (ele.hasAttribute(DEPENDS_ON_ATTRIBUTE)) {
				String dependsOn = ele.getAttribute(DEPENDS_ON_ATTRIBUTE);
				bd.setDependsOn(StringUtils.tokenizeToStringArray(dependsOn, BEAN_NAME_DELIMITERS));
			}

			if (ele.hasAttribute(FACTORY_METHOD_ATTRIBUTE)) {
				bd.setFactoryMethodName(ele.getAttribute(FACTORY_METHOD_ATTRIBUTE));
			}
			if (ele.hasAttribute(FACTORY_BEAN_ATTRIBUTE)) {
				bd.setFactoryBeanName(ele.getAttribute(FACTORY_BEAN_ATTRIBUTE));
			}

			String dependencyCheck = ele.getAttribute(DEPENDENCY_CHECK_ATTRIBUTE);
			if (DEFAULT_VALUE.equals(dependencyCheck)) {
				dependencyCheck = this.defaultDependencyCheck;
			}
			bd.setDependencyCheck(getDependencyCheck(dependencyCheck));

			String autowire = ele.getAttribute(AUTOWIRE_ATTRIBUTE);
			if (DEFAULT_VALUE.equals(autowire)) {
				autowire = this.defaultAutowire;
			}
			bd.setAutowireMode(getAutowireMode(autowire));

			String initMethodName = ele.getAttribute(INIT_METHOD_ATTRIBUTE);
			if (!initMethodName.equals("")) {
				bd.setInitMethodName(initMethodName);
			}
			String destroyMethodName = ele.getAttribute(DESTROY_METHOD_ATTRIBUTE);
			if (!destroyMethodName.equals("")) {
				bd.setDestroyMethodName(destroyMethodName);
			}

			parseLookupOverrideSubElements(ele, beanName, bd.getMethodOverrides());
			parseReplacedMethodSubElements(ele, beanName, bd.getMethodOverrides());

			bd.setResourceDescription(this.resource.getDescription());

			if (ele.hasAttribute(ABSTRACT_ATTRIBUTE)) {
				bd.setAbstract(TRUE_VALUE.equals(ele.getAttribute(ABSTRACT_ATTRIBUTE)));
			}

			if (ele.hasAttribute(SINGLETON_ATTRIBUTE) ) {
				bd.setSingleton(TRUE_VALUE.equals(ele.getAttribute(SINGLETON_ATTRIBUTE)));
			}

			String lazyInit = ele.getAttribute(LAZY_INIT_ATTRIBUTE);
			if (DEFAULT_VALUE.equals(lazyInit) && bd.isSingleton()) {
				// Just apply default to singletons, as lazy-init has no meaning for prototypes.
				lazyInit = this.defaultLazyInit;
			}
			bd.setLazyInit(TRUE_VALUE.equals(lazyInit));

			return bd;
		}
		catch (ClassNotFoundException ex) {
			throw new BeanDefinitionStoreException(
					this.resource, beanName, "Bean class [" + className + "] not found", ex);
		}
		catch (NoClassDefFoundError err) {
			throw new BeanDefinitionStoreException(
					this.resource, beanName, "Class that bean class [" + className + "] depends on not found", err);
		}
	}

	protected int getDependencyCheck(String att) {
		int dependencyCheckCode = AbstractBeanDefinition.DEPENDENCY_CHECK_NONE;
		if (DEPENDENCY_CHECK_ALL_ATTRIBUTE_VALUE.equals(att)) {
			dependencyCheckCode = AbstractBeanDefinition.DEPENDENCY_CHECK_ALL;
		}
		else if (DEPENDENCY_CHECK_SIMPLE_ATTRIBUTE_VALUE.equals(att)) {
			dependencyCheckCode = AbstractBeanDefinition.DEPENDENCY_CHECK_SIMPLE;
		}
		else if (DEPENDENCY_CHECK_OBJECTS_ATTRIBUTE_VALUE.equals(att)) {
			dependencyCheckCode = AbstractBeanDefinition.DEPENDENCY_CHECK_OBJECTS;
		}
		// Else leave default value.
		return dependencyCheckCode;
	}

	protected int getAutowireMode(String att) {
		int autowire = AbstractBeanDefinition.AUTOWIRE_NO;
		if (AUTOWIRE_BY_NAME_VALUE.equals(att)) {
			autowire = AbstractBeanDefinition.AUTOWIRE_BY_NAME;
		}
		else if (AUTOWIRE_BY_TYPE_VALUE.equals(att)) {
			autowire = AbstractBeanDefinition.AUTOWIRE_BY_TYPE;
		}
		else if (AUTOWIRE_CONSTRUCTOR_VALUE.equals(att)) {
			autowire = AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR;
		}
		else if (AUTOWIRE_AUTODETECT_VALUE.equals(att)) {
			autowire = AbstractBeanDefinition.AUTOWIRE_AUTODETECT;
		}
		// Else leave default value.
		return autowire;
	}

	/**
	 * Parse constructor-arg sub-elements of the given bean element.
	 */
	protected ConstructorArgumentValues parseConstructorArgElements(Element beanEle, String beanName)
			throws BeanDefinitionStoreException {

		NodeList nl = beanEle.getChildNodes();
		ConstructorArgumentValues cargs = new ConstructorArgumentValues();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && CONSTRUCTOR_ARG_ELEMENT.equals(node.getNodeName())) {
				parseConstructorArgElement((Element) node, beanName, cargs);
			}
		}
		return cargs;
	}

	/**
	 * Parse property sub-elements of the given bean element.
	 */
	protected MutablePropertyValues parsePropertyElements(Element beanEle, String beanName)
			throws BeanDefinitionStoreException {

		NodeList nl = beanEle.getChildNodes();
		MutablePropertyValues pvs = new MutablePropertyValues();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && PROPERTY_ELEMENT.equals(node.getNodeName())) {
				parsePropertyElement((Element) node, beanName, pvs);
			}
		}
		return pvs;
	}

	/**
	 * Parse lookup-override sub-elements of the given bean element.
	 */
	protected void parseLookupOverrideSubElements(Element beanEle, String beanName, MethodOverrides overrides)
			throws BeanDefinitionStoreException {

		NodeList nl = beanEle.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && LOOKUP_METHOD_ELEMENT.equals(node.getNodeName())) {
				Element ele = (Element) node;
				String methodName = ele.getAttribute(NAME_ATTRIBUTE);
				String beanRef = ele.getAttribute(BEAN_ELEMENT);
				overrides.addOverride(new LookupOverride(methodName, beanRef));
			}
		}
	}

	/**
	 * Parse replaced-method sub-elements of the given bean element.
	 */
	protected void parseReplacedMethodSubElements(Element beanEle, String beanName, MethodOverrides overrides)
			throws BeanDefinitionStoreException {

		NodeList nl = beanEle.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && REPLACED_METHOD_ELEMENT.equals(node.getNodeName())) {
				Element replacedMethodEle = (Element) node;
				String name = replacedMethodEle.getAttribute(NAME_ATTRIBUTE);
				String callback = replacedMethodEle.getAttribute(REPLACER_ATTRIBUTE);
				ReplaceOverride replaceOverride = new ReplaceOverride(name, callback);
				// Look for arg-type match elements.
				List argTypeEles = DomUtils.getChildElementsByTagName(replacedMethodEle, ARG_TYPE_ELEMENT);
				for (Iterator it = argTypeEles.iterator(); it.hasNext();) {
					Element argTypeEle = (Element) it.next();
					replaceOverride.addTypeIdentifier(argTypeEle.getAttribute(ARG_TYPE_MATCH_ATTRIBUTE));
				}
				overrides.addOverride(replaceOverride);
			}
		}
	}

	/**
	 * Parse a constructor-arg element.
	 */
	protected void parseConstructorArgElement(Element ele, String beanName, ConstructorArgumentValues cargs)
			throws BeanDefinitionStoreException {

		Object val = parsePropertyValue(ele, beanName, null);
		String indexAttr = ele.getAttribute(INDEX_ATTRIBUTE);
		String typeAttr = ele.getAttribute(TYPE_ATTRIBUTE);
		if (StringUtils.hasLength(indexAttr)) {
			try {
				int index = Integer.parseInt(indexAttr);
				if (index < 0) {
					throw new BeanDefinitionStoreException(this.resource, beanName, "'index' cannot be lower than 0");
				}
				if (StringUtils.hasLength(typeAttr)) {
					cargs.addIndexedArgumentValue(index, val, typeAttr);
				}
				else {
					cargs.addIndexedArgumentValue(index, val);
				}
			}
			catch (NumberFormatException ex) {
				throw new BeanDefinitionStoreException(this.resource, beanName,
						"Attribute 'index' of tag 'constructor-arg' must be an integer");
			}
		}
		else {
			if (StringUtils.hasLength(typeAttr)) {
				cargs.addGenericArgumentValue(val, typeAttr);
			}
			else {
				cargs.addGenericArgumentValue(val);
			}
		}
	}

	/**
	 * Parse a property element.
	 */
	protected void parsePropertyElement(Element ele, String beanName, MutablePropertyValues pvs)
			throws BeanDefinitionStoreException {

		String propertyName = ele.getAttribute(NAME_ATTRIBUTE);
		if (!StringUtils.hasLength(propertyName)) {
			throw new BeanDefinitionStoreException(
					this.resource, beanName, "Tag 'property' must have a 'name' attribute");
		}
		if (pvs.contains(propertyName)) {
			throw new BeanDefinitionStoreException(
					this.resource, beanName, "Multiple 'property' definitions for property '" + propertyName + "'");
		}
		Object val = parsePropertyValue(ele, beanName, propertyName);
		pvs.addPropertyValue(propertyName, val);
	}

	/**
	 * Get the value of a property element. May be a list etc.
	 * Also used for constructor arguments, "propertyName" being null in this case.
	 */
	protected Object parsePropertyValue(Element ele, String beanName, String propertyName)
			throws BeanDefinitionStoreException {

		String elementName = (propertyName != null) ?
				"<property> element for property '" + propertyName + "'" :
				"<constructor-arg> element";

		// Should only have one child element: ref, value, list, etc.
		NodeList nl = ele.getChildNodes();
		Element subElement = null;
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element candidateEle = (Element) nl.item(i);
				if (DESCRIPTION_ELEMENT.equals(candidateEle.getTagName())) {
					// Keep going: we don't use this value for now.
				}
				else {
					// Child element is what we're looking for.
					if (subElement != null) {
						throw new BeanDefinitionStoreException(
								this.resource, beanName, elementName + " must not contain more than one sub-element");
					}
					subElement = candidateEle;
				}
			}
		}

		boolean hasRefAttribute = ele.hasAttribute(REF_ATTRIBUTE);
		boolean hasValueAttribute = ele.hasAttribute(VALUE_ATTRIBUTE);
		if ((hasRefAttribute && hasValueAttribute) ||
				((hasRefAttribute || hasValueAttribute)) && subElement != null) {
			throw new BeanDefinitionStoreException(
					this.resource, beanName, elementName +
					" is only allowed to contain either a 'ref' attribute OR a 'value' attribute OR a sub-element");
		}
		if (hasRefAttribute) {
			return new RuntimeBeanReference(ele.getAttribute(REF_ATTRIBUTE));
		}
		else if (hasValueAttribute) {
			return ele.getAttribute(VALUE_ATTRIBUTE);
		}

		if (subElement == null) {
			// Neither child element nor "ref" or "value" attribute found.
			throw new BeanDefinitionStoreException(
					this.resource, beanName, elementName + " must specify a ref or value");
		}

		return parsePropertySubElement(subElement, beanName);
	}

	/**
	 * Parse a value, ref or collection sub-element of a property or
	 * constructor-arg element.
	 * @param ele subelement of property element; we don't know which yet
	 */
	protected Object parsePropertySubElement(Element ele, String beanName) throws BeanDefinitionStoreException {
		if (ele.getTagName().equals(BEAN_ELEMENT)) {
			return parseBeanDefinitionElement(ele);
		}
		else if (ele.getTagName().equals(REF_ELEMENT)) {
			// A generic reference to any name of any bean.
			String beanRef = ele.getAttribute(BEAN_REF_ATTRIBUTE);
			if (!StringUtils.hasLength(beanRef)) {
				// A reference to the id of another bean in the same XML file.
				beanRef = ele.getAttribute(LOCAL_REF_ATTRIBUTE);
				if (!StringUtils.hasLength(beanRef)) {
					// A reference to the id of another bean in a parent context.
					beanRef = ele.getAttribute(PARENT_REF_ATTRIBUTE);
					if (!StringUtils.hasLength(beanRef)) {
						throw new BeanDefinitionStoreException(
								this.resource, beanName, "'bean', 'local' or 'parent' is required for a reference");
					}
					return new RuntimeBeanReference(beanRef, true);
				}
			}
			return new RuntimeBeanReference(beanRef);
		}
		else if (ele.getTagName().equals(IDREF_ELEMENT)) {
			// A generic reference to any name of any bean.
			String beanRef = ele.getAttribute(BEAN_REF_ATTRIBUTE);
			if (!StringUtils.hasLength(beanRef)) {
				// A reference to the id of another bean in the same XML file.
				beanRef = ele.getAttribute(LOCAL_REF_ATTRIBUTE);
				if (!StringUtils.hasLength(beanRef)) {
					throw new BeanDefinitionStoreException(
							this.resource, beanName, "Either 'bean' or 'local' is required for an idref");
				}
			}
			return beanRef;
		}
		else if (ele.getTagName().equals(VALUE_ELEMENT)) {
			// It's a literal value.
			String value = DomUtils.getTextValue(ele);
			if (ele.hasAttribute(TYPE_ATTRIBUTE)) {
				String typeClassName = ele.getAttribute(TYPE_ATTRIBUTE);
				try {
					Class typeClass = ClassUtils.forName(typeClassName, this.beanDefinitionReader.getBeanClassLoader());
					return new TypedStringValue(value, typeClass);
				}
				catch (ClassNotFoundException ex) {
					throw new BeanDefinitionStoreException(
							this.resource, beanName, "Value type class [" + typeClassName + "] not found", ex);
				}
			}
			return value;
		}
		else if (ele.getTagName().equals(NULL_ELEMENT)) {
			// It's a distinguished null value.
			return null;
		}
		else if (ele.getTagName().equals(LIST_ELEMENT)) {
			return parseListElement(ele, beanName);
		}
		else if (ele.getTagName().equals(SET_ELEMENT)) {
			return parseSetElement(ele, beanName);
		}
		else if (ele.getTagName().equals(MAP_ELEMENT)) {
			return parseMapElement(ele, beanName);
		}
		else if (ele.getTagName().equals(PROPS_ELEMENT)) {
			return parsePropsElement(ele, beanName);
		}
		throw new BeanDefinitionStoreException(
					this.resource, beanName, "Unknown property sub-element: <" + ele.getTagName() + ">");
	}

	/**
	 * Parse a list element.
	 */
	protected List parseListElement(Element collectionEle, String beanName) throws BeanDefinitionStoreException {
		NodeList nl = collectionEle.getChildNodes();
		ManagedList list = new ManagedList(nl.getLength());
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element ele = (Element) nl.item(i);
				list.add(parsePropertySubElement(ele, beanName));
			}
		}
		return list;
	}

	/**
	 * Parse a set element.
	 */
	protected Set parseSetElement(Element collectionEle, String beanName) throws BeanDefinitionStoreException {
		NodeList nl = collectionEle.getChildNodes();
		ManagedSet set = new ManagedSet(nl.getLength());
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element ele = (Element) nl.item(i);
				set.add(parsePropertySubElement(ele, beanName));
			}
		}
		return set;
	}

	/**
	 * Parse a map element.
	 */
	protected Map parseMapElement(Element mapEle, String beanName) throws BeanDefinitionStoreException {
		List entryEles = DomUtils.getChildElementsByTagName(mapEle, ENTRY_ELEMENT);
		Map map = new ManagedMap(entryEles.size());

		for (Iterator it = entryEles.iterator(); it.hasNext();) {
			Element entryEle = (Element) it.next();
			// Should only have one value child element: ref, value, list, etc.
			// Optionally, there might be a key child element.
			NodeList entrySubNodes = entryEle.getChildNodes();

			Element keyEle = null;
			Element valueEle = null;
			for (int j = 0; j < entrySubNodes.getLength(); j++) {
				if (entrySubNodes.item(j) instanceof Element) {
					Element candidateEle = (Element) entrySubNodes.item(j);
					if (candidateEle.getTagName().equals(KEY_ELEMENT)) {
						if (keyEle != null) {
							throw new BeanDefinitionStoreException(
									this.resource, beanName, "<entry> is only allowed to contain one <key> sub-element");
						}
						keyEle = candidateEle;
					}
					else {
						// Child element is what we're looking for.
						if (valueEle != null) {
							throw new BeanDefinitionStoreException(
									this.resource, beanName, "<entry> must not contain more than one value sub-element");
						}
						valueEle = candidateEle;
					}
				}
			}

			// Extract key from attribute or sub-element.
			Object key = null;
			boolean hasKeyAttribute = entryEle.hasAttribute(KEY_ATTRIBUTE);
			boolean hasKeyRefAttribute = entryEle.hasAttribute(KEY_REF_ATTRIBUTE);
			if ((hasKeyAttribute && hasKeyRefAttribute) ||
					((hasKeyAttribute || hasKeyRefAttribute)) && keyEle != null) {
				throw new BeanDefinitionStoreException(
						this.resource, beanName, "<entry> is only allowed to contain either " +
						"a 'key' attribute OR a 'key-ref' attribute OR a <key> sub-element");
			}
			if (hasKeyAttribute) {
				key = entryEle.getAttribute(KEY_ATTRIBUTE);
			}
			else if (hasKeyRefAttribute) {
				key = new RuntimeBeanReference(entryEle.getAttribute(KEY_REF_ATTRIBUTE));
			}
			else if (keyEle != null) {
				key = parseKeyElement(keyEle, beanName);
			}
			else {
				throw new BeanDefinitionStoreException(
						this.resource, beanName, "<entry> must specify a key");
			}

			// Extract value from attribute or sub-element.
			Object value = null;
			boolean hasValueAttribute = entryEle.hasAttribute(VALUE_ATTRIBUTE);
			boolean hasValueRefAttribute = entryEle.hasAttribute(VALUE_REF_ATTRIBUTE);
			if ((hasValueAttribute && hasValueRefAttribute) ||
					((hasValueAttribute || hasValueRefAttribute)) && valueEle != null) {
				throw new BeanDefinitionStoreException(
						this.resource, beanName, "<entry> is only allowed to contain either " +
						"a 'value' attribute OR a 'value-ref' attribute OR a value sub-element");
			}
			if (hasValueAttribute) {
				value = entryEle.getAttribute(VALUE_ATTRIBUTE);
			}
			else if (hasValueRefAttribute) {
				value = new RuntimeBeanReference(entryEle.getAttribute(VALUE_REF_ATTRIBUTE));
			}
			else if (valueEle != null) {
				value = parsePropertySubElement(valueEle, beanName);
			}
			else {
				throw new BeanDefinitionStoreException(
						this.resource, beanName, "<entry> must specify a value");
			}

			// Add final key and value to the Map.
			map.put(key, value);
		}

		return map;
	}

	/**
	 * Parse a key sub-element of a map element.
	 */
	protected Object parseKeyElement(Element keyEle, String beanName) throws BeanDefinitionStoreException {
		NodeList nl = keyEle.getChildNodes();
		Element subElement = null;
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element candidateEle = (Element) nl.item(i);
				// Child element is what we're looking for.
				if (subElement != null) {
					throw new BeanDefinitionStoreException(
							this.resource, beanName, "<key> must not contain more than one value sub-element");
				}
				subElement = candidateEle;
			}
		}
		return parsePropertySubElement(subElement, beanName);
	}

	/**
	 * Parse a props element.
	 */
	protected Properties parsePropsElement(Element propsEle, String beanName) throws BeanDefinitionStoreException {
		Properties props = new Properties();
		List propEles = DomUtils.getChildElementsByTagName(propsEle, PROP_ELEMENT);
		for (Iterator it = propEles.iterator(); it.hasNext();) {
			Element propEle = (Element) it.next();
			String key = propEle.getAttribute(KEY_ATTRIBUTE);
			// Trim the text value to avoid unwanted whitespace
			// caused by typical XML formatting.
			String value = DomUtils.getTextValue(propEle).trim();
			props.setProperty(key, value);
		}
		return props;
	}

}
