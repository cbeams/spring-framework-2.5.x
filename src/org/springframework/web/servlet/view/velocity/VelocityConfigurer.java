/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.view.velocity;

import org.springframework.ui.velocity.VelocityEngineFactory;
import org.springframework.web.util.WebUtils;

/**
 * JavaBean to configure Velocity for web usage, via the "configLocation" and/or
 * "velocityProperties" bean properties. If neither of them is set, the default
 * config location "WEB-INF/velocity.properties" will be used.
 *
 * <p>This bean must be included in the application context of any application
 * using Spring's VelocityView for web MVC. It exists purely to configure Velocity.
 * It is not meant to be referenced by application components but just internally
 * by VelocityView. Implements VelocityConfiguration to be found by VelocityView
 * without depending on the bean name the configurer.
 *
 * <p>The default config file location, applied only when no "configLocation" and
 * no "velocityProperties" set, is "WEB-INF/velocity.properties".
 *
 * <p>When using Velocity's FileResourceLoader, the "appRootMarker" mechanism can
 * be used to refer to the web app resource base within a Velocity property value.
 * By default, the marker "${webapp.root}" gets replaced with the web app root
 * directory. Note that this will only work with expanded WAR files.
 *
 * <p>Example Velocity properties that leverage the "appRootMarker" mechanism:
 * <p><code>
 * resource.loader=file<br>
 * file.resource.loader.class=org.apache.velocity.runtime.resource.loader.FileResourceLoader<br>
 * file.resource.loader.path=${webapp.root}/WEB-INF/velocity
 * </code>
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: VelocityConfigurer.java,v 1.5 2003-10-22 15:19:13 jhoeller Exp $
 * @see #setConfigLocation
 * @see #setVelocityProperties
 * @see #setAppRootMarker
 * @see VelocityView
 */
public class VelocityConfigurer extends VelocityEngineFactory implements VelocityConfiguration {

	public static final String DEFAULT_CONFIG_LOCATION = "WEB-INF/velocity.properties";

	public static final String DEFAULT_APP_ROOT_MARKER = "${" + WebUtils.DEFAULT_WEB_APP_ROOT_KEY + "}";

	public VelocityConfigurer() {
		setAppRootMarker(DEFAULT_APP_ROOT_MARKER);
	}

	protected String getDefaultConfigLocation() {
		return DEFAULT_CONFIG_LOCATION;
	}

}
