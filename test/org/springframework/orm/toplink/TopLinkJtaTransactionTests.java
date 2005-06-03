/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.orm.toplink;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Status;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;
import oracle.toplink.exceptions.TopLinkException;
import oracle.toplink.sessions.Session;
import org.easymock.MockControl;

import org.springframework.transaction.MockJtaTransaction;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Juergen Hoeller
 * @author <a href="mailto:james.x.clark@oracle.com">James Clark</a>
 * @since 28.04.2005
 */
public class TopLinkJtaTransactionTests extends TestCase {

	public void testParticipatingJtaTransactionWithWithRequiresNew() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		MockControl tmControl = MockControl.createControl(TransactionManager.class);
		TransactionManager tm = (TransactionManager) tmControl.getMock();
		MockControl tx1Control = MockControl.createControl(javax.transaction.Transaction.class);
		javax.transaction.Transaction tx1 = (javax.transaction.Transaction) tx1Control.getMock();

		MockControl session1Control = MockControl.createControl(Session.class);
		Session session1 = (Session) session1Control.getMock();
		MockControl session2Control = MockControl.createControl(Session.class);
		final Session session2 = (Session) session2Control.getMock();
		final MockSessionFactory sf = new MockSessionFactory(session1);

		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_ACTIVE, 3);
		ut.begin();
		utControl.setVoidCallable(2);
		tm.suspend();
		tmControl.setReturnValue(tx1, 1);
		tm.resume(tx1);
		tmControl.setVoidCallable(1);
		ut.commit();
		utControl.setVoidCallable(2);

//		session1.hasExternalTransactionController();
//		session1Control.setReturnValue(true,1);
		session1.release();
		session1Control.setVoidCallable(1);
//		session2.hasExternalTransactionController();
//		session2Control.setReturnValue(true,1);
		session2.release();
		session2Control.setVoidCallable(1);

		utControl.replay();
		tmControl.replay();
		session1Control.replay();
		session2Control.replay();

		JtaTransactionManager ptm = new JtaTransactionManager();
		ptm.setUserTransaction(ut);
		ptm.setTransactionManager(tm);
		final TransactionTemplate tt = new TransactionTemplate(ptm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				SessionFactoryUtils.getSession(sf, true);
				final SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
				assertTrue("Has thread session", holder != null);
				sf.setSession(session2);

				tt.execute(new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						TopLinkTemplate ht = new TopLinkTemplate(sf);
						return ht.executeFind(new TopLinkCallback() {
							public Object doInTopLink(Session session) {
								assertTrue("Not enclosing session", session != holder.getSession());
								return null;
							}
						});
					}
				});
				assertTrue("Same thread session as before",
						holder.getSession() == SessionFactoryUtils.getSession(sf, false));
				return null;
			}
		});
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));

		utControl.verify();
		tmControl.verify();
		session1Control.verify();
		session2Control.verify();
	}

	public void testJtaTransactionCommit() throws Exception {
		doTestJtaTransactionCommit(Status.STATUS_NO_TRANSACTION);
	}

	public void testJtaTransactionCommitWithExisting() throws Exception {
		doTestJtaTransactionCommit(Status.STATUS_ACTIVE);
	}

	private void doTestJtaTransactionCommit(int status) throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(status, 1);
		if (status == Status.STATUS_NO_TRANSACTION) {
			ut.begin();
			utControl.setVoidCallable(1);
			ut.getStatus();
			utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
			ut.commit();
			utControl.setVoidCallable(1);
		}
		utControl.replay();

		final MockControl sessionControl = MockControl.createControl(Session.class);
		final Session session = (Session) sessionControl.getMock();
		final SessionFactory sf = new SingleSessionFactory(session);

		sessionControl.replay();

		JtaTransactionManager ptm = new JtaTransactionManager(ut);
		TransactionTemplate tt = new TransactionTemplate(ptm);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				try {
					assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isSynchronizationActive());
					assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
					TopLinkTemplate ht = new TopLinkTemplate(sf);
					List htl = ht.executeFind(new TopLinkCallback() {
						public Object doInTopLink(Session sess) {
							assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
							assertEquals(session, sess);
							return l;
						}
					});

					ht = new TopLinkTemplate(sf);
					htl = ht.executeFind(new TopLinkCallback() {
						public Object doInTopLink(Session sess) {
							assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
							assertEquals(session, sess);
							return l;
						}
					});
					assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));

					sessionControl.verify();
					sessionControl.reset();

					try {
						session.release();
						sessionControl.setVoidCallable(1);
					}
					catch (TopLinkException e) {
					}
					sessionControl.replay();
					return htl;
				}
				catch (Error err) {
					err.printStackTrace();
					throw err;
				}
			}
		});

		assertTrue("Correct result list", result == l);
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		utControl.verify();
		sessionControl.verify();
	}

	public void testJtaTransactionCommitWithJtaTm() throws Exception {
		doTestJtaTransactionCommitWithJtaTm(Status.STATUS_NO_TRANSACTION);
	}

	public void testJtaTransactionCommitWithJtaTmAndExisting() throws Exception {
		doTestJtaTransactionCommitWithJtaTm(Status.STATUS_ACTIVE);
	}

	private void doTestJtaTransactionCommitWithJtaTm(int status) throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(status, 1);
		if (status == Status.STATUS_NO_TRANSACTION) {
			ut.begin();
			utControl.setVoidCallable(1);
			ut.getStatus();
			utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
			ut.commit();
			utControl.setVoidCallable(1);
		}

		MockControl tmControl = MockControl.createControl(TransactionManager.class);
		TransactionManager tm = (TransactionManager) tmControl.getMock();
		MockJtaTransaction transaction = new MockJtaTransaction();
		tm.getStatus();
		tmControl.setReturnValue(Status.STATUS_ACTIVE, 6);
		tm.getTransaction();
		tmControl.setReturnValue(transaction, 6);

		final MockControl sessionControl = MockControl.createControl(Session.class);
		final Session session = (Session) sessionControl.getMock();
		final SessionFactory sf = new SingleSessionFactory(session);

		utControl.replay();
		tmControl.replay();
		sessionControl.replay();

		JtaTransactionManager ptm = new JtaTransactionManager(ut);
		TransactionTemplate tt = new TransactionTemplate(ptm);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				try {
					assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isSynchronizationActive());
					assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));

					TopLinkTemplate ht = new TopLinkTemplate(sf);
					List htl = ht.executeFind(new TopLinkCallback() {
						public Object doInTopLink(Session sess) {
							assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
							assertEquals(session, sess);
							return l;
						}
					});

					ht = new TopLinkTemplate(sf);
					htl = ht.executeFind(new TopLinkCallback() {
						public Object doInTopLink(Session sess) {
							assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
							assertEquals(session, sess);
							return l;
						}
					});

					assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
					sessionControl.verify();
					sessionControl.reset();
					try {
						session.release();
						sessionControl.setVoidCallable(1);
					}
					catch (TopLinkException e) {
					}
					sessionControl.replay();
					return htl;
				}
				catch (Error err) {
					err.printStackTrace();
					throw err;
				}
			}
		});

		assertTrue("Correct result list", result == l);
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		utControl.verify();
		sessionControl.verify();
	}
