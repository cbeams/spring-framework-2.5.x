package org.springframework.orm.hibernate;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import net.sf.hibernate.FlushMode;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.JDBCException;
import net.sf.hibernate.ObjectDeletedException;
import net.sf.hibernate.ObjectNotFoundException;
import net.sf.hibernate.PersistentObjectException;
import net.sf.hibernate.Query;
import net.sf.hibernate.QueryException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.StaleObjectStateException;
import net.sf.hibernate.TransientObjectException;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.type.Type;
import org.easymock.EasyMock;
import org.easymock.MockControl;

import org.springframework.beans.TestBean;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.dao.OptimisticLockingFailureException;

/**
 * @author Juergen Hoeller
 * @since 06.05.2003
 */
public class HibernateTemplateTests extends TestCase {

	public void testExecuteWithNewSession() {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
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
		sfControl.activate();
		sessionControl.activate();

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
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
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
		sfControl.activate();
		sessionControl.activate();

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
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
		Session session = (Session) sessionControl.getMock();
		sfControl.activate();
		sessionControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		ht.setAllowCreate(false);
		SessionFactoryUtils.getThreadObjectManager().bindThreadObject(sf, new SessionHolder(session));
		final List l = new ArrayList();
		l.add("test");
		List result = ht.executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return l;
			}
		});
		assertTrue("Correct result list", result == l);
		SessionFactoryUtils.getThreadObjectManager().removeThreadObject(sf);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testExecuteWithThreadBoundAndFlushEager() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
		Session session = (Session) sessionControl.getMock();
		session.flush();
		sessionControl.setVoidCallable(1);
		sfControl.activate();
		sessionControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		ht.setFlushModeName("FLUSH_EAGER");
		ht.setAllowCreate(false);
		SessionFactoryUtils.getThreadObjectManager().bindThreadObject(sf, new SessionHolder(session));
		final List l = new ArrayList();
		l.add("test");
		List result = ht.executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return l;
			}
		});
		assertTrue("Correct result list", result == l);
		SessionFactoryUtils.getThreadObjectManager().removeThreadObject(sf);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testExecuteWithEntityInterceptor() throws HibernateException {
		MockControl interceptorControl = EasyMock.controlFor(net.sf.hibernate.Interceptor.class);
		Interceptor entityInterceptor = (Interceptor) interceptorControl.getMock();
		interceptorControl.activate();
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
		Session session = (Session) sessionControl.getMock();
		sf.openSession(entityInterceptor);
		sfControl.setReturnValue(session, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.activate();
		sessionControl.activate();

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

	public void testLoad() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
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
		sfControl.activate();
		sessionControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		Object result = ht.load(TestBean.class, "");
		assertTrue("Correct result", result == tb);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testLoadWithNotFound() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
		Session session = (Session) sessionControl.getMock();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.load(TestBean.class, "");
		sessionControl.setThrowable(new ObjectNotFoundException("msg", "id", TestBean.class));
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.activate();
		sessionControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		Object result = ht.load(TestBean.class, "");
		assertTrue("Correct result", result == null);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testSaveOrUpdate() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
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
		sfControl.activate();
		sessionControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		ht.saveOrUpdate(tb);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testSave() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
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
		sfControl.activate();
		sessionControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		assertEquals("Correct return value", ht.save(tb), new Integer(0));
		sfControl.verify();
		sessionControl.verify();
	}

	public void testUpdate() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
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
		sfControl.activate();
		sessionControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		ht.update(tb);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testDelete() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
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
		sfControl.activate();
		sessionControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		ht.delete(tb);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testDeleteAll() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
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
		sfControl.activate();
		sessionControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List tbs = new ArrayList();
		tbs.add(tb1);
		tbs.add(tb2);
		ht.deleteAll(tbs);
		sfControl.verify();
		sessionControl.verify();
	}

	public void testFind() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = EasyMock.controlFor(Query.class);
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
		sfControl.activate();
		sessionControl.activate();
		queryControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.find("some query string");
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindWithParameter() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = EasyMock.controlFor(Query.class);
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
		sfControl.activate();
		sessionControl.activate();
		queryControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.find("some query string", "myvalue");
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindWithParameterAndType() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = EasyMock.controlFor(Query.class);
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
		sfControl.activate();
		sessionControl.activate();
		queryControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.find("some query string", "myvalue", Hibernate.STRING);
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindWithParameters() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = EasyMock.controlFor(Query.class);
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
		sfControl.activate();
		sessionControl.activate();
		queryControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.find("some query string", new Object[] {"myvalue1", new Integer(2)});
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindWithParametersAndTypes() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = EasyMock.controlFor(Query.class);
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
		sfControl.activate();
		sessionControl.activate();
		queryControl.activate();

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
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = EasyMock.controlFor(Query.class);
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
		sfControl.activate();
		sessionControl.activate();
		queryControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.findByValueBean("some query string", tb);
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindByNamedQuery() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = EasyMock.controlFor(Query.class);
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
		sfControl.activate();
		sessionControl.activate();
		queryControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.findByNamedQuery("some query name");
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindByNamedQueryWithParameter() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = EasyMock.controlFor(Query.class);
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
		sfControl.activate();
		sessionControl.activate();
		queryControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.findByNamedQuery("some query name", "myvalue");
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindByNamedQueryWithParameterAndType() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = EasyMock.controlFor(Query.class);
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
		sfControl.activate();
		sessionControl.activate();
		queryControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.findByNamedQuery("some query name", "myvalue", Hibernate.STRING);
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindByNamedQueryWithParameters() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = EasyMock.controlFor(Query.class);
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
		sfControl.activate();
		sessionControl.activate();
		queryControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.findByNamedQuery("some query name", new Object[] {"myvalue1", new Integer(2)});
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testFindByNamedQueryWithParametersAndTypes() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = EasyMock.controlFor(Query.class);
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
		sfControl.activate();
		sessionControl.activate();
		queryControl.activate();

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

	public void testFindByNamedQueryAndValueBean() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl queryControl = EasyMock.controlFor(Query.class);
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
		sfControl.activate();
		sessionControl.activate();
		queryControl.activate();

		HibernateTemplate ht = new HibernateTemplate(sf);
		List result = ht.findByNamedQueryAndValueBean("some query name", tb);
		assertTrue("Correct list", result == list);
		sfControl.verify();
		sessionControl.verify();
		queryControl.verify();
	}

	public void testExceptions() {
		try {
			createTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					throw new JDBCException(null);
				}
			});
		}
		catch (HibernateJdbcException ex) {
			// expected
		}
		catch (Exception ex) {
			fail("Should have thrown HibernateJdbcException");
		}

		try {
			createTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					throw new QueryException("");
				}
			});
		}
		catch (InvalidDataAccessResourceUsageException ex) {
			// expected
		}
		catch (Exception ex) {
			fail("Should have thrown InvalidDataAccessResourceUsageException");
		}

		try {
			createTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					throw new StaleObjectStateException(TestBean.class, "");
				}
			});
		}
		catch (OptimisticLockingFailureException ex) {
			// expected
		}
		catch (Exception ex) {
			fail("Should have thrown OptimisticLockingFailureException");
		}

		try {
			createTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					throw new PersistentObjectException("");
				}
			});
		}
		catch (InvalidDataAccessApiUsageException ex) {
			// expected
		}
		catch (Exception ex) {
			fail("Should have thrown InvalidDataAccessApiUsageException");
		}

		try {
			createTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					throw new TransientObjectException("");
				}
			});
		}
		catch (InvalidDataAccessApiUsageException ex) {
			// expected
		}
		catch (Exception ex) {
			fail("Should have thrown InvalidDataAccessApiUsageException");
		}

		try {
			createTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					throw new ObjectDeletedException("", "");
				}
			});
		}
		catch (InvalidDataAccessApiUsageException ex) {
			// expected
		}
		catch (Exception ex) {
			fail("Should have thrown InvalidDataAccessApiUsageException");
		}

		try {
			createTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					throw new HibernateException("");
				}
			});
		}
		catch (HibernateSystemException ex) {
			// expected
		}
		catch (Exception ex) {
			fail("Should have thrown HibernateSystemException");
		}
	}

	private HibernateTemplate createTemplate() throws HibernateException {
		MockControl sfControl = EasyMock.controlFor(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = EasyMock.controlFor(Session.class);
		Session session = (Session) sessionControl.getMock();
		sf.openSession();
		sfControl.setReturnValue(session);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.activate();
		sessionControl.activate();
		return new HibernateTemplate(sf);
	}

}
