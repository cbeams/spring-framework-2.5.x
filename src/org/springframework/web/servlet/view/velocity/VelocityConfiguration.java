/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.view.velocity;

import org.apache.velocity.app.VelocityEngine;

/**
 * Interface to be implemented by objects that configure
 * and manage a VelocityEngine for automatic lookup.
 * @author Rod Johnson
 * @version $Id: VelocityConfiguration.java,v 1.2 2003-10-22 15:19:13 jhoeller Exp $
 * @see VelocityView
 */
public interface VelocityConfiguration {
	
	/**
	 * Return the VelocityEngine for this web application context.
	 * May be unique to one servlet, or shared in the root context.
	 */
	VelocityEngine getVelocityEngine();

}
