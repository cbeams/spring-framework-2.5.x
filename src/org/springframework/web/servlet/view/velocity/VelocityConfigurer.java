/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.view.velocity;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.ui.velocity.VelocityEngineFactory;

/**
 * JavaBean to configure Velocity for web usage, via the "configLocation" and/or
 * "velocityProperties" and/or "resourceLoaderPath" bean properties. If neither of them
 * is set, the default config location "/WEB-INF/velocity.properties" will be used.
 *
 * <p>This bean must be included in the application context of any application
 * using Spring's VelocityView for web MVC. It exists purely to configure Velocity.
 * It is not meant to be referenced by application components but just internally
 * by VelocityView. Implements VelocityConfig to be found by VelocityView without
 * depending on the bean name the configurer. Each DispatcherServlet can define its
 * own VelocityConfigurer if desired.
 *
 * <p>The simplest way to use this class is to specify just a "resourceLoaderPath":
 * the VelocityEngine does not need any more configuration then.
 *
 * <p><code>
 * &lt;bean id="velocityConfig" class="org.springframework.web.servlet.view.velocity.VelocityConfigurer"&gt;<br>
 * &nbsp;&nbsp;&lt;property name="resourceLoaderPath"&gt;&lt;value&gt;/WEB-INF/velocity/&lt;/value&gt;&lt;/property&gt;<br>
 * &lt;/bean&gt;
 * </code>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: VelocityConfigurer.java,v 1.10 2003-12-30 00:26:44 jhoeller Exp $
 * @see #setConfigLocation
 * @see #setVelocityProperties
 * @see #setResourceLoaderPath
 * @see VelocityView
 */
public class VelocityConfigurer extends VelocityEngineFactory
		implements ApplicationContextAware, VelocityConfig {

	public static final String DEFAULT_CONFIG_LOCATION = "/WEB-INF/velocity.properties";

	private ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		initVelocityEngine();
	}

	protected Resource getDefaultConfigLocation() throws IOException {
		return this.applicationContext.getResource(DEFAULT_CONFIG_LOCATION);
	}

}
