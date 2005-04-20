package org.springframework.orm.toplink;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import oracle.toplink.exceptions.TopLinkException;
import oracle.toplink.sessions.Session;
import oracle.toplink.sessions.UnitOfWork;

import org.easymock.MockControl;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;
import org.springframework.orm.toplink.sessions.SessionHolder;
import org.springframework.orm.toplink.sessions.SpringSession;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author James Clark
 */
public class TransactionManagerTests extends TestCase {
    
	public void testTransactionCommit() throws SQLException, TopLinkException {
		
		MockControl sessionControl = MockControl.createControl(SpringSession.class);
		final SpringSession session = (SpringSession) sessionControl.getMock();
		MockControl uowControl = MockControl.createControl(UnitOfWork.class);
		UnitOfWork uow = (UnitOfWork)uowControl.getMock();

		final SessionFactory sf = new MockSessionFactory(session);

		// during commit, TM must get the active UOW (using SessionHolder.getTransaction())
		session.getActiveUnitOfWork();
		sessionControl.setReturnValue(uow, 1);
		uow.commit();
		uowControl.setVoidCallable();
		// post commit, TM ensures that the Tx is reset
		session.resetTransaction();
		sessionControl.setVoidCallable(1);
		// session should be released when it was bound explicitly by the TM
		session.release();
		sessionControl.setVoidCallable();

		sessionControl.replay();
		uowControl.replay();

		TopLinkTransactionManager tm = new TopLinkTransactionManager();
		tm.setJdbcExceptionTranslator(new SQLStateSQLExceptionTranslator());
		tm.setSessionFactory(sf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
		tt.setTimeout(10);
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
				TopLinkTemplate template = new TopLinkTemplate(sf);
				return template.execute(new TopLinkCallback() {
                    public Object doInToplink(Session session)
                            throws TopLinkException, SQLException
                    {
                        return null;
                    }
                });
			}
		});
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		sessionControl.verify();
		uowControl.verify();
	}

	public void testTransactionRollback() throws TopLinkException, SQLException
    {
        MockControl sessionControl = MockControl
                .createControl(SpringSession.class);
        final SpringSession session = (SpringSession) sessionControl.getMock();
        MockControl uowControl = MockControl.createControl(UnitOfWork.class);
        UnitOfWork uow = (UnitOfWork) uowControl.getMock();

        final SessionFactory sf = new MockSessionFactory(session);

        session.resetTransaction();
        sessionControl.setVoidCallable(1);
        session.release();
        sessionControl.setVoidCallable(1);

        sessionControl.replay();
        uowControl.replay();

        TopLinkTransactionManager tm = new TopLinkTransactionManager();
        tm.setJdbcExceptionTranslator(new SQLStateSQLExceptionTranslator());
        tm.setSessionFactory(sf);
        TransactionTemplate tt = new TransactionTemplate(tm);
        tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
        tt.setTimeout(10);
        assertTrue("Hasn't thread session", !TransactionSynchronizationManager
                .hasResource(sf));
        assertTrue("JTA synchronizations not active",
                !TransactionSynchronizationManager.isSynchronizationActive());

        try
        {
            Object result = tt.execute(new TransactionCallback() {
                public Object doInTransaction(TransactionStatus status)
                {
                    assertTrue("Has thread session",
                            TransactionSynchronizationManager.hasResource(sf));
                    TopLinkTemplate template = new TopLinkTemplate(sf);
                    return template.execute(new TopLinkCallback() {
                        public Object doInToplink(Session session)
                                throws TopLinkException, SQLException
                        {
                            throw new RuntimeException("failure");
                        }
                    });
                }
            });
            fail();
        }
        catch (Throwable t)
        {
            assertTrue(t.getMessage().equals("failure"));
        }
        assertTrue("Hasn't thread session", !TransactionSynchronizationManager
                .hasResource(sf));
        assertTrue("JTA synchronizations not active",
                !TransactionSynchronizationManager.isSynchronizationActive());
        
        sessionControl.verify();
        uowControl.verify();
    }

	public void testTransactionRollbackOnly() throws TopLinkException,
            SQLException
    {
        MockControl sessionControl = MockControl
                .createControl(SpringSession.class);
        final SpringSession session = (SpringSession) sessionControl.getMock();
        MockControl uowControl = MockControl.createControl(UnitOfWork.class);
        UnitOfWork uow = (UnitOfWork) uowControl.getMock();

        final SessionFactory sf = new MockSessionFactory(session);

        session.resetTransaction();
        sessionControl.setVoidCallable(1);
        session.release();
        sessionControl.setVoidCallable();

        sessionControl.replay();
        uowControl.replay();

        TopLinkTransactionManager tm = new TopLinkTransactionManager();
        tm.setJdbcExceptionTranslator(new SQLStateSQLExceptionTranslator());
        tm.setSessionFactory(sf);
        TransactionTemplate tt = new TransactionTemplate(tm);
        tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
        tt.setTimeout(10);
        assertTrue("Hasn't thread session", !TransactionSynchronizationManager
                .hasResource(sf));
        assertTrue("JTA synchronizations not active",
                !TransactionSynchronizationManager.isSynchronizationActive());

        Object result = tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status)
            {
                assertTrue("Has thread session",
                        TransactionSynchronizationManager.hasResource(sf));
                TopLinkTemplate template = new TopLinkTemplate(sf);
                template.execute(new TopLinkCallback() {
                    public Object doInToplink(Session session)
                            throws TopLinkException, SQLException
                    {
                        return null;
                    }
                });
                status.setRollbackOnly();
                return null;
            }
        });
        assertTrue("Hasn't thread session", !TransactionSynchronizationManager
                .hasResource(sf));
        assertTrue("JTA synchronizations not active",
                !TransactionSynchronizationManager.isSynchronizationActive());
        sessionControl.verify();
        uowControl.verify();
    }

	public void testParticipatingTransactionWithCommit()
            throws TopLinkException, SQLException
    {
        MockControl sessionControl = MockControl
                .createControl(SpringSession.class);
        final SpringSession session = (SpringSession) sessionControl.getMock();
        MockControl uowControl = MockControl.createControl(UnitOfWork.class);
        UnitOfWork uow = (UnitOfWork) uowControl.getMock();

        final SessionFactory sf = new MockSessionFactory(session);

        session.getActiveUnitOfWork();
        sessionControl.setReturnValue(uow, 1);
        uow.commit();
        uowControl.setVoidCallable();
        session.resetTransaction();
        sessionControl.setVoidCallable(1);
        session.release();
        sessionControl.setVoidCallable();

        sessionControl.replay();
        uowControl.replay();

        PlatformTransactionManager tm = new TopLinkTransactionManager(sf);
        final TransactionTemplate tt = new TransactionTemplate(tm);

        Object result = tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status)
            {
                return tt.execute(new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus status)
                    {
                        TopLinkTemplate ht = new TopLinkTemplate(sf);
                        return ht.executeFind(new TopLinkCallback() {
                            public Object doInToplink(Session injectedSession)
                            {
                                assertTrue(session==injectedSession);
                                return null;
                            }
                        });
                    }
                });
            }
        });

        sessionControl.verify();
        uowControl.verify();
    }

	public void testParticipatingTransactionWithRollback()
            throws TopLinkException, SQLException
    {
        MockControl sessionControl = MockControl
                .createControl(SpringSession.class);
        final SpringSession session = (SpringSession) sessionControl.getMock();
        MockControl uowControl = MockControl.createControl(UnitOfWork.class);
        UnitOfWork uow = (UnitOfWork) uowControl.getMock();

        final SessionFactory sf = new MockSessionFactory(session);

        session.resetTransaction();
        sessionControl.setVoidCallable(1);
        session.release();
        sessionControl.setVoidCallable();

        sessionControl.replay();
        uowControl.replay();

        PlatformTransactionManager tm = new TopLinkTransactionManager(sf);
        final TransactionTemplate tt = new TransactionTemplate(tm);
        try
        {
            tt.execute(new TransactionCallback() {
                public Object doInTransaction(TransactionStatus status)
                {
                    return tt.execute(new TransactionCallback() {
                        public Object doInTransaction(TransactionStatus status)
                        {
                            TopLinkTemplate ht = new TopLinkTemplate(sf);
                            return ht.executeFind(new TopLinkCallback() {
                                public Object doInToplink(Session session)
                                {
                                    throw new RuntimeException(
                                            "application exception");
                                }
                            });
                        }
                    });
                }
            });
            fail("Should not thrown RuntimeException");
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex.getMessage().equals("application exception"));
        }
        sessionControl.verify();
        uowControl.verify();
    }

	public void testParticipatingTransactionWithRollbackOnly()
            throws TopLinkException, SQLException
    {
        MockControl sessionControl = MockControl
                .createControl(SpringSession.class);
        final SpringSession session = (SpringSession) sessionControl.getMock();
        MockControl uowControl = MockControl.createControl(UnitOfWork.class);
        UnitOfWork uow = (UnitOfWork) uowControl.getMock();

        final SessionFactory sf = new MockSessionFactory(session);

        session.resetTransaction();
        sessionControl.setVoidCallable(1);
        session.release();
        sessionControl.setVoidCallable();

        sessionControl.replay();
        uowControl.replay();

        PlatformTransactionManager tm = new TopLinkTransactionManager(sf);
        final TransactionTemplate tt = new TransactionTemplate(tm);

        tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status)
            {
                tt.execute(new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus status)
                    {
                        TopLinkTemplate ht = new TopLinkTemplate(sf);
                        ht.execute(new TopLinkCallback() {
                            public Object doInToplink(Session session)
                            {
                                return null;
                            }
                        });
                        status.setRollbackOnly();
                        return null;
                    }
                });
                return null;
            }
        });

        sessionControl.verify();
        uowControl.verify();
    }

	public void testParticipatingTransactionWithWithRequiresNew()
            throws TopLinkException, SQLException
    {
        MockControl session1Control = MockControl
                .createControl(SpringSession.class);
        final SpringSession session1 = (SpringSession) session1Control.getMock();
        MockControl session2Control = MockControl
        	.createControl(SpringSession.class);
        	final SpringSession session2 = (SpringSession) session2Control.getMock();

        MockControl uow1Control = MockControl.createControl(UnitOfWork.class);
        UnitOfWork uow1 = (UnitOfWork) uow1Control.getMock();
        MockControl uow2Control = MockControl.createControl(UnitOfWork.class);
        UnitOfWork uow2 = (UnitOfWork) uow2Control.getMock();

        final MockSessionFactory sf = new MockSessionFactory(session1);

        session2.getActiveUnitOfWork();
        session2Control.setReturnValue(uow2, 1);
        uow2.commit();
        uow2Control.setVoidCallable();
        session2.resetTransaction();
        session2Control.setVoidCallable(1);
        session2.release();
        session2Control.setVoidCallable();

        session1.getActiveUnitOfWork();
        session1Control.setReturnValue(uow1, 1);
        uow1.commit();
        uow1Control.setVoidCallable();
        session1.resetTransaction();
        session1Control.setVoidCallable(1);
        session1.release();
        session1Control.setVoidCallable();

        session1Control.replay();
        uow1Control.replay();
        session2Control.replay();
        uow2Control.replay();
        
        PlatformTransactionManager tm = new TopLinkTransactionManager(sf);
        final TransactionTemplate tt = new TransactionTemplate(tm);
        tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        assertTrue("Hasn't thread session", !TransactionSynchronizationManager
                .hasResource(sf));
        tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status)
            {
                final SessionHolder holder = (SessionHolder) TransactionSynchronizationManager
                        .getResource(sf);
                assertTrue("Has thread session", holder != null);
                sf.setSession(session2);
                tt.execute(new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus status)
                    {
                        TopLinkTemplate ht = new TopLinkTemplate(sf);
                        return ht.execute(new TopLinkCallback() {
                            public Object doInToplink(Session session)
                            {
                                assertTrue("Not enclosing session",
                                        session != holder.getSession());
                                return null;
                            }
                        });
                    }
                });
                assertTrue("Same thread session as before",
                        holder.getSession() == SessionFactoryUtils.getSession(
                                sf, false));
                return null;
            }
        });
        assertTrue("Hasn't thread session", !TransactionSynchronizationManager
                .hasResource(sf));

        session1Control.verify();
        session2Control.verify();
        uow1Control.verify();
        uow2Control.verify();
    }

	public void testParticipatingTransactionWithWithNotSupported()
            throws TopLinkException, SQLException
    {
        MockControl sessionControl = MockControl
                .createControl(SpringSession.class);
        final SpringSession session = (SpringSession) sessionControl.getMock();
        MockControl uowControl = MockControl.createControl(UnitOfWork.class);
        UnitOfWork uow = (UnitOfWork) uowControl.getMock();

        final SessionFactory sf = new MockSessionFactory(session);

        session.getActiveUnitOfWork();
        sessionControl.setReturnValue(uow, 1);
        uow.commit();
        uowControl.setVoidCallable();
        session.resetTransaction();
        sessionControl.setVoidCallable(1);
        session.release();
        sessionControl.setVoidCallable(2);

        sessionControl.replay();
        uowControl.replay();

        TopLinkTransactionManager tm = new TopLinkTransactionManager(sf);
        final TransactionTemplate tt = new TransactionTemplate(tm);
        tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
        tt.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status)
            {
                SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
                assertTrue("Has thread session", holder != null);
                tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
                tt.execute(new TransactionCallback() {
                    public Object doInTransaction(TransactionStatus status)
                    {
                        assertTrue("Hasn't thread session",!TransactionSynchronizationManager.hasResource(sf));
                        TopLinkTemplate ht = new TopLinkTemplate(sf);
                        
                        return ht.execute(new TopLinkCallback() {
                            public Object doInToplink(Session session)
                            {
                                return null;
                            }
                        });
                    }
                });
                assertTrue("Same thread session as before", holder.getSession() == SessionFactoryUtils.getSession(sf, false));
                return null;
            }
        });
        assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));

        sessionControl.verify();
        uowControl.verify();
    }

    public void testTransactionWithPropagationSupports() throws TopLinkException, SQLException {
            MockControl sessionControl = MockControl
                .createControl(SpringSession.class);
        final SpringSession session = (SpringSession) sessionControl.getMock();
        MockControl uowControl = MockControl.createControl(UnitOfWork.class);
        UnitOfWork uow = (UnitOfWork) uowControl.getMock();

        final SessionFactory sf = new MockSessionFactory(session);

        // not a new transaction, won't start a new one
        session.release();
        sessionControl.setVoidCallable();

        sessionControl.replay();
        uowControl.replay();

		PlatformTransactionManager tm = new TopLinkTransactionManager(sf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));

		tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
				assertTrue("Is not new transaction", !status.isNewTransaction());
				TopLinkTemplate ht = new TopLinkTemplate(sf);
				ht.execute(new TopLinkCallback() {
					public Object doInToplink(Session session) {
						return null;
					}
				});
				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
				return null;
			}
		});

		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		uowControl.verify();
		sessionControl.verify();
	}



	public void testTransactionCommitWithReadOnly() throws TopLinkException, SQLException {

	    MockControl sessionControl = MockControl
        .createControl(SpringSession.class);
	    final SpringSession session = (SpringSession) sessionControl.getMock();
        MockControl uowControl = MockControl.createControl(UnitOfWork.class);
        UnitOfWork uow = (UnitOfWork) uowControl.getMock();

        final SessionFactory sf = new MockSessionFactory(session);

        session.resetTransaction();
        sessionControl.setVoidCallable(1);
        session.release();
        sessionControl.setVoidCallable();

        sessionControl.replay();
        uowControl.replay();

		TopLinkTransactionManager tm = new TopLinkTransactionManager(sf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setReadOnly(true);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
				TopLinkTemplate ht = new TopLinkTemplate(sf);
				return ht.executeFind(new TopLinkCallback() {
					public Object doInToplink(Session session) throws TopLinkException {
						return l;
					}
				});
			}
		});
		assertTrue("Correct result list", result == l);

		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		sessionControl.verify();
		uowControl.verify();
	}


	public void testTransactionCommitWithPreBound() throws TopLinkException, SQLException {

	    try
        {
            MockControl sessionControl = MockControl.createControl(SpringSession.class);
            final SpringSession session = (SpringSession) sessionControl.getMock();
            MockControl uowControl = MockControl.createControl(UnitOfWork.class);
            UnitOfWork uow = (UnitOfWork) uowControl.getMock();

            final SessionFactory sf = new MockSessionFactory(session);

            session.getActiveUnitOfWork();
            sessionControl.setReturnValue(uow,1);
            uow.commit();
            uowControl.setVoidCallable();
            session.resetTransaction();
            sessionControl.setVoidCallable(1);
            // session is not released because it was pre-bound

            sessionControl.replay();
            uowControl.replay();

            TopLinkTransactionManager tm = new TopLinkTransactionManager();
            tm.setSessionFactory(sf);
            TransactionTemplate tt = new TransactionTemplate(tm);
            tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
            final List l = new ArrayList();
            l.add("test");
            assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
            TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
            assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));

            Object result = tt.execute(new TransactionCallback() {
            	public Object doInTransaction(TransactionStatus status) {
            		assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
            		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
            		//assertTrue("Has thread transaction", sessionHolder.getTransaction() != null);
            		TopLinkTemplate ht = new TopLinkTemplate(sf);
            		return ht.executeFind(new TopLinkCallback() {
            			public Object doInToplink(Session sess) throws TopLinkException {
            				assertEquals(session, sess);
            				return l;
            			}
            		});
            	}
            });
            assertTrue("Correct result list", result == l);

            assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
            SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
            //assertTrue("Hasn't thread transaction", sessionHolder.getTransaction() == null);
            TransactionSynchronizationManager.unbindResource(sf);
            assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
            sessionControl.verify();
            uowControl.verify();
        }
        catch (TransactionException e)
        {
            e.printStackTrace();
            fail();
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();
            fail();
        }
	}

	public void testTransactionRollbackWithPreBound() throws TopLinkException, SQLException
	{
		MockControl sessionControl = MockControl.createControl(SpringSession.class);
		final SpringSession session = (SpringSession) sessionControl.getMock();
		MockControl uowControl = MockControl.createControl(UnitOfWork.class);
		final UnitOfWork uow1 = (UnitOfWork) uowControl.getMock();
		MockControl uow2Control = MockControl.createControl(UnitOfWork.class);
		final UnitOfWork uow2 = (UnitOfWork) uow2Control.getMock();

        final SessionFactory sf = new MockSessionFactory(session);

        session.getActiveUnitOfWork();
        sessionControl.setReturnValue(uow1,1);
        session.resetTransaction();
        sessionControl.setVoidCallable(1);
        session.getActiveUnitOfWork();
        sessionControl.setReturnValue(uow2,1);
        session.getActiveUnitOfWork();
        sessionControl.setReturnValue(uow2,1);
        uow2.commit();
        uow2Control.setVoidCallable(1);
        session.resetTransaction();
        sessionControl.setVoidCallable(1);
        
        sessionControl.replay();
        uowControl.replay();
        uow2Control.replay();

		TopLinkTransactionManager tm = new TopLinkTransactionManager();
		tm.setSessionFactory(sf);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
		assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));

		tt.execute(new TransactionCallbackWithoutResult() {
			public void doInTransactionWithoutResult(TransactionStatus status) {
				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
				SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);

				assertEquals(uow1, sessionHolder.getTransaction());

				tt.execute(new TransactionCallbackWithoutResult() {
					public void doInTransactionWithoutResult(TransactionStatus status) {
						status.setRollbackOnly();
						TopLinkTemplate ht = new TopLinkTemplate(sf);
	
						ht.execute(new TopLinkCallback() {
							public Object doInToplink(Session sess) throws TopLinkException {
								assertEquals(session, sess);
								return null;
							}
						});
					}
				});
			}
		});

		assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
		assertFalse("Hasn't thread transaction", sessionHolder.hasTransaction());
		assertTrue("Not marked rollback-only", !sessionHolder.isRollbackOnly());

		tt.execute(new TransactionCallbackWithoutResult() {
			public void doInTransactionWithoutResult(TransactionStatus status) {
				
			    assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
				SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
				assertEquals(uow2, sessionHolder.getTransaction());
				
				TopLinkTemplate ht = new TopLinkTemplate(sf);
				ht.execute(new TopLinkCallback() {
					public Object doInToplink(Session sess) throws TopLinkException {
						assertEquals(session, sess);
						return null;
					}
				});
			}
		});

		assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
		assertFalse("Hasn't thread transaction", sessionHolder.hasTransaction());
		TransactionSynchronizationManager.unbindResource(sf);
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		
		sessionControl.verify();
		uowControl.verify();
		uow2Control.verify();
	}
