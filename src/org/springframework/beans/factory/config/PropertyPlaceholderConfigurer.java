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
 * of the specified properties. This can be turned off via "checkSystemProperties".
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
 * @see #setCheckSystemProperties
 * @see System#getProperty(String)
 */
public class PropertyPlaceholderConfigurer extends PropertyResourceConfigurer {

	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";


	private String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

	private String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

	private boolean checkSystemProperties = true;


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
	 * Whether to check system properties if not being able to resolve a
	 * placeholder with the specified properties. For example, will resolve
	 * ${user.dir} to the "user.dir" system property (if not overridden
	 * in the specified properties).
	 * <p>Default is true.
	 */
	public void setCheckSystemProperties(boolean checkSystemProperties) {
		this.checkSystemProperties = checkSystemProperties;
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
			Object val = ias.get(index);

			if (val instanceof String) {
				String strVal = (String) val;
				String newStrVal = parseValue(props, strVal, null);
				if (!newStrVal.equals(strVal)) {
					ias.put(index, newStrVal);
				}
			}

			else if (val instanceof RuntimeBeanReference) {
        RuntimeBeanReference ref = (RuntimeBeanReference) val;
        String newBeanName = parseValue(props, ref.getBeanName(), null);
				if (!newBeanName.equals(ref.getBeanName())) {
					RuntimeBeanReference newRef = new RuntimeBeanReference(newBeanName);
					ias.put(index, newRef);
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
			Object val = it.next();

			if (val instanceof String) {
				String strVal = (String) val;
				String newStrVal = parseValue(props, strVal, null);
				if (!newStrVal.equals(strVal)) {
					gas.remove(val);
					gas.add(newStrVal);
				}
			}

			else if (val instanceof RuntimeBeanReference) {
        RuntimeBeanReference ref = (RuntimeBeanReference) val;
        String newBeanName = parseValue(props, ref.getBeanName(), null);
				if (!newBeanName.equals(ref.getBeanName())) {
					RuntimeBeanReference newRef = new RuntimeBeanReference(newBeanName);
					gas.remove(val);
					gas.add(newRef);
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
				String propVal = props.getProperty(placeholder);
				if (propVal == null && this.checkSystemProperties) {
					// try system property (e.g. "user.dir")
					propVal = System.getProperty(placeholder);
				}
				if (propVal != null) {
					propVal = parseValue(props, propVal, originalPlaceholder);
					logger.debug("Resolving placeholder '" + placeholder + "' to [" + propVal + "]");
					strVal = strVal.substring(0, startIndex) + propVal + strVal.substring(endIndex+1);
					startIndex = strVal.indexOf(this.placeholderPrefix, startIndex + propVal.length());
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

}
