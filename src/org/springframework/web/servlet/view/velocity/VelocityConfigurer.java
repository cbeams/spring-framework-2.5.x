/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.view.velocity;

import java.io.IOException;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.ui.velocity.VelocityEngineFactory;

/**
 * JavaBean to configure Velocity for web usage, via the "configLocation"
 * and/or "velocityProperties" and/or "resourceLoaderPath" bean properties.
 * The simplest way to use this class is to specify just a "resourceLoaderPath":
 * You do not need any further configuration then.
 *
 * <p><code>
 * &lt;bean id="velocityConfig" class="org.springframework.web.servlet.view.velocity.VelocityConfigurer"&gt;<br>
 * &nbsp;&nbsp;&lt;property name="resourceLoaderPath"&gt;&lt;value&gt;/WEB-INF/velocity/&lt;/value&gt;&lt;/property&gt;<br>
 * &lt;/bean&gt;
 * </code>
 *
 * <p>This bean must be included in the application context of any application
 * using Spring's VelocityView for web MVC. It exists purely to configure Velocity.
 * It is not meant to be referenced by application components but just internally
 * by VelocityView. Implements VelocityConfig to be found by VelocityView without
 * depending on the bean name the configurer. Each DispatcherServlet can define its
 * own VelocityConfigurer if desired.
 *
 * <p>Note that you can also refer to a preconfigured VelocityEngine instance, for
 * example one set up by VelocityEngineFactoryBean, via the "velocityEngine" property.
 * This allows to shared a VelocityEngine for web and email usage, for example.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: VelocityConfigurer.java,v 1.12 2004-03-14 21:40:05 jhoeller Exp $
 * @see #setConfigLocation
 * @see #setVelocityProperties
 * @see #setResourceLoaderPath
 * @see #setVelocityEngine
 * @see org.springframework.ui.velocity.VelocityEngineFactoryBean
 * @see VelocityView
 */
public class VelocityConfigurer extends VelocityEngineFactory
		implements VelocityConfig, InitializingBean, ResourceLoaderAware {

	private VelocityEngine velocityEngine;

	/**
	 * Set a preconfigured VelocityEngine to use for the Velocity web config, e.g.
	 * a shared one for web and email usage, set up via VelocityEngineFactoryBean.
	 * If this is not set, VelocityEngineFactory's properties (inherited by this
	 * class) have to be specified.
	 * @see org.springframework.ui.velocity.VelocityEngineFactoryBean
	 */
	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}

	/**
	 * Initialize VelocityEngineFactory's VelocityEngine
	 * if not overridden by a preconfigured VelocityEngine.
	 * @see #createVelocityEngine
	 * @see #setVelocityEngine
	 */
	public void afterPropertiesSet() throws IOException, VelocityException {
		if (this.velocityEngine == null) {
			this.velocityEngine = createVelocityEngine();
		}
	}

	public VelocityEngine getVelocityEngine() {
		return this.velocityEngine;
	}

}
