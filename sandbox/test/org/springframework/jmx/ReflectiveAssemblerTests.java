/*
 * Created on Jul 5, 2004
 */
package org.springframework.jmx;


/**
 * @author robh
 */
public class ReflectiveAssemblerTests extends AbstractJmxAssemblerTests {

	protected static final String OBJECT_NAME = "bean:name=testBean1";

	public ReflectiveAssemblerTests(String name) {
		super(name);
	}
	
	protected String getObjectName() {
		return OBJECT_NAME;
	}
	
	protected int getExpectedOperationCount() {
	    return 2;
	}
	
	protected int getExpectedAttributeCount() {
	    return 2;
	}
}