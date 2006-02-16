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
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.support.MethodOverrides;
import org.springframework.beans.factory.support.ParseState;
import org.springframework.beans.factory.support.ReaderContext;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Stateful helper class used to parse XML bean definitions. Intended for use
 * by both the main parser and any extension {@link BeanDefinitionParser BeanDefinitionParsers}
 * or {@link BeanDefinitionDecorator BeanDefinitionDecorators}.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 2.0
 */
public class XmlBeanDefinitionParserHelper {

	private static final String BEAN_NAME_DELIMITERS = ",; ";

	/**
	 * Value of a T/F attribute that represents true.
	 * Anything else represents false. Case seNsItive.
	 */
	private static final String TRUE_VALUE = "true";

	private static final String DEFAULT_VALUE = "default";

	private static final String DESCRIPTION_ELEMENT = "description";

	private static final String AUTOWIRE_BY_NAME_VALUE = "byName";

	private static final String AUTOWIRE_BY_TYPE_VALUE = "byType";

	private static final String AUTOWIRE_CONSTRUCTOR_VALUE = "constructor";

	private static final String AUTOWIRE_AUTODETECT_VALUE = "autodetect";

	private static final String DEPENDENCY_CHECK_ALL_ATTRIBUTE_VALUE = "all";

	private static final String DEPENDENCY_CHECK_SIMPLE_ATTRIBUTE_VALUE = "simple";

	private static final String DEPENDENCY_CHECK_OBJECTS_ATTRIBUTE_VALUE = "objects";

	private static final String NAME_ATTRIBUTE = "name";

	private static final String BEAN_ELEMENT = "bean";

	private static final String ID_ATTRIBUTE = "id";

	private static final String PARENT_ATTRIBUTE = "parent";

	private static final String CLASS_ATTRIBUTE = "class";

	private static final String ABSTRACT_ATTRIBUTE = "abstract";

	private static final String SINGLETON_ATTRIBUTE = "singleton";

	private static final String LAZY_INIT_ATTRIBUTE = "lazy-init";

	private static final String AUTOWIRE_ATTRIBUTE = "autowire";

	private static final String DEPENDENCY_CHECK_ATTRIBUTE = "dependency-check";

	private static final String DEPENDS_ON_ATTRIBUTE = "depends-on";

	private static final String INIT_METHOD_ATTRIBUTE = "init-method";

	private static final String DESTROY_METHOD_ATTRIBUTE = "destroy-method";

	private static final String FACTORY_METHOD_ATTRIBUTE = "factory-method";

	private static final String FACTORY_BEAN_ATTRIBUTE = "factory-bean";

	private static final String CONSTRUCTOR_ARG_ELEMENT = "constructor-arg";

	private static final String INDEX_ATTRIBUTE = "index";

	private static final String TYPE_ATTRIBUTE = "type";

	private static final String PROPERTY_ELEMENT = "property";

	private static final String REF_ATTRIBUTE = "ref";

	private static final String VALUE_ATTRIBUTE = "value";

	private static final String LOOKUP_METHOD_ELEMENT = "lookup-method";

	private static final String REPLACED_METHOD_ELEMENT = "replaced-method";

	private static final String REPLACER_ATTRIBUTE = "replacer";

	private static final String ARG_TYPE_ELEMENT = "arg-type";

	private static final String ARG_TYPE_MATCH_ATTRIBUTE = "match";

	private static final String REF_ELEMENT = "ref";

	private static final String IDREF_ELEMENT = "idref";

	private static final String BEAN_REF_ATTRIBUTE = "bean";

	private static final String LOCAL_REF_ATTRIBUTE = "local";

	private static final String PARENT_REF_ATTRIBUTE = "parent";

	private static final String VALUE_ELEMENT = "value";

	private static final String NULL_ELEMENT = "null";

	private static final String LIST_ELEMENT = "list";

	private static final String SET_ELEMENT = "set";

	private static final String MAP_ELEMENT = "map";

	private static final String ENTRY_ELEMENT = "entry";

	private static final String KEY_ELEMENT = "key";

	private static final String KEY_ATTRIBUTE = "key";

