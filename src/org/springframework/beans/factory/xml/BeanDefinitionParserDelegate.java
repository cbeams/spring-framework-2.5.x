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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.parsing.BeanEntry;
import org.springframework.beans.factory.parsing.ConstructorArgumentEntry;
import org.springframework.beans.factory.parsing.ParseState;
import org.springframework.beans.factory.parsing.PropertyEntry;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.LookupOverride;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedProperties;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.support.MethodOverrides;
import org.springframework.beans.factory.support.ReaderContext;
import org.springframework.beans.factory.support.ReplaceOverride;
import org.springframework.core.AttributeAccessor;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;

/**
 * Stateful delegate class used to parse XML bean definitions.
 * Intended for use by both the main parser and any extension
 * {@link BeanDefinitionParser BeanDefinitionParsers}
 * or {@link BeanDefinitionDecorator BeanDefinitionDecorators}.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Rod Johnson
 * @since 2.0
 * @see ParserContext
 * @see DefaultBeanDefinitionDocumentReader
 */
public class BeanDefinitionParserDelegate {

	public static final String BEANS_NAMESPACE_URI = "http://www.springframework.org/schema/beans";

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

	public static final String NAME_ATTRIBUTE = "name";

	public static final String BEAN_ELEMENT = "bean";

	public static final String META_ELEMENT = "meta";

	public static final String ID_ATTRIBUTE = "id";

	public static final String PARENT_ATTRIBUTE = "parent";

	public static final String CLASS_ATTRIBUTE = "class";

	public static final String ABSTRACT_ATTRIBUTE = "abstract";

	public static final String SCOPE_ATTRIBUTE = "scope";

	public static final String SINGLETON_ATTRIBUTE = "singleton";

	public static final String LAZY_INIT_ATTRIBUTE = "lazy-init";

	public static final String AUTOWIRE_ATTRIBUTE = "autowire";

	public static final String AUTOWIRE_CANDIDATE_ATTRIBUTE = "autowire-candidate";

	public static final String DEPENDENCY_CHECK_ATTRIBUTE = "dependency-check";

	public static final String DEPENDS_ON_ATTRIBUTE = "depends-on";

	public static final String INIT_METHOD_ATTRIBUTE = "init-method";

	public static final String DESTROY_METHOD_ATTRIBUTE = "destroy-method";

	public static final String FACTORY_METHOD_ATTRIBUTE = "factory-method";

	public static final String FACTORY_BEAN_ATTRIBUTE = "factory-bean";

	public static final String CONSTRUCTOR_ARG_ELEMENT = "constructor-arg";

	public static final String INDEX_ATTRIBUTE = "index";

	public static final String TYPE_ATTRIBUTE = "type";

	public static final String VALUE_TYPE_ATTRIBUTE = "value-type";

	public static final String KEY_TYPE_ATTRIBUTE = "key-type";

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

	public static final String MERGE_ATTRIBUTE = "merge";

	public static final String DEFAULT_LAZY_INIT_ATTRIBUTE = "default-lazy-init";

	public static final String DEFAULT_AUTOWIRE_ATTRIBUTE = "default-autowire";

	public static final String DEFAULT_DEPENDENCY_CHECK_ATTRIBUTE = "default-dependency-check";

	public static final String DEFAULT_INIT_METHOD_ATTRIBUTE = "default-init-method";

	public static final String DEFAULT_DESTROY_METHOD_ATTRIBUTE = "default-destroy-method";

	public static final String DEFAULT_MERGE_ATTRIBUTE = "default-merge";


    /**
	 * {@link Log} instance for this class.
	 */
	protected final Log logger = LogFactory.getLog(getClass());

	private final XmlReaderContext readerContext;

	private ParseState parseState = new ParseState();

	private String defaultLazyInit;

	private String defaultAutowire;

	private String defaultDependencyCheck;

	private String defaultInitMethod;

	private String defaultDestroyMethod;

	private String defaultMerge;

	/**
	 * Stores all used bean names so we can enforce uniqueness on a per file basis.
	 */
	private final Set usedNames = new HashSet();

	/**
	 * Create a new <code>XmlBeanDefinitionParserHelper</code> associated with the
	 * supplied {@link ReaderContext}.
	 */
	public BeanDefinitionParserDelegate(XmlReaderContext readerContext) {
		Assert.notNull(readerContext, "'readerContext' cannot be null.");
		this.readerContext = readerContext;
	}


