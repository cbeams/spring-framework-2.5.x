/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.view.freemarker;

import java.io.IOException;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.ui.freemarker.FreemarkerConfigurationFactory;

/**
 * JavaBean to configure FreeMarker for web usage, via the "configLocation"
 * and/or "freemarkerSettings" and/or "templateLoaderPath" properties.
 * The simplest way to use this class is to specify just a "templateLoaderPath":
 * You do not need any further configuration then.
 *
 * <p><code>
 * &lt;bean id="freemarkerConfig" class="org.springframework.web.servlet.view.freemarker.FreemarkerConfigurer"&gt;<br>
 * &nbsp;&nbsp;&lt;property name="templateLoaderPath"&gt;&lt;value&gt;/WEB-INF/freemarker/&lt;/value&gt;&lt;/property&gt;<br>
 * &lt;/bean&gt;
 * </code>
 *
 * <p>This bean must be included in the application context of any application
 * using Spring's FreemarkerView for web MVC. It exists purely to configure FreeMarker.
 * It is not meant to be referenced by application components but just internally
 * by FreemarkerView. Implements FreemarkerConfig to be found by FreemarkerView without
 * depending on the bean name the configurer. Each DispatcherServlet can define its
 * own FreemarkerConfigurer if desired.
 *
 * <p>Note that you can also refer to a preconfigured FreeMarker Configuration
 * instance, for example one set up by FreemarkerConfigurationFactoryBean, via
 * the "configuration" property. This allows to share a FreeMarker Configuration
 * for web and email usage, for example.
 *
 * <p>Note: Spring's FreeMarker support requires FreeMarker 2.3 or higher.
 *
 * @author Darren Davison
 * @since 3/3/2004
 * @version $Id: FreemarkerConfigurer.java,v 1.3 2004-03-15 07:57:12 jhoeller Exp $
 * @see #setConfigLocation
 * @see #setFreemarkerSettings
 * @see #setTemplateLoaderPath
 * @see #setConfiguration
 * @see org.springframework.ui.freemarker.FreemarkerConfigurationFactoryBean
 * @see FreemarkerView
 */
public class FreemarkerConfigurer extends FreemarkerConfigurationFactory
		implements FreemarkerConfig, InitializingBean, ResourceLoaderAware {

	private Configuration configuration;

	/**
	 * Set a preconfigured Configuration to use for the FreeMarker web config, e.g. a
	 * shared one for web and email usage, set up via FreemarkerConfigurationFactoryBean.
	 * If this is not set, FreemarkerConfigurationFactory's properties (inherited by
	 * this class) have to be specified.
	 * @see org.springframework.ui.freemarker.FreemarkerConfigurationFactoryBean
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Initialize FreemarkerConfigurationFactory's Configuration
	 * if not overridden by a preconfigured FreeMarker Configuation.
	 * @see #createConfiguration
	 * @see #setConfiguration
	 */
	public void afterPropertiesSet() throws IOException, TemplateException {
		if (this.configuration == null) {
			this.configuration = createConfiguration();
		}
	}

	public synchronized Configuration getConfiguration() {
		return this.configuration;
	}

}
