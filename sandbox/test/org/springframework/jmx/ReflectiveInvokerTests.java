/*
 * Created on Jul 8, 2004
 */
package org.springframework.jmx;

import org.springframework.jmx.invokers.reflection.ReflectiveMBeanInvoker;

/**
 * @author robh
 *
 */
public class ReflectiveInvokerTests extends AbstractJmxInvokerTests {

	private static final String OBJECT_NAME = "bean:name=testBean1";
	
	public ReflectiveInvokerTests(String name) {
		super(name);
	}
	
	protected String getObjectName() {
		return OBJECT_NAME;
	}
	
	protected MBeanInvoker getInvoker() {
		return new ReflectiveMBeanInvoker();
	}

}
