
package org.springframework.jndi;

import junit.framework.TestCase;

import org.springframework.jndi.support.ExpectedLookupTemplate;

/**
 * @author Rod Johnson
 */
public class JndiObjectFactoryBeanTests extends TestCase {

	/**
	 * Constructor for JndiObjectFactoryBeanTests.
	 * @param arg0
	 */
	public JndiObjectFactoryBeanTests(String arg0) {
		super(arg0);
	}


	public void testNoJndiName() {
		JndiObjectFactoryBean jof = new JndiObjectFactoryBean();
		try {
			jof.afterPropertiesSet();
			fail();
		}
		catch (Exception ex) {
			
		}
	}
	
	
	public void testLookupReturns() throws Exception {
		JndiObjectFactoryBean jof = new JndiObjectFactoryBean();
		Object o = new Object();
		jof.setJndiTemplate(new ExpectedLookupTemplate("java:comp/env/foo", o));
		jof.setJndiName("foo");
		jof.afterPropertiesSet();
		assertTrue(jof.getObject() == o);
	}
}
