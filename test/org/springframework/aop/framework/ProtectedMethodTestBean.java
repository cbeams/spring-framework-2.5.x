/*
 * Created on Sep 28, 2004
 */
package org.springframework.aop.framework;

/**
 * @author robh
 *
 */
public class ProtectedMethodTestBean {

	private String s = getString();
	
	protected String getString() {
		return "foo";
	}
}
