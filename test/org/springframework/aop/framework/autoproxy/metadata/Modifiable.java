/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.autoproxy.metadata;

/**
 * Used as a mixin.
 * @author Rod Johnson
 * @version $Id: Modifiable.java,v 1.1 2003-12-12 21:31:25 johnsonr Exp $
 */
public interface Modifiable {

	boolean isModified();
	
	void acceptChanges();
	
}
