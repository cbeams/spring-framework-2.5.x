/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.jndi;

import javax.naming.Context;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.easymock.MockControl;

/**
 * 
 * @author Rod Johnson
 * @since 08-Jul-2003
 * @version $Id: JndiTemplateTests.java,v 1.3 2003-11-20 15:20:43 johnsonr Exp $
 */
public class JndiTemplateTests extends TestCase {

	/**
	 * Constructor for JndiTemplateTests.
	 * @param arg0
	 */
	public JndiTemplateTests(String arg0) {
		super(arg0);
	}
	
	public void testLookupSucceeds() throws Exception {
		Object o = new Object();
		String name = "foo";
		MockControl mc = MockControl.createControl(Context.class);
		final Context mock = (Context) mc.getMock();
		mock.lookup(name);
		mc.setReturnValue(o);
		mock.close();
		mc.setVoidCallable(1);
		mc.replay();

		JndiTemplate jt = new JndiTemplate() {
			protected Context createInitialContext() throws NamingException {
				return mock;
			}
		};

		Object o2 = jt.lookup(name);
		assertEquals(o, o2);
		mc.verify();
	}
	
	public void testLookupFails() throws Exception {
		NamingException ne = new NamingException();
		String name = "foo";
		MockControl mc = MockControl.createControl(Context.class);
		final Context mock = (Context) mc.getMock();
		mock.lookup(name);
		mc.setThrowable(ne);
		mock.close();
		mc.setVoidCallable(1);
		mc.replay();

		JndiTemplate jt = new JndiTemplate() {
			protected Context createInitialContext() throws NamingException {
				return mock;
			}
		};

		try {
			jt.lookup(name);
			fail("Should have thrown NamingException");
		}
		catch (NamingException ex) {
			// Ok
		}
		mc.verify();
	}
	
	public void testBind() throws Exception {
		Object o = new Object();
		String name = "foo";
		MockControl mc = MockControl.createControl(Context.class);
		final Context mock = (Context) mc.getMock();
		mock.bind(name, o);
		mc.setVoidCallable(1);
		mock.close();
		mc.setVoidCallable(1);
		mc.replay();
		
		JndiTemplate jt = new JndiTemplate() {
			protected Context createInitialContext() throws NamingException {
				return mock;
			}
		};
		
		jt.bind(name, o);
		mc.verify();
	}
	
	public void testUnbind() throws Exception {
		String name = "something";
		MockControl mc = MockControl.createControl(Context.class);
		final Context mock = (Context) mc.getMock();
		mock.unbind(name);
		mc.setVoidCallable(1);
		mock.close();
		mc.setVoidCallable(1);
		mc.replay();
	
		JndiTemplate jt = new JndiTemplate() {
			protected Context createInitialContext() throws NamingException {
				return mock;
			}
		};
	
		jt.unbind(name);
		mc.verify();
	}

}
