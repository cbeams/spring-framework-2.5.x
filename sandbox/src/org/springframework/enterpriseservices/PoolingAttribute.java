/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.enterpriseservices;

/**
 * TODO not currently used
 * @author Rod Johnson
 * @version $Id: PoolingAttribute.java,v 1.1 2003-11-22 09:05:39 johnsonr Exp $
 */
public class PoolingAttribute {
	
	private int size;
	
	//private Class

	/**
	 * @return
	 */
	public int getSize() {
		return this.size;
	}

	/**
	 * @param size
	 */
	public void setSize(int size) {
		this.size = size;
	}

}