	private static final String KEY_REF_ATTRIBUTE = "key-ref";

	private static final String VALUE_REF_ATTRIBUTE = "value-ref";

	private static final String PROPS_ELEMENT = "props";

	private static final String PROP_ELEMENT = "prop";

	private static final String MERGE_ATTRIBUTE = "merge";

	/**
	 * {@link Log} instance for this class.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	private ParseState parseState = new ParseState();

	/**
	 * The {@link ReaderContext} used for error reporting.
	 */
	private ReaderContext readerContext;

	private String defaultLazyInit;

	private String defaultAutowire;

	private String defaultDependencyCheck;

	private String defaultInitMethod;

	private String defaultDestroyMethod;

	private String defaultMerge;

	/**
	 * Creates a new <code>XmlBeanDefinitionParserHelper</code> associated with the
	 * supplied {@link ReaderContext}.
	 */
	public XmlBeanDefinitionParserHelper(ReaderContext readerContext) {
		this.readerContext = readerContext;
	}

	/**
	 * Gets the {@link ReaderContext} associated with this helper instance.
	 */
	public ReaderContext getReaderContext() {
		return this.readerContext;
	}

	/**
	 * Set the default lazy-init flag for the document that's currently parsed.
	 */
	protected final void setDefaultLazyInit(String defaultLazyInit) {
		this.defaultLazyInit = defaultLazyInit;
	}

	/**
	 * Return the default lazy-init flag for the document that's currently parsed.
	 */
	protected final String getDefaultLazyInit() {
		return defaultLazyInit;
	}

	/**
	 * Set the default autowire setting for the document that's currently parsed.
	 */
	protected final void setDefaultAutowire(String defaultAutowire) {
		this.defaultAutowire = defaultAutowire;
	}

	/**
	 * Return the default autowire setting for the document that's currently parsed.
	 */
	protected final String getDefaultAutowire() {
		return defaultAutowire;
	}

	/**
	 * Set the default dependency-check setting for the document that's currently parsed.
	 */
	protected final void setDefaultDependencyCheck(String defaultDependencyCheck) {
		this.defaultDependencyCheck = defaultDependencyCheck;
	}

	/**
	 * Return the default dependency-check setting for the document that's currently parsed.
	 */
	protected final String getDefaultDependencyCheck() {
		return defaultDependencyCheck;
	}

	/**
	 * Set the default init-method setting for the document that's currently parsed.
	 */
	protected final void setDefaultInitMethod(String defaultInitMethod) {
		this.defaultInitMethod = defaultInitMethod;
	}

	/**
	 * Return the default init-method setting for the document that's currently parsed.
	 */
	protected final String getDefaultInitMethod() {
		return defaultInitMethod;
	}

	/**
	 * Set the default destroy-method setting for the document that's currently parsed.
	 */
	protected final void setDefaultDestroyMethod(String defaultDestroyMethod) {
		this.defaultDestroyMethod = defaultDestroyMethod;
	}

	/**
	 * Return the default destroy-method setting for the document that's currently parsed.
	 */
	protected final String getDefaultDestroyMethod() {
		return defaultDestroyMethod;
	}

	/**
	 * Set the default merge setting for the document that's currently parsed.
	 */
	protected final void setDefaultMerge(String defaultMerge) {
		this.defaultMerge = defaultMerge;
	}

	/**
	 * Return the default merge setting for the document that's currently parsed.
	 */
	protected final String getDefaultMerge() {
		return defaultMerge;
	}

	/**
	 * Parses the supplied <code>&lt;bean&gt;</code> element. May return <code>null</code>
	 * if there were errors during parse. Errors are reported to the
	 * {@link org.springframework.beans.factory.support.ProblemReporter}.
	 */
	protected BeanDefinitionHolder parseBeanDefinitionElement(Element ele, boolean isInnerBean) {

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

		if (beanDefinition != null) {
			if (!StringUtils.hasText(beanName) && beanDefinition instanceof AbstractBeanDefinition) {
				beanName = BeanDefinitionReaderUtils.generateBeanName(
								(AbstractBeanDefinition) beanDefinition, getReaderContext().getReader().getBeanFactory(), isInnerBean);
				if (logger.isDebugEnabled()) {
					logger.debug("Neither XML 'id' nor 'name' specified - " +
									"using generated bean name [" + beanName + "]");
				}
			}

			String[] aliasesArray = (String[]) aliases.toArray(new String[aliases.size()]);
			return new BeanDefinitionHolder(beanDefinition, beanName, aliasesArray);
		}
		return null;
	}

