package org.springframework.orm.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;
import net.sf.hibernate.FlushMode;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.JDBCException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import org.easymock.MockControl;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.JtaTransactionTestSuite;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * @author Juergen Hoeller
 */
public class HibernateTransactionManagerTests extends TestCase {

	public void testTransactionCommit() throws SQLException, HibernateException {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();
		final List list = new ArrayList();
		list.add("test");
		con.getTransactionIsolation();
		conControl.setReturnValue(Connection.TRANSACTION_READ_COMMITTED);
		con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		conControl.setVoidCallable(1);
		con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.connection();
		sessionControl.setReturnValue(con, 4);
		session.createQuery("some query string");
		sessionControl.setReturnValue(query, 1);
		query.setTimeout(10);
		queryControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		tx.commit();
		txControl.setVoidCallable(1);
		dsControl.replay();
		conControl.replay();
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();
		queryControl.replay();

		HibernateTransactionManager tm = new HibernateTransactionManager();
		tm.setSessionFactory(sf);
		tm.setDataSource(ds);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
		tt.setTimeout(10);
		assertTrue("Hasn't thread session", !SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
		assertTrue("Hasn't thread connection", !DataSourceUtils.getThreadObjectManager().hasThreadObject(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isActive());

		try {
			Object result = tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue("Has thread session", SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
					assertTrue("Has thread connection", DataSourceUtils.getThreadObjectManager().hasThreadObject(ds));
					HibernateTemplate ht = new HibernateTemplate(sf);
					return ht.find("some query string");
				}
			});
			assertTrue("Correct result list", result == list);
		}
		catch (RuntimeException ex) {
			fail("Should not have thrown RuntimeException");
		}

		assertTrue("Hasn't thread session", !SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
		assertTrue("Hasn't thread connection", !DataSourceUtils.getThreadObjectManager().hasThreadObject(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isActive());
		dsControl.verify();
		conControl.verify();
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
		queryControl.verify();
	}

	public void testTransactionRollback() throws HibernateException, SQLException {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		tx.rollback();
		txControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new HibernateTransactionManager(sf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		assertTrue("Hasn't thread session", !SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isActive());

		try {
			tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue("Has thread session", SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
					HibernateTemplate ht = new HibernateTemplate(sf);
					return ht.executeFind(new HibernateCallback() {
						public Object doInHibernate(Session session) {
							throw new RuntimeException("application exception");
						}
					});
				}
			});
			fail("Should have thrown RuntimeException");
		}
		catch (RuntimeException ex) {
			// expected
		}

		assertTrue("Hasn't thread session", !SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isActive());
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
	}

	public void testTransactionRollbackOnly() throws HibernateException, SQLException {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		tx.rollback();
		txControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new HibernateTransactionManager(sf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		assertTrue("Hasn't thread session", !SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));

		try {
			tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue("Has thread session", SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
					HibernateTemplate ht = new HibernateTemplate(sf);
					ht.setFlushMode(HibernateTemplate.FLUSH_EAGER);
					ht.execute(new HibernateCallback() {
						public Object doInHibernate(Session session) {
							return null;
						}
					});
					status.setRollbackOnly();
					return null;
				}
			});
		}
		catch (RuntimeException ex) {
			fail("Should not have thrown RuntimeException");
		}

		assertTrue("Hasn't thread session", !SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
	}

