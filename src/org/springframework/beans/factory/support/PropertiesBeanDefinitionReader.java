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

package org.springframework.beans.factory.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.core.io.Resource;

/**
 * Bean definition reader for a simple properties format.
 * Provides bean definition registration methods for Map/Properties and
 * ResourceBundle. Typically applied to a DefaultListableBeanFactory.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 26.11.2003
 * @see DefaultListableBeanFactory
 */
public class PropertiesBeanDefinitionReader extends AbstractBeanDefinitionReader {

	/**
	 * Value of a T/F attribute that represents true.
	 * Anything else represents false. Case seNsItive.
	 */
	public static final String TRUE_VALUE = "true";

	/**
	 * Separator between bean name and property name.
	 * We follow normal Java conventions.
	 */
	public static final String SEPARATOR = ".";

	/**
	 * Prefix for the class property of a root bean definition.
	 */
	public static final String CLASS_KEY = "class";

	/**
	 * Special string added to distinguish owner.(abstract)=true
	 * Default is false.
	 */
	public static final String ABSTRACT_KEY = "(abstract)";

	/**
	 * Special string added to distinguish owner.(singleton)=true
	 * Default is true.
	 */
	public static final String SINGLETON_KEY = "(singleton)";

	/**
	 * Special string added to distinguish owner.(lazy-init)=true
	 * Default is false.
	 */
	public static final String LAZY_INIT_KEY = "(lazy-init)";

	/**
	 * Reserved "property" to indicate the parent of a child bean definition.
	 */
	public static final String PARENT_KEY = "parent";

	/**
	 * Property suffix for references to other beans in the current
	 * BeanFactory: e.g. owner.dog(ref)=fido.
	 * Whether this is a reference to a singleton or a prototype
	 * will depend on the definition of the target bean.
	 */
	public static final String REF_SUFFIX = "(ref)";

	/**
	 * Prefix before values referencing other beans.
	 */
	public static final String REF_PREFIX = "*";


	/** Name of default parent bean */
	private String defaultParentBean;


	/**
	 * Create new PropertiesBeanDefinitionReader for the given bean factory.
	 */
	public PropertiesBeanDefinitionReader(BeanDefinitionRegistry beanFactory) {
		super(beanFactory);
	}

	/**
	 * Set the default parent bean for this bean factory.
	 * If a child bean definition handled by this factory provides neither
	 * a parent nor a class attribute, this default value gets used.
	 * <p>Can be used e.g. for view definition files, to define a parent
	 * with a default view class and common attributes for all views.
	 * View definitions that define their own parent or carry their own
	 * class can still override this.
	 * <p>Strictly speaking, the rule that a default parent setting does
	 * not apply to a bean definition that carries a class is there for
	 * backwards compatiblity reasons. It still matches the typical use case.
	 */
	public void setDefaultParentBean(String defaultParentBean) {
		this.defaultParentBean = defaultParentBean;
	}

	/**
	 * Return the default parent bean for this bean factory.
	 */
	public String getDefaultParentBean() {
		return defaultParentBean;
	}


	/**
	 * Load bean definitions from the specified properties file,
	 * using all property keys (i.e. not filtering by prefix).
	 * @param resource the resource descriptor for the properties file
	 * @return the number of bean definitions found
	 * @throws BeansException in case of loading or parsing errors
	 * @see #loadBeanDefinitions(org.springframework.core.io.Resource, String)
	 */
	public int loadBeanDefinitions(Resource resource) {
		return loadBeanDefinitions(resource, null);
	}

	/**
	 * Load bean definitions from the specified properties file.
	 * @param resource the resource descriptor for the properties file
	 * @return the number of bean definitions found
	 * @throws BeansException in case of loading or parsing errors
	 */
	public int loadBeanDefinitions(Resource resource, String prefix) {
		Properties props = new Properties();
		try {
			InputStream is = resource.getInputStream();
			try {
				props.load(is);
			}
			finally {
				is.close();
			}
			return registerBeanDefinitions(props, prefix, resource.getDescription());
		}
		catch (IOException ex) {
			throw new BeanDefinitionStoreException("IOException parsing properties from " + resource, ex);
		}
	}

