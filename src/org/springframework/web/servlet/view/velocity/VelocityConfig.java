/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.view.velocity;

import org.apache.velocity.app.VelocityEngine;

/**
 * Interface to be implemented by objects that configure and manage a
 * VelocityEngine for automatic lookup in a web environment. Detected
 * and used by VelocityView.
 * @author Rod Johnson
 * @version $Id: VelocityConfig.java,v 1.2 2004-03-14 21:40:05 jhoeller Exp $
 * @see VelocityConfigurer
 * @see VelocityView
 */
public interface VelocityConfig {
	
	/**
	 * Return the VelocityEngine for the current web application context.
	 * May be unique to one servlet, or shared in the root context.
	 * @return the VelocityEngine
	 */
	VelocityEngine getVelocityEngine();

}
