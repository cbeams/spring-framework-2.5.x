package org.springframework.beans.factory.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.RuntimeBeanReference;

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
 * properties file has to contain an entry for each defined placeholder.
 *
 * <p>Note that the context definition <i>is</i> aware of being incomplete;
 * this is immediately obvious when looking at the XML definition file.
 *
 * <p>In case of multiple PropertyPlaceholderConfigurers that define different values for
 * the same placeholder, the <i>first</i> one will win (due to the replacement mechanism).
 *
 * @author Juergen Hoeller
 * @since 02.10.2003
 * @see PropertyOverrideConfigurer
 */
public class PropertyPlaceholderConfigurer extends PropertyResourceConfigurer {

	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	private String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

	private String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

	/**
	 * Set the prefix that a placeholder string starts with.
	 * The default is "${".
	 */
	public void setPlaceholderPrefix(String placeholderPrefix) {
		this.placeholderPrefix = placeholderPrefix;
	}

	/**
	 * Set the suffix that a placeholder string ends with.
	 * The default is "}".
	 */
	public void setPlaceholderSuffix(String placeholderSuffix) {
		this.placeholderSuffix = placeholderSuffix;
	}

	protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props)
			throws BeansException {
		String[] beanNames = beanFactory.getBeanDefinitionNames();
		for (int i = 0; i < beanNames.length; i++) {
			String beanName = beanNames[i];
			PropertyValues pvs = beanFactory.getPropertyValues(beanName);
			if (pvs != null) {
				for (int j = 0; j < pvs.getPropertyValues().length; j++) {
					PropertyValue pv = pvs.getPropertyValues()[j];

					if (pv.getValue() instanceof String) {
						String strVal = (String) pv.getValue();
						String newStrVal = parseValue(props, strVal);
						if (!newStrVal.equals(strVal)) {
							beanFactory.overridePropertyValue(beanName, new PropertyValue(pv.getName(), newStrVal));
							logger.debug("Property '" + beanName + "." + pv.getName() + "' set to [" + newStrVal + "]");
						}
					}
					else if (pv.getValue() instanceof RuntimeBeanReference) {
            RuntimeBeanReference ref = (RuntimeBeanReference) pv.getValue();
            String newBeanName = parseValue(props, ref.getBeanName());
						if (!newBeanName.equals(ref.getBeanName())) {
							RuntimeBeanReference newRef = new RuntimeBeanReference(newBeanName);
              beanFactory.overridePropertyValue(beanName, new PropertyValue(pv.getName(), newRef));
							logger.debug("Property '" + beanName + "." + pv.getName() + "' set to bean reference '" + beanName + "'");
						}
					}

					else if (pv.getValue() instanceof List) {
						List listVal = (List) pv.getValue();
						for (int k = 0; k < listVal.size(); k++) {
							Object elem = listVal.get(k);
							if (elem instanceof String) {
								String strVal = (String) elem;
								String newStrVal = parseValue(props, strVal);
								if (!newStrVal.equals(strVal)) {
									listVal.set(k, newStrVal);
									logger.debug("Property '" + beanName + "." + pv.getName() + "' set to [" + newStrVal + "]");
								}
							}
							else if (elem instanceof RuntimeBeanReference) {
								RuntimeBeanReference ref = (RuntimeBeanReference) elem;
								String newBeanName = parseValue(props, ref.getBeanName());
								if (!newBeanName.equals(ref.getBeanName())) {
									RuntimeBeanReference newRef = new RuntimeBeanReference(newBeanName);
									listVal.set(k, newRef);
									logger.debug("List element '" + k + "' of property '" + beanName + "." + pv.getName() +
															 "' set to bean reference '" + beanName + "'");
								}
							}
						}
					}

					else if (pv.getValue() instanceof Map) {
						Map mapVal = (Map) pv.getValue();
						for (Iterator it = new HashMap(mapVal).keySet().iterator(); it.hasNext();) {
							Object key = it.next();
							Object elem = mapVal.get(key);
							if (elem instanceof String) {
								String strVal = (String) elem;
								String newStrVal = parseValue(props, strVal);
								if (!newStrVal.equals(strVal)) {
									mapVal.put(key, newStrVal);
									logger.debug("Property '" + beanName + "." + pv.getName() + "' set to [" + newStrVal + "]");
								}
							}
							else if (elem instanceof RuntimeBeanReference) {
								RuntimeBeanReference ref = (RuntimeBeanReference) elem;
								String newBeanName = parseValue(props, ref.getBeanName());
								if (!newBeanName.equals(ref.getBeanName())) {
									RuntimeBeanReference newRef = new RuntimeBeanReference(newBeanName);
									mapVal.put(key, newRef);
									logger.debug("Map element '" + key + "' of property '" + beanName + "." + pv.getName() +
															 "' set to bean reference '" + beanName + "'");
								}
							}
						}
					}
				}
			}
		}
	}

	protected String parseValue(Properties prop, String strVal) throws BeansException {
    return parseValue(prop, strVal, null);
	}

	/**
	 * Parse values recursively to be able to resolve cross-references between placeholder values.
	 */
	protected String parseValue(Properties prop, String strVal, String originalPlaceholder) throws BeansException {
		int startIndex = strVal.indexOf(this.placeholderPrefix);
		while (startIndex != -1) {
			int endIndex = strVal.indexOf(this.placeholderSuffix, startIndex + this.placeholderPrefix.length());
			if (startIndex != -1 && endIndex != -1) {
				String placeholder = strVal.substring(startIndex + this.placeholderPrefix.length(), endIndex);
				if (originalPlaceholder == null) {
					originalPlaceholder = placeholder;
				}
				else if (placeholder.equals(originalPlaceholder)) {
					throw new BeanDefinitionStoreException("Circular placeholder reference '" + placeholder +
																								 "' in property definitions [" + prop + "]");
				}
				String propVal = prop.getProperty(placeholder);
				if (propVal != null) {
					propVal = parseValue(prop, propVal, originalPlaceholder);
					logger.debug("Resolving placeholder '" + placeholder + "' to [" + propVal + "]");
					strVal = strVal.substring(0, startIndex) + propVal + strVal.substring(endIndex+1);
					startIndex = strVal.indexOf(this.placeholderPrefix, startIndex + propVal.length());
				}
				else {
					logger.debug("Could not resolve placeholder '" + placeholder + "'");
					startIndex = strVal.indexOf(this.placeholderPrefix, endIndex);
				}
			}
			else {
				startIndex = -1;
			}
		}
		return strVal;
	}

}
