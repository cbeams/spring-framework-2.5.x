/*
 * The Spring Framework is published under the terms
 * of the Apache Software License. 
 */
package org.springframework.metadata.support;

/**
 * A test attribute that does not have a public no arg constructor
 * 
 * 
 * @author Mark Pollack
 * @since Oct 6, 2003
 * @version $Id: PrivateCtorAttribute.java,v 1.1 2003-11-22 09:05:42 johnsonr Exp $
 */
public class PrivateCtorAttribute {

	/**
	 * Do nothing private ctor.
	 */
	private PrivateCtorAttribute() {
		
	}
}
