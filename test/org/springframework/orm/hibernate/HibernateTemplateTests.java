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

package org.springframework.orm.hibernate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.sf.hibernate.FlushMode;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.JDBCException;
import net.sf.hibernate.LockMode;
import net.sf.hibernate.ObjectDeletedException;
import net.sf.hibernate.ObjectNotFoundException;
import net.sf.hibernate.PersistentObjectException;
import net.sf.hibernate.Query;
import net.sf.hibernate.QueryException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.StaleObjectStateException;
import net.sf.hibernate.TransientObjectException;
import net.sf.hibernate.WrongClassException;
import net.sf.hibernate.type.Type;
import org.easymock.MockControl;

import org.springframework.beans.TestBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author Juergen Hoeller
 * @since 06.05.2003
 */
public class HibernateTemplateTests extends TestCase {

	public void testExecuteWithNewSession() {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		try {
			sf.openSession();
			sfControl.setReturnValue(session, 1);
			session.flush();
			sessionControl.setVoidCallable(1);
			session.close();
			sessionControl.setReturnValue(null, 1);
		}
		catch (HibernateException ex) {
		}
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		assertTrue("Correct allowCreate default", ht.isAllowCreate());
		assertTrue("Correct flushMode default", ht.getFlushMode() == HibernateTemplate.FLUSH_AUTO);
		final List l = new ArrayList();
		l.add("test");
		List result = ht.executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return l;
			}
		});
		assertTrue("Correct result list", result == l);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testExecuteWithNewSessionAndFlushNever() {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		try {
			sf.openSession();
			sfControl.setReturnValue(session, 1);
			session.setFlushMode(FlushMode.NEVER);
			sessionControl.setVoidCallable(1);
			session.close();
			sessionControl.setReturnValue(null, 1);
		}
		catch (HibernateException ex) {
		}
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		ht.setFlushMode(HibernateTemplate.FLUSH_NEVER);
		final List l = new ArrayList();
		l.add("test");
		List result = ht.executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return l;
			}
		});
		assertTrue("Correct result list", result == l);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testExecuteWithNotAllowCreate() {
		HibernateTemplate ht = new HibernateTemplate();
		ht.setAllowCreate(false);
		try {
			ht.execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException{
					return null;
				}
			});
			fail("Should have thrown IllegalStateException");
		}
		catch (IllegalStateException ex) {
			// expected
		}
	}

	public void testExecuteWithNotAllowCreateAndThreadBound() {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		ht.setAllowCreate(false);
		TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
		final List l = new ArrayList();
		l.add("test");
		List result = ht.executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return l;
			}
		});
		assertTrue("Correct result list", result == l);
		TransactionSynchronizationManager.unbindResource(sf);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testExecuteWithThreadBoundAndFlushEager() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		session.flush();
		sessionControl.setVoidCallable(1);
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		ht.setFlushModeName("FLUSH_EAGER");
		ht.setAllowCreate(false);
		TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
		final List l = new ArrayList();
		l.add("test");
		List result = ht.executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return l;
			}
		});
		assertTrue("Correct result list", result == l);
		TransactionSynchronizationManager.unbindResource(sf);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testExecuteWithEntityInterceptor() throws HibernateException {
		MockControl interceptorControl = MockControl.createControl(net.sf.hibernate.Interceptor.class);
		Interceptor entityInterceptor = (Interceptor) interceptorControl.getMock();
		interceptorControl.replay();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		sf.openSession(entityInterceptor);
		sfControl.setReturnValue(session, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		ht.setEntityInterceptor(entityInterceptor);
		final List l = new ArrayList();
		l.add("test");
		List result = ht.executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return l;
			}
		});
		assertTrue("Correct result list", result == l);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testGet() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		TestBean tb = new TestBean();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.get(TestBean.class, "");
		sessionControl.setReturnValue(tb, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		Object result = ht.get(TestBean.class, "");
		assertTrue("Correct result", result == tb);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testGetWithLockMode() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		TestBean tb = new TestBean();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.get(TestBean.class, "", LockMode.UPGRADE_NOWAIT);
		sessionControl.setReturnValue(tb, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		Object result = ht.get(TestBean.class, "", LockMode.UPGRADE_NOWAIT);
		assertTrue("Correct result", result == tb);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testLoad() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		TestBean tb = new TestBean();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.load(TestBean.class, "");
		sessionControl.setReturnValue(tb, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		Object result = ht.load(TestBean.class, "");
		assertTrue("Correct result", result == tb);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testLoadWithNotFound() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.load(TestBean.class, "id");
		ObjectNotFoundException onfex = new ObjectNotFoundException("id", TestBean.class);
		sessionControl.setThrowable(onfex);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		try {
			ht.load(TestBean.class, "id");
			fail("Should have thrown HibernateObjectRetrievalFailureException");
		}
		catch (HibernateObjectRetrievalFailureException ex) {
			// expected
			assertEquals(TestBean.class, ex.getPersistentClass());
			assertEquals("id", ex.getIdentifier());
			assertEquals(onfex, ex.getCause());
		}
		sfControl.verify();
		sessionControl.verify();
	}

	public void testLoadWithLockMode() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		TestBean tb = new TestBean();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.load(TestBean.class, "", LockMode.UPGRADE);
		sessionControl.setReturnValue(tb, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		Object result = ht.load(TestBean.class, "", LockMode.UPGRADE);
		assertTrue("Correct result", result == tb);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testEvict() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		TestBean tb = new TestBean();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.evict(tb);
		sessionControl.setVoidCallable(1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		ht.evict(tb);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testLock() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		TestBean tb = new TestBean();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.lock(tb, LockMode.WRITE);
		sessionControl.setVoidCallable(1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
			sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		ht.lock(tb, LockMode.WRITE);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testSave() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		TestBean tb = new TestBean();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.save(tb);
		sessionControl.setReturnValue(new Integer(0), 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		assertEquals("Correct return value", ht.save(tb), new Integer(0));
		sfControl.verify();
		sessionControl.verify();
	}

	public void testSaveWithId() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		TestBean tb = new TestBean();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.save(tb, "id");
		sessionControl.setVoidCallable(1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		ht.save(tb, "id");
		sfControl.verify();
		sessionControl.verify();
	}

	public void testSaveOrUpdate() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		TestBean tb = new TestBean();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.saveOrUpdate(tb);
		sessionControl.setVoidCallable(1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
			sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		ht.saveOrUpdate(tb);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testUpdate() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		TestBean tb = new TestBean();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.update(tb);
		sessionControl.setVoidCallable(1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
			sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		ht.update(tb);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testUpdateWithLockMode() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		TestBean tb = new TestBean();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.update(tb);
		sessionControl.setVoidCallable(1);
		session.lock(tb, LockMode.UPGRADE);
		sessionControl.setVoidCallable(1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
			sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		ht.update(tb, LockMode.UPGRADE);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testDelete() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		TestBean tb = new TestBean();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.delete(tb);
		sessionControl.setVoidCallable(1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		ht.delete(tb);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testDeleteWithLock() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		TestBean tb = new TestBean();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.lock(tb, LockMode.UPGRADE);
		sessionControl.setVoidCallable(1);
		session.delete(tb);
		sessionControl.setVoidCallable(1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		ht.delete(tb, LockMode.UPGRADE);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testDeleteAll() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		TestBean tb1 = new TestBean();
		TestBean tb2 = new TestBean();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.delete(tb1);
		sessionControl.setVoidCallable(1);
		session.delete(tb2);
		sessionControl.setVoidCallable(1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
			sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List tbs = new ArrayList();
		tbs.add(tb1);
		tbs.add(tb2);
		ht.deleteAll(tbs);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testFind() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();
		List list = new ArrayList();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.createQuery("some query string");
		sessionControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		queryControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.find("some query string");
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindWithParameter() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();
		List list = new ArrayList();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.createQuery("some query string");
		sessionControl.setReturnValue(query, 1);
		query.setParameter(0, "myvalue");
		queryControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		queryControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.find("some query string", "myvalue");
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindWithParameterAndType() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();
		List list = new ArrayList();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.createQuery("some query string");
		sessionControl.setReturnValue(query, 1);
		query.setParameter(0, "myvalue", Hibernate.STRING);
		queryControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		queryControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.find("some query string", "myvalue", Hibernate.STRING);
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindWithParameters() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();
		List list = new ArrayList();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.createQuery("some query string");
		sessionControl.setReturnValue(query, 1);
		query.setParameter(0, "myvalue1");
		queryControl.setReturnValue(query, 1);
		query.setParameter(1, new Integer(2));
		queryControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		queryControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.find("some query string", new Object[] {"myvalue1", new Integer(2)});
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindWithParametersAndTypes() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();
		List list = new ArrayList();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.createQuery("some query string");
		sessionControl.setReturnValue(query, 1);
		query.setParameter(0, "myvalue1", Hibernate.STRING);
		queryControl.setReturnValue(query, 1);
		query.setParameter(1, new Integer(2), Hibernate.INTEGER);
		queryControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		queryControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.find("some query string",
													new Object[] {"myvalue1", new Integer(2)},
													new Type[] {Hibernate.STRING, Hibernate.INTEGER});
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindWithParametersAndTypesForInvalidArguments() {
		HibernateTemplate ht = new HibernateTemplate();
		try {
			ht.find("some query string",
							new Object[] {"myvalue1", new Integer(2)},
							new Type[] {Hibernate.STRING});
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testFindByValueBean() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();
		TestBean tb = new TestBean();
		List list = new ArrayList();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.createQuery("some query string");
		sessionControl.setReturnValue(query, 1);
		query.setProperties(tb);
		queryControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		queryControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.findByValueBean("some query string", tb);
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindByNamedQuery() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();
		List list = new ArrayList();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.getNamedQuery("some query name");
		sessionControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		queryControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.findByNamedQuery("some query name");
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindByNamedQueryWithParameter() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();
		List list = new ArrayList();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.getNamedQuery("some query name");
		sessionControl.setReturnValue(query, 1);
		query.setParameter(0, "myvalue");
		queryControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		queryControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.findByNamedQuery("some query name", "myvalue");
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindByNamedQueryWithParameterAndType() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();
		List list = new ArrayList();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.getNamedQuery("some query name");
		sessionControl.setReturnValue(query, 1);
		query.setParameter(0, "myvalue", Hibernate.STRING);
		queryControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		queryControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.findByNamedQuery("some query name", (Object) "myvalue", Hibernate.STRING);
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindByNamedQueryWithParameters() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();
		List list = new ArrayList();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.getNamedQuery("some query name");
		sessionControl.setReturnValue(query, 1);
		query.setParameter(0, "myvalue1");
		queryControl.setReturnValue(query, 1);
		query.setParameter(1, new Integer(2));
		queryControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		queryControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.findByNamedQuery("some query name", new Object[] {"myvalue1", new Integer(2)});
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindByNamedQueryWithParametersAndTypes() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();
		List list = new ArrayList();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.getNamedQuery("some query name");
		sessionControl.setReturnValue(query, 1);
		query.setParameter(0, "myvalue1", Hibernate.STRING);
		queryControl.setReturnValue(query, 1);
		query.setParameter(1, new Integer(2), Hibernate.INTEGER);
		queryControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		queryControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.findByNamedQuery("some query name",
																			new Object[] {"myvalue1", new Integer(2)},
																			new Type[] {Hibernate.STRING, Hibernate.INTEGER});
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindByNamedQueryWithParametersAndTypesForInvalidArguments() {
		HibernateTemplate ht = new HibernateTemplate();
		try {
			ht.findByNamedQuery("some query string",
													new Object[] {"myvalue1", new Integer(2)},
													new Type[] {Hibernate.STRING});
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testFindByNamedQueryWithNamedParameter() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();
		List list = new ArrayList();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.getNamedQuery("some query name");
		sessionControl.setReturnValue(query, 1);
		query.setParameter("myparam", "myvalue");
		queryControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		queryControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.findByNamedQuery("some query name", "myparam", "myvalue");
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindByNamedQueryWithNamedParameterAndType() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();
		List list = new ArrayList();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.getNamedQuery("some query name");
		sessionControl.setReturnValue(query, 1);
		query.setParameter("myparam", "myvalue", Hibernate.STRING);
		queryControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		queryControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.findByNamedQuery("some query name", "myparam", "myvalue", Hibernate.STRING);
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindByNamedQueryWithNamedParameters() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();
		List list = new ArrayList();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.getNamedQuery("some query name");
		sessionControl.setReturnValue(query, 1);
		query.setParameter("myparam1", "myvalue1");
		queryControl.setReturnValue(query, 1);
		query.setParameter("myparam2", new Integer(2));
		queryControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		queryControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.findByNamedQuery("some query name", new String[] {"myparam1", "myparam2"}, new Object[] {"myvalue1", new Integer(2)});
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindByNamedQueryWithNamedParametersAndTypes() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();
		List list = new ArrayList();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.getNamedQuery("some query name");
		sessionControl.setReturnValue(query, 1);
		query.setParameter("myparam1", "myvalue1", Hibernate.STRING);
		queryControl.setReturnValue(query, 1);
		query.setParameter("myparam2", new Integer(2), Hibernate.INTEGER);
		queryControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		queryControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.findByNamedQuery("some query name",
							new String[] {"myparam1", "myparam2"},
							new Object[] {"myvalue1", new Integer(2)},
							new Type[] {Hibernate.STRING, Hibernate.INTEGER});
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindByNamedQueryAndValueBean() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();
		TestBean tb = new TestBean();
		List list = new ArrayList();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.getNamedQuery("some query name");
		sessionControl.setReturnValue(query, 1);
		query.setProperties(tb);
		queryControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		queryControl.replay();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.findByNamedQueryAndValueBean("some query name", tb);
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testExceptions() throws HibernateException {
		final SQLException sqlex = new SQLException("argh", "27");
		try {
			createTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					throw new JDBCException(sqlex);
				}
			});
			fail("Should have thrown DataIntegrityViolationException");
		}
		catch (DataIntegrityViolationException ex) {
			// expected
			assertEquals(sqlex, ex.getCause());
		}

		try {
			createTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					throw new ObjectDeletedException("msg", "id", TestBean.class);
				}
			});
			fail("Should have thrown HibernateObjectRetrievalFailureException");
		}
		catch (HibernateObjectRetrievalFailureException ex) {
			// expected
		}

		final WrongClassException wcex = new WrongClassException("msg", "id", TestBean.class);
		try {
			createTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					throw wcex;
				}
			});
			fail("Should have thrown HibernateObjectRetrievalFailureException");
		}
		catch (HibernateObjectRetrievalFailureException ex) {
			// expected
			assertEquals(TestBean.class, ex.getPersistentClass());
			assertEquals("id", ex.getIdentifier());
			assertEquals(wcex, ex.getCause());
		}

		final StaleObjectStateException sosex = new StaleObjectStateException(TestBean.class, "id");
		try {
			createTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					throw sosex;
				}
			});
			fail("Should have thrown HibernateOptimisticLockingFailureException");
		}
		catch (HibernateOptimisticLockingFailureException ex) {
			// expected
			assertEquals(TestBean.class, ex.getPersistentClass());
			assertEquals("id", ex.getIdentifier());
			assertEquals(sosex, ex.getCause());
		}

		final QueryException qex = new QueryException("msg");
		qex.setQueryString("query");
		try {
			createTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					throw qex;
				}
			});
			fail("Should have thrown InvalidDataAccessResourceUsageException");
		}
		catch (HibernateQueryException ex) {
			// expected
			assertEquals(qex, ex.getCause());
			assertEquals("query", ex.getQueryString());
		}

		try {
			createTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					throw new PersistentObjectException("");
				}
			});
			fail("Should have thrown InvalidDataAccessApiUsageException");
		}
		catch (InvalidDataAccessApiUsageException ex) {
			// expected
		}

		try {
			createTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					throw new TransientObjectException("");
				}
			});
			fail("Should have thrown InvalidDataAccessApiUsageException");
		}
		catch (InvalidDataAccessApiUsageException ex) {
			// expected
		}

		final HibernateException hex = new HibernateException("msg");
		try {
			createTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					throw hex;
				}
			});
			fail("Should have thrown HibernateSystemException");
		}
		catch (HibernateSystemException ex) {
			// expected
			assertEquals(hex, ex.getCause());
		}
	}

	private HibernateTemplate createTemplate() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		sf.openSession();
		sfControl.setReturnValue(session);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		return new HibernateTemplate(sf);
	}

}
