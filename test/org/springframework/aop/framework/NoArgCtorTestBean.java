/*
 * Created on Oct 5, 2004
 */
package org.springframework.aop.framework;

/**
 * @author robh
 *
 */
public class NoArgCtorTestBean {

	private boolean called = false;
	
	public NoArgCtorTestBean(String x, int y) {
		called = true;
	}
	
	public boolean wasCalled() {
		return called;
	}
	
	public void reset() {
		called = false;
	}
}