	/**
	 * Get the {@link ReaderContext} associated with this helper instance.
	 */
	public XmlReaderContext getReaderContext() {
		return this.readerContext;
	}

	/**
	 * Set the default lazy-init flag for the document that's currently parsed.
	 */
	public final void setDefaultLazyInit(String defaultLazyInit) {
		this.defaultLazyInit = defaultLazyInit;
	}

	/**
	 * Return the default lazy-init flag for the document that's currently parsed.
	 */
	public final String getDefaultLazyInit() {
		return defaultLazyInit;
	}

	/**
	 * Set the default autowire setting for the document that's currently parsed.
	 */
	public final void setDefaultAutowire(String defaultAutowire) {
		this.defaultAutowire = defaultAutowire;
	}

	/**
	 * Return the default autowire setting for the document that's currently parsed.
	 */
	public final String getDefaultAutowire() {
		return defaultAutowire;
	}

	/**
	 * Set the default dependency-check setting for the document that's currently parsed.
	 */
	public final void setDefaultDependencyCheck(String defaultDependencyCheck) {
		this.defaultDependencyCheck = defaultDependencyCheck;
	}

	/**
	 * Return the default dependency-check setting for the document that's currently parsed.
	 */
	public final String getDefaultDependencyCheck() {
		return defaultDependencyCheck;
	}

	/**
	 * Set the default init-method setting for the document that's currently parsed.
	 */
	public final void setDefaultInitMethod(String defaultInitMethod) {
		this.defaultInitMethod = defaultInitMethod;
	}

	/**
	 * Return the default init-method setting for the document that's currently parsed.
	 */
	public final String getDefaultInitMethod() {
		return defaultInitMethod;
	}

	/**
	 * Set the default destroy-method setting for the document that's currently parsed.
	 */
	public final void setDefaultDestroyMethod(String defaultDestroyMethod) {
		this.defaultDestroyMethod = defaultDestroyMethod;
	}

	/**
	 * Return the default destroy-method setting for the document that's currently parsed.
	 */
	public final String getDefaultDestroyMethod() {
		return defaultDestroyMethod;
	}

	/**
	 * Set the default merge setting for the document that's currently parsed.
	 */
	public final void setDefaultMerge(String defaultMerge) {
		this.defaultMerge = defaultMerge;
	}

	/**
	 * Return the default merge setting for the document that's currently parsed.
	 */
	public final String getDefaultMerge() {
		return defaultMerge;
	}


	/**
	 * Initialize the default lazy-init, autowire, dependency check settings,
	 * init-method, destroy-method and merge settings.
	 */
	public void initDefaults(Element root) {
		setDefaultLazyInit(root.getAttribute(DEFAULT_LAZY_INIT_ATTRIBUTE));
		setDefaultAutowire(root.getAttribute(DEFAULT_AUTOWIRE_ATTRIBUTE));
		setDefaultDependencyCheck(root.getAttribute(DEFAULT_DEPENDENCY_CHECK_ATTRIBUTE));
		if (root.hasAttribute(DEFAULT_INIT_METHOD_ATTRIBUTE)) {
			setDefaultInitMethod(root.getAttribute(DEFAULT_INIT_METHOD_ATTRIBUTE));
		}
		if (root.hasAttribute(DEFAULT_DESTROY_METHOD_ATTRIBUTE)) {
			setDefaultDestroyMethod(root.getAttribute(DEFAULT_DESTROY_METHOD_ATTRIBUTE));
		}
		setDefaultMerge(root.getAttribute(DEFAULT_MERGE_ATTRIBUTE));
	}

	/**
	 * Parses the supplied <code>&lt;bean&gt;</code> element. May return <code>null</code>
	 * if there were errors during parse. Errors are reported to the
	 * {@link org.springframework.beans.factory.parsing.ProblemReporter}.
	 */
	public BeanDefinitionHolder parseBeanDefinitionElement(Element ele) {
		return parseBeanDefinitionElement(ele, null);
	}

