
package org.springframework.jndi;

import javax.naming.NamingException;

import junit.framework.TestCase;

import org.springframework.jndi.support.ExpectedLookupTemplate;

/**
 * @author Rod Johnson
 */
public class JndiObjectFactoryBeanTests extends TestCase {

	public void testNoJndiName() throws NamingException {
		JndiObjectFactoryBean jof = new JndiObjectFactoryBean();
		try {
			jof.afterPropertiesSet();
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
		}
	}
	
	public void testLookupWithFullNameAndInContainerTrue() throws Exception {
		JndiObjectFactoryBean jof = new JndiObjectFactoryBean();
		Object o = new Object();
		jof.setJndiTemplate(new ExpectedLookupTemplate("java:comp/env/foo", o));
		jof.setJndiName("java:comp/env/foo");
		jof.afterPropertiesSet();
		assertTrue(jof.getObject() == o);
	}

	public void testLookupWithFullNameAndInContainerFalse() throws Exception {
		JndiObjectFactoryBean jof = new JndiObjectFactoryBean();
		Object o = new Object();
		jof.setJndiTemplate(new ExpectedLookupTemplate("java:comp/env/foo", o));
		jof.setJndiName("java:comp/env/foo");
		jof.setInContainer(false);
		jof.afterPropertiesSet();
		assertTrue(jof.getObject() == o);
	}

	public void testLookupWithSchemeNameAndInContainerTrue() throws Exception {
		JndiObjectFactoryBean jof = new JndiObjectFactoryBean();
		Object o = new Object();
		jof.setJndiTemplate(new ExpectedLookupTemplate("java:foo", o));
		jof.setJndiName("java:foo");
		jof.afterPropertiesSet();
		assertTrue(jof.getObject() == o);
	}

	public void testLookupWithSchemeNameAndInContainerFalse() throws Exception {
		JndiObjectFactoryBean jof = new JndiObjectFactoryBean();
		Object o = new Object();
		jof.setJndiTemplate(new ExpectedLookupTemplate("java:foo", o));
		jof.setJndiName("java:foo");
		jof.setInContainer(false);
		jof.afterPropertiesSet();
		assertTrue(jof.getObject() == o);
	}

	public void testLookupWithShortNameAndInContainerTrue() throws Exception {
		JndiObjectFactoryBean jof = new JndiObjectFactoryBean();
		Object o = new Object();
		jof.setJndiTemplate(new ExpectedLookupTemplate("java:comp/env/foo", o));
		jof.setJndiName("foo");
		jof.afterPropertiesSet();
		assertTrue(jof.getObject() == o);
	}

	public void testLookupWithShortNameAndInContainerFalse() throws Exception {
		JndiObjectFactoryBean jof = new JndiObjectFactoryBean();
		Object o = new Object();
		jof.setJndiTemplate(new ExpectedLookupTemplate("java:comp/env/foo", o));
		jof.setJndiName("foo");
		jof.setInContainer(false);
		try {
			jof.afterPropertiesSet();
			fail("Should have thrown NamingException");
		}
		catch (NamingException ex) {
			// expected
		}
	}

	public void testLookupWithArbitraryNameAndInContainerFalse() throws Exception {
		JndiObjectFactoryBean jof = new JndiObjectFactoryBean();
		Object o = new Object();
		jof.setJndiTemplate(new ExpectedLookupTemplate("foo", o));
		jof.setJndiName("foo");
		jof.setInContainer(false);
		jof.afterPropertiesSet();
		assertTrue(jof.getObject() == o);
	}

}
