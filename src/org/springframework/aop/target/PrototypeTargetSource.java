/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.target;


/**
 * TargetSource that creates a new instance of the target bean for each request.
 * Can only be used in a bean factory.
 * @author Rod Johnson
 * @version $Id: PrototypeTargetSource.java,v 1.3 2003-12-11 10:58:12 johnsonr Exp $
 */
public final class PrototypeTargetSource extends AbstractPrototypeTargetSource {

	public Object getTarget() {
		return newPrototypeInstance();
	}
	
	/**
	 * @see org.springframework.aop.TargetSource#releaseTarget()
	 */
	public void releaseTarget(Object target) {
		// Do nothing
	}

}