	/**
	 * Parses the supplied <code>&lt;bean&gt;</code> element. May return <code>null</code>
	 * if there were errors during parse. Errors are reported to the
	 * {@link org.springframework.beans.factory.parsing.ProblemReporter}.
	 */
	public BeanDefinitionHolder parseBeanDefinitionElement(Element ele, BeanDefinition containingBean) {
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

		if (containingBean == null) {
			checkNameUniqueness(beanName, aliases, ele);
		}

		BeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean);

		if (beanDefinition != null) {
			if (!StringUtils.hasText(beanName) && beanDefinition instanceof AbstractBeanDefinition) {
				beanName = BeanDefinitionReaderUtils.generateBeanName(
						(AbstractBeanDefinition) beanDefinition, getReaderContext().getReader().getBeanFactory(),
						(containingBean != null));
				if (logger.isDebugEnabled()) {
					logger.debug("Neither XML 'id' nor 'name' specified - " +
							"using generated bean name [" + beanName + "]");
				}
			}

			String[] aliasesArray = StringUtils.toStringArray(aliases);
			return new BeanDefinitionHolder(beanDefinition, beanName, aliasesArray);
		}
		return null;
	}

	private void checkNameUniqueness(String beanName, List aliases, Element beanElement) {
		String foundName = null;

		if (StringUtils.hasText(beanName) && this.usedNames.contains(beanName)) {
			foundName = beanName;
		}
		this.usedNames.add(beanName);

		if (foundName == null) {
			foundName = (String) CollectionUtils.findFirstMatch(this.usedNames, aliases);
		}
		this.usedNames.addAll(aliases);

		if (foundName != null) {
			getReaderContext().error("Bean name '" + foundName + "' is already used in this file.", beanElement);
		}
	}

	/**
	 * Parse the BeanDefinition itself, without regard to name or aliases. May return
	 * <code>null</code> if problems occured during the parse of the bean definition.
	 */
	public BeanDefinition parseBeanDefinitionElement(Element ele, String beanName, BeanDefinition containingBean) {
		String className = null;
		if (ele.hasAttribute(CLASS_ATTRIBUTE)) {
			className = ele.getAttribute(CLASS_ATTRIBUTE);
		}
		String parent = null;
		if (ele.hasAttribute(PARENT_ATTRIBUTE)) {
			parent = ele.getAttribute(PARENT_ATTRIBUTE);
		}

		try {
			this.parseState.push(new BeanEntry(beanName));

			AbstractBeanDefinition bd = BeanDefinitionReaderUtils.createBeanDefinition(
					parent, className, getReaderContext().getReader().getBeanClassLoader());

			if (ele.hasAttribute(SCOPE_ATTRIBUTE)) {
				// Spring 2.0 "scope" attribute
				bd.setScope(ele.getAttribute(SCOPE_ATTRIBUTE));
				if (ele.hasAttribute(SINGLETON_ATTRIBUTE)) {
					error("Specify either 'scope' or 'singleton', not both", ele);
				}
			}
			else if (ele.hasAttribute(SINGLETON_ATTRIBUTE)) {
				// Spring 1.x "singleton" attribute
				bd.setSingleton(TRUE_VALUE.equals(ele.getAttribute(SINGLETON_ATTRIBUTE)));
			}
			else if (containingBean != null) {
				// Take default from containing bean in case of an inner bean definition.
				bd.setSingleton(containingBean.isSingleton());
			}

			if (ele.hasAttribute(ABSTRACT_ATTRIBUTE)) {
				bd.setAbstract(TRUE_VALUE.equals(ele.getAttribute(ABSTRACT_ATTRIBUTE)));
			}

			String lazyInit = ele.getAttribute(LAZY_INIT_ATTRIBUTE);
			if (DEFAULT_VALUE.equals(lazyInit) && bd.isSingleton()) {
				// Just apply default to singletons, as lazy-init has no meaning for prototypes.
				lazyInit = getDefaultLazyInit();
			}
			bd.setLazyInit(TRUE_VALUE.equals(lazyInit));

			if (ele.hasAttribute(AUTOWIRE_CANDIDATE_ATTRIBUTE)) {
				bd.setAutowireCandidate(TRUE_VALUE.equals(ele.getAttribute(AUTOWIRE_CANDIDATE_ATTRIBUTE)));
			}

			String autowire = ele.getAttribute(AUTOWIRE_ATTRIBUTE);
			if (DEFAULT_VALUE.equals(autowire)) {
				autowire = getDefaultAutowire();
			}
			bd.setAutowireMode(getAutowireMode(autowire));

			String dependencyCheck = ele.getAttribute(DEPENDENCY_CHECK_ATTRIBUTE);
			if (DEFAULT_VALUE.equals(dependencyCheck)) {
				dependencyCheck = getDefaultDependencyCheck();
			}
			bd.setDependencyCheck(getDependencyCheck(dependencyCheck));

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

			parseMetaElements(ele, bd);
			parseLookupOverrideSubElements(ele, bd.getMethodOverrides());
			parseReplacedMethodSubElements(ele, bd.getMethodOverrides());

			parseConstructorArgElements(ele, bd);
			parsePropertyElements(ele, bd);

			bd.setResourceDescription(getReaderContext().getResource().getDescription());
			bd.setSource(extractSource(ele));

			return bd;
		}
		catch (ClassNotFoundException ex) {
			error("Bean class [" + className + "] not found", ele, ex);
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

	public void parseMetaElements(Element ele, AttributeAccessor attributeAccessor) {
		NodeList nl = ele.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && DomUtils.nodeNameEquals(node, META_ELEMENT)) {
				Element metaElement = (Element) node;
				String key = metaElement.getAttribute(KEY_ATTRIBUTE);
				String value = metaElement.getAttribute(VALUE_ATTRIBUTE);
				attributeAccessor.setAttribute(key, value);
			}
		}
	}

	/**
	 * Invoke the {@link org.springframework.beans.factory.parsing.SourceExtractor} to pull the
	 * source metadata from the supplied {@link Element}.
	 */
	protected Object extractSource(Element ele) {
		return getReaderContext().extractSource(ele);
	}

	public int getDependencyCheck(String att) {
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

	public int getAutowireMode(String att) {
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
	public void parseConstructorArgElements(Element beanEle, BeanDefinition bd) {
		NodeList nl = beanEle.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && DomUtils.nodeNameEquals(node, CONSTRUCTOR_ARG_ELEMENT)) {
				parseConstructorArgElement((Element) node, bd);
			}
		}
	}

	/**
	 * Parse property sub-elements of the given bean element.
	 */
	public void parsePropertyElements(Element beanEle, BeanDefinition bd) {
		NodeList nl = beanEle.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && DomUtils.nodeNameEquals(node, PROPERTY_ELEMENT)) {
				parsePropertyElement((Element) node, bd);
			}
		}
	}

	/**
	 * Parse lookup-override sub-elements of the given bean element.
	 */
	public void parseLookupOverrideSubElements(Element beanEle, MethodOverrides overrides) {
		NodeList nl = beanEle.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && DomUtils.nodeNameEquals(node, LOOKUP_METHOD_ELEMENT)) {
				Element ele = (Element) node;
				String methodName = ele.getAttribute(NAME_ATTRIBUTE);
				String beanRef = ele.getAttribute(BEAN_ELEMENT);
				LookupOverride override = new LookupOverride(methodName, beanRef);
				override.setSource(extractSource(ele));
				overrides.addOverride(override);
			}
		}
	}

	/**
	 * Parse replaced-method sub-elements of the given bean element.
	 */
	public void parseReplacedMethodSubElements(Element beanEle, MethodOverrides overrides) {
		NodeList nl = beanEle.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && DomUtils.nodeNameEquals(node, REPLACED_METHOD_ELEMENT)) {
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
				replaceOverride.setSource(extractSource(replacedMethodEle));
				overrides.addOverride(replaceOverride);
			}
		}
	}

	/**
	 * Parse a constructor-arg element.
	 */
	public void parseConstructorArgElement(Element ele, BeanDefinition bd) {
		String indexAttr = ele.getAttribute(INDEX_ATTRIBUTE);
		String typeAttr = ele.getAttribute(TYPE_ATTRIBUTE);
		if (StringUtils.hasLength(indexAttr)) {
			try {
				int index = Integer.parseInt(indexAttr);
				if (index < 0) {
					error("'index' cannot be lower than 0", ele);
				}
				try {
					this.parseState.push(new ConstructorArgumentEntry(index));
					Object value = parsePropertyValue(ele, bd, null);
					ConstructorArgumentValues.ValueHolder valueHolder = new ConstructorArgumentValues.ValueHolder(value);
					if (StringUtils.hasLength(typeAttr)) {
						valueHolder.setType(typeAttr);
					}
					valueHolder.setSource(extractSource(ele));
					bd.getConstructorArgumentValues().addIndexedArgumentValue(index, valueHolder);
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
				this.parseState.push(new ConstructorArgumentEntry());
				Object value = parsePropertyValue(ele, bd, null);
				ConstructorArgumentValues.ValueHolder valueHolder = new ConstructorArgumentValues.ValueHolder(value);
				if (StringUtils.hasLength(typeAttr)) {
					valueHolder.setType(typeAttr);
				}
				valueHolder.setSource(extractSource(ele));
				bd.getConstructorArgumentValues().addGenericArgumentValue(valueHolder);
			}
			finally {
				this.parseState.pop();
			}
		}
	}

	/**
	 * Parse a property element.
	 */
	public void parsePropertyElement(Element ele, BeanDefinition bd) {
		String propertyName = ele.getAttribute(NAME_ATTRIBUTE);
		if (!StringUtils.hasLength(propertyName)) {
			error("Tag 'property' must have a 'name' attribute", ele);
			return;
		}
		this.parseState.push(new PropertyEntry(propertyName));
		try {
			if (bd.getPropertyValues().contains(propertyName)) {
				error("Multiple 'property' definitions for property '" + propertyName + "'", ele);
				return;
			}
			Object val = parsePropertyValue(ele, bd, propertyName);
			PropertyValue pv = new PropertyValue(propertyName, val);
			parseMetaElements(ele, pv);
			pv.setSource(extractSource(ele));
			bd.getPropertyValues().addPropertyValue(pv);
		}
		finally {
			this.parseState.pop();
		}
	}


	/**
	 * Get the value of a property element. May be a list etc.
	 * Also used for constructor arguments, "propertyName" being null in this case.
	 */
	public Object parsePropertyValue(Element ele, BeanDefinition bd, String propertyName) {
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
					if (subElement != null && !META_ELEMENT.equals(subElement.getTagName())) {
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

		return parsePropertySubElement(subElement, bd);
	}

	public Object parsePropertySubElement(Element ele, BeanDefinition bd) {
		return parsePropertySubElement(ele, bd, null);
	}

	/**
	 * Parse a value, ref or collection sub-element of a property or
	 * constructor-arg element.
	 * @param ele subelement of property element; we don't know which yet
	 * @param defaultTypeClassName the default type (class name) for any
	 * <code>&lt;value&gt;</code> tag that might be created
	 */
	public Object parsePropertySubElement(Element ele, BeanDefinition bd, String defaultTypeClassName) {
		if (!isDefaultNamespace(ele.getNamespaceURI())) {
			return parseNestedCustomElement(ele, bd);
		}
		else if (DomUtils.nodeNameEquals(ele, BEAN_ELEMENT)) {
			return parseBeanDefinitionElement(ele, bd);
		}
		else if (DomUtils.nodeNameEquals(ele, REF_ELEMENT)) {
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
		else if (DomUtils.nodeNameEquals(ele, IDREF_ELEMENT)) {
			// A generic reference to any name of any bean.
			String beanRef = ele.getAttribute(BEAN_REF_ATTRIBUTE);
			if (!StringUtils.hasLength(beanRef)) {
				// A reference to the id of another bean in the same XML file.
				beanRef = ele.getAttribute(LOCAL_REF_ATTRIBUTE);
				if (!StringUtils.hasLength(beanRef)) {
					error("Either 'bean' or 'local' is required for <idref> element", ele);
				}
			}
			return new RuntimeBeanNameReference(beanRef);
		}
		else if (DomUtils.nodeNameEquals(ele, VALUE_ELEMENT)) {
			// It's a literal value.
			String value = DomUtils.getTextValue(ele);
			String typeClassName = ele.getAttribute(TYPE_ATTRIBUTE);
			if (!StringUtils.hasText(typeClassName)) {
				typeClassName = defaultTypeClassName;
			}
			if (StringUtils.hasText(typeClassName)) {
				try {
					return buildTypedStringValue(value, typeClassName);
				}
				catch (ClassNotFoundException ex) {
					error("Type class [" + typeClassName + "] not found for <value> element", ele, ex);
				}
			}
			return value;
		}
		else if (DomUtils.nodeNameEquals(ele, NULL_ELEMENT)) {
			// It's a distinguished null value.
			return null;
		}
		else if (DomUtils.nodeNameEquals(ele, LIST_ELEMENT)) {
			return parseListElement(ele, bd);
		}
		else if (DomUtils.nodeNameEquals(ele, SET_ELEMENT)) {
			return parseSetElement(ele, bd);
		}
		else if (DomUtils.nodeNameEquals(ele, MAP_ELEMENT)) {
			return parseMapElement(ele, bd);
		}
		else if (DomUtils.nodeNameEquals(ele, PROPS_ELEMENT)) {
			return parsePropsElement(ele);
		}
		error("Unknown property sub-element: [" + ele.getTagName() + "]", ele);
		return null;
	}

	/**
	 * Parse a list element.
	 */
	public List parseListElement(Element collectionEle, BeanDefinition bd) {
		String defaultTypeClassName = collectionEle.getAttribute(VALUE_TYPE_ATTRIBUTE);
		NodeList nl = collectionEle.getChildNodes();
		ManagedList list = new ManagedList(nl.getLength());
		list.setSource(extractSource(collectionEle));
		list.setMergeEnabled(parseMergeAttribute(collectionEle));
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element ele = (Element) nl.item(i);
				list.add(parsePropertySubElement(ele, bd, defaultTypeClassName));
			}
		}
		return list;
	}

	/**
	 * Parse a set element.
	 */
	public Set parseSetElement(Element collectionEle, BeanDefinition bd) {
		String defaultTypeClassName = collectionEle.getAttribute(VALUE_TYPE_ATTRIBUTE);
		NodeList nl = collectionEle.getChildNodes();
		ManagedSet set = new ManagedSet(nl.getLength());
		set.setSource(extractSource(collectionEle));
		set.setMergeEnabled(parseMergeAttribute(collectionEle));
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				Element ele = (Element) nl.item(i);
				set.add(parsePropertySubElement(ele, bd, defaultTypeClassName));
			}
		}
		return set;
	}

	/**
	 * Parse a map element.
	 */
	public Map parseMapElement(Element mapEle, BeanDefinition bd) {
		String defaultKeyTypeClassName = mapEle.getAttribute(KEY_TYPE_ATTRIBUTE);
		String defaultValueTypeClassName = mapEle.getAttribute(VALUE_TYPE_ATTRIBUTE);

		List entryEles = DomUtils.getChildElementsByTagName(mapEle, ENTRY_ELEMENT);
		ManagedMap map = new ManagedMap(entryEles.size());
		map.setMergeEnabled(parseMergeAttribute(mapEle));
		map.setSource(extractSource(mapEle));

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
					if (DomUtils.nodeNameEquals(candidateEle, KEY_ELEMENT)) {
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
				key = extractTypedStringValueIfNecessary(
						mapEle, entryEle.getAttribute(KEY_ATTRIBUTE), defaultKeyTypeClassName);
			}
			else if (hasKeyRefAttribute) {
				String refName = entryEle.getAttribute(KEY_REF_ATTRIBUTE);
				if (!StringUtils.hasText(refName)) {
					error("<entry> element contains empty 'key-ref' attribute", entryEle);
				}
				key = new RuntimeBeanReference(refName);
			}
			else if (keyEle != null) {
				key = parseKeyElement(keyEle, bd, defaultKeyTypeClassName);
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
				value = extractTypedStringValueIfNecessary(
						mapEle, entryEle.getAttribute(VALUE_ATTRIBUTE), defaultValueTypeClassName);
			}
			else if (hasValueRefAttribute) {
				String refName = entryEle.getAttribute(VALUE_REF_ATTRIBUTE);
				if (!StringUtils.hasText(refName)) {
					error("<entry> element contains empty 'value-ref' attribute", entryEle);
				}
				value = new RuntimeBeanReference(refName);
			}
			else if (valueEle != null) {
				value = parsePropertySubElement(valueEle, bd, defaultValueTypeClassName);
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
	 * If the supplied <code>defaultTypeClassName</code> argument is <code>null</code>
	 * or zero-length, then the value of the <code>attributeValue</code> is returned.
	 * Otherwise, if the <code>Class</code> named by the <code>defaultTypeClassName</code>
	 * can be loaded, a {@link TypedStringValue} instance wrapping this <code>Class</code>
	 * and the <code>attributeValue</code> is returned. Otherwise, <code>null</code>
	 * is returned.
	 */
	private Object extractTypedStringValueIfNecessary(
			Element mapElement, String attributeValue, String defaultTypeClassName) {

		if (!StringUtils.hasText(defaultTypeClassName)) {
			return attributeValue;
		}
		try {
			return buildTypedStringValue(attributeValue, defaultTypeClassName);
		}
		catch (ClassNotFoundException ex) {
			error("Unable to load class '" + defaultTypeClassName + "' for Map key/value type", mapElement, ex);
			return attributeValue;
		}
	}

	/**
	 * Parse a key sub-element of a map element.
	 */
	public Object parseKeyElement(Element keyEle, BeanDefinition bd, String defaultKeyTypeClassName) {
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
		return parsePropertySubElement(subElement, bd, defaultKeyTypeClassName);
	}

	/**
	 * Parse a props element.
	 */
	public Properties parsePropsElement(Element propsEle) {
		ManagedProperties props = new ManagedProperties();
		props.setSource(extractSource(propsEle));
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
	public boolean parseMergeAttribute(Element collectionElement) {
		String value = collectionElement.getAttribute(MERGE_ATTRIBUTE);
		if (DEFAULT_VALUE.equals(value)) {
			value = getDefaultMerge();
		}
		return TRUE_VALUE.equals(value);
	}

	public BeanDefinition parseCustomElement(Element ele) {
		return parseCustomElement(ele, null);
	}

	public BeanDefinition parseCustomElement(Element ele, BeanDefinition containingBd) {
		String namespaceUri = ele.getNamespaceURI();
		NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve(namespaceUri);
		if (handler == null) {
			getReaderContext().error("Unable to locate NamespaceHandler for namespace [" + namespaceUri + "]", ele);
			return null;
		}
		return handler.parse(ele, new ParserContext(getReaderContext(), this, containingBd));
	}

	public BeanDefinitionHolder decorateBeanDefinitionIfRequired(Element element, BeanDefinitionHolder definitionHolder) {
		BeanDefinitionHolder finalDefinition = definitionHolder;

		// decorate based on custom attributes first
		NamedNodeMap attributes = element.getAttributes();
		for(int i = 0; i < attributes.getLength(); i++) {
			Node node = attributes.item(i);
			finalDefinition = decorateIfRequired(node, finalDefinition);
		}

		// decorate based on custom nested elements
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				finalDefinition = decorateIfRequired(node, finalDefinition);
			}
		}
		return finalDefinition;
	}

	private BeanDefinitionHolder decorateIfRequired(Node node, BeanDefinitionHolder finalDefinition) {
		String uri = node.getNamespaceURI();
		if (!isDefaultNamespace(uri)) {
			NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve(uri);
			finalDefinition = handler.decorate(node, finalDefinition, new ParserContext(getReaderContext(), this));
		}
		return finalDefinition;
	}

	public boolean isDefaultNamespace(String namespaceUri) {
		return (!StringUtils.hasLength(namespaceUri) || BEANS_NAMESPACE_URI.equals(namespaceUri));
	}

	private Object parseNestedCustomElement(Element candidateEle, BeanDefinition containingBd) {
		BeanDefinition innerDefinition = parseCustomElement(candidateEle, containingBd);
		if (innerDefinition == null) {
			error("Incorrect usage of element '" + candidateEle.getNodeName() + "' in a nested manner. " +
					"This tag cannot be used nested inside <property>.", candidateEle);
			return null;
		}
		return innerDefinition;
	}

	protected TypedStringValue buildTypedStringValue(String value, String targetTypeName)
			throws ClassNotFoundException {

		ClassLoader classLoader = getReaderContext().getReader().getBeanClassLoader();
		if (classLoader != null) {
			Class targetType = ClassUtils.forName(targetTypeName, classLoader);
			return new TypedStringValue(value, targetType);
		}
		return new TypedStringValue(value, targetTypeName);
	}

	private void error(String message, Object source) {
		getReaderContext().error(message, source, this.parseState.snapshot());
	}

	private void error(String message, Object source, Throwable cause) {
		getReaderContext().error(message, source, this.parseState.snapshot(), cause);
	}

}
