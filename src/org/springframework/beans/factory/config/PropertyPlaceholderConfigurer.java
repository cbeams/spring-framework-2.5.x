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

package org.springframework.beans.factory.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.core.Constants;

/**
 * A property resource configurer that resolves placeholders in bean property values of
 * context definitions. It <i>pulls</i> values from a properties file into bean definitions.
 *
 * <p>The default placeholder syntax follows the Ant / Log4J / JSP EL style:<br><br>
 * <code>
 * &nbsp;&nbsp;${...}
 * </code>
 *
 * <p>Example XML context definition:<br><br>
 * <code>
 * &nbsp;&nbsp;&lt;bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource"&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;property name="driverClassName"&gt;&lt;value&gt;${driver}&lt;/value&gt;&lt;/property&gt;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;property name="url"&gt;&lt;value&gt;jdbc:${dbname}&lt;/value&gt;&lt;/property&gt;<br>
 * &nbsp;&nbsp;&lt;/bean&gt;
 * </code>
 *
 * <p>Example properties file:<br><br>
 * <code>
 * &nbsp;&nbsp;driver=com.mysql.jdbc.Driver<br>
 * &nbsp;&nbsp;dbname=mysql:mydb
 * </code>
 *
 * <p>PropertyPlaceholderConfigurer checks simple property values, lists, maps,
 * props, and bean names in bean references. Furthermore, placeholder values can
 * also cross-reference other placeholders, like:<br><br>
 * <code>
 * &nbsp;&nbsp;rootPath=myrootdir
 * &nbsp;&nbsp;subPath=${rootPath}/subdir
 * </code>
 *
 * <p>In contrast to PropertyOverrideConfigurer, this configurer allows to fill in
 * explicit placeholders in context definitions. Therefore, the original definition
 * cannot specify any default values for such bean properties, and the placeholder
 * properties file is supposed to contain an entry for each defined placeholder.
 *
 * <p>If a configurer cannot resolve a placeholder, a BeanDefinitionStoreException
 * will be thrown. If you want to check against multiple properties files, specify
 * multiple resources via the "locations" setting. You can also define multiple
 * PropertyPlaceholderConfigurers, each with its <i>own</i> placeholder syntax.
 *
 * <p>Default property values can be defined via "properties", to make overriding
 * definitions in properties files optional. A configurer will also check against
 * system properties (e.g. "user.dir") if it cannot resolve a placeholder with any
 * of the specified properties. This can be customized via "systemPropertiesMode".
 *
 * <p>Note that the context definition <i>is</i> aware of being incomplete;
 * this is immediately obvious when looking at the XML definition file.
 *
 * @author Juergen Hoeller
 * @since 02.10.2003
 * @see PropertyOverrideConfigurer
 * @see #setLocations
 * @see #setProperties
 * @see #setPlaceholderPrefix
 * @see #setPlaceholderSuffix
 * @see #setSystemPropertiesMode
 * @see System#getProperty(String)
 * @version $Id: PropertyPlaceholderConfigurer.java,v 1.9 2004-03-18 02:46:07 trisberg Exp $
 */
public class PropertyPlaceholderConfigurer extends PropertyResourceConfigurer {

	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";


	/** Never check system properties. */
	public static final int SYSTEM_PROPERTIES_MODE_NEVER = 0;

	/**
	 * Check system properties if not resolvable in the specified properties.
	 * This is the default.
	 */
	public static final int SYSTEM_PROPERTIES_MODE_FALLBACK = 1;

	/**
	 * Check system properties first, before trying the specified properties.
	 * This allows system properties to override any other property source.
	 */
	public static final int SYSTEM_PROPERTIES_MODE_OVERRIDE = 2;


	private static final Constants constants = new Constants(PropertyPlaceholderConfigurer.class);

	private String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

	private String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

	private int systemPropertiesMode = SYSTEM_PROPERTIES_MODE_FALLBACK;

	private boolean ignoreUnresolvablePlaceholders = false;


	/**
	 * Set the prefix that a placeholder string starts with.
	 * The default is "${".
	 * @see #DEFAULT_PLACEHOLDER_PREFIX
	 */
	public void setPlaceholderPrefix(String placeholderPrefix) {
		this.placeholderPrefix = placeholderPrefix;
	}

	/**
	 * Set the suffix that a placeholder string ends with.
	 * The default is "}".
	 * @see #DEFAULT_PLACEHOLDER_SUFFIX
	 */
	public void setPlaceholderSuffix(String placeholderSuffix) {
		this.placeholderSuffix = placeholderSuffix;
	}

