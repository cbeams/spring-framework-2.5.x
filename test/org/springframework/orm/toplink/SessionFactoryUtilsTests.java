/*
 * Created on Mar 18, 2005
 *
 */

package org.springframework.orm.toplink;

import junit.framework.TestCase;
import oracle.toplink.sessions.Session;
import org.easymock.MockControl;

import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author jclark
 *
 *         Test the SessionFactoryUtils which handles most of the TopLink specific ThreadLocal work for the Spring container.
 *         Test situations where Session is both bound and unbound.
 *         Also test situations where a PlatformTransactionManager is currently active, or when TopLink is still configured to use
 *         it's ExternalTransactionController (should be rare)
 */
public class SessionFactoryUtilsTests extends TestCase {

	/**
	 * When no Session is bound and allowCreate is "false", we should throw an IllegalStateException
	 *
	 * When no Session is bound, and allowCreate is "true", we should get a Session but it should not be bound to the Thread
	 * afterwards
	 */
	public void testNoSessionBound() {
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();

		SessionFactory factory = new SingleSessionFactory(session);

		session.hasExternalTransactionController();
		sessionControl.setReturnValue(false, 1);

		sessionControl.replay();
		try {
			Session boundSession = SessionFactoryUtils.getSession(factory, false);
			fail();
		}
		catch (Throwable t) {
			assertTrue(t.getClass().equals(IllegalStateException.class));
		}

		Session boundSession = SessionFactoryUtils.getSession(factory, true);
		assertTrue(session == boundSession);
		assertFalse(TransactionSynchronizationManager.hasResource(factory));
		assertFalse(TransactionSynchronizationManager.isSynchronizationActive());
	}

	/**
	 * When called with no previous Session bound, "allowCreate", and "allowSynchronization", Session should be
	 * returned, it should be bound to the Thread, and a TopLinkSyncrhonizationListener should be in the list of
	 * Thread Synchronizations
	 */
	public void testNoSessionBoundAllowAndInit() {
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();

		SessionFactory factory = new SingleSessionFactory(session);

		session.hasExternalTransactionController();
		sessionControl.setReturnValue(false, 1);

		sessionControl.replay();

		Session boundSession = SessionFactoryUtils.getSession(factory, true, true);
		assertTrue(session == boundSession);

		org.springframework.orm.toplink.SessionHolder holder = (org.springframework.orm.toplink.SessionHolder) TransactionSynchronizationManager.getResource(factory);
		assertTrue(holder == null);

		TransactionSynchronizationManager.initSynchronization();

		boundSession = SessionFactoryUtils.getSession(factory, true, true);
		assertTrue(session == boundSession);
		assertTrue(TransactionSynchronizationManager.getSynchronizations().size() == 1);
		//assertTrue(TransactionSynchronizationManager.getSynchronizations().get(0).getClass()==TopLinkSynchronizationListener.class);
		assertTrue(TransactionSynchronizationManager.hasResource(factory));
		assertTrue(session == ((SessionHolder) TransactionSynchronizationManager.getResource(factory)).getSession());

		TransactionSynchronizationManager.clearSynchronization();
		TransactionSynchronizationManager.unbindResource(factory);
	}

	public void testNoSessionBoundAllowAndNoInit() {
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();

		SessionFactory factory = new SingleSessionFactory(session);

		session.hasExternalTransactionController();
		sessionControl.setReturnValue(false, 2);

		sessionControl.replay();

		Session boundSession = SessionFactoryUtils.getSession(factory, true, true);
		assertTrue(session == boundSession);

		org.springframework.orm.toplink.SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(factory);
		assertTrue(holder == null);

		boundSession = SessionFactoryUtils.getSession(factory, true, true);
		assertTrue(session == boundSession);
		assertFalse(TransactionSynchronizationManager.hasResource(factory));
	}

	/**
	 * When called with no previous Session bound, and the Session is configured to use an ETC, then we are NOT using
	 * a Spring PlatformTransactionManager.  In this case, we should register a JTA Syncrhronization to clean up the Spring Resource.
	 * The Session should be bound to the Thread and then removed by the JTA Syncrhonization.
	 *
	 */
//    public void testNoSessionBoundWithSynchronizationAndETC()
//    {
//        MockControl sessionControl = MockControl.createControl(Session.class);
//        Session session = (Session)sessionControl.getMock();
// 
//        SessionFactory factory = new SingleSessionFactory(session);
//
//        session.hasExternalTransactionController();
//        sessionControl.setReturnValue(true,1);
//
//        sessionControl.replay();
//
//        SessionHolder holder = (SessionHolder)TransactionSynchronizationManager.getResource(factory);
//        assertTrue(holder==null);
//
//        Session boundSession = SessionFactoryUtils.getSession(factory,true,true);
//        assertTrue(session==boundSession);
//        assertTrue(TransactionSynchronizationManager.hasResource(factory));
//        
//        // callbacks on Synchronization Object - simulate JTA
////        Synchronization synchronization = (Synchronization)MockTransaction.synchronization.get();
////        synchronization.beforeCompletion();
////        synchronization.afterCompletion(0);
//        
//        // make sure everything is cleaned up afterwards
////        assertFalse(TransactionSynchronizationManager.hasResource(factory));
//        TransactionSynchronizationManager.unbindResource(factory);
//    }
}
