package org.springframework.context.support;

import junit.framework.TestCase;

public class ClasspathXmlApplicationContextTests extends TestCase {
	
	public ClasspathXmlApplicationContextTests(String name) {
		super(name);
	}
	
	public void testMultiple() throws Exception {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] { 
				"/org/springframework/context/support/contextB.xml",
				"/org/springframework/context/support/contextC.xml",
				"/org/springframework/context/support/contextA.xml" });
	}

}
