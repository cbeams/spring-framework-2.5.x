/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.beans.factory.support;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanCircularReferenceException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.StringUtils;

/**
 * Concrete implementation of ListableBeanFactory.
 * Includes convenient methods to populate the factory from a Map
 * and a ResourceBundle, and to add bean defintions one by one.
 *
 * <p>Can be used as a standalone bean factory,
 * or as a superclass for custom bean factories.
 *
 * @author Rod Johnson
 * @since 16 April 2001
 * @version $Id: DefaultListableBeanFactory.java,v 1.1 2003-11-22 17:20:29 jhoeller Exp $
 */
public class DefaultListableBeanFactory extends AbstractBeanFactory implements ConfigurableListableBeanFactory {

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


	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------

	/** Map of BeanDefinition objects, keyed by prototype name */
	private Map beanDefinitionMap = new HashMap();

	/** Name of default parent bean */
	private String defaultParentBean;


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * Create a new DefaultListableBeanFactory.
	 */
	public DefaultListableBeanFactory() {
		super();
	}

	/**
	 * Create a new DefaultListableBeanFactory with the given parent.
	 */
	public DefaultListableBeanFactory(BeanFactory parentBeanFactory) {
		super(parentBeanFactory);
	}

	/**
	 * Set the default parent bean for this bean factory.
	 * If a child bean definition (i.e. a definition without class
	 * attribute) handled by this factory doesn't provide a parent
	 * attribute, this default value gets used.
	 * <p>Can be used e.g. for view definition files, to define a
	 * parent with common attributes for all views.
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


	//---------------------------------------------------------------------
	// Implementation of ListableBeanFactory
	//---------------------------------------------------------------------

	public int getBeanDefinitionCount() {
		return this.beanDefinitionMap.size();
	}

	public String[] getBeanDefinitionNames() {
		return getBeanDefinitionNames(null);
	}

	/**
	 * Note that this method is slow. Don't invoke it too often:
	 * it's best used only in application initialization.
	 */
	public String[] getBeanDefinitionNames(Class type) {
		Set keys = this.beanDefinitionMap.keySet();
		Set matches = new HashSet();
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			String name = (String) itr.next();
			if (type == null || type.isAssignableFrom(getMergedBeanDefinition(name, false).getBeanClass())) {
				matches.add(name);
			}
		}
		matches.addAll(Arrays.asList(getSingletonNames(type)));
		return (String[]) matches.toArray(new String[matches.size()]);
	}

	public Map getBeansOfType(Class type, boolean includePrototypes, boolean includeFactoryBeans) {
		String[] beanNames = getBeanDefinitionNames(type);
		Map result = new HashMap();
		for (int i = 0; i < beanNames.length; i++) {
			if (includePrototypes || isSingleton(beanNames[i])) {
				result.put(beanNames[i], getBean(beanNames[i]));
			}
		}
		if (includeFactoryBeans) {
			String[] factoryNames = getBeanDefinitionNames(FactoryBean.class);
			for (int i = 0; i < factoryNames.length; i++) {
				try {
					FactoryBean factory = (FactoryBean) getBean(FACTORY_BEAN_PREFIX + factoryNames[i]);
					Class objectType = factory.getObjectType();
					if ((objectType == null && factory.isSingleton()) ||
							((factory.isSingleton() || includePrototypes) &&
							objectType != null && type.isAssignableFrom(objectType))) {
						Object createdObject = getBean(factoryNames[i]);
						if (type.isInstance(createdObject)) {
							result.put(factoryNames[i], createdObject);
						}
					}
				}
				catch (FactoryBeanCircularReferenceException ex) {
					// we're currently creating that FactoryBean
					// sensible to ignore it, as we are just looking for a certain type
					logger.debug("Ignoring exception on FactoryBean type check", ex);
				}
			}
		}
		return result;
	}


	//---------------------------------------------------------------------
	// Public methods
	//---------------------------------------------------------------------

	/**
	 * Ensure that even potentially unreferenced singletons are instantiated.
	 * Subclasses or callers should invoke this if they want this behavior.
	 */
	public void preInstantiateSingletons() {
		// Ensure that unreferenced singletons are instantiated
		logger.info("Pre-instantiating singletons in factory [" + this + "]");
		String[] beanNames = getBeanDefinitionNames();
		for (int i = 0; i < beanNames.length; i++) {
			RootBeanDefinition bd = getMergedBeanDefinition(beanNames[i], false);
			if (bd.isSingleton() && !bd.isLazyInit()) {
				if (FactoryBean.class.isAssignableFrom(bd.getBeanClass())) {
					FactoryBean factory = (FactoryBean) getBean(FACTORY_BEAN_PREFIX + beanNames[i]);
					if (factory.isSingleton()) {
						getBean(beanNames[i]);
					}
				}
				else {
					getBean(beanNames[i]);
				}
			}
		}
	}

	/**
	 * Subclasses or users should call this method to register new bean definitions
	 * with this class. All other registration methods in this class use this method.
	 * <p>This method isn't guaranteed to be threadsafe. It should be called
	 * before any bean instances are accessed.
	 * @param beanName name of the bean instance to register
	 * @param beanDefinition definition of the bean instance to register
	 * @throws BeanDefinitionStoreException in the bean definition is invalid
	 */
	public void registerBeanDefinition(String beanName, AbstractBeanDefinition beanDefinition)
			throws BeanDefinitionStoreException {
		try {
			beanDefinition.validate();
		}
		catch (BeanDefinitionValidationException ex) {
			throw new BeanDefinitionStoreException("Validation of bean definition with name '" + beanName + "' failed", ex);
		}
		Object oldBeanDefinition = this.beanDefinitionMap.get(beanName);
		if (oldBeanDefinition != null) {
			throw new BeanDefinitionStoreException("Could not register bean definition [" + beanDefinition +
			                                       "] under bean name '" + beanName + "': there's already bean definition [" +
			                                       oldBeanDefinition + " bound");
		}
		this.beanDefinitionMap.put(beanName, beanDefinition);
	}

	/**
	 * Register bean definitions in a resource bundle,
	 * using all property keys (i.e. not filtering by prefix).
	 * @see #registerBeanDefinitions(ResourceBundle, String)
	 * @throws BeanDefinitionStoreException in a bean definition is invalid
	 */
	public int registerBeanDefinitions(ResourceBundle rb) throws BeanDefinitionStoreException {
		return registerBeanDefinitions(rb, null);
	}

	/**
	 * Register bean definitions in a ResourceBundle. Similar syntax
	 * as for a Map. This method is useful to enable standard
	 * Java internationalization support.
	 * @throws BeanDefinitionStoreException in a bean definition is invalid
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
	 * Register bean definitions in a properties file,
	 * using all property keys (i.e. not filtering by prefix).
	 * @see #registerBeanDefinitions(Map, String)
	 * @throws BeanDefinitionStoreException in a bean definition is invalid
	 */
	public int registerBeanDefinitions(Map m) throws BeansException {
		return registerBeanDefinitions(m, null);
	}

	/**
	 * Register valid bean definitions in a properties file.
	 * Ignore ineligible properties.
	 * @param m Map name -> property (String or Object). Property values
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
	 * @param prefix The match or filter within the keys
	 * in the map: e.g. 'beans.'
	 * @return the number of bean definitions found
	 * @throws BeanDefinitionStoreException in a bean definition is invalid
	 */
	public int registerBeanDefinitions(Map m, String prefix) throws BeansException {
		if (prefix == null)
			prefix = "";
		int beanCount = 0;

		Set keys = m.keySet();
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			if (key.startsWith(prefix)) {
				// Key is of form prefix<name>.property
				String nameAndProperty = key.substring(prefix.length());
				int sepIndx = nameAndProperty.indexOf(SEPARATOR);
				if (sepIndx != -1) {
					String beanName = nameAndProperty.substring(0, sepIndx);
					logger.debug("Found bean name '" + beanName + "'");
					if (this.beanDefinitionMap.get(beanName) == null) {
						// If we haven't already registered it...
						registerBeanDefinition(beanName, m, prefix + beanName);
						++beanCount;
					}
				}
				else {
					// Ignore it: it wasn't a valid bean name and property,
					// although it did start with the required prefix
					logger.debug("Invalid bean name and property [" + nameAndProperty + "]");
				}
			}	// if the key started with the prefix we're looking for
		}	// while there are more keys

		return beanCount;
	}

	/**
	 * Get all property values, given a prefix (which will be stripped)
	 * and add the bean they define to the factory with the given name
	 * @param beanName name of the bean to define
	 * @param m Map containing string pairs
	 * @param prefix prefix of each entry, which will be stripped
	 * @throws BeanDefinitionStoreException in the bean definition is invalid
	 */
	private void registerBeanDefinition(String beanName, Map m, String prefix) throws BeansException {
		String className = null;
		String parent = null;
		boolean singleton = true;
		boolean lazyInit = false;

		MutablePropertyValues pvs = new MutablePropertyValues();
		Set keys = m.keySet();
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			if (key.startsWith(prefix + SEPARATOR)) {
				String property = key.substring(prefix.length() + SEPARATOR.length());
				if (property.equals(CLASS_KEY)) {
					className = (String) m.get(key);
				}
				else if (property.equals(SINGLETON_KEY)) {
					String val = (String) m.get(key);
					singleton = (val == null) || val.equals(TRUE_VALUE);
				}
				else if (property.equals(LAZY_INIT_KEY)) {
					String val = (String) m.get(key);
					lazyInit = val.equals(TRUE_VALUE);
				}
				else if (property.equals(PARENT_KEY)) {
					parent = (String) m.get(key);
				}
				else if (property.endsWith(REF_SUFFIX)) {
					// This isn't a real property, but a reference to another prototype
					// Extract property name: property is of form dog(ref)
					property = property.substring(0, property.length() - REF_SUFFIX.length());
					String ref = (String) m.get(key);

					// It doesn't matter if the referenced bean hasn't yet been registered:
					// this will ensure that the reference is resolved at rungime
					// Default is not to use singleton
					Object val = new RuntimeBeanReference(ref);
					pvs.addPropertyValue(new PropertyValue(property, val));
				}
				else{
					// Normal bean property
					Object val = m.get(key);
					if (val instanceof String) {
						String sval = (String) val;
						// If it starts with unescaped prefix...
						if (sval.startsWith(REF_PREFIX)) {
							// Expand reference
							String targetName = ((String) val).substring(1);
							if (sval.startsWith("**")) {
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

		if (parent == null)
			parent = defaultParentBean;

		if (className == null && parent == null)
			throw new FatalBeanException("Invalid bean definition. Classname or parent must be supplied for bean with name '" +
			                             beanName + "'", null);

		try {
			AbstractBeanDefinition beanDefinition = null;
			if (className != null) {
				// Load the class using a special class loader if one is available.
				// Otherwise rely on the thread context classloader.
				Class clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
				beanDefinition = new RootBeanDefinition(clazz, pvs);
			}
			else {
				beanDefinition = new ChildBeanDefinition(parent, pvs);
			}

			beanDefinition.setSingleton(singleton);
			beanDefinition.setLazyInit(lazyInit);
			registerBeanDefinition(beanName, beanDefinition);
		}
		catch (ClassNotFoundException ex) {
			throw new FatalBeanException("Cannot find class [" + className + "] for bean with name '" + beanName + "'", ex);
		}
	}


	//---------------------------------------------------------------------
	// Implementation of superclass protected abstract methods
	//---------------------------------------------------------------------

	protected AbstractBeanDefinition getBeanDefinition(String beanName) throws NoSuchBeanDefinitionException {
		AbstractBeanDefinition bd = (AbstractBeanDefinition) this.beanDefinitionMap.get(beanName);
		if (bd == null) {
			throw new NoSuchBeanDefinitionException(beanName, toString());
		}
		return bd;
	}

	protected Map findMatchingBeans(Class requiredType) {
		return getBeansOfType(requiredType, true, true);
	}


	public String toString() {
		return getClass().getName() + " with defined beans [" + StringUtils.arrayToDelimitedString(getBeanDefinitionNames(), ",") + "]";
	}

}
