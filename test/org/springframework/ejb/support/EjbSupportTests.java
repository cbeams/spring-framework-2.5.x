/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.ejb.support;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenContext;
import javax.ejb.SessionContext;
import javax.jms.Message;
import javax.naming.NamingException;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanFactoryLoader;
import org.springframework.beans.factory.support.BootstrapException;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.jndi.support.SimpleNamingContextBuilder;

/**
 * Most of the value of the tests here is in being forced
 * to implement ejbCreate() methods.
 * @author Rod Johnson
 * @since 21-May-2003
 * @version $Id: EjbSupportTests.java,v 1.5 2003-12-30 02:04:18 jhoeller Exp $
 */
public class EjbSupportTests extends TestCase {

	public void testSfsb() throws CreateException {
		MockControl mc = MockControl.createControl(SessionContext.class);
		SessionContext sc = (SessionContext) mc.getMock();
		mc.replay();
		
		final BeanFactory bf = new StaticListableBeanFactory();
		BeanFactoryLoader bfl = new BeanFactoryLoader() {
			public BeanFactory loadBeanFactory() throws BootstrapException {
				return bf;
			}
			public void unloadBeanFactory(BeanFactory bf) throws FatalBeanException {
			}
		};
		
		// Basically the test is what needed to be implemented here!
		class MySfsb extends AbstractStatefulSessionBean {
			public void ejbCreate() throws CreateException {
				loadBeanFactory();
				assertTrue(getBeanFactory() == bf);
				assertTrue(logger != null);
			}
			public void ejbActivate() throws EJBException, RemoteException {
				throw new UnsupportedOperationException("ejbActivate");
			}
			public void ejbPassivate() throws EJBException, RemoteException {
				throw new UnsupportedOperationException("ejbPassivate");
			}

		};
		
		MySfsb sfsb = new MySfsb();
		sfsb.setBeanFactoryLoader(bfl);
		sfsb.setSessionContext(sc);
		sfsb.ejbCreate();
		assertTrue(sc == sfsb.getSessionContext());
	}
	
	/**
	 * Check there's a helpful message if no JNDI key is present
	 */
	public void testHelpfulNamingLookupMessage() throws NamingException {
		SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		
		MockControl mc = MockControl.createControl(SessionContext.class);
		SessionContext sc = (SessionContext) mc.getMock();
		mc.replay();
	
		// Leave with default XmlBeanFactoryLoader
	
		// Basically the test is what needed to be implemented here!
		AbstractStatelessSessionBean slsb = new AbstractStatelessSessionBean() {
			public void onEjbCreate() {
			}
		};
	
		slsb.setSessionContext(sc);
		try {
			slsb.ejbCreate();
			fail();
		}
		catch (CreateException ex) {
			assertTrue(ex.getMessage().indexOf("environment") != -1);
			assertTrue(ex.getMessage().indexOf("ejb/BeanFactoryPath") != -1);
		}
	}
	
	public void testSlsb() throws Exception {
		MockControl mc = MockControl.createControl(SessionContext.class);
		SessionContext sc = (SessionContext) mc.getMock();
		mc.replay();
		
		final BeanFactory bf = new StaticListableBeanFactory();
		BeanFactoryLoader bfl = new BeanFactoryLoader() {
			public BeanFactory loadBeanFactory() throws BootstrapException {
				return bf;
			}
			public void unloadBeanFactory(BeanFactory bf) throws FatalBeanException {
			}
		};
	
		AbstractStatelessSessionBean slsb = new AbstractStatelessSessionBean() {
			protected void onEjbCreate() throws CreateException {
				assertTrue(getBeanFactory() == bf);
				assertTrue(logger != null);
			}
		};
		// Must call this method before ejbCreate()
		slsb.setBeanFactoryLoader(bfl);
		slsb.setSessionContext(sc);
		assertTrue(sc == slsb.getSessionContext());
		slsb.ejbCreate();
		try {
			slsb.ejbActivate();
			fail("Shouldn't allow activation of SLSB");
		}
		catch (IllegalStateException ex) {
			// Ok
		}
		try {
			slsb.ejbPassivate();
			fail("Shouldn't allow passivation of SLSB");
		}
		catch (IllegalStateException ex) {
			// Ok
		}
	}


	public void testJmsMdb() throws Exception {
		MockControl mc = MockControl.createControl(MessageDrivenContext.class);
		MessageDrivenContext sc = (MessageDrivenContext) mc.getMock();
		mc.replay();
	
		final BeanFactory bf = new StaticListableBeanFactory();
		BeanFactoryLoader bfl = new BeanFactoryLoader() {
			public BeanFactory loadBeanFactory() throws BootstrapException {
				return bf;
			}
			public void unloadBeanFactory(BeanFactory bf) throws FatalBeanException {
			}
		};

		AbstractJmsMessageDrivenBean mdb = new AbstractJmsMessageDrivenBean() {
			protected void onEjbCreate() {
				assertTrue(getBeanFactory() == bf);
				assertTrue(logger != null);
			}

			public void onMessage(Message arg0) {
				throw new UnsupportedOperationException("onMessage");
			}
		};
		// Must call this method before ejbCreate()
		mdb.setBeanFactoryLoader(bfl);
		mdb.setMessageDrivenContext(sc);
		assertTrue(sc == mdb.getMessageDrivenContext());
		mdb.ejbCreate();
	}
	
	public void testCannotLoadBeanFactory() throws Exception {
		MockControl mc = MockControl.createControl(SessionContext.class);
		SessionContext sc = (SessionContext) mc.getMock();
		mc.replay();
	
		final BeanFactory bf = new StaticListableBeanFactory();
		BeanFactoryLoader bfl = new BeanFactoryLoader() {
			public BeanFactory loadBeanFactory() throws BootstrapException {
				throw new BootstrapException("", null);
			}
			public void unloadBeanFactory(BeanFactory bf) throws FatalBeanException {
			}
		};

		AbstractStatelessSessionBean slsb = new AbstractStatelessSessionBean() {
			protected void onEjbCreate() throws CreateException {
			}
		};
		// Must call this method before ejbCreate()
		slsb.setBeanFactoryLoader(bfl);
		slsb.setSessionContext(sc);
		
		try {
			slsb.ejbCreate();
			fail();
		}
		catch (CreateException ex) {
			// Ok
		}
	}
}
