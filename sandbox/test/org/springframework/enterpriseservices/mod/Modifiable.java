/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.enterpriseservices.mod;

/**
 * Used as a mixin.
 * @author Rod Johnson
 * @version $Id: Modifiable.java,v 1.1 2003-11-22 09:05:44 johnsonr Exp $
 */
public interface Modifiable {

	boolean isModified();
	
	void acceptChanges();
	
}
