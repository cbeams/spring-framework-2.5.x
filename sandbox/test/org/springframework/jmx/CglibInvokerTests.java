/*
 * Created on Jul 8, 2004
 */
package org.springframework.jmx;

/**
 * @author robh
 *
 */
public class CglibInvokerTests extends AbstractJmxInvokerTests {

	private static final String OBJECT_NAME = "bean:name=testBean2";
	
	public CglibInvokerTests(String name) {
		super(name);
	}
	
	protected String getObjectName() {
		return OBJECT_NAME;
	}

}
