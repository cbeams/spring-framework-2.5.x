/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.orm.jpa;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import javax.sql.DataSource;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Costin Leau
 * @author Juergen Hoeller
 */
public class JpaTransactionManagerTests extends TestCase {

	private MockControl factoryControl, managerControl, txControl;

	private EntityManager manager;

	private EntityTransaction tx;

	private EntityManagerFactory factory;

	private JpaTransactionManager transactionManager;

	private JpaTemplate template;

	private TransactionTemplate tt;

	protected void setUp() throws Exception {
		factoryControl = MockControl.createControl(EntityManagerFactory.class);
		factory = (EntityManagerFactory) factoryControl.getMock();
		managerControl = MockControl.createControl(EntityManager.class);
		manager = (EntityManager) managerControl.getMock();
		txControl = MockControl.createControl(EntityTransaction.class);
		tx = (EntityTransaction) txControl.getMock();

		transactionManager = new JpaTransactionManager(factory);
		template = new JpaTemplate(factory);
		template.afterPropertiesSet();
		tt = new TransactionTemplate(transactionManager);

		factoryControl.expectAndReturn(factory.createEntityManager(), manager);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		tx.begin();
		manager.close();
	}

	protected void tearDown() throws Exception {
		assertTrue(TransactionSynchronizationManager.getResourceMap().isEmpty());
		assertFalse(TransactionSynchronizationManager.isSynchronizationActive());
		assertFalse(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
		assertFalse(TransactionSynchronizationManager.isActualTransactionActive());

		factoryControl = null;
		managerControl = null;
		txControl = null;
		manager = null;
		factory = null;
		transactionManager = null;
		template = null;
		tt = null;
		tx = null;
	}

	public void testTransactionCommit() {
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		txControl.expectAndReturn(tx.getRollbackOnly(), false);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		tx.commit();
		manager.flush();

		factoryControl.replay();
		managerControl.replay();
		txControl.replay();

		final List<String> l = new ArrayList<String>();
		l.add("test");

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.hasResource(factory));
				return template.execute(new JpaCallback() {
					public Object doInJpa(EntityManager em) {
						em.flush();
						return l;
					}
				});
			}
		});
		assertSame(l, result);

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
	}

	public void testTransactionCommitWithRollbackException() {
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		txControl.expectAndReturn(tx.getRollbackOnly(), true);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		tx.commit();
		txControl.setThrowable(new RollbackException());
		manager.flush();

		factoryControl.replay();
		managerControl.replay();
		txControl.replay();

		final List<String> l = new ArrayList<String>();
		l.add("test");

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		try {
			Object result = tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue(TransactionSynchronizationManager.hasResource(factory));
					return template.execute(new JpaCallback() {
						public Object doInJpa(EntityManager em) {
							em.flush();
							return l;
						}
					});
				}
			});
			assertSame(l, result);
		}
		catch (TransactionSystemException tse) {
			// it's okay
			assertTrue(tse.getCause() instanceof RollbackException);
		}

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
	}

	public void testTransactionRollback() {
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		txControl.expectAndReturn(tx.isActive(), true);
		tx.rollback();

		factoryControl.replay();
		managerControl.replay();
		txControl.replay();

		final List<String> l = new ArrayList<String>();
		l.add("test");

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		try {
			tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue(TransactionSynchronizationManager.hasResource(factory));
					return template.execute(new JpaCallback() {
						public Object doInJpa(EntityManager em) {
							throw new RuntimeException("some exception");
						}
					});
				}
			});
			fail("expected exception");
		}
		catch (RuntimeException e) {
			// okay
		}

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
	}

	public void testTransactionRollbackWithAlreadyRolledBack() {
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		txControl.expectAndReturn(tx.isActive(), false);
		// tx.rollback();

		factoryControl.replay();
		managerControl.replay();
		txControl.replay();

		final List<String> l = new ArrayList<String>();
		l.add("test");

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		try {
			tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue(TransactionSynchronizationManager.hasResource(factory));
					return template.execute(new JpaCallback() {
						public Object doInJpa(EntityManager em) {
							throw new RuntimeException("some exception");
						}
					});
				}
			});
			fail("expected exception");
		}
		catch (RuntimeException e) {
			// okay
		}

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
	}

	public void testTransactionRollbackOnly() {
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		txControl.expectAndReturn(tx.isActive(), true);
		manager.flush();
		tx.rollback();

		factoryControl.replay();
		managerControl.replay();
		txControl.replay();

		final List<String> l = new ArrayList<String>();
		l.add("test");

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.hasResource(factory));

				Object res = template.execute(new JpaCallback() {
					public Object doInJpa(EntityManager em) {
						em.flush();
						return l;
					}
				});
				status.setRollbackOnly();

				return res;
			}
		});

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
	}

	public void testParticipatingTransactionWithCommit() {
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		manager.flush();

		factoryControl.replay();
		managerControl.replay();
		txControl.replay();

		final List<String> l = new ArrayList<String>();
		l.add("test");

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				txControl.reset();
				txControl.expectAndReturn(tx.isActive(), true);
				txControl.expectAndReturn(tx.getRollbackOnly(), false);
				tx.commit();
				txControl.replay();

				assertTrue(TransactionSynchronizationManager.hasResource(factory));

				return tt.execute(new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {

						return template.execute(new JpaCallback() {
							public Object doInJpa(EntityManager em) {
								em.flush();
								return l;
							}
						});
					}
				});
			}
		});

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
	}

	public void testParticipatingTransactionWithRollback() {
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		factoryControl.replay();
		managerControl.replay();
		txControl.replay();

		final List<String> l = new ArrayList<String>();
		l.add("test");

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		try {
			tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					txControl.reset();
					txControl.expectAndReturn(tx.isActive(), true);
					txControl.expectAndReturn(tx.isActive(), true);
					txControl.expectAndReturn(tx.isActive(), true);
					tx.setRollbackOnly();
					tx.rollback();
					txControl.replay();

					assertTrue(TransactionSynchronizationManager.hasResource(factory));
					return tt.execute(new TransactionCallback() {
						public Object doInTransaction(TransactionStatus status) {
							return template.execute(new JpaCallback() {
								public Object doInJpa(EntityManager em) {
									throw new RuntimeException("exception");
								}
							});
						}
					});
				}
			});
			fail("expected exception");
		}
		catch (RuntimeException e) {
			// okay
		}

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
	}

	public void testParticipatingTransactionWithRollbackOnly() {
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		manager.flush();

		factoryControl.replay();
		managerControl.replay();
		txControl.replay();

		final List<String> l = new ArrayList<String>();
		l.add("test");

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		try {
			tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					txControl.reset();
					txControl.expectAndReturn(tx.isActive(), true);
					txControl.expectAndReturn(tx.isActive(), true);
					tx.setRollbackOnly();
					txControl.expectAndReturn(tx.getRollbackOnly(), true);
					tx.commit();
					txControl.setThrowable(new RollbackException());
					txControl.replay();

					assertTrue(TransactionSynchronizationManager.hasResource(factory));

					return tt.execute(new TransactionCallback() {
						public Object doInTransaction(TransactionStatus status) {

							template.execute(new JpaCallback() {
								public Object doInJpa(EntityManager em2) {
									em2.flush();
									return l;
								}
							});

							status.setRollbackOnly();
							return null;
						}
					});
				}
			});
			fail("expected exception");
		}
		catch (TransactionSystemException tse) {
			// it's okay
			assertTrue(tse.getCause() instanceof RollbackException);
		}

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
	}

	public void testParticipatingTransactionWithWithRequiresNew() {
		factoryControl.expectAndReturn(factory.createEntityManager(), manager);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);

		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		manager.flush();
		manager.close();
		factoryControl.replay();
		managerControl.replay();
		txControl.replay();

		final List<String> l = new ArrayList<String>();
		l.add("test");

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				txControl.verify();
				txControl.reset();
				txControl.expectAndReturn(tx.isActive(), true);

				tx.begin();
				txControl.expectAndReturn(tx.getRollbackOnly(), false);
				tx.commit();
				txControl.expectAndReturn(tx.getRollbackOnly(), false);
				tx.commit();

				txControl.replay();

				assertTrue(TransactionSynchronizationManager.hasResource(factory));
				return tt.execute(new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						return template.execute(new JpaCallback() {
							public Object doInJpa(EntityManager em2) {
								em2.flush();
								return l;
							}
						});
					}
				});
			}
		});
		assertSame(l, result);

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
	}

	public void testParticipatingTransactionWithWithRequiresNewAndPrebound() {
		factoryControl.expectAndReturn(factory.createEntityManager(), manager);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);

		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		txControl.expectAndReturn(tx.isActive(), true);
		manager.flush();
		manager.close();
		factoryControl.replay();
		managerControl.replay();
		txControl.replay();

		final List<String> l = new ArrayList<String>();
		l.add("test");

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		TransactionSynchronizationManager.bindResource(factory, new EntityManagerHolder(manager));

		try {
			Object result = tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					txControl.verify();
					txControl.reset();
					txControl.expectAndReturn(tx.isActive(), true);

					tx.begin();
					txControl.expectAndReturn(tx.getRollbackOnly(), false);
					tx.commit();
					txControl.expectAndReturn(tx.getRollbackOnly(), false);
					tx.commit();

					txControl.replay();

					JpaTemplate template2 = new JpaTemplate(factory);
					template2.execute(new JpaCallback() {

						public Object doInJpa(EntityManager em) throws PersistenceException {
							return null;
						}
					});

					assertTrue(TransactionSynchronizationManager.hasResource(factory));
					return tt.execute(new TransactionCallback() {
						public Object doInTransaction(TransactionStatus status) {
							return template.execute(new JpaCallback() {
								public Object doInJpa(EntityManager em2) {
									em2.flush();
									return l;
								}
							});
						}
					});
				}
			});
			assertSame(l, result);
		}
		finally {
			TransactionSynchronizationManager.unbindResource(factory);
		}

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
	}

	public void testTransactionCommitWithPropagationSupports() {
		managerControl.reset();
		txControl.reset();

		manager.flush();
		manager.close();

		factoryControl.replay();
		managerControl.replay();
		txControl.replay();

		final List<String> l = new ArrayList<String>();
		l.add("test");

		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue(!TransactionSynchronizationManager.hasResource(factory));
				assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
				assertTrue(!status.isNewTransaction());
				return template.execute(new JpaCallback() {
					public Object doInJpa(EntityManager em) {
						em.flush();
						return l;
					}
				});
			}
		});
		assertSame(l, result);

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
	}

	public void testTransactionRollbackWithPropagationSupports() {
		managerControl.reset();
		txControl.reset();

		manager.flush();
		manager.close();

		factoryControl.replay();
		managerControl.replay();
		txControl.replay();

		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue(!TransactionSynchronizationManager.hasResource(factory));
				assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
				assertTrue(!status.isNewTransaction());
				template.execute(new JpaCallback() {
					public Object doInJpa(EntityManager em) {
						em.flush();
						return null;
					}
				});
				status.setRollbackOnly();
				return null;
			}
		});

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
	}

	public void testTransactionCommitWithPrebound() {
		factoryControl.reset();
		managerControl.reset();
		txControl.reset();

		managerControl.expectAndReturn(manager.getTransaction(), tx, 4);
		txControl.expectAndReturn(tx.isActive(), false);
		tx.begin();
		txControl.expectAndReturn(tx.getRollbackOnly(), false);
		tx.commit();

		factoryControl.replay();
		managerControl.replay();
		txControl.replay();

		final List<String> l = new ArrayList<String>();
		l.add("test");

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		TransactionSynchronizationManager.bindResource(factory, new EntityManagerHolder(manager));

		try {
			Object result = tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue(TransactionSynchronizationManager.hasResource(factory));
					assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
					return template.execute(new JpaCallback() {
						public Object doInJpa(EntityManager em) {
							return l;
						}
					});
				}
			});
			assertSame(l, result);

			assertTrue(TransactionSynchronizationManager.hasResource(factory));
			assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		}
		finally {
			TransactionSynchronizationManager.unbindResource(factory);
		}

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
	}

	public void testTransactionRollbackWithPrebound() {
		factoryControl.reset();
		managerControl.reset();
		txControl.reset();

		managerControl.expectAndReturn(manager.getTransaction(), tx, 3);
		txControl.expectAndReturn(tx.isActive(), false);
		tx.begin();
		txControl.expectAndReturn(tx.isActive(), true);
		tx.rollback();
		manager.clear();

		factoryControl.replay();
		managerControl.replay();
		txControl.replay();

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		TransactionSynchronizationManager.bindResource(factory, new EntityManagerHolder(manager));

		try {
			tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue(TransactionSynchronizationManager.hasResource(factory));
					assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
					template.execute(new JpaCallback() {
						public Object doInJpa(EntityManager em) {
							return null;
						}
					});
					status.setRollbackOnly();
					return null;
				}
			});

			assertTrue(TransactionSynchronizationManager.hasResource(factory));
			assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		}
		finally {
			TransactionSynchronizationManager.unbindResource(factory);
		}

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
	}


	public void testTransactionCommitWithPreboundAndPropagationSupports() {
		factoryControl.reset();
		managerControl.reset();
		txControl.reset();

		managerControl.expectAndReturn(manager.getTransaction(), tx, 1);
		txControl.expectAndReturn(tx.isActive(), false);
		manager.joinTransaction();
		manager.flush();

		factoryControl.replay();
		managerControl.replay();
		txControl.replay();

		final List<String> l = new ArrayList<String>();
		l.add("test");

		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		TransactionSynchronizationManager.bindResource(factory, new EntityManagerHolder(manager));

		try {
			Object result = tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue(TransactionSynchronizationManager.hasResource(factory));
					assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
					assertTrue(!status.isNewTransaction());
					return template.execute(new JpaCallback() {
						public Object doInJpa(EntityManager em) {
							em.flush();
							return l;
						}
					});
				}
			});
			assertSame(l, result);

			assertTrue(TransactionSynchronizationManager.hasResource(factory));
			assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		}
		finally {
			TransactionSynchronizationManager.unbindResource(factory);
		}

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
	}

	public void testTransactionRollbackWithPreboundAndPropagationSupports() {
		factoryControl.reset();
		managerControl.reset();
		txControl.reset();

		managerControl.expectAndReturn(manager.getTransaction(), tx, 1);
		txControl.expectAndReturn(tx.isActive(), false);
		manager.joinTransaction();
		manager.flush();
		manager.clear();

		factoryControl.replay();
		managerControl.replay();
		txControl.replay();

		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		TransactionSynchronizationManager.bindResource(factory, new EntityManagerHolder(manager));

		try {
			tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue(TransactionSynchronizationManager.hasResource(factory));
					assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
					assertTrue(!status.isNewTransaction());
					template.execute(new JpaCallback() {
						public Object doInJpa(EntityManager em) {
							em.flush();
							return null;
						}
					});
					status.setRollbackOnly();
					return null;
				}
			});

			assertTrue(TransactionSynchronizationManager.hasResource(factory));
			assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());
		}
		finally {
			TransactionSynchronizationManager.unbindResource(factory);
		}

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
	}

	public void testTransactionCommitWithDataSource() throws SQLException {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();
		transactionManager.setDataSource(ds);
		
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		managerControl.expectAndReturn(manager.getTransaction(), tx);
		txControl.expectAndReturn(tx.getRollbackOnly(), false);
		tx.commit();
		manager.flush();
		
		factoryControl.replay();
		managerControl.replay();
		txControl.replay();
		dsControl.replay();

		final List<String> l = new ArrayList<String>();
		l.add("test");

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue(TransactionSynchronizationManager.hasResource(factory));
				assertTrue(TransactionSynchronizationManager.isSynchronizationActive());
				return template.execute(new JpaCallback() {
					public Object doInJpa(EntityManager em) {
						em.flush();
						return l;
					}
				});
			}
		});
		
		assertTrue(result == l);

		assertTrue(!TransactionSynchronizationManager.hasResource(factory));
		assertTrue(!TransactionSynchronizationManager.isSynchronizationActive());

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
		dsControl.verify();
	}

	public void testInvalidIsolation() {
		tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
		txControl.reset();
		managerControl.reset();

		manager.close();

		factoryControl.replay();
		managerControl.replay();
		txControl.replay();

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

		factoryControl.verify();
		managerControl.verify();
		txControl.verify();
	}

}
