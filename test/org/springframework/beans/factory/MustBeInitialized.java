/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory;


/**
 * Simple test of BeanFactory initialization
 * @author Rod Johnson
 * @since 12-Mar-2003
 * @version $Revision: 1.1.1.1 $
 */
public class MustBeInitialized implements InitializingBean {

	private boolean inited; 
	
	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		this.inited = true;
	}
	
	/**
	 * Dummy business method that will fail unless the factory
	 * managed the bean's lifecycle correctly
	 */
	public void businessMethod() {
		if (!this.inited)
			throw new RuntimeException("Factory didn't call afterPropertiesSet() on MustBeInitialized object");
	}

}
