package org.springframework.context.config;

import java.util.Properties;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.support.ListableBeanFactoryImpl;

/**
 * A property resource configurer that resolves placeholders in bean property values of
 * context definitions. It <i>pulls</i> values from a properties file into bean definitions.
 *
 * <p>The default placeholder syntax follows the JSP EL resp.
 * Log4J style:<br>
 * <code>
 * ${...}
 * </code>
 *
 * <p>Example properties file:
 * <code>
 * driver=com.mysql.jdbc.Driver
 * dbname=mysql:mydb
 * </code>
 *
 * <p>Example XML context definition:
 * <code>
 * &lt;bean id="dataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource"&gt;
 * &lt;property name="driverClassName"&gt;&lt;value&gt;${driver}&lt;/value&gt;&lt;/property&gt;
 * &lt;property name="url"&gt;&lt;value&gt;jdbc:${dbname}&lt;/value&gt;&lt;/property&gt;
 * &lt;/bean&gt;
 * </code>
 *
 * <p>In contrast to PropertyOverrideConfigurer, this configurer allows to fill in
 * explicit placeholders in context definitions. Therefore, the original definition
 * cannot specify any default values for such bean properties, and the placeholder
 * properties file has to contain an entry for each defined placeholder.
 *
 * <p>Note that the context definition <i>is</i> aware of being incomplete,
 * this is immediately obvious when looking at the XML definition file.
 *
 * <p>In case of multiple PropertyPlaceHolderConfigurers that define different values for
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

	protected void processProperties(ListableBeanFactoryImpl beanFactory, Properties prop) {
		String[] beanNames = beanFactory.getBeanDefinitionNames();
		for (int i = 0; i < beanNames.length; i++) {
			String beanName = beanNames[i];
			PropertyValues pvs = beanFactory.getPropertyValues(beanName);
			if (pvs != null) {
				for (int j = 0; j < pvs.getPropertyValues().length; j++) {
					PropertyValue pv = pvs.getPropertyValues()[j];
					if (pv.getValue() instanceof String) {
						String strVal = ((String) pv.getValue());
						int startIndex = strVal.indexOf(this.placeholderPrefix);
						int endIndex = strVal.indexOf(this.placeholderSuffix, startIndex + this.placeholderPrefix.length());
						if (startIndex != -1 && endIndex != -1) {
							String placeholder = strVal.substring(startIndex + this.placeholderPrefix.length(), endIndex);
							String propValue = prop.getProperty(placeholder);
							if (propValue != null) {
								String newVal = strVal.substring(0, startIndex) + propValue + strVal.substring(endIndex+1);
								beanFactory.overridePropertyValue(beanName, new PropertyValue(pv.getName(), newVal));
								logger.debug("Resolving placeholder " + placeholder + " to [" + propValue + "]");
								logger.debug("Property '" + beanName + "." + pv.getName() + "' set to [" + newVal + "]");
							}
							else {
								logger.debug("Could not resolve placeholder " + placeholder);
							}
						}
					}
				}
			}
		}
	}

}
