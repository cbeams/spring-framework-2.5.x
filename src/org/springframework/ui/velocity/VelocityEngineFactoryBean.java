/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.ui.velocity;

import org.springframework.beans.factory.FactoryBean;

/**
 * Factory bean that configures a VelocityEngine and provides it as bean
 * reference. This bean is intended for any kind of usage of Velocity in
 * application code, e.g. for generating email content. For web views,
 * VelocityConfigurer is used to set up a VelocityEngine for views.
 *
 * <p>See base class VelocityEngineFactory for configuration details.
 *
 * @author Juergen Hoeller
 * @see #setConfigLocation
 * @see #setVelocityProperties
 * @see #setAppRootMarker
 * @see CommonsLoggingLogSystem
 * @see org.springframework.web.servlet.view.velocity.VelocityConfigurer
 */
public class VelocityEngineFactoryBean extends VelocityEngineFactory implements FactoryBean {

	public Object getObject() {
		return getVelocityEngine();
	}

	public boolean isSingleton() {
		return true;
	}

}
