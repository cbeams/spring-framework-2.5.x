/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.ejb.access;

import java.lang.reflect.Proxy;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.naming.NamingException;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.beans.MethodInvocationException;
import org.springframework.jndi.JndiTemplate;

/**
 * Tests Business Methods pattern
 * @author Rod Johnson
 * @since 21-May-2003
 * @version $Id: LocalStatelessSessionProxyFactoryBeanTests.java,v 1.3 2003-09-21 09:07:16 johnsonr Exp $
 */
public class LocalStatelessSessionProxyFactoryBeanTests extends TestCase {

	/**
	 * Constructor for LocalStatelessSessionProxyFactoryBeanTests.
	 * @param arg0
	 */
	public LocalStatelessSessionProxyFactoryBeanTests(String arg0) {
		super(arg0);
	}
	
	public void testInvokesMethod() throws Exception {
		final int value = 11;
		final String jndiName = "foo";
		
		MockControl ec = MockControl.createControl(MyEjb.class);
		MyEjb myEjb = (MyEjb) ec.getMock();
		myEjb.getValue();
		ec.setReturnValue(value, 1);
		ec.replay();
		
		MockControl mc = MockControl.createControl(MyHome.class);
		final MyHome home = (MyHome) mc.getMock();
		home.create();
		mc.setReturnValue(myEjb, 1);
		mc.replay();
		
		JndiTemplate jt = new JndiTemplate() {
			public Object lookup(String name) throws NamingException {
				// parameterize
				assertTrue(name.equals("java:comp/env/" + jndiName));
				return home;
			}
		};
		
		LocalStatelessSessionProxyFactoryBean fb = new LocalStatelessSessionProxyFactoryBean();
		fb.setJndiName(jndiName);
		fb.setBusinessInterface(MyBusinessMethods.class);
		fb.setJndiTemplate(jt);
		
		// Need lifecycle methods
		fb.afterPropertiesSet();

		MyBusinessMethods mbm = (MyBusinessMethods) fb.getObject();
		assertTrue(Proxy.isProxyClass(mbm.getClass()));
		assertTrue(mbm.getValue() == value);
		mc.verify();	
		ec.verify();	
	}
	
	
	public void testCreateException() throws Exception {
		final String jndiName = "foo";
	
		final CreateException cex = new CreateException();
		MockControl mc = MockControl.createControl(MyHome.class);
		final MyHome home = (MyHome) mc.getMock();
		home.create();
		mc.setThrowable(cex);
		mc.replay();
	
		JndiTemplate jt = new JndiTemplate() {
			public Object lookup(String name) throws NamingException {
				// parameterize
				assertTrue(name.equals(jndiName));
				return home;
			}
		};
	
		LocalStatelessSessionProxyFactoryBean fb = new LocalStatelessSessionProxyFactoryBean();
		fb.setJndiName(jndiName);
		fb.setInContainer(false);	// no java:comp/env prefix
		fb.setBusinessInterface(MyBusinessMethods.class);
		assertEquals(fb.getBusinessInterface(), MyBusinessMethods.class);
		fb.setJndiTemplate(jt);
	
		// Need lifecycle methods
		fb.afterPropertiesSet();

		MyBusinessMethods mbm = (MyBusinessMethods) fb.getObject();
		assertTrue(Proxy.isProxyClass(mbm.getClass()));
		
		try {
			mbm.getValue();
			fail("Should have failed to create EJB");
		}
		catch (MethodInvocationException ex) {
			assertTrue(ex.getRootCause() == cex);
		}
		
		mc.verify();	
	}
	
	public void testNoBusinessInterfaceSpecified() throws Exception {
		// Will do JNDI lookup to get home but won't call create
		// Could actually try to figure out interface from create?
		final String jndiName = "foo";

		MockControl mc = MockControl.createControl(MyHome.class);
		final MyHome home = (MyHome) mc.getMock();
		mc.replay();

		JndiTemplate jt = new JndiTemplate() {
			public Object lookup(String name) throws NamingException {
				// parameterize
				assertTrue(name.equals("java:comp/env/" + jndiName));
				return home;
			} 
		};

		SimpleRemoteStatelessSessionProxyFactoryBean fb = new SimpleRemoteStatelessSessionProxyFactoryBean();
		fb.setJndiName(jndiName);
		// Don't set business interface
		fb.setJndiTemplate(jt);
	
		// Check it's a singleton
		assertTrue(fb.isSingleton());

		try {
			fb.afterPropertiesSet();
			fail("Should have failed to create EJB");
		}
		catch (IllegalArgumentException ex) {
			// TODO more appropriate exception?
			assertTrue(ex.getMessage().indexOf("businessInterface") != 1);
		}

		// Expect no methods on home
		mc.verify();	
	}
	
	
	public static interface MyHome extends EJBLocalHome {
		MyBusinessMethods create() throws CreateException;	
	}
	
	public static interface MyBusinessMethods  {
		int getValue();
	}
	
	public static interface MyEjb extends EJBLocalObject, MyBusinessMethods {
		
	}

}