	/**
	 * Parse the BeanDefinition itself, without regard to name or aliases. May return
	 * <code>null</code> if problems occured during the parse of the bean definition.
	 */
	protected BeanDefinition parseBeanDefinitionElement(Element ele, String beanName) {

		String className = null;
		if (ele.hasAttribute(CLASS_ATTRIBUTE)) {
			className = ele.getAttribute(CLASS_ATTRIBUTE);
		}
		String parent = null;
		if (ele.hasAttribute(PARENT_ATTRIBUTE)) {
			parent = ele.getAttribute(PARENT_ATTRIBUTE);
		}

		try {
			this.parseState.bean(beanName);
			ConstructorArgumentValues cargs = parseConstructorArgElements(ele);
			MutablePropertyValues pvs = parsePropertyElements(ele);

			AbstractBeanDefinition bd = BeanDefinitionReaderUtils.createBeanDefinition(
							className, parent, cargs, pvs, getReaderContext().getReader().getBeanClassLoader());

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
				dependencyCheck = getDefaultDependencyCheck();
			}
			bd.setDependencyCheck(getDependencyCheck(dependencyCheck));

			String autowire = ele.getAttribute(AUTOWIRE_ATTRIBUTE);
			if (DEFAULT_VALUE.equals(autowire)) {
				autowire = getDefaultAutowire();
			}
			bd.setAutowireMode(getAutowireMode(autowire));

			if (ele.hasAttribute(INIT_METHOD_ATTRIBUTE)) {
				String initMethodName = ele.getAttribute(INIT_METHOD_ATTRIBUTE);
				if (!"".equals(initMethodName)) {
					bd.setInitMethodName(initMethodName);
				}
			}
			else {
				if (getDefaultInitMethod() != null) {
					bd.setInitMethodName(getDefaultInitMethod());
					bd.setEnforceInitMethod(false);
				}
			}

			if (ele.hasAttribute(DESTROY_METHOD_ATTRIBUTE)) {
				String destroyMethodName = ele.getAttribute(DESTROY_METHOD_ATTRIBUTE);
				if (!"".equals(destroyMethodName)) {
					bd.setDestroyMethodName(destroyMethodName);
				}
			}
			else {
				if (getDefaultDestroyMethod() != null) {
					bd.setDestroyMethodName(getDefaultDestroyMethod());
					bd.setEnforceDestroyMethod(false);
				}
			}

			parseLookupOverrideSubElements(ele, bd.getMethodOverrides());
			parseReplacedMethodSubElements(ele, bd.getMethodOverrides());

			bd.setResourceDescription(getReaderContext().getResource().getDescription());

			if (ele.hasAttribute(ABSTRACT_ATTRIBUTE)) {
				bd.setAbstract(TRUE_VALUE.equals(ele.getAttribute(ABSTRACT_ATTRIBUTE)));
			}

			if (ele.hasAttribute(SINGLETON_ATTRIBUTE)) {
				bd.setSingleton(TRUE_VALUE.equals(ele.getAttribute(SINGLETON_ATTRIBUTE)));
			}

			String lazyInit = ele.getAttribute(LAZY_INIT_ATTRIBUTE);
			if (DEFAULT_VALUE.equals(lazyInit) && bd.isSingleton()) {
				// Just apply default to singletons, as lazy-init has no meaning for prototypes.
				lazyInit = getDefaultLazyInit();
			}
			bd.setLazyInit(TRUE_VALUE.equals(lazyInit));

			return bd;
		}
		catch (ClassNotFoundException ex) {
			error("Bean class [" + className + "] not found", ele,ex);
		}
		catch (NoClassDefFoundError err) {
			error("Class that bean class [" + className + "] depends on not found", ele, err);
		}
		catch (Throwable ex) {
			error("Unexpected failure during bean definition parsing", ele, ex);
		}
		finally {
			this.parseState.pop();
		}

