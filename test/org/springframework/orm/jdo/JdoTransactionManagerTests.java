/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.orm.jdo;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.jdbc.datasource.SimpleConnectionHandle;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Juergen Hoeller
 */
public class JdoTransactionManagerTests extends TestCase {

	public void testTransactionCommit() {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		final PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 2);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm, 1);
		pm.currentTransaction();
		pmControl.setReturnValue(tx, 2);
		pm.close();
		pmControl.setVoidCallable(1);
		tx.begin();
		txControl.setVoidCallable(1);
		tx.commit();
		txControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Has thread pm", TransactionSynchronizationManager.hasResource(pmf));
				JdoTemplate jt = new JdoTemplate(pmf);
				return jt.execute(new JdoCallback() {
					public Object doInJdo(PersistenceManager pm) {
						return l;
					}
				});
			}
		});
		assertTrue("Correct result list", result == l);

		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		pmfControl.verify();
		pmControl.verify();
		txControl.verify();
	}

	public void testTransactionRollback() {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		final PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 2);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm, 1);
		pm.currentTransaction();
		pmControl.setReturnValue(tx, 2);
		pm.close();
		pmControl.setVoidCallable(1);
		tx.begin();
		txControl.setVoidCallable(1);
		tx.rollback();
		txControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		try {
			tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue("Has thread pm", TransactionSynchronizationManager.hasResource(pmf));
					JdoTemplate jt = new JdoTemplate(pmf);
					return jt.execute(new JdoCallback() {
						public Object doInJdo(PersistenceManager pm) {
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

		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		pmfControl.verify();
		pmControl.verify();
		txControl.verify();
	}

	public void testTransactionRollbackOnly() {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		final PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 2);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm, 1);
		pm.currentTransaction();
		pmControl.setReturnValue(tx, 2);
		pm.close();
		pmControl.setVoidCallable(1);
		tx.begin();
		txControl.setVoidCallable(1);
		tx.rollback();
		txControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));

		tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Has thread pm", TransactionSynchronizationManager.hasResource(pmf));
				JdoTemplate jt = new JdoTemplate(pmf);
				jt.execute(new JdoCallback() {
					public Object doInJdo(PersistenceManager pm) {
						return null;
					}
				});
				status.setRollbackOnly();
				return null;
			}
		});

		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));
		pmfControl.verify();
		pmControl.verify();
		txControl.verify();
	}

	public void testParticipatingTransactionWithCommit() {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		final PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		final MockControl txControl = MockControl.createControl(Transaction.class);
		final Transaction tx = (Transaction) txControl.getMock();

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 2);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm, 1);
		pm.currentTransaction();
		pmControl.setReturnValue(tx, 3);
		pm.close();
		pmControl.setVoidCallable(1);
		tx.begin();
		txControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		final List l = new ArrayList();
		l.add("test");

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				txControl.reset();
				tx.isActive();
				txControl.setReturnValue(true, 1);
				tx.commit();
				txControl.setVoidCallable(1);
				txControl.replay();

				return tt.execute(new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						JdoTemplate jt = new JdoTemplate(pmf);
						return jt.execute(new JdoCallback() {
							public Object doInJdo(PersistenceManager pm) {
								return l;
							}
						});
					}
				});
			}
		});
		assertTrue("Correct result list", result == l);

		pmfControl.verify();
		pmControl.verify();
		txControl.verify();
	}

	public void testParticipatingTransactionWithRollback() {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		final PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		final MockControl txControl = MockControl.createControl(Transaction.class);
		final Transaction tx = (Transaction) txControl.getMock();

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 2);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm, 1);
		pm.currentTransaction();
		pmControl.setReturnValue(tx, 3);
		pm.close();
		pmControl.setVoidCallable(1);
		tx.isActive();
		txControl.setReturnValue(false, 1);
		tx.begin();
		txControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		try {
			tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					txControl.reset();
					tx.isActive();
					txControl.setReturnValue(true, 1);
					tx.rollback();
					txControl.setVoidCallable(1);
					txControl.replay();

					return tt.execute(new TransactionCallback() {
						public Object doInTransaction(TransactionStatus status) {
							JdoTemplate jt = new JdoTemplate(pmf);
							return jt.execute(new JdoCallback() {
								public Object doInJdo(PersistenceManager pm) {
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
		pmfControl.verify();
		pmControl.verify();
		txControl.verify();
	}

	public void testParticipatingTransactionWithRollbackOnly() {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		final PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		final MockControl txControl = MockControl.createControl(Transaction.class);
		final Transaction tx = (Transaction) txControl.getMock();

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 2);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm, 1);
		pm.currentTransaction();
		pmControl.setReturnValue(tx, 3);
		pm.close();
		pmControl.setVoidCallable(1);
		tx.begin();
		txControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		final List l = new ArrayList();
		l.add("test");

		tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				txControl.reset();
				tx.isActive();
				txControl.setReturnValue(true, 1);
				tx.rollback();
				txControl.setVoidCallable(1);
				txControl.replay();

				return tt.execute(new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						JdoTemplate jt = new JdoTemplate(pmf);
						jt.execute(new JdoCallback() {
							public Object doInJdo(PersistenceManager pm) {
								return l;
							}
						});
						status.setRollbackOnly();
						return null;
					}
				});
			}
		});

		pmfControl.verify();
		pmControl.verify();
		txControl.verify();
	}

	public void testParticipatingTransactionWithWithRequiresNew() {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		final PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		final MockControl txControl = MockControl.createControl(Transaction.class);
		final Transaction tx = (Transaction) txControl.getMock();

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 2);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm, 2);
		pm.currentTransaction();
		pmControl.setReturnValue(tx, 5);
		tx.begin();
		txControl.setVoidCallable(1);
		pm.close();
		pmControl.setVoidCallable(2);
		pmfControl.replay();
		pmControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		final List l = new ArrayList();
		l.add("test");

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				txControl.reset();
				tx.isActive();
				txControl.setReturnValue(true, 1);
				tx.begin();
				txControl.setVoidCallable(1);
				tx.commit();
				txControl.setVoidCallable(2);
				txControl.replay();

				return tt.execute(new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						JdoTemplate jt = new JdoTemplate(pmf);
						return jt.execute(new JdoCallback() {
							public Object doInJdo(PersistenceManager pm) {
								return l;
							}
						});
					}
				});
			}
		});
		assertTrue("Correct result list", result == l);

		pmfControl.verify();
		pmControl.verify();
		txControl.verify();
	}

	public void testJtaTransactionCommit() throws Exception {
		MockControl utControl = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utControl.getMock();
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_NO_TRANSACTION, 1);
		ut.getStatus();
		utControl.setReturnValue(Status.STATUS_ACTIVE, 1);
		ut.begin();
		utControl.setVoidCallable(1);
		ut.commit();
		utControl.setVoidCallable(1);
		utControl.replay();

		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		final PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		final PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm, 1);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();

		JtaTransactionManager ptm = new JtaTransactionManager(ut);
		TransactionTemplate tt = new TransactionTemplate(ptm);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("JTA synchronizations active", TransactionSynchronizationManager.isSynchronizationActive());
				assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));
				JdoTemplate jt = new JdoTemplate(pmf);
				jt.execute(new JdoCallback() {
					public Object doInJdo(PersistenceManager pm2) {
						assertTrue("Has thread pm", TransactionSynchronizationManager.hasResource(pmf));
						assertEquals(pm, pm2);
						return l;
					}
				});
				Object result = jt.execute(new JdoCallback() {
					public Object doInJdo(PersistenceManager pm2) {
						assertTrue("Has thread pm", TransactionSynchronizationManager.hasResource(pmf));
						assertEquals(pm, pm2);
						return l;
					}
				});
				assertTrue("Has thread pm", TransactionSynchronizationManager.hasResource(pmf));
				return result;
			}
		});
		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));
		assertTrue("Correct result list", result == l);

		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		pmfControl.verify();
		pmControl.verify();
	}

	public void testTransactionCommitWithPropagationSupports() {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		final PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 2);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm, 1);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));
				assertTrue("Is not new transaction", !status.isNewTransaction());
				JdoTemplate jt = new JdoTemplate(pmf);
				return jt.execute(new JdoCallback() {
					public Object doInJdo(PersistenceManager pm) {
						return l;
					}
				});
			}
		});
		assertTrue("Correct result list", result == l);

		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));
		pmfControl.verify();
		pmControl.verify();
	}

	public void testInvalidIsolation() {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		final PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm, 1);
		pm.currentTransaction();
		pmControl.setReturnValue(null, 1);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
		try {
			tt.execute(new TransactionCallbackWithoutResult() {
				protected void doInTransactionWithoutResult(TransactionStatus status) {
				}
			});
			fail("Should have thrown InvalidIsolationLevelException");
		}
		catch (InvalidIsolationLevelException ex) {
			// expected
		}

		pmfControl.verify();
	}

	public void testTransactionCommitWithPrebound() {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		final PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 2);
		pm.currentTransaction();
		pmControl.setReturnValue(tx, 3);
		tx.isActive();
		txControl.setReturnValue(false, 1);
		tx.begin();
		txControl.setVoidCallable(1);
		tx.commit();
		txControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new JdoTransactionManager(pmf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		TransactionSynchronizationManager.bindResource(pmf, new PersistenceManagerHolder(pm));
		assertTrue("Has thread pm", TransactionSynchronizationManager.hasResource(pmf));

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Has thread pm", TransactionSynchronizationManager.hasResource(pmf));
				JdoTemplate jt = new JdoTemplate(pmf);
				return jt.execute(new JdoCallback() {
					public Object doInJdo(PersistenceManager pm) {
						return l;
					}
				});
			}
		});
		assertTrue("Correct result list", result == l);

		assertTrue("Has thread pm", TransactionSynchronizationManager.hasResource(pmf));
		TransactionSynchronizationManager.unbindResource(pmf);
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		pmfControl.verify();
		pmControl.verify();
		txControl.verify();
	}

	public void testTransactionCommitWithDataSource() throws SQLException {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		final PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		MockControl dialectControl = MockControl.createControl(JdoDialect.class);
		JdoDialect dialect = (JdoDialect) dialectControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		final PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		final Connection con = (Connection) conControl.getMock();
		ConnectionHandle conHandle = new SimpleConnectionHandle(con);

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm, 1);
		pm.currentTransaction();
		pmControl.setReturnValue(tx, 2);
		pm.close();
		pmControl.setVoidCallable(1);
		TransactionTemplate tt = new TransactionTemplate();
		dialect.beginTransaction(tx, tt);
		dialectControl.setReturnValue(null, 1);
		dialect.getJdbcConnection(pm, false);
		dialectControl.setReturnValue(conHandle, 1);
		dialect.releaseJdbcConnection(conHandle, pm);
		dialectControl.setVoidCallable(1);
		dialect.cleanupTransaction(null);
		dialectControl.setVoidCallable(1);
		tx.commit();
		txControl.setVoidCallable(1);
		pmfControl.replay();
		dsControl.replay();
		dialectControl.replay();
		pmControl.replay();
		txControl.replay();
		conControl.replay();

		JdoTransactionManager tm = new JdoTransactionManager();
		tm.setPersistenceManagerFactory(pmf);
		tm.setDataSource(ds);
		tm.setJdoDialect(dialect);
		tt.setTransactionManager(tm);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Has thread pm", TransactionSynchronizationManager.hasResource(pmf));
				assertTrue("Has thread con", TransactionSynchronizationManager.hasResource(ds));
				JdoTemplate jt = new JdoTemplate(pmf);
				return jt.execute(new JdoCallback() {
					public Object doInJdo(PersistenceManager pm) {
						return l;
					}
				});
			}
		});
		assertTrue("Correct result list", result == l);

		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));
		assertTrue("Hasn't thread con", !TransactionSynchronizationManager.hasResource(ds));
		pmfControl.verify();
		dsControl.verify();
		dialectControl.verify();
		pmControl.verify();
		txControl.verify();
		conControl.verify();
	}

	public void testTransactionCommitWithAutoDetectedDataSource() throws SQLException {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		final PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		MockControl dialectControl = MockControl.createControl(JdoDialect.class);
		JdoDialect dialect = (JdoDialect) dialectControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		final PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		final Connection con = (Connection) conControl.getMock();
		ConnectionHandle conHandle = new SimpleConnectionHandle(con);

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(ds, 2);
		con.getMetaData();
		conControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm, 1);
		pm.currentTransaction();
		pmControl.setReturnValue(tx, 2);
		pm.close();
		pmControl.setVoidCallable(1);
		TransactionTemplate tt = new TransactionTemplate();
		dialect.beginTransaction(tx, tt);
		dialectControl.setReturnValue(null, 1);
		dialect.getJdbcConnection(pm, false);
		dialectControl.setReturnValue(conHandle, 1);
		dialect.releaseJdbcConnection(conHandle, pm);
		dialectControl.setVoidCallable(1);
		dialect.cleanupTransaction(null);
		dialectControl.setVoidCallable(1);
		tx.commit();
		txControl.setVoidCallable(1);
		pmfControl.replay();
		dsControl.replay();
		dialectControl.replay();
		pmControl.replay();
		txControl.replay();
		conControl.replay();

		JdoTransactionManager tm = new JdoTransactionManager();
		tm.setPersistenceManagerFactory(pmf);
		tm.setJdoDialect(dialect);
		tm.afterPropertiesSet();
		tt.setTransactionManager(tm);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Has thread pm", TransactionSynchronizationManager.hasResource(pmf));
				assertTrue("Has thread con", TransactionSynchronizationManager.hasResource(ds));
				JdoTemplate jt = new JdoTemplate(pmf);
				return jt.execute(new JdoCallback() {
					public Object doInJdo(PersistenceManager pm) {
						return l;
					}
				});
			}
		});
		assertTrue("Correct result list", result == l);

		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));
		assertTrue("Hasn't thread con", !TransactionSynchronizationManager.hasResource(ds));
		pmfControl.verify();
		dsControl.verify();
		dialectControl.verify();
		pmControl.verify();
		txControl.verify();
		conControl.verify();
	}

	public void testTransactionCommitWithAutoDetectedDataSourceAndNoConnection() throws SQLException {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		final PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		MockControl dialectControl = MockControl.createControl(JdoDialect.class);
		final JdoDialect dialect = (JdoDialect) dialectControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		final PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(ds, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm, 1);
		pm.currentTransaction();
		pmControl.setReturnValue(tx, 2);
		pm.close();
		pmControl.setVoidCallable(1);
		TransactionTemplate tt = new TransactionTemplate();
		dialect.beginTransaction(tx, tt);
		dialectControl.setReturnValue(null, 1);
		dialect.getJdbcConnection(pm, false);
		dialectControl.setReturnValue(null, 1);
		dialect.cleanupTransaction(null);
		dialectControl.setVoidCallable(1);
		tx.commit();
		txControl.setVoidCallable(1);
		pmfControl.replay();
		dsControl.replay();
		dialectControl.replay();
		pmControl.replay();
		txControl.replay();
		conControl.replay();

		JdoTransactionManager tm = new JdoTransactionManager();
		tm.setPersistenceManagerFactory(pmf);
		tm.setJdoDialect(dialect);
		tm.afterPropertiesSet();
		tt.setTransactionManager(tm);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Has thread pm", TransactionSynchronizationManager.hasResource(pmf));
				assertTrue("Hasn't thread con", !TransactionSynchronizationManager.hasResource(ds));
				JdoTemplate jt = new JdoTemplate();
				jt.setPersistenceManagerFactory(pmf);
				jt.setJdoDialect(dialect);
				return jt.execute(new JdoCallback() {
					public Object doInJdo(PersistenceManager pm) {
						return l;
					}
				});
			}
		});
		assertTrue("Correct result list", result == l);

		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));
		assertTrue("Hasn't thread con", !TransactionSynchronizationManager.hasResource(ds));
		pmfControl.verify();
		dsControl.verify();
		dialectControl.verify();
		pmControl.verify();
		txControl.verify();
		conControl.verify();
	}

	public void testExistingTransactionWithPropagationNestedAndRollback() throws SQLException {
		doTestExistingTransactionWithPropagationNestedAndRollback(false);
	}

	public void testExistingTransactionWithManualSavepointAndRollback() throws SQLException {
		doTestExistingTransactionWithPropagationNestedAndRollback(true);
	}

	private void doTestExistingTransactionWithPropagationNestedAndRollback(final boolean manualSavepoint)
			throws SQLException {

		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		final PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		MockControl dialectControl = MockControl.createControl(JdoDialect.class);
		JdoDialect dialect = (JdoDialect) dialectControl.getMock();
		MockControl pmControl = MockControl.createControl(PersistenceManager.class);
		final PersistenceManager pm = (PersistenceManager) pmControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		final Connection con = (Connection) conControl.getMock();
		MockControl mdControl = MockControl.createControl(DatabaseMetaData.class);
		DatabaseMetaData md = (DatabaseMetaData) mdControl.getMock();
		MockControl spControl = MockControl.createControl(Savepoint.class);
		Savepoint sp = (Savepoint) spControl.getMock();

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm, 1);
		pm.currentTransaction();
		pmControl.setReturnValue(tx, manualSavepoint ? 2 : 3);
		pm.close();
		pmControl.setVoidCallable(1);
		md.supportsSavepoints();
		mdControl.setReturnValue(true, 1);
		con.getMetaData();
		conControl.setReturnValue(md, 1);
		con.setSavepoint();
		conControl.setReturnValue(sp, 1);
		con.rollback(sp);
		conControl.setVoidCallable(1);
		final TransactionTemplate tt = new TransactionTemplate();
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
		dialect.beginTransaction(tx, tt);
		dialectControl.setReturnValue(null, 1);
		ConnectionHandle conHandle = new SimpleConnectionHandle(con);
		dialect.getJdbcConnection(pm, false);
		dialectControl.setReturnValue(conHandle, 1);
		dialect.releaseJdbcConnection(conHandle, pm);
		dialectControl.setVoidCallable(1);
		dialect.cleanupTransaction(null);
		dialectControl.setVoidCallable(1);
		if (!manualSavepoint) {
			tx.isActive();
			txControl.setReturnValue(true, 1);
		}
		tx.commit();
		txControl.setVoidCallable(1);
		pmfControl.replay();
		dsControl.replay();
		dialectControl.replay();
		pmControl.replay();
		txControl.replay();
		conControl.replay();
		mdControl.replay();
		spControl.replay();

		JdoTransactionManager tm = new JdoTransactionManager();
		tm.setNestedTransactionAllowed(true);
		tm.setPersistenceManagerFactory(pmf);
		tm.setDataSource(ds);
		tm.setJdoDialect(dialect);
		tt.setTransactionManager(tm);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Has thread pm", TransactionSynchronizationManager.hasResource(pmf));
				assertTrue("Has thread con", TransactionSynchronizationManager.hasResource(ds));
				if (manualSavepoint) {
					Object savepoint = status.createSavepoint();
					status.rollbackToSavepoint(savepoint);
				}
				else {
					tt.execute(new TransactionCallbackWithoutResult() {
						protected void doInTransactionWithoutResult(TransactionStatus status) {
							assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(pmf));
							assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
							status.setRollbackOnly();
						}
					});
				}
				JdoTemplate jt = new JdoTemplate(pmf);
				return jt.execute(new JdoCallback() {
					public Object doInJdo(PersistenceManager pm) {
						return l;
					}
				});
			}
		});
		assertTrue("Correct result list", result == l);

		assertTrue("Hasn't thread pm", !TransactionSynchronizationManager.hasResource(pmf));
		assertTrue("Hasn't thread con", !TransactionSynchronizationManager.hasResource(ds));
		pmfControl.verify();
		dsControl.verify();
		dialectControl.verify();
		pmControl.verify();
		txControl.verify();
		conControl.verify();
		mdControl.verify();
		spControl.verify();
	}

	protected void tearDown() {
		assertTrue(TransactionSynchronizationManager.getResourceMap().isEmpty());
		assertFalse(TransactionSynchronizationManager.isSynchronizationActive());
	}

}