//
//	public void testJtaTransactionWithFlushFailure() throws Exception {
//		MockControl utControl = MockControl.createControl(UserTransaction.class);
//		UserTransaction ut = (UserTransaction) utControl.getMock();
//		ut.getStatus();
//		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
//		ut.getStatus();
//		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
//		ut.begin();
//		utControl.setVoidCallable(1);
//		ut.rollback();
//		utControl.setVoidCallable(1);
//		utControl.replay();
//
//		MockControl sfControl = MockControl.createControl(SessionFactory.class);
//		final SessionFactory sf = (SessionFactory) sfControl.getMock();
//		final MockControl sessionControl = MockControl.createControl(Session.class);
//		final Session session = (Session) sessionControl.getMock();
//		sf.openSession();
//		sfControl.setReturnValue(session, 1);
//		session.getSessionFactory();
//		sessionControl.setReturnValue(sf, 1);
//		sfControl.replay();
//		sessionControl.replay();
//
//		JtaTransactionManager ptm = new JtaTransactionManager(ut);
//		TransactionTemplate tt = new TransactionTemplate(ptm);
//		final List l = new ArrayList();
//		l.add("test");
//		final HibernateException flushEx = new HibernateException("flush failure");
//		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//
//		try {
//			tt.execute(new TransactionCallback() {
//				public Object doInTransaction(TransactionStatus status) {
//					try {
//						assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isSynchronizationActive());
//						assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//						HibernateTemplate ht = new HibernateTemplate(sf);
//						ht.setExposeNativeSession(true);
//						List htl = ht.executeFind(new HibernateCallback() {
//							public Object doInHibernate(Session sess) {
//								assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//								assertEquals(session, sess);
//								return l;
//							}
//						});
//						ht = new HibernateTemplate(sf);
//						ht.setExposeNativeSession(true);
//						htl = ht.executeFind(new HibernateCallback() {
//							public Object doInHibernate(Session sess) {
//								assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//								assertEquals(session, sess);
//								return l;
//							}
//						});
//						assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//						sessionControl.verify();
//						sessionControl.reset();
//						try {
//							session.getFlushMode();
//							sessionControl.setReturnValue(FlushMode.AUTO, 1);
//							session.flush();
//							sessionControl.setThrowable(flushEx);
//							session.close();
//							sessionControl.setReturnValue(null, 1);
//						}
//						catch (HibernateException e) {
//						}
//						sessionControl.replay();
//						return htl;
//					}
//					catch (Error err) {
//						err.printStackTrace();
//						throw err;
//					}
//				}
//			});
//		}
//		catch (DataAccessException ex) {
//			// expected
//			assertTrue(flushEx == ex.getCause());
//		}
//
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//
//		utControl.verify();
//		sfControl.verify();
//		sessionControl.verify();
//	}
//
//	public void testJtaTransactionRollback() throws Exception {
//		MockControl utControl = MockControl.createControl(UserTransaction.class);
//		UserTransaction ut = (UserTransaction) utControl.getMock();
//		ut.getStatus();
//		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
//		ut.begin();
//		utControl.setVoidCallable(1);
//		ut.rollback();
//		utControl.setVoidCallable(1);
//		utControl.replay();
//
//		MockControl sfControl = MockControl.createControl(SessionFactory.class);
//		final SessionFactory sf = (SessionFactory) sfControl.getMock();
//		final MockControl sessionControl = MockControl.createControl(Session.class);
//		final Session session = (Session) sessionControl.getMock();
//		sf.openSession();
//		sfControl.setReturnValue(session, 1);
//		session.getSessionFactory();
//		sessionControl.setReturnValue(sf, 1);
//		sfControl.replay();
//		sessionControl.replay();
//
//		JtaTransactionManager ptm = new JtaTransactionManager(ut);
//		TransactionTemplate tt = new TransactionTemplate(ptm);
//		final List l = new ArrayList();
//		l.add("test");
//		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//
//		Object result = tt.execute(new TransactionCallback() {
//			public Object doInTransaction(TransactionStatus status) {
//				try {
//					assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isSynchronizationActive());
//					HibernateTemplate ht = new HibernateTemplate(sf);
//					List htl = ht.executeFind(new HibernateCallback() {
//						public Object doInHibernate(Session session) {
//							return l;
//						}
//					});
//					status.setRollbackOnly();
//					sessionControl.verify();
//					sessionControl.reset();
//					try {
//						session.close();
//					}
//					catch (HibernateException ex) {
//					}
//					sessionControl.setReturnValue(null, 1);
//					sessionControl.replay();
//					return htl;
//				}
//				catch (Error err) {
//					err.printStackTrace();
//					throw err;
//				}
//			}
//		});
//		assertTrue("Correct result list", result == l);
//
//		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//		utControl.verify();
//		sfControl.verify();
//		sessionControl.verify();
//	}
//
//	public void testJtaTransactionCommitWithPreBound() throws Exception {
//		doTestJtaTransactionCommitWithPreBound(false, false);
//	}
//
//	public void testJtaTransactionCommitWithPreBoundAndFlushModeNever() throws Exception {
//		doTestJtaTransactionCommitWithPreBound(false, true);
//	}
//
//	public void testJtaTransactionCommitWithJtaTmAndPreBound() throws Exception {
//		doTestJtaTransactionCommitWithPreBound(true, false);
//	}
//
//	public void testJtaTransactionCommitWithJtaTmAndPreBoundAndFlushModeNever() throws Exception {
//		doTestJtaTransactionCommitWithPreBound(true, true);
//	}
//
//	protected void doTestJtaTransactionCommitWithPreBound(boolean jtaTm, final boolean flushNever) throws Exception {
//		MockControl utControl = MockControl.createControl(UserTransaction.class);
//		UserTransaction ut = (UserTransaction) utControl.getMock();
//		ut.getStatus();
//		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
//		ut.getStatus();
//		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
//		ut.begin();
//		utControl.setVoidCallable(1);
//		ut.commit();
//		utControl.setVoidCallable(1);
//
//		MockControl tmControl = MockControl.createControl(TransactionManager.class);
//		TransactionManager tm = (TransactionManager) tmControl.getMock();
//		if (jtaTm) {
//			MockJtaTransaction transaction = new MockJtaTransaction();
//			tm.getStatus();
//			tmControl.setReturnValue(Status.STATUS_ACTIVE, 1);
//			tm.getTransaction();
//			tmControl.setReturnValue(transaction, 1);
//		}
//
//		MockControl sfControl = MockControl.createControl(SessionFactoryImplementor.class);
//		final SessionFactoryImplementor sf = (SessionFactoryImplementor) sfControl.getMock();
//		final MockControl sessionControl = MockControl.createControl(SessionImplementor.class);
//		final SessionImplementor session = (SessionImplementor) sessionControl.getMock();
//		sf.getConnectionProvider();
//		sfControl.setReturnValue(null, 1);
//		sf.getTransactionManager();
//		sfControl.setReturnValue((jtaTm ? tm : null), 1);
//		session.getFlushMode();
//		if (flushNever) {
//			sessionControl.setReturnValue(FlushMode.NEVER, 1);
//			session.setFlushMode(FlushMode.AUTO);
//			sessionControl.setVoidCallable(1);
//		}
//		else {
//			sessionControl.setReturnValue(FlushMode.AUTO, 1);
//		}
//
//		utControl.replay();
//		tmControl.replay();
//		sfControl.replay();
//		sessionControl.replay();
//
//		TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
//		try {
//			JtaTransactionManager ptm = new JtaTransactionManager(ut);
//			TransactionTemplate tt = new TransactionTemplate(ptm);
//			final List l = new ArrayList();
//			l.add("test");
//			assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//			assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//
//			Object result = tt.execute(new TransactionCallback() {
//				public Object doInTransaction(TransactionStatus status) {
//					try {
//						assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isSynchronizationActive());
//						assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//						HibernateTemplate ht = new HibernateTemplate(sf);
//						ht.setExposeNativeSession(true);
//						List htl = null;
//						for (int i = 0; i < 5; i++) {
//							htl = ht.executeFind(new HibernateCallback() {
//								public Object doInHibernate(Session sess) {
//									assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//									assertEquals(session, sess);
//									return l;
//								}
//							});
//							assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//						}
//						sessionControl.verify();
//						sessionControl.reset();
//						try {
//							session.getFlushMode();
//							sessionControl.setReturnValue(FlushMode.AUTO, 1);
//							session.flush();
//							sessionControl.setVoidCallable(1);
//							if (flushNever) {
//								session.setFlushMode(FlushMode.NEVER);
//								sessionControl.setVoidCallable(1);
//							}
//							session.afterTransactionCompletion(true);
//							sessionControl.setVoidCallable(1);
//						}
//						catch (HibernateException e) {
//						}
//						sessionControl.replay();
//						return htl;
//					}
//					catch (Error err) {
//						err.printStackTrace();
//						throw err;
//					}
//				}
//			});
//
//			assertTrue("Correct result list", result == l);
//			assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//			assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//		}
//		finally {
//			TransactionSynchronizationManager.unbindResource(sf);
//		}
//
//		utControl.verify();
//		tmControl.verify();
//		sfControl.verify();
//		sessionControl.verify();
//	}
//
//	public void testJtaTransactionRollbackWithPreBound() throws Exception {
//		MockControl utControl = MockControl.createControl(UserTransaction.class);
//		UserTransaction ut = (UserTransaction) utControl.getMock();
//		ut.getStatus();
//		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
//		ut.getStatus();
//		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
//		ut.getStatus();
//		utControl.setReturnValue(Status.STATUS_MARKED_ROLLBACK, 1);
//		ut.begin();
//		utControl.setVoidCallable(1);
//		ut.setRollbackOnly();
//		utControl.setVoidCallable(1);
//		ut.rollback();
//		utControl.setVoidCallable(1);
//		utControl.replay();
//
//		MockControl sfControl = MockControl.createControl(SessionFactory.class);
//		final SessionFactory sf = (SessionFactory) sfControl.getMock();
//		final MockControl sessionControl = MockControl.createControl(Session.class);
//		final Session session = (Session) sessionControl.getMock();
//		session.getSessionFactory();
//		sessionControl.setReturnValue(sf, 1);
//		session.getFlushMode();
//		sessionControl.setReturnValue(FlushMode.AUTO, 1);
//		sfControl.replay();
//		sessionControl.replay();
//
//		TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
//		try {
//			JtaTransactionManager ptm = new JtaTransactionManager(ut);
//			final TransactionTemplate tt = new TransactionTemplate(ptm);
//			assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//			assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//
//			tt.execute(new TransactionCallbackWithoutResult() {
//				public void doInTransactionWithoutResult(TransactionStatus status) {
//					tt.execute(new TransactionCallbackWithoutResult() {
//						public void doInTransactionWithoutResult(TransactionStatus status) {
//							status.setRollbackOnly();
//							try {
//								assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isSynchronizationActive());
//								assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//								HibernateTemplate ht = new HibernateTemplate(sf);
//								ht.setExposeNativeSession(true);
//								for (int i = 0; i < 5; i++) {
//									ht.execute(new HibernateCallback() {
//										public Object doInHibernate(Session sess) {
//											assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//											assertEquals(session, sess);
//											return null;
//										}
//									});
//									assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//								}
//								sessionControl.verify();
//								sessionControl.reset();
//								session.clear();
//								sessionControl.setVoidCallable(1);
//								sessionControl.replay();
//							}
//							catch (Error err) {
//								err.printStackTrace();
//								throw err;
//							}
//						}
//					});
//				}
//			});
//
//			assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//			assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//		}
//		finally {
//			TransactionSynchronizationManager.unbindResource(sf);
//		}
//
//		utControl.verify();
//		sfControl.verify();
//		sessionControl.verify();
//	}
//
//	public void testJtaSessionSynchronization() throws Exception {
//		MockControl tmControl = MockControl.createControl(TransactionManager.class);
//		TransactionManager tm = (TransactionManager) tmControl.getMock();
//		MockJtaTransaction transaction = new MockJtaTransaction();
//		tm.getStatus();
//		tmControl.setReturnValue(Status.STATUS_ACTIVE, 6);
//		tm.getTransaction();
//		tmControl.setReturnValue(transaction, 6);
//
//		MockControl sfControl = MockControl.createControl(SessionFactoryImplementor.class);
//		final SessionFactoryImplementor sf = (SessionFactoryImplementor) sfControl.getMock();
//		final MockControl sessionControl = MockControl.createControl(Session.class);
//		final Session session = (Session) sessionControl.getMock();
//		sf.getConnectionProvider();
//		sfControl.setReturnValue(null, 1);
//		sf.openSession();
//		sfControl.setReturnValue(session, 1);
//		sf.getTransactionManager();
//		sfControl.setReturnValue(tm, 6);
//		session.getFlushMode();
//		sessionControl.setReturnValue(FlushMode.AUTO, 1);
//		session.flush();
//		sessionControl.setVoidCallable(1);
//		session.close();
//		sessionControl.setReturnValue(null, 1);
//
//		tmControl.replay();
//		sfControl.replay();
//		sessionControl.replay();
//
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		HibernateTemplate ht = new HibernateTemplate(sf);
//		ht.setExposeNativeSession(true);
//		for (int i = 0; i < 5; i++) {
//			ht.executeFind(new HibernateCallback() {
//				public Object doInHibernate(Session sess) {
//					assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//					assertEquals(session, sess);
//					return null;
//				}
//			});
//		}
//
//		Synchronization synchronization = transaction.getSynchronization();
//		assertTrue("JTA synchronization registered", synchronization != null);
//		synchronization.beforeCompletion();
//		synchronization.afterCompletion(Status.STATUS_COMMITTED);
//
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//
//		tmControl.verify();
//		sfControl.verify();
//		sessionControl.verify();
//	}
//
//	public void testJtaSessionSynchronizationWithRollback() throws Exception {
//		MockControl tmControl = MockControl.createControl(TransactionManager.class);
//		TransactionManager tm = (TransactionManager) tmControl.getMock();
//		MockJtaTransaction transaction = new MockJtaTransaction();
//		tm.getStatus();
//		tmControl.setReturnValue(Status.STATUS_ACTIVE, 6);
//		tm.getTransaction();
//		tmControl.setReturnValue(transaction, 6);
//
//		final HibernateException flushEx = new HibernateException("flush failure");
//		MockControl sfControl = MockControl.createControl(SessionFactoryImplementor.class);
//		final SessionFactoryImplementor sf = (SessionFactoryImplementor) sfControl.getMock();
//		final MockControl sessionControl = MockControl.createControl(Session.class);
//		final Session session = (Session) sessionControl.getMock();
//		sf.getConnectionProvider();
//		sfControl.setReturnValue(null, 1);
//		sf.openSession();
//		sfControl.setReturnValue(session, 1);
//		sf.getTransactionManager();
//		sfControl.setReturnValue(tm, 6);
//		session.close();
//		sessionControl.setReturnValue(null, 1);
//
//		tmControl.replay();
//		sfControl.replay();
//		sessionControl.replay();
//
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		HibernateTemplate ht = new HibernateTemplate(sf);
//		ht.setExposeNativeSession(true);
//		for (int i = 0; i < 5; i++) {
//			ht.executeFind(new HibernateCallback() {
//				public Object doInHibernate(Session sess) {
//					assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//					assertEquals(session, sess);
//					return null;
//				}
//			});
//		}
//
//		Synchronization synchronization = transaction.getSynchronization();
//		assertTrue("JTA synchronization registered", synchronization != null);
//		synchronization.afterCompletion(Status.STATUS_ROLLEDBACK);
//
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//
//		tmControl.verify();
//		sfControl.verify();
//		sessionControl.verify();
//	}
//
//	public void testJtaSessionSynchronizationWithFlushFailure() throws Exception {
//		MockControl tmControl = MockControl.createControl(TransactionManager.class);
//		TransactionManager tm = (TransactionManager) tmControl.getMock();
//		MockJtaTransaction transaction = new MockJtaTransaction();
//		tm.getStatus();
//		tmControl.setReturnValue(Status.STATUS_ACTIVE, 6);
//		tm.getTransaction();
//		tmControl.setReturnValue(transaction, 6);
//		tm.setRollbackOnly();
//		tmControl.setVoidCallable(1);
//
//		final HibernateException flushEx = new HibernateException("flush failure");
//		MockControl sfControl = MockControl.createControl(SessionFactoryImplementor.class);
//		final SessionFactoryImplementor sf = (SessionFactoryImplementor) sfControl.getMock();
//		final MockControl sessionControl = MockControl.createControl(Session.class);
//		final Session session = (Session) sessionControl.getMock();
//		sf.getConnectionProvider();
//		sfControl.setReturnValue(null, 1);
//		sf.openSession();
//		sfControl.setReturnValue(session, 1);
//		sf.getTransactionManager();
//		sfControl.setReturnValue(tm, 6);
//		session.getFlushMode();
//		sessionControl.setReturnValue(FlushMode.AUTO, 1);
//		session.flush();
//		sessionControl.setThrowable(flushEx, 1);
//		session.close();
//		sessionControl.setReturnValue(null, 1);
//
//		tmControl.replay();
//		sfControl.replay();
//		sessionControl.replay();
//
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		HibernateTemplate ht = new HibernateTemplate(sf);
//		ht.setExposeNativeSession(true);
//		for (int i = 0; i < 5; i++) {
//			ht.executeFind(new HibernateCallback() {
//				public Object doInHibernate(Session sess) {
//					assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//					assertEquals(session, sess);
//					return null;
//				}
//			});
//		}
//
//		Synchronization synchronization = transaction.getSynchronization();
//		assertTrue("JTA synchronization registered", synchronization != null);
//		synchronization.beforeCompletion();
//		synchronization.afterCompletion(Status.STATUS_COMMITTED);
//
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//
//		tmControl.verify();
//		sfControl.verify();
//		sessionControl.verify();
//	}
//
//	public void testJtaSessionSynchronizationWithSuspendedTransaction() throws Exception {
//		MockControl tmControl = MockControl.createControl(TransactionManager.class);
//		TransactionManager tm = (TransactionManager) tmControl.getMock();
//		MockJtaTransaction transaction1 = new MockJtaTransaction();
//		MockJtaTransaction transaction2 = new MockJtaTransaction();
//		tm.getStatus();
//		tmControl.setReturnValue(Status.STATUS_ACTIVE, 5);
//		tm.getTransaction();
//		tmControl.setReturnValue(transaction1, 2);
//		tm.getTransaction();
//		tmControl.setReturnValue(transaction2, 3);
//
//		MockControl sfControl = MockControl.createControl(SessionFactoryImplementor.class);
//		final SessionFactoryImplementor sf = (SessionFactoryImplementor) sfControl.getMock();
//		final MockControl session1Control = MockControl.createControl(Session.class);
//		final Session session1 = (Session) session1Control.getMock();
//		final MockControl session2Control = MockControl.createControl(Session.class);
//		final Session session2 = (Session) session2Control.getMock();
//		sf.getConnectionProvider();
//		sfControl.setReturnValue(null, 1);
//		sf.openSession();
//		sfControl.setReturnValue(session1, 1);
//		sf.openSession();
//		sfControl.setReturnValue(session2, 1);
//		sf.getTransactionManager();
//		sfControl.setReturnValue(tm, 5);
//		session1.getFlushMode();
//		session1Control.setReturnValue(FlushMode.AUTO, 1);
//		session2.getFlushMode();
//		session2Control.setReturnValue(FlushMode.AUTO, 1);
//		session1.flush();
//		session1Control.setVoidCallable(1);
//		session2.flush();
//		session2Control.setVoidCallable(1);
//		session1.close();
//		session1Control.setReturnValue(null, 1);
//		session2.close();
//		session2Control.setReturnValue(null, 1);
//
//		tmControl.replay();
//		sfControl.replay();
//		session1Control.replay();
//		session2Control.replay();
//
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		HibernateTemplate ht = new HibernateTemplate(sf);
//		ht.setExposeNativeSession(true);
//		ht.executeFind(new HibernateCallback() {
//			public Object doInHibernate(Session sess) {
//				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//				assertEquals(session1, sess);
//				return null;
//			}
//		});
//		ht.executeFind(new HibernateCallback() {
//			public Object doInHibernate(Session sess) {
//				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//				assertEquals(session2, sess);
//				return null;
//			}
//		});
//
//		Synchronization synchronization2 = transaction2.getSynchronization();
//		assertTrue("JTA synchronization registered", synchronization2 != null);
//		synchronization2.beforeCompletion();
//		synchronization2.afterCompletion(Status.STATUS_COMMITTED);
//
//		Synchronization synchronization1 = transaction1.getSynchronization();
//		assertTrue("JTA synchronization registered", synchronization1 != null);
//		synchronization1.beforeCompletion();
//		synchronization1.afterCompletion(Status.STATUS_COMMITTED);
//
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//
//		tmControl.verify();
//		sfControl.verify();
//		session1Control.verify();
//		session2Control.verify();
//	}
//
//	public void testJtaSessionSynchronizationWithNonSessionFactoryImplementor() throws Exception {
//		MockControl tmControl = MockControl.createControl(TransactionManager.class);
//		TransactionManager tm = (TransactionManager) tmControl.getMock();
//		MockJtaTransaction transaction = new MockJtaTransaction();
//		tm.getStatus();
//		tmControl.setReturnValue(Status.STATUS_ACTIVE, 6);
//		tm.getTransaction();
//		tmControl.setReturnValue(transaction, 6);
//
//		MockControl sfControl = MockControl.createControl(SessionFactory.class);
//		final SessionFactory sf = (SessionFactory) sfControl.getMock();
//		final MockControl sessionControl = MockControl.createControl(Session.class);
//		final Session session = (Session) sessionControl.getMock();
//		MockControl sfiControl = MockControl.createControl(SessionFactoryImplementor.class);
//		final SessionFactoryImplementor sfi = (SessionFactoryImplementor) sfiControl.getMock();
//		sf.openSession();
//		sfControl.setReturnValue(session, 1);
//		session.getSessionFactory();
//		sessionControl.setReturnValue(sfi, 6);
//		sfi.getTransactionManager();
//		sfiControl.setReturnValue(tm, 6);
//		session.getFlushMode();
//		sessionControl.setReturnValue(FlushMode.AUTO, 1);
//		session.flush();
//		sessionControl.setVoidCallable(1);
//		session.close();
//		sessionControl.setReturnValue(null, 1);
//
//		tmControl.replay();
//		sfControl.replay();
//		sessionControl.replay();
//		sfiControl.replay();
//
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		HibernateTemplate ht = new HibernateTemplate(sf);
//		ht.setExposeNativeSession(true);
//		for (int i = 0; i < 5; i++) {
//			ht.executeFind(new HibernateCallback() {
//				public Object doInHibernate(Session sess) {
//					assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//					assertEquals(session, sess);
//					return null;
//				}
//			});
//		}
//
//		Synchronization synchronization = transaction.getSynchronization();
//		assertTrue("JTA Synchronization registered", synchronization != null);
//		synchronization.beforeCompletion();
//		synchronization.afterCompletion(Status.STATUS_COMMITTED);
//
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//
//		tmControl.verify();
//		sfControl.verify();
//		sessionControl.verify();
//		sfiControl.verify();
//	}
//
//	public void testJtaSessionSynchronizationWithSpringTransactionLaterOn() throws Exception {
//		MockControl utControl = MockControl.createControl(UserTransaction.class);
//		UserTransaction ut = (UserTransaction) utControl.getMock();
//		MockControl tmControl = MockControl.createControl(TransactionManager.class);
//		TransactionManager tm = (TransactionManager) tmControl.getMock();
//		MockJtaTransaction transaction = new MockJtaTransaction();
//		ut.getStatus();
//		utControl.setReturnValue(Status.STATUS_ACTIVE, 2);
//		tm.getStatus();
//		tmControl.setReturnValue(Status.STATUS_ACTIVE, 6);
//		tm.getTransaction();
//		tmControl.setReturnValue(transaction, 6);
//
//		MockControl sfControl = MockControl.createControl(SessionFactoryImplementor.class);
//		final SessionFactoryImplementor sf = (SessionFactoryImplementor) sfControl.getMock();
//		final MockControl sessionControl = MockControl.createControl(Session.class);
//		final Session session = (Session) sessionControl.getMock();
//		sf.getConnectionProvider();
//		sfControl.setReturnValue(null, 1);
//		sf.openSession();
//		sfControl.setReturnValue(session, 1);
//		sf.getTransactionManager();
//		sfControl.setReturnValue(tm, 6);
//		session.getFlushMode();
//		sessionControl.setReturnValue(FlushMode.AUTO, 1);
//		session.flush();
//		sessionControl.setVoidCallable(1);
//		session.close();
//		sessionControl.setReturnValue(null, 1);
//
//		utControl.replay();
//		tmControl.replay();
//		sfControl.replay();
//		sessionControl.replay();
//
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		final HibernateTemplate ht = new HibernateTemplate(sf);
//		ht.setExposeNativeSession(true);
//		for (int i = 0; i < 2; i++) {
//			ht.executeFind(new HibernateCallback() {
//				public Object doInHibernate(Session sess) {
//					assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//					assertEquals(session, sess);
//					return null;
//				}
//			});
//		}
//
//		TransactionTemplate tt = new TransactionTemplate(new JtaTransactionManager(ut));
//		tt.execute(new TransactionCallbackWithoutResult() {
//			protected void doInTransactionWithoutResult(TransactionStatus status) {
//				for (int i = 2; i < 5; i++) {
//					ht.executeFind(new HibernateCallback() {
//						public Object doInHibernate(Session sess) {
//							assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//							assertEquals(session, sess);
//							return null;
//						}
//					});
//				}
//			}
//		});
//
//		Synchronization synchronization = transaction.getSynchronization();
//		assertTrue("JTA synchronization registered", synchronization != null);
//		synchronization.beforeCompletion();
//		synchronization.afterCompletion(Status.STATUS_COMMITTED);
//
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//
//		utControl.verify();
//		tmControl.verify();
//		sfControl.verify();
//		sessionControl.verify();
//	}
//
//	public void testJtaSessionSynchronizationWithPreBound() throws Exception {
//		doTestJtaSessionSynchronizationWithPreBound(false);
//	}
//
//	public void testJtaJtaSessionSynchronizationWithPreBoundAndFlushNever() throws Exception {
//		doTestJtaSessionSynchronizationWithPreBound(true);
//	}
//
//	private void doTestJtaSessionSynchronizationWithPreBound(boolean flushNever) throws Exception {
//		MockControl tmControl = MockControl.createControl(TransactionManager.class);
//		TransactionManager tm = (TransactionManager) tmControl.getMock();
//		MockJtaTransaction transaction = new MockJtaTransaction();
//		tm.getStatus();
//		tmControl.setReturnValue(Status.STATUS_ACTIVE, 6);
//		tm.getTransaction();
//		tmControl.setReturnValue(transaction, 6);
//
//		MockControl sfControl = MockControl.createControl(SessionFactoryImplementor.class);
//		final SessionFactoryImplementor sf = (SessionFactoryImplementor) sfControl.getMock();
//		final MockControl sessionControl = MockControl.createControl(Session.class);
//		final Session session = (Session) sessionControl.getMock();
//		sf.getConnectionProvider();
//		sfControl.setReturnValue(null, 1);
//		sf.getTransactionManager();
//		sfControl.setReturnValue(tm, 6);
//		session.getFlushMode();
//		if (flushNever) {
//			sessionControl.setReturnValue(FlushMode.NEVER, 1);
//			session.setFlushMode(FlushMode.AUTO);
//			sessionControl.setVoidCallable(1);
//		}
//		else {
//			sessionControl.setReturnValue(FlushMode.AUTO, 1);
//		}
//
//		tmControl.replay();
//		sfControl.replay();
//		sessionControl.replay();
//
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
//		try {
//			assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//			HibernateTemplate ht = new HibernateTemplate(sf);
//			ht.setExposeNativeSession(true);
//			for (int i = 0; i < 5; i++) {
//				ht.executeFind(new HibernateCallback() {
//					public Object doInHibernate(Session sess) {
//						assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//						assertEquals(session, sess);
//						return null;
//					}
//				});
//			}
//
//			sessionControl.verify();
//			sessionControl.reset();
//			session.getFlushMode();
//			sessionControl.setReturnValue(FlushMode.AUTO, 1);
//			session.flush();
//			sessionControl.setVoidCallable(1);
//			if (flushNever) {
//				session.setFlushMode(FlushMode.NEVER);
//				sessionControl.setVoidCallable(1);
//			}
//			sessionControl.replay();
//
//			Synchronization synchronization = transaction.getSynchronization();
//			assertTrue("JTA synchronization registered", synchronization != null);
//			synchronization.beforeCompletion();
//			synchronization.afterCompletion(Status.STATUS_COMMITTED);
//			assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//		}
//		finally {
//			TransactionSynchronizationManager.unbindResource(sf);
//		}
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//
//		tmControl.verify();
//		sfControl.verify();
//		sessionControl.verify();
//	}
//
//	public void testJtaSessionSynchronizationWithRemoteTransaction() throws Exception {
//		MockControl tmControl = MockControl.createControl(TransactionManager.class);
//		TransactionManager tm = (TransactionManager) tmControl.getMock();
//		MockJtaTransaction transaction = new MockJtaTransaction();
//
//		MockControl sfControl = MockControl.createControl(SessionFactoryImplementor.class);
//		final SessionFactoryImplementor sf = (SessionFactoryImplementor) sfControl.getMock();
//		final MockControl sessionControl = MockControl.createControl(Session.class);
//		final Session session = (Session) sessionControl.getMock();
//
//		for (int j = 0; j < 2; j++) {
//			tmControl.reset();
//			sfControl.reset();
//			sessionControl.reset();
//
//			tm.getStatus();
//			tmControl.setReturnValue(Status.STATUS_ACTIVE, 6);
//			tm.getTransaction();
//			tmControl.setReturnValue(transaction, 6);
//
//			sf.getConnectionProvider();
//			sfControl.setReturnValue(null, 1);
//			sf.openSession();
//			sfControl.setReturnValue(session, 1);
//			sf.getTransactionManager();
//			sfControl.setReturnValue(tm, 6);
//			session.getFlushMode();
//			sessionControl.setReturnValue(FlushMode.AUTO, 1);
//			session.flush();
//			sessionControl.setVoidCallable(1);
//			session.close();
//			sessionControl.setReturnValue(null, 1);
//
//			tmControl.replay();
//			sfControl.replay();
//			sessionControl.replay();
//
//			if (j == 0) {
//				assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//			}
//			else {
//				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//			}
//
//			HibernateTemplate ht = new HibernateTemplate(sf);
//			ht.setExposeNativeSession(true);
//			for (int i = 0; i < 5; i++) {
//				ht.executeFind(new HibernateCallback() {
//					public Object doInHibernate(Session sess) {
//						assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//						assertEquals(session, sess);
//						return null;
//					}
//				});
//			}
//
//			final Synchronization synchronization = transaction.getSynchronization();
//			assertTrue("JTA synchronization registered", synchronization != null);
//
//			// Call synchronization in a new thread, to simulate a synchronization
//			// triggered by a new remote call from a remote transaction coordinator.
//			Thread synch = new Thread() {
//				public void run() {
//					synchronization.beforeCompletion();
//					synchronization.afterCompletion(Status.STATUS_COMMITTED);
//				}
//			};
//			synch.start();
//			synch.join();
//
//			assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//			SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
//			assertTrue("Thread session holder empty", sessionHolder.isEmpty());
//			assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//
//			tmControl.verify();
//			sfControl.verify();
//			sessionControl.verify();
//		}
//
//		TransactionSynchronizationManager.unbindResource(sf);
//	}

	protected void tearDown() {
		assertTrue(TransactionSynchronizationManager.getResourceMap().isEmpty());
		assertFalse(TransactionSynchronizationManager.isSynchronizationActive());
	}

}
