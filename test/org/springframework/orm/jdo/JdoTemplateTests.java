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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.jdo.JDODataStoreException;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.JDOOptimisticVerificationException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author Juergen Hoeller
 * @since 03.06.2003
 */
public class JdoTemplateTests extends TestCase {

	private MockControl pmfControl;
	private PersistenceManagerFactory pmf;
	private MockControl pmControl;
	private PersistenceManager pm;

	protected void setUp() {
		pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		pmf = (PersistenceManagerFactory) pmfControl.getMock();
		pmControl = MockControl.createControl(PersistenceManager.class);
		pm = (PersistenceManager) pmControl.getMock();
	}

	protected void tearDown() {
		try {
			pmfControl.verify();
			pmControl.verify();
		}
		catch (IllegalStateException ex) {
			// ignore: test method didn't call replay
		}
	}

	public void testTemplateExecuteWithNotAllowCreate() {
		JdoTemplate jt = new JdoTemplate();
		jt.setAllowCreate(false);
		try {
			jt.execute(new JdoCallback() {
				public Object doInJdo(PersistenceManager pm) {
					return null;
				}
			});
			fail("Should have thrown IllegalStateException");
		}
		catch (IllegalStateException ex) {
			// expected
		}
	}

	public void testTemplateExecuteWithNotAllowCreateAndThreadBound() {
		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmfControl.replay();
		pmControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		jt.setAllowCreate(false);
		TransactionSynchronizationManager.bindResource(pmf, new PersistenceManagerHolder(pm));
		final List l = new ArrayList();
		l.add("test");
		List result = (List) jt.execute(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) {
				return l;
			}
		});
		assertTrue("Correct result list", result == l);
		TransactionSynchronizationManager.unbindResource(pmf);
	}

	public void testTemplateExecuteWithNewPersistenceManager() {
		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm, 1);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		final List l = new ArrayList();
		l.add("test");
		List result = (List) jt.execute(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) {
				return l;
			}
		});
		assertTrue("Correct result list", result == l);
	}

	public void testTemplateExecuteWithThreadBoundAndFlushEager() {
		MockControl dialectControl = MockControl.createControl(JdoDialect.class);
		JdoDialect dialect = (JdoDialect) dialectControl.getMock();

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		dialect.flush(pm);
		dialectControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		dialectControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		jt.setJdoDialect(dialect);
		jt.setFlushEager(true);
		jt.setAllowCreate(false);
		TransactionSynchronizationManager.bindResource(pmf, new PersistenceManagerHolder(pm));
		final List l = new ArrayList();
		l.add("test");
		List result = (List) jt.execute(new JdoCallback() {
			public Object doInJdo(PersistenceManager pm) {
				return l;
			}
		});
		assertTrue("Correct result list", result == l);
		TransactionSynchronizationManager.unbindResource(pmf);
		dialectControl.verify();
	}

	public void testGetObjectById() {
		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		pm.getObjectById("0", true);
		pmControl.setReturnValue("A");
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		assertEquals("A", jt.getObjectById("0"));
	}

	public void testGetObjectByIdWithClassAndValue() {
		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		pm.newObjectIdInstance(String.class, "0");
		pmControl.setReturnValue("OID");
		pm.getObjectById("OID", true);
		pmControl.setReturnValue("A");
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		assertEquals("A", jt.getObjectById(String.class, "0"));
	}

	public void testEvict() {
		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		pm.evict("0");
		pmControl.setVoidCallable(1);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		jt.evict("0");
	}

	public void testEvictAll() {
		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		pm.evictAll();
		pmControl.setVoidCallable(1);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		jt.evictAll();
	}

	public void testRefresh() {
		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		pm.refresh("0");
		pmControl.setVoidCallable(1);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		jt.refresh("0");
	}

	public void testRefreshAll() {
		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		pm.refreshAll();
		pmControl.setVoidCallable(1);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		jt.refreshAll();
	}

	public void testMakePersistent() {
		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		pm.makePersistent("0");
		pmControl.setVoidCallable(1);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		jt.makePersistent("0");
	}

	public void testDeletePersistent() {
		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		pm.deletePersistent("0");
		pmControl.setVoidCallable(1);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		jt.deletePersistent("0");
	}

	public void testDeletePersistentAll() {
		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		Collection coll = new HashSet();
		pm.deletePersistentAll(coll);
		pmControl.setVoidCallable(1);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		jt.deletePersistentAll(coll);
	}

	public void testFlush() {
		MockControl dialectControl = MockControl.createControl(JdoDialect.class);
		JdoDialect dialect = (JdoDialect) dialectControl.getMock();

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		dialect.flush(pm);
		dialectControl.setVoidCallable(1);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		dialectControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		jt.setJdoDialect(dialect);
		jt.flush();
		dialectControl.verify();
	}

	public void testFind() {
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		pm.newQuery(String.class);
		pmControl.setReturnValue(query);
		Collection coll = new HashSet();
		query.execute();
		queryControl.setReturnValue(coll);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		queryControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		assertEquals(coll, jt.find(String.class));
		queryControl.verify();
	}

	public void testFindWithFilter() {
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		pm.newQuery(String.class, "a == b");
		pmControl.setReturnValue(query);
		Collection coll = new HashSet();
		query.execute();
		queryControl.setReturnValue(coll);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		queryControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		assertEquals(coll, jt.find(String.class, "a == b"));
		queryControl.verify();
	}

	public void testFindWithFilterAndOrdering() {
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		pm.newQuery(String.class, "a == b");
		pmControl.setReturnValue(query);
		query.setOrdering("c asc");
		queryControl.setVoidCallable(1);
		Collection coll = new HashSet();
		query.execute();
		queryControl.setReturnValue(coll);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		queryControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		assertEquals(coll, jt.find(String.class, "a == b", "c asc"));
		queryControl.verify();
	}

	public void testFindWithParameterArray() {
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		pm.newQuery(String.class, "a == b");
		pmControl.setReturnValue(query);
		query.declareParameters("params");
		queryControl.setVoidCallable(1);
		Object[] values = new Object[0];
		Collection coll = new HashSet();
		query.executeWithArray(values);
		queryControl.setReturnValue(coll);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		queryControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		assertEquals(coll, jt.find(String.class, "a == b", "params", values));
		queryControl.verify();
	}

	public void testFindWithParameterArrayAndOrdering() {
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		pm.newQuery(String.class, "a == b");
		pmControl.setReturnValue(query);
		query.declareParameters("params");
		queryControl.setVoidCallable(1);
		query.setOrdering("c asc");
		queryControl.setVoidCallable(1);
		Object[] values = new Object[0];
		Collection coll = new HashSet();
		query.executeWithArray(values);
		queryControl.setReturnValue(coll);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		queryControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		assertEquals(coll, jt.find(String.class, "a == b", "params", values, "c asc"));
		queryControl.verify();
	}

	public void testFindWithParameterMap() {
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		pm.newQuery(String.class, "a == b");
		pmControl.setReturnValue(query);
		query.declareParameters("params");
		queryControl.setVoidCallable(1);
		Map values = new HashMap();
		Collection coll = new HashSet();
		query.executeWithMap(values);
		queryControl.setReturnValue(coll);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		queryControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		assertEquals(coll, jt.find(String.class, "a == b", "params", values));
		queryControl.verify();
	}

	public void testFindWithParameterMapAndOrdering() {
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();

		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm);
		pm.newQuery(String.class, "a == b");
		pmControl.setReturnValue(query);
		query.declareParameters("params");
		queryControl.setVoidCallable(1);
		query.setOrdering("c asc");
		queryControl.setVoidCallable(1);
		Map values = new HashMap();
		Collection coll = new HashSet();
		query.executeWithMap(values);
		queryControl.setReturnValue(coll);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		queryControl.replay();

		JdoTemplate jt = new JdoTemplate(pmf);
		assertEquals(coll, jt.find(String.class, "a == b", "params", values, "c asc"));
		queryControl.verify();
	}

	public void testTemplateExceptions() {
		try {
			createTemplate().execute(new JdoCallback() {
				public Object doInJdo(PersistenceManager pm) {
					throw new JDOObjectNotFoundException();
				}
			});
			fail("Should have thrown JdoObjectRetrievalFailureException");
		}
		catch (JdoObjectRetrievalFailureException ex) {
			// expected
		}

		try {
			createTemplate().execute(new JdoCallback() {
				public Object doInJdo(PersistenceManager pm) {
					throw new JDOOptimisticVerificationException();
				}
			});
			fail("Should have thrown JdoOptimisticLockingFailureException");
		}
		catch (JdoOptimisticLockingFailureException ex) {
			// expected
		}

		try {
			createTemplate().execute(new JdoCallback() {
				public Object doInJdo(PersistenceManager pm) {
					throw new JDODataStoreException();
				}
			});
			fail("Should have thrown JdoResourceFailureException");
		}
		catch (JdoResourceFailureException ex) {
			// expected
		}

		try {
			createTemplate().execute(new JdoCallback() {
				public Object doInJdo(PersistenceManager pm) {
					throw new JDOFatalDataStoreException();
				}
			});
			fail("Should have thrown JdoResourceFailureException");
		}
		catch (JdoResourceFailureException ex) {
			// expected
		}

		try {
			createTemplate().execute(new JdoCallback() {
				public Object doInJdo(PersistenceManager pm) {
					throw new JDOUserException();
				}
			});
			fail("Should have thrown JdoUsageException");
		}
		catch (JdoUsageException ex) {
			// expected
		}

		try {
			createTemplate().execute(new JdoCallback() {
				public Object doInJdo(PersistenceManager pm) {
					throw new JDOFatalUserException();
				}
			});
			fail("Should have thrown JdoUsageException");
		}
		catch (JdoUsageException ex) {
			// expected
		}

		try {
			createTemplate().execute(new JdoCallback() {
				public Object doInJdo(PersistenceManager pm) {
					throw new JDOException();
				}
			});
			fail("Should have thrown JdoSystemException");
		}
		catch (JdoSystemException ex) {
			// expected
		}
	}

	public void testTranslateException() {
		MockControl dialectControl = MockControl.createControl(JdoDialect.class);
		JdoDialect dialect = (JdoDialect) dialectControl.getMock();
		final JDOException ex = new JDOException();
		dialect.translateException(ex);
		dialectControl.setReturnValue(new DataIntegrityViolationException("test", ex));
		dialectControl.replay();
		try {
			JdoTemplate template = createTemplate();
			template.setJdoDialect(dialect);
			template.execute(new JdoCallback() {
				public Object doInJdo(PersistenceManager pm) {
					throw ex;
				}
			});
			fail("Should have thrown DataIntegrityViolationException");
		}
		catch (DataIntegrityViolationException dive) {
			// expected
		}
		dialectControl.verify();
	}

	private JdoTemplate createTemplate() {
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		pmfControl.reset();
		pmControl.reset();
		pmf.getConnectionFactory();
		pmfControl.setReturnValue(null, 1);
		pmf.getPersistenceManager();
		pmfControl.setReturnValue(pm, 1);
		pm.close();
		pmControl.setVoidCallable(1);
		pmfControl.replay();
		pmControl.replay();
		txControl.replay();
		return new JdoTemplate(pmf);
	}

}
