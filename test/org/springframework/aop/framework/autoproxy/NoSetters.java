/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.autoproxy;

/**
 * 
 * @author Rod Johnson
 * @version $Id: NoSetters.java,v 1.1 2003-12-12 16:50:43 johnsonr Exp $
 */
public class NoSetters {
	
	public void A() {
		
	}
	
	public int getB() {
		return -1;
	}

}
