/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.autoproxy.target;

/**
 * Simple pooling attribute that can drive automatic creation of a TargetSource.
 * 
 * @author Rod Johnson
 * @version $Id: PoolingAttribute.java,v 1.1 2003-12-12 18:43:45 johnsonr Exp $
 */
public class PoolingAttribute {
	
	private int size;
	
	
	public PoolingAttribute(int size) {
		this.size = size;
	}

	/**
	 * @return
	 */
	public int getSize() {
		return this.size;
	}

	/**
	 * @param size
	 */
	//public void setSize(int size) {
	//	this.size = size;
	//}

}
