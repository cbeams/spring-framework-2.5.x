/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework.autoproxy.target;

/**
 * Simple pooling attribute that can drive automatic creation of a TargetSource.
 * @author Rod Johnson
 * @version $Id: PoolingAttribute.java,v 1.2 2003-12-30 01:07:11 jhoeller Exp $
 */
public class PoolingAttribute {
	
	private int size;

	public PoolingAttribute(int size) {
		this.size = size;
	}

	public int getSize() {
		return this.size;
	}

}