	public void testNestedTransactionCommit() throws HibernateException, SQLException {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		tx.commit();
		txControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new HibernateTransactionManager(sf);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		final List l = new ArrayList();
		l.add("test");

		try {
			Object result = tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					return tt.execute(new TransactionCallback() {
						public Object doInTransaction(TransactionStatus status) {
							HibernateTemplate ht = new HibernateTemplate(sf);
							ht.setFlushMode(HibernateTemplate.FLUSH_EAGER);
							return ht.executeFind(new HibernateCallback() {
								public Object doInHibernate(Session session) {
									return l;
								}
							});
						}
					});
				}
			});
			assertTrue("Correct result list", result == l);
		}
		catch (RuntimeException ex) {
			fail("Should not have thrown RuntimeException");
		}

		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
	}

	public void testNestedTransactionRollback() throws HibernateException, SQLException {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		tx.rollback();
		txControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new HibernateTransactionManager(sf);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		try {
			tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					return tt.execute(new TransactionCallback() {
						public Object doInTransaction(TransactionStatus status) {
							HibernateTemplate ht = new HibernateTemplate(sf);
							ht.setFlushMode(HibernateTemplate.FLUSH_EAGER);
							return ht.executeFind(new HibernateCallback() {
								public Object doInHibernate(Session session) {
									throw new RuntimeException("application exception");
								}
							});
						}
					});
				}
			});
			fail("Should not thrown RuntimeException");
		}
		catch (RuntimeException ex) {
			// expected
		}
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
	}

	public void testNestedTransactionRollbackOnly() throws HibernateException, SQLException {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		tx.rollback();
		txControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new HibernateTransactionManager(sf);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		final List l = new ArrayList();
		l.add("test");

		try {
			tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					return tt.execute(new TransactionCallback() {
						public Object doInTransaction(TransactionStatus status) {
							HibernateTemplate ht = new HibernateTemplate(sf);
							ht.execute(new HibernateCallback() {
								public Object doInHibernate(Session session) {
									return l;
								}
							});
							status.setRollbackOnly();
							return null;
						}
					});
				}
			});
		}
		catch (RuntimeException ex) {
			fail("Should not have thrown RuntimeException");
		}

		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
	}

	public void testJtaTransactionCommit() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.begin();
		utControl.setVoidCallable(1);
		ut.commit();
		utControl.setVoidCallable(1);
		utControl.replay();

		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		final MockControl sessionControl = MockControl.createControl(Session.class);
		final Session session = (Session) sessionControl.getMock();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		sfControl.replay();
		sessionControl.replay();

		TransactionTemplate tt = JtaTransactionTestSuite.getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isActive());

		try {
			Object result = tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					try {
						assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isActive());
						HibernateTemplate ht = new HibernateTemplate(sf);
						List htl = ht.executeFind(new HibernateCallback() {
							public Object doInHibernate(Session session) {
								return l;
							}
						});
						ht = new HibernateTemplate(sf);
						htl = ht.executeFind(new HibernateCallback() {
							public Object doInHibernate(Session session) {
								return l;
							}
						});
						sessionControl.verify();
						sessionControl.reset();
						try {
							session.flush();
							sessionControl.setVoidCallable(1);
							session.close();
							sessionControl.setReturnValue(null, 1);
						}
						catch (HibernateException e) {
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
		}
		catch (RuntimeException ex) {
			fail("Should not have thrown RuntimeException: " + ex.getMessage());
		}

		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isActive());
		utControl.verify();
		sfControl.verify();
		sessionControl.verify();
	}

	public void testJtaTransactionRollback() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.begin();
		utControl.setVoidCallable(1);
		ut.rollback();
		utControl.setVoidCallable(1);
		utControl.replay();

		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		final MockControl sessionControl = MockControl.createControl(Session.class);
		final Session session = (Session) sessionControl.getMock();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		sfControl.replay();
		sessionControl.replay();

		TransactionTemplate tt = JtaTransactionTestSuite.getTransactionTemplateForJta(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isActive());

		try {
			Object result = tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					try {
						assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isActive());
						HibernateTemplate ht = new HibernateTemplate(sf);
						List htl = ht.executeFind(new HibernateCallback() {
							public Object doInHibernate(Session session) {
								return l;
							}
						});
						status.setRollbackOnly();
						sessionControl.verify();
						sessionControl.reset();
						try {
							session.close();
						}
						catch (HibernateException ex) {
						}
						sessionControl.setReturnValue(null, 1);
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
		}
		catch (RuntimeException ex) {
			fail("Should not have thrown RuntimeException: " + ex.getMessage());
		}

		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isActive());
		utControl.verify();
		sfControl.verify();
		sessionControl.verify();
	}

	public void testTransactionCommitWithPrebound() throws HibernateException, SQLException {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.connection();
		sessionControl.setReturnValue(con, 4);
		con.getTransactionIsolation();
		conControl.setReturnValue(Connection.TRANSACTION_READ_COMMITTED);
		con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		conControl.setVoidCallable(1);
		con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		conControl.setVoidCallable(1);
		tx.commit();
		txControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		dsControl.replay();
		conControl.replay();
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();

		HibernateTransactionManager tm = new HibernateTransactionManager();
		tm.setSessionFactory(sf);
		tm.setDataSource(ds);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("Hasn't thread connection", !DataSourceUtils.getThreadObjectManager().hasThreadObject(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isActive());
		SessionFactoryUtils.getThreadObjectManager().bindThreadObject(sf, new SessionHolder(session));
		assertTrue("Has thread session", SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));

		try {
			Object result = tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue("Has thread session", SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
					assertTrue("Has thread connection", DataSourceUtils.getThreadObjectManager().hasThreadObject(ds));
					HibernateTemplate ht = new HibernateTemplate(sf);
					return ht.executeFind(new HibernateCallback() {
						public Object doInHibernate(Session session) throws HibernateException {
							return l;
						}
					});
				}
			});
			assertTrue("Correct result list", result == l);
		}
		catch (RuntimeException ex) {
			fail("Should not have thrown RuntimeException");
		}

		assertTrue("Has thread session", SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
		SessionFactoryUtils.getThreadObjectManager().removeThreadObject(sf);
		assertTrue("Hasn't thread connection", !DataSourceUtils.getThreadObjectManager().hasThreadObject(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isActive());
		dsControl.verify();
		conControl.verify();
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
		conControl.verify();
	}

	public void testTransactionCommitWithEntityInterceptor() throws HibernateException, SQLException {
		MockControl interceptorControl = MockControl.createControl(net.sf.hibernate.Interceptor.class);
		Interceptor entityInterceptor = (Interceptor) interceptorControl.getMock();
		interceptorControl.replay();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		sf.openSession(entityInterceptor);
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		tx.commit();
		txControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();
		conControl.replay();

		HibernateTransactionManager tm = new HibernateTransactionManager(sf);
		tm.setEntityInterceptor(entityInterceptor);
		TransactionTemplate tt = new TransactionTemplate(tm);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("Hasn't thread session", !SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isActive());

		try {
			Object result = tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue("Has thread session", SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
					HibernateTemplate ht = new HibernateTemplate(sf);
					return ht.executeFind(new HibernateCallback() {
						public Object doInHibernate(Session session) throws HibernateException {
							return l;
						}
					});
				}
			});
			assertTrue("Correct result list", result == l);
		}
		catch (RuntimeException ex) {
			fail("Should not have thrown RuntimeException");
		}

		assertTrue("Hasn't thread session", !SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isActive());
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
		conControl.verify();
	}

	public void testTransactionCommitWithReadOnly() throws HibernateException, SQLException {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.setFlushMode(FlushMode.NEVER);
		sessionControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 2);
		session.close();
		sessionControl.setReturnValue(null, 1);
		tx.commit();
		txControl.setVoidCallable(1);
		con.setReadOnly(true);
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(true, 1);
		con.setReadOnly(false);
		conControl.setVoidCallable(1);
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();
		conControl.replay();

		HibernateTransactionManager tm = new HibernateTransactionManager(sf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setReadOnly(true);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("Hasn't thread session", !SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isActive());

		try {
			Object result = tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue("Has thread session", SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
					HibernateTemplate ht = new HibernateTemplate(sf);
					return ht.executeFind(new HibernateCallback() {
						public Object doInHibernate(Session session) throws HibernateException {
							return l;
						}
					});
				}
			});
			assertTrue("Correct result list", result == l);
		}
		catch (RuntimeException ex) {
			fail("Should not have thrown RuntimeException");
		}

		assertTrue("Hasn't thread session", !SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isActive());
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
		conControl.verify();
	}

	public void testTransactionCommitWithFlushingFailure() throws HibernateException, SQLException {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		tx.commit();
		txControl.setThrowable(new JDBCException(new SQLException("argh", "27")), 1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		tx.rollback();
		txControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();
		conControl.replay();

		HibernateTransactionManager tm = new HibernateTransactionManager(sf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("Hasn't thread session", !SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isActive());

		try {
			tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue("Has thread session", SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
					HibernateTemplate ht = new HibernateTemplate(sf);
					return ht.executeFind(new HibernateCallback() {
						public Object doInHibernate(Session session) throws HibernateException {
							return l;
						}
					});
				}
			});
			fail("Should have thrown DataIntegrityViolationException");
		}
		catch (DataIntegrityViolationException ex) {
			// expected
		}

		assertTrue("Hasn't thread session", !SessionFactoryUtils.getThreadObjectManager().hasThreadObject(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isActive());
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
		conControl.verify();
	}

}