	/**
	 * Set how to check system properties: as fallback, as override, or never.
	 * For example, will resolve ${user.dir} to the "user.dir" system property.
	 * <p>The default is "fallback": If not being able to resolve a placeholder
	 * with the specified properties, a system property will be tried.
	 * "override" will check for a system property first, before trying the
	 * specified properties. "never" will not check system properties at all.
	 * @see #SYSTEM_PROPERTIES_MODE_NEVER
	 * @see #SYSTEM_PROPERTIES_MODE_FALLBACK
	 * @see #SYSTEM_PROPERTIES_MODE_OVERRIDE
	 */
	public void setSystemPropertiesMode(int systemPropertiesMode) {
		this.systemPropertiesMode = systemPropertiesMode;
	}

	/**
	 * Set the system property mode by the name of the corresponding constant,
	 * e.g. "SYSTEM_PROPERTIES_MODE_OVERRIDE".
	 * @param constantName name of the constant
	 * @throws java.lang.IllegalArgumentException if an invalid constant was specified
	 * @see #setSystemPropertiesMode
	 */
	public void setSystemPropertiesModeName(String constantName) throws IllegalArgumentException {
		this.systemPropertiesMode = constants.asNumber(constantName).intValue();
	}

	/**
	 * Set whether to ignore unresolvable placeholders. Default is false:
	 * An exception will be thrown if a placeholder cannot not be resolved.
	 */
	public void setIgnoreUnresolvablePlaceholders(boolean ignoreUnresolvablePlaceholders) {
		this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
	}


	protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props)
			throws BeansException {
		String[] beanNames = beanFactory.getBeanDefinitionNames();
		for (int i = 0; i < beanNames.length; i++) {
			MutablePropertyValues pvs = beanFactory.getPropertyValues(beanNames[i]);
			if (pvs != null) {
				parsePropertyValues(props, pvs);
			}
			ConstructorArgumentValues cas = beanFactory.getConstructorArgumentValues(beanNames[i]);
			if (cas != null) {
				parseIndexedArgumentValues(props, cas.getIndexedArgumentValues());
				parseGenericArgumentValues(props, cas.getGenericArgumentValues());
			}
		}
	}

	protected void parsePropertyValues(Properties props, MutablePropertyValues pvs) {
		for (int j = 0; j < pvs.getPropertyValues().length; j++) {
			PropertyValue pv = pvs.getPropertyValues()[j];

			if (pv.getValue() instanceof String) {
				String strVal = (String) pv.getValue();
				String newStrVal = parseValue(props, strVal, null);
				if (!newStrVal.equals(strVal)) {
					pvs.addPropertyValue(pv.getName(), newStrVal);
				}
			}

			else if (pv.getValue() instanceof RuntimeBeanReference) {
        RuntimeBeanReference ref = (RuntimeBeanReference) pv.getValue();
        String newBeanName = parseValue(props, ref.getBeanName(), null);
				if (!newBeanName.equals(ref.getBeanName())) {
					RuntimeBeanReference newRef = new RuntimeBeanReference(newBeanName);
          pvs.addPropertyValue(pv.getName(), newRef);
				}
			}

			else if (pv.getValue() instanceof List) {
				parseList(props, (List) pv.getValue());
			}

			else if (pv.getValue() instanceof Map) {
				parseMap(props, (Map) pv.getValue());
			}
		}
	}

	protected void parseIndexedArgumentValues(Properties props, Map ias) {
		for (Iterator it = ias.keySet().iterator(); it.hasNext();) {
			Integer index = (Integer) it.next();
			ConstructorArgumentValues.ValueHolder valueHolder = (ConstructorArgumentValues.ValueHolder) ias.get(index);
			Object val = valueHolder.getValue();

			if (val instanceof String) {
				String strVal = (String) val;
				String newStrVal = parseValue(props, strVal, null);
				valueHolder.setValue(newStrVal);
			}

			else if (val instanceof RuntimeBeanReference) {
        RuntimeBeanReference ref = (RuntimeBeanReference) val;
        String newBeanName = parseValue(props, ref.getBeanName(), null);
				if (!newBeanName.equals(ref.getBeanName())) {
					RuntimeBeanReference newRef = new RuntimeBeanReference(newBeanName);
					valueHolder.setValue(newRef);
				}
			}

			else if (val instanceof List) {
				parseList(props, (List) val);
			}

			else if (val instanceof Map) {
				parseMap(props, (Map) val);
			}
		}
	}

	protected void parseGenericArgumentValues(Properties props, Set gas) {
		for (Iterator it = gas.iterator(); it.hasNext();) {
			ConstructorArgumentValues.ValueHolder valueHolder = (ConstructorArgumentValues.ValueHolder) it.next();
			Object val = valueHolder.getValue();

			if (val instanceof String) {
				String strVal = (String) val;
				String newStrVal = parseValue(props, strVal, null);
				valueHolder.setValue(newStrVal);
			}
			else if (val instanceof RuntimeBeanReference) {
				RuntimeBeanReference ref = (RuntimeBeanReference) val;
				String newBeanName = parseValue(props, ref.getBeanName(), null);
				if (!newBeanName.equals(ref.getBeanName())) {
					valueHolder.setValue(new RuntimeBeanReference(newBeanName));
				}
			}
			else if (val instanceof List) {
				parseList(props, (List) val);
			}
			else if (val instanceof Map) {
				parseMap(props, (Map) val);
			}
		}
	}

	/**
	 * Parse the given List, exchanging its values if necessary.
	 */
	protected void parseList(Properties props, List listVal) {
		for (int k = 0; k < listVal.size(); k++) {
			Object elem = listVal.get(k);
			if (elem instanceof String) {
				String strVal = (String) elem;
				String newStrVal = parseValue(props, strVal, null);
				if (!newStrVal.equals(strVal)) {
					listVal.set(k, newStrVal);
				}
			}
			else if (elem instanceof RuntimeBeanReference) {
				RuntimeBeanReference ref = (RuntimeBeanReference) elem;
				String newBeanName = parseValue(props, ref.getBeanName(), null);
				if (!newBeanName.equals(ref.getBeanName())) {
					RuntimeBeanReference newRef = new RuntimeBeanReference(newBeanName);
					listVal.set(k, newRef);
				}
			}
		}
	}

	/**
	 * Parse the given Map, exchanging its values if necessary.
	 */
	protected void parseMap(Properties props, Map mapVal) {
		for (Iterator it = new HashMap(mapVal).keySet().iterator(); it.hasNext();) {
			Object key = it.next();
			Object elem = mapVal.get(key);
			if (elem instanceof String) {
				String strVal = (String) elem;
				String newStrVal = parseValue(props, strVal, null);
				if (!newStrVal.equals(strVal)) {
					mapVal.put(key, newStrVal);
				}
			}
			else if (elem instanceof RuntimeBeanReference) {
				RuntimeBeanReference ref = (RuntimeBeanReference) elem;
				String newBeanName = parseValue(props, ref.getBeanName(), null);
				if (!newBeanName.equals(ref.getBeanName())) {
					RuntimeBeanReference newRef = new RuntimeBeanReference(newBeanName);
					mapVal.put(key, newRef);
				}
			}
		}
	}

	/**
	 * Parse values recursively to be able to resolve cross-references between placeholder values.
	 */
	protected String parseValue(Properties props, String strVal, String originalPlaceholder) throws BeansException {
		int startIndex = strVal.indexOf(this.placeholderPrefix);
		while (startIndex != -1) {
			int endIndex = strVal.indexOf(this.placeholderSuffix, startIndex + this.placeholderPrefix.length());
			if (endIndex != -1) {
				String placeholder = strVal.substring(startIndex + this.placeholderPrefix.length(), endIndex);
				if (originalPlaceholder == null) {
					originalPlaceholder = placeholder;
				}
				else if (placeholder.equals(originalPlaceholder)) {
					throw new BeanDefinitionStoreException("Circular placeholder reference '" + placeholder +
																								 "' in property definitions [" + props + "]");
				}
				String propVal = null;
				if (this.systemPropertiesMode == SYSTEM_PROPERTIES_MODE_OVERRIDE) {
					propVal = System.getProperty(placeholder);
				}
				if (propVal == null) {
					propVal = resolvePlaceholder(placeholder, props);
				}
				if (propVal == null && this.systemPropertiesMode == SYSTEM_PROPERTIES_MODE_FALLBACK) {
					propVal = System.getProperty(placeholder);
				}
				if (propVal != null) {
					propVal = parseValue(props, propVal, originalPlaceholder);
					logger.debug("Resolving placeholder '" + placeholder + "' to [" + propVal + "]");
					strVal = strVal.substring(0, startIndex) + propVal + strVal.substring(endIndex+1);
					startIndex = strVal.indexOf(this.placeholderPrefix, startIndex + propVal.length());
				}
				else if (this.ignoreUnresolvablePlaceholders) {
					// return unprocessed value
					return strVal;
				}
				else {
					throw new BeanDefinitionStoreException("Could not resolve placeholder '" + placeholder + "'");
				}
			}
			else {
				startIndex = -1;
			}
		}
		return strVal;
	}

	/**
	 * Resolve the given placeholder using the given properties.
	 * Default implementation simply checks for a corresponding property key.
	 * <p>Subclasses can override this for customized placeholder-to-key mappings
	 * or custom resolution strategies, possibly just using the given properties
	 * as fallback.
	 * <p>Note that system properties will still be checked before respectively
	 * after this method is invoked, according to the system properties mode.
	 * @param placeholder the placeholder to resolve
	 * @param props the merged properties of this configurer
	 * @return the resolved value
	 * @see #setSystemPropertiesMode
	 */
	protected String resolvePlaceholder(String placeholder, Properties props) {
		return props.getProperty(placeholder);
	}

}