		return null;
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
	protected ConstructorArgumentValues parseConstructorArgElements(Element beanEle) {

		NodeList nl = beanEle.getChildNodes();
		ConstructorArgumentValues cargs = new ConstructorArgumentValues();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && CONSTRUCTOR_ARG_ELEMENT.equals(node.getNodeName())) {
				parseConstructorArgElement((Element) node, cargs);
			}
		}
		return cargs;
	}

	/**
	 * Parse property sub-elements of the given bean element.
	 */
	protected MutablePropertyValues parsePropertyElements(Element beanEle) {

		NodeList nl = beanEle.getChildNodes();
		MutablePropertyValues pvs = new MutablePropertyValues();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && PROPERTY_ELEMENT.equals(node.getNodeName())) {
				parsePropertyElement((Element) node, pvs);
			}
		}
		return pvs;
	}

	/**
	 * Parse lookup-override sub-elements of the given bean element.
	 */
	protected void parseLookupOverrideSubElements(Element beanEle, MethodOverrides overrides) {

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
	protected void parseReplacedMethodSubElements(Element beanEle, MethodOverrides overrides) {

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
	protected void parseConstructorArgElement(Element ele, ConstructorArgumentValues cargs) {

		String indexAttr = ele.getAttribute(INDEX_ATTRIBUTE);
		String typeAttr = ele.getAttribute(TYPE_ATTRIBUTE);
		if (StringUtils.hasLength(indexAttr)) {
			try {
				int index = Integer.parseInt(indexAttr);
				if (index < 0) {
					error("'index' cannot be lower than 0", ele);
				}

				try {
					this.parseState.constructor(index);
					Object val = parsePropertyValue(ele, null);
					if (StringUtils.hasLength(typeAttr)) {
						cargs.addIndexedArgumentValue(index, val, typeAttr);
					}
					else {
						cargs.addIndexedArgumentValue(index, val);
					}
				}
				finally {
					this.parseState.pop();
				}
			}
			catch (NumberFormatException ex) {
				error("Attribute 'index' of tag 'constructor-arg' must be an integer", ele);
			}
		}
		else {
			try {
				this.parseState.constructor();
				Object val = parsePropertyValue(ele, null);
				if (StringUtils.hasLength(typeAttr)) {
					cargs.addGenericArgumentValue(val, typeAttr);
				}
				else {
					cargs.addGenericArgumentValue(val);
				}
			}
			finally {
				this.parseState.pop();
			}
		}
	}

	/**
	 * Parse a property element.
	 */
	protected void parsePropertyElement(Element ele, MutablePropertyValues pvs) {

		try {
			String propertyName = ele.getAttribute(NAME_ATTRIBUTE);
			if (!StringUtils.hasLength(propertyName)) {
				error("Tag 'property' must have a 'name' attribute", ele);
				return;
			}
			if (pvs.contains(propertyName)) {
				error("Multiple 'property' definitions for property '" + propertyName + "'", ele);
				return;
			}
			this.parseState.property(propertyName);
			Object val = parsePropertyValue(ele, propertyName);
			pvs.addPropertyValue(propertyName, val);
		}
		finally {
			this.parseState.pop();
		}
	}


	/**
	 * Get the value of a property element. May be a list etc.
	 * Also used for constructor arguments, "propertyName" being null in this case.
	 */
	protected Object parsePropertyValue(Element ele, String propertyName) {

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
						error(elementName + " must not contain more than one sub-element", ele);
					}
					subElement = candidateEle;
				}
			}
		}

		boolean hasRefAttribute = ele.hasAttribute(REF_ATTRIBUTE);
		boolean hasValueAttribute = ele.hasAttribute(VALUE_ATTRIBUTE);
		if ((hasRefAttribute && hasValueAttribute) ||
						((hasRefAttribute || hasValueAttribute)) && subElement != null) {
			error(elementName +
							" is only allowed to contain either 'ref' attribute OR 'value' attribute OR sub-element", ele);
		}
		if (hasRefAttribute) {
			String refName = ele.getAttribute(REF_ATTRIBUTE);
			if (!StringUtils.hasText(refName)) {
				error(elementName + " contains empty 'ref' attribute", ele);
			}
			return new RuntimeBeanReference(refName);
		}
		else if (hasValueAttribute) {
			return ele.getAttribute(VALUE_ATTRIBUTE);
		}

		if (subElement == null) {
			// Neither child element nor "ref" or "value" attribute found.
			error(elementName + " must specify a ref or value", ele);
		}

		return parsePropertySubElement(subElement);
	}

	/**
	 * Parse a value, ref or collection sub-element of a property or
	 * constructor-arg element.
	 * @param ele subelement of property element; we don't know which yet
	 */
	protected Object parsePropertySubElement(Element ele) {
		if (ele.getTagName().equals(BEAN_ELEMENT)) {
			return parseBeanDefinitionElement(ele, true);
		}
		else if (ele.getTagName().equals(REF_ELEMENT)) {
			// A generic reference to any name of any bean.
			String refName = ele.getAttribute(BEAN_REF_ATTRIBUTE);
			boolean toParent = false;
			if (!StringUtils.hasLength(refName)) {
				// A reference to the id of another bean in the same XML file.
				refName = ele.getAttribute(LOCAL_REF_ATTRIBUTE);
				if (!StringUtils.hasLength(refName)) {
					// A reference to the id of another bean in a parent context.
					refName = ele.getAttribute(PARENT_REF_ATTRIBUTE);
					toParent = true;
					if (!StringUtils.hasLength(refName)) {
						error("'bean', 'local' or 'parent' is required for <ref> element", ele);
					}
				}
			}
			if (!StringUtils.hasText(refName)) {
				error("<ref> element contains empty target attribute", ele);
			}
			return new RuntimeBeanReference(refName, toParent);
		}
		else if (ele.getTagName().equals(IDREF_ELEMENT)) {
			// A generic reference to any name of any bean.
			String beanRef = ele.getAttribute(BEAN_REF_ATTRIBUTE);
			if (!StringUtils.hasLength(beanRef)) {
				// A reference to the id of another bean in the same XML file.
				beanRef = ele.getAttribute(LOCAL_REF_ATTRIBUTE);
				if (!StringUtils.hasLength(beanRef)) {
					error("Either 'bean' or 'local' is required for <idref> element", ele);
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
					Class typeClass = ClassUtils.forName(typeClassName, getReaderContext().getReader().getBeanClassLoader());
					return new TypedStringValue(value, typeClass);
				}
				catch (ClassNotFoundException ex) {
					error("Type class [" + typeClassName + "] not found for <value> element", ele, ex);
				}
			}
			return value;
		}
		else if (ele.getTagName().equals(NULL_ELEMENT)) {
			// It's a distinguished null value.
			return null;
		}
		else if (ele.getTagName().equals(LIST_ELEMENT)) {
			return parseListElement(ele);
		}
		else if (ele.getTagName().equals(SET_ELEMENT)) {
			return parseSetElement(ele);
		}
		else if (ele.getTagName().equals(MAP_ELEMENT)) {
			return parseMapElement(ele);
		}
		else if (ele.getTagName().equals(PROPS_ELEMENT)) {
			return parsePropsElement(ele);
		}
		error("Unknown property sub-element: [" + ele.getTagName() + "]", ele);
		return null;
	}

	/**
	 * Parse a list element.
	 */
	protected List parseListElement(Element collectionEle) {
		NodeList nl = collectionEle.getChildNodes();
		ManagedList list = new ManagedList(nl.getLength());
		list.setMergeEnabled(parseMergeAttribute(collectionEle));
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element ele = (Element) nl.item(i);
				list.add(parsePropertySubElement(ele));
			}
		}
		return list;
	}

	/**
	 * Parse a set element.
	 */
	protected Set parseSetElement(Element collectionEle) {
		NodeList nl = collectionEle.getChildNodes();
		ManagedSet set = new ManagedSet(nl.getLength());
		set.setMergeEnabled(parseMergeAttribute(collectionEle));
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element ele = (Element) nl.item(i);
				set.add(parsePropertySubElement(ele));
			}
		}
		return set;
	}

	/**
	 * Parse a map element.
	 */
	protected Map parseMapElement(Element mapEle) {
		List entryEles = DomUtils.getChildElementsByTagName(mapEle, ENTRY_ELEMENT);
		ManagedMap map = new ManagedMap(entryEles.size());
		map.setMergeEnabled(parseMergeAttribute(mapEle));

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
							error("<entry> element is only allowed to contain one <key> sub-element", entryEle);
						}
						keyEle = candidateEle;
					}
					else {
						// Child element is what we're looking for.
						if (valueEle != null) {
							error("<entry> element must not contain more than one value sub-element", entryEle);
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
				error("<entry> element is only allowed to contain either " +
								"a 'key' attribute OR a 'key-ref' attribute OR a <key> sub-element", entryEle);
			}
			if (hasKeyAttribute) {
				key = entryEle.getAttribute(KEY_ATTRIBUTE);
			}
			else if (hasKeyRefAttribute) {
				String refName = entryEle.getAttribute(KEY_REF_ATTRIBUTE);
				if (!StringUtils.hasText(refName)) {
					error("<entry> element contains empty 'key-ref' attribute", entryEle);
				}
				key = new RuntimeBeanReference(refName);
			}
			else if (keyEle != null) {
				key = parseKeyElement(keyEle);
			}
			else {
				error("<entry> element must specify a key", entryEle);
			}

			// Extract value from attribute or sub-element.
			Object value = null;
			boolean hasValueAttribute = entryEle.hasAttribute(VALUE_ATTRIBUTE);
			boolean hasValueRefAttribute = entryEle.hasAttribute(VALUE_REF_ATTRIBUTE);
			if ((hasValueAttribute && hasValueRefAttribute) ||
							((hasValueAttribute || hasValueRefAttribute)) && valueEle != null) {
				error("<entry> element is only allowed to contain either " +
								"'value' attribute OR 'value-ref' attribute OR <value> sub-element", entryEle);
			}
			if (hasValueAttribute) {
				value = entryEle.getAttribute(VALUE_ATTRIBUTE);
			}
			else if (hasValueRefAttribute) {
				String refName = entryEle.getAttribute(VALUE_REF_ATTRIBUTE);
				if (!StringUtils.hasText(refName)) {
					error("<entry> element contains empty 'value-ref' attribute", entryEle);
				}
				value = new RuntimeBeanReference(refName);
			}
			else if (valueEle != null) {
				value = parsePropertySubElement(valueEle);
			}
			else {
				error("<entry> element must specify a value", entryEle);
			}

			// Add final key and value to the Map.
			map.put(key, value);
		}

		return map;
	}

	/**
	 * Parse a key sub-element of a map element.
	 */
	protected Object parseKeyElement(Element keyEle) {
		NodeList nl = keyEle.getChildNodes();
		Element subElement = null;
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element candidateEle = (Element) nl.item(i);
				// Child element is what we're looking for.

				if (subElement != null) {
					error("<key> element must not contain more than one value sub-element", keyEle);
				}
				subElement = candidateEle;
			}
		}
		return parsePropertySubElement(subElement);
	}

	/**
	 * Parse a props element.
	 */
	protected Properties parsePropsElement(Element propsEle) {
		ManagedProperties props = new ManagedProperties();
		props.setMergeEnabled(parseMergeAttribute(propsEle));
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

	/**
	 * Parse the merge attribute of a collection element, if any.
	 */
	protected boolean parseMergeAttribute(Element collectionElement) {
		String value = collectionElement.getAttribute(MERGE_ATTRIBUTE);
		if (DEFAULT_VALUE.equals(value)) {
			value = getDefaultMerge();
		}
		return TRUE_VALUE.equals(value);
	}

	private void error(String message, Object source) {
		getReaderContext().error(message, source, this.parseState.snapshot());
	}

	private void error(String message, Object source, Throwable cause) {
		getReaderContext().error(message, source, this.parseState.snapshot(), cause);
	}
}