	/**
	 * Register bean definitions contained in a resource bundle,
	 * using all property keys (i.e. not filtering by prefix).
	 * @return the number of bean definitions found
	 * @throws BeansException in case of loading or parsing errors
	 * @see #registerBeanDefinitions(java.util.ResourceBundle, String)
	 */
	public int registerBeanDefinitions(ResourceBundle rb) throws BeanDefinitionStoreException {
		return registerBeanDefinitions(rb, null);
	}

	/**
	 * Register bean definitions contained in a ResourceBundle.
	 * <p>Similar syntax as for a Map. This method is useful to enable
	 * standard Java internationalization support.
	 * @return the number of bean definitions found
	 * @throws BeansException in case of loading or parsing errors
	 */
	public int registerBeanDefinitions(ResourceBundle rb, String prefix) throws BeanDefinitionStoreException {
		// Simply create a map and call overloaded method
		Map m = new HashMap();
		Enumeration keys = rb.getKeys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			m.put(key, rb.getObject(key));
		}
		return registerBeanDefinitions(m, prefix);
	}

	/**
	 * Register bean definitions contained in a Map,
	 * using all property keys (i.e. not filtering by prefix).
	 * @return the number of bean definitions found
	 * @throws BeansException in case of loading or parsing errors
	 * @see #registerBeanDefinitions(java.util.Map, String, String)
	 */
	public int registerBeanDefinitions(Map map) throws BeansException {
		return registerBeanDefinitions(map, null);
	}

	/**
	 * Register bean definitions contained in a Map.
	 * Ignore ineligible properties.
	 * @param map Map name -> property (String or Object). Property values
	 * will be strings if coming from a Properties file etc. Property names
	 * (keys) <b>must</b> be strings. Class keys must be Strings.
	 * <code>
	 * employee.class=MyClass              // special property
	 * //employee.abstract=true              // this prototype can't be instantiated directly
	 * employee.group=Insurance Services   // real property
	 * employee.usesDialUp=false           // default unless overriden
	 *
	 * employee.manager(ref)=tony		   // reference to another prototype defined in the same file
	 *									   // cyclic and unresolved references will be detected
	 * salesrep.parent=employee
	 * salesrep.department=Sales and Marketing
	 *
	 * techie.parent=employee
	 * techie.department=Software Engineering
	 * techie.usesDialUp=true              // overridden property
	 * </code>
	 * @param prefix The match or filter within the keys in the map: e.g. 'beans.'
	 * @return the number of bean definitions found
	 * @throws BeansException in case of loading or parsing errors
	 */
	public int registerBeanDefinitions(Map map, String prefix) throws BeansException {
		return registerBeanDefinitions(map, prefix, "(no description)");
	}

	/**
	 * Register bean definitions contained in a Map.
	 * Ignore ineligible properties.
	 * @param map Map name -> property (String or Object). Property values
	 * will be strings if coming from a Properties file etc. Property names
	 * (keys) <b>must</b> be strings. Class keys must be Strings.
	 * @param prefix match or filter within the keys in the map: e.g. 'beans.'
	 * @param resourceDescription description of the resource that the Map came from
	 * (for logging purposes)
	 * @return the number of bean definitions found
	 * @throws BeansException in case of loading or parsing errors
	 * @see #registerBeanDefinitions(Map, String)
	 */
	public int registerBeanDefinitions(Map map, String prefix, String resourceDescription) throws BeansException {
		if (prefix == null) {
			prefix = "";
		}
		int beanCount = 0;

		Set keys = map.keySet();
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			if (key.startsWith(prefix)) {
				// Key is of form prefix<name>.property
				String nameAndProperty = key.substring(prefix.length());
				int sepIndx = nameAndProperty.indexOf(SEPARATOR);
				if (sepIndx != -1) {
					String beanName = nameAndProperty.substring(0, sepIndx);
					if (logger.isDebugEnabled()) {
						logger.debug("Found bean name '" + beanName + "'");
					}
					if (!getBeanFactory().containsBeanDefinition(beanName)) {
						// If we haven't already registered it...
						registerBeanDefinition(beanName, map, prefix + beanName, resourceDescription);
						++beanCount;
					}
				}
				else {
					// Ignore it: it wasn't a valid bean name and property,
					// although it did start with the required prefix
					if (logger.isDebugEnabled()) {
						logger.debug("Invalid bean name and property [" + nameAndProperty + "]");
					}
				}
			}	// if the key started with the prefix we're looking for
		}	// while there are more keys

		return beanCount;
	}

	/**
	 * Get all property values, given a prefix (which will be stripped)
	 * and add the bean they define to the factory with the given name
	 * @param beanName name of the bean to define
	 * @param map Map containing string pairs
	 * @param prefix prefix of each entry, which will be stripped
	 * @param resourceDescription description of the resource that the Map came from
	 * (for logging purposes)
	 * @throws BeansException if the bean definition could not be parsed or registered
	 */
	protected void registerBeanDefinition(String beanName, Map map, String prefix, String resourceDescription)
			throws BeansException {

		String className = null;
		String parent = null;
		boolean isAbstract = false;
		boolean singleton = true;
		boolean lazyInit = false;

		MutablePropertyValues pvs = new MutablePropertyValues();
		Set keys = map.keySet();
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			if (key.startsWith(prefix + SEPARATOR)) {
				String property = key.substring(prefix.length() + SEPARATOR.length());
				if (property.equals(CLASS_KEY)) {
					className = (String) map.get(key);
				}
				else if (property.equals(ABSTRACT_KEY)) {
					String val = (String) map.get(key);
					isAbstract = val.equals(TRUE_VALUE);
				}
				else if (property.equals(SINGLETON_KEY)) {
					String val = (String) map.get(key);
					singleton = (val == null) || val.equals(TRUE_VALUE);
				}
				else if (property.equals(LAZY_INIT_KEY)) {
					String val = (String) map.get(key);
					lazyInit = val.equals(TRUE_VALUE);
				}
				else if (property.equals(PARENT_KEY)) {
					parent = (String) map.get(key);
				}
				else if (property.endsWith(REF_SUFFIX)) {
					// This isn't a real property, but a reference to another prototype
					// Extract property name: property is of form dog(ref)
					property = property.substring(0, property.length() - REF_SUFFIX.length());
					String ref = (String) map.get(key);

					// It doesn't matter if the referenced bean hasn't yet been registered:
					// this will ensure that the reference is resolved at rungime
					// Default is not to use singleton
					Object val = new RuntimeBeanReference(ref);
					pvs.addPropertyValue(new PropertyValue(property, val));
				}
				else{
					// normal bean property
					Object val = map.get(key);
					if (val instanceof String) {
						String strVal = (String) val;
						// if it starts with a reference prefix...
						if (strVal.startsWith(REF_PREFIX)) {
							// expand reference
							String targetName = strVal.substring(1);
							if (targetName.startsWith(REF_PREFIX)) {
								// escaped prefix -> use plain value
								val = targetName;
							}
							else {
								val = new RuntimeBeanReference(targetName);
							}
						}
					}
					pvs.addPropertyValue(new PropertyValue(property, val));
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug(pvs.toString());
		}

		// Just use default parent if we're not dealing with the parent itself,
		// and if there's no class name specified. The latter has to happen for
		// backwards compatibility reasons.
		if (parent == null && className == null && !beanName.equals(this.defaultParentBean)) {
			parent = this.defaultParentBean;
		}

		try {
			AbstractBeanDefinition bd = BeanDefinitionReaderUtils.createBeanDefinition(
					className, parent, null, pvs, getBeanClassLoader());
			bd.setAbstract(isAbstract);
			bd.setSingleton(singleton);
			bd.setLazyInit(lazyInit);
			getBeanFactory().registerBeanDefinition(beanName, bd);
		}
		catch (ClassNotFoundException ex) {
			throw new BeanDefinitionStoreException(resourceDescription, beanName,
			                                       "Bean class [" + className + "] not found", ex);
		}
		catch (NoClassDefFoundError err) {
			throw new BeanDefinitionStoreException(resourceDescription, beanName,
			                                       "Class that bean class [" + className + "] depends on not found", err);
		}
	}

}
