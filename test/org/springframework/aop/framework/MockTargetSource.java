/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import org.springframework.aop.TargetSource;

/**
 * 
 * @author Rod Johnson
 * @version $Id: MockTargetSource.java,v 1.1 2003-11-30 18:02:39 johnsonr Exp $
 */
public class MockTargetSource implements TargetSource {
	
	private Object target;
	
	public int gets;
	
	public int releases;
	
	public void reset() {
		this.target = null;
		gets = releases = 0;
	}
	
	public void setTarget(Object target) {
		this.target = target;
	}

	/**
	 * @see org.springframework.aop.TargetSource#getTargetClass()
	 */
	public Class getTargetClass() {
		return target.getClass();
	}

	/**
	 * @see org.springframework.aop.TargetSource#getTarget()
	 */
	public Object getTarget() throws Exception {
		++gets;
		return target;
	}

	/**
	 * @see org.springframework.aop.TargetSource#releaseTarget(java.lang.Object)
	 */
	public void releaseTarget(Object pTarget) throws Exception {
		if (pTarget != this.target)
			throw new RuntimeException("Released wrong target");
		++releases;
	}
	
	/**
	 * Check that gets and releases match
	 *
	 */
	public void verify() {
		if (gets != releases)
			throw new RuntimeException("Expectation failed: " + gets + " gets and " + releases + " releases");
	}

}