//
//	public void testExistingTransactionWithPropagationNestedAndRollback() throws SQLException, HibernateException {
//		doTestExistingTransactionWithPropagationNestedAndRollback(false);
//	}
//
//	public void testExistingTransactionWithManualSavepointAndRollback() throws SQLException, HibernateException {
//		doTestExistingTransactionWithPropagationNestedAndRollback(true);
//	}
//
//	private void doTestExistingTransactionWithPropagationNestedAndRollback(final boolean manualSavepoint)
//			throws SQLException, HibernateException {
//
//		MockControl dsControl = MockControl.createControl(DataSource.class);
//		final DataSource ds = (DataSource) dsControl.getMock();
//		MockControl conControl = MockControl.createControl(Connection.class);
//		Connection con = (Connection) conControl.getMock();
//		MockControl mdControl = MockControl.createControl(DatabaseMetaData.class);
//		DatabaseMetaData md = (DatabaseMetaData) mdControl.getMock();
//		MockControl spControl = MockControl.createControl(Savepoint.class);
//		Savepoint sp = (Savepoint) spControl.getMock();
//		MockControl sfControl = MockControl.createControl(SessionFactory.class);
//		final SessionFactory sf = (SessionFactory) sfControl.getMock();
//		MockControl sessionControl = MockControl.createControl(Session.class);
//		Session session = (Session) sessionControl.getMock();
//		MockControl txControl = MockControl.createControl(Transaction.class);
//		Transaction tx = (Transaction) txControl.getMock();
//		MockControl queryControl = MockControl.createControl(Query.class);
//		Query query = (Query) queryControl.getMock();
//
//		final List list = new ArrayList();
//		list.add("test");
//		con.isReadOnly();
//		conControl.setReturnValue(false, 1);
//		sf.openSession();
//		sfControl.setReturnValue(session, 1);
//		session.beginTransaction();
//		sessionControl.setReturnValue(tx, 1);
//		session.connection();
//		sessionControl.setReturnValue(con, 2);
//		md.supportsSavepoints();
//		mdControl.setReturnValue(true, 1);
//		con.getMetaData();
//		conControl.setReturnValue(md, 1);
//		con.setSavepoint();
//		conControl.setReturnValue(sp, 1);
//		con.rollback(sp);
//		conControl.setVoidCallable(1);
//		session.createQuery("some query string");
//		sessionControl.setReturnValue(query, 1);
//		query.list();
//		queryControl.setReturnValue(list, 1);
//		session.close();
//		sessionControl.setReturnValue(null, 1);
//		tx.commit();
//		txControl.setVoidCallable(1);
//		dsControl.replay();
//		conControl.replay();
//		mdControl.replay();
//		spControl.replay();
//		sfControl.replay();
//		sessionControl.replay();
//		txControl.replay();
//		queryControl.replay();
//
//		HibernateTransactionManager tm = new HibernateTransactionManager();
//		tm.setJdbcExceptionTranslator(new SQLStateSQLExceptionTranslator());
//		tm.setNestedTransactionAllowed(true);
//		tm.setSessionFactory(sf);
//		tm.setDataSource(ds);
//		final TransactionTemplate tt = new TransactionTemplate(tm);
//		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
//		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//
//		Object result = tt.execute(new TransactionCallback() {
//			public Object doInTransaction(TransactionStatus status) {
//				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//				assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
//				if (manualSavepoint) {
//					Object savepoint = status.createSavepoint();
//					status.rollbackToSavepoint(savepoint);
//				}
//				else {
//					tt.execute(new TransactionCallbackWithoutResult() {
//						protected void doInTransactionWithoutResult(TransactionStatus status) {
//							assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//							assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
//							status.setRollbackOnly();
//						}
//					});
//				}
//				HibernateTemplate ht = new HibernateTemplate(sf);
//				return ht.find("some query string");
//			}
//		});
//		assertTrue("Correct result list", result == list);
//
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
//		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//		dsControl.verify();
//		conControl.verify();
//		mdControl.verify();
//		spControl.verify();
//		sfControl.verify();
//		sessionControl.verify();
//		txControl.verify();
//		queryControl.verify();
//	}
//
//
//	public void testTransactionCommitWithNonExistingDatabaseAndLazyConnection() throws HibernateException, IOException {
//		DriverManagerDataSource dsTarget = new DriverManagerDataSource();
//		final LazyConnectionDataSourceProxy ds = new LazyConnectionDataSourceProxy();
//		ds.setTargetDataSource(dsTarget);
//		ds.setDefaultAutoCommit(true);
//		ds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
//		
//		LocalSessionFactoryBean lsfb = new LocalSessionFactoryBean();
//		lsfb.setDataSource(ds);
//		lsfb.afterPropertiesSet();
//		final SessionFactory sf = (SessionFactory) lsfb.getObject();
//
//		HibernateTransactionManager tm = new HibernateTransactionManager();
//		tm.setSessionFactory(sf);
//		tm.afterPropertiesSet();
//		TransactionTemplate tt = new TransactionTemplate(tm);
//		tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
//		tt.setTimeout(10);
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
//		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//
//		tt.execute(new TransactionCallback() {
//			public Object doInTransaction(TransactionStatus status) {
//				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
//				assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
//				HibernateTemplate ht = new HibernateTemplate(sf);
//				return ht.find("from java.lang.Object");
//			}
//		});
//
//		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
//		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
//		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
//	}
	
	protected void tearDown() {
		assertTrue(TransactionSynchronizationManager.getResourceMap().isEmpty());
		assertFalse(TransactionSynchronizationManager.isSynchronizationActive());
	}

	private class MockSessionFactory extends SessionFactory
	{
	    private Session session;
	    public MockSessionFactory(Session session)
	    {
	        super(null);
	        this.session = session;
	    }
	    /* (non-Javadoc)
         * @see org.springframework.orm.toplink.SessionFactory#createSession()
         */
        public Session createSession()
        {
            return this.session;
        }
        
        public void setSession(Session session)
        {
            this.session = session;
        }
	}
    protected void setUp() throws Exception
    {
        super.setUp();
        //TransactionSynchronizationManager.getResourceMap().clear();
    }
}
