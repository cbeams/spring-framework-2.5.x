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

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.sql.SQLException;

import junit.framework.TestCase;
import net.sf.hibernate.FlushMode;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.JDBCException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import org.aopalliance.intercept.Interceptor;
import org.aopalliance.intercept.Invocation;
import org.aopalliance.intercept.MethodInvocation;
import org.easymock.MockControl;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author Juergen Hoeller
 */
public class HibernateInterceptorTests extends TestCase {

	public void testInterceptorWithNewSession() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.getSessionFactory();
		sessionControl.setReturnValue(sf);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateInterceptor interceptor = new HibernateInterceptor();
		interceptor.setSessionFactory(sf);
		try {
			interceptor.invoke(new TestInvocation(sf));
		}
		catch (Throwable t) {
			fail("Should not have thrown Throwable: " + t.getMessage());
		}

		sfControl.verify();
		sessionControl.verify();
	}

	public void testInterceptorWithNewSessionAndFlushNever() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.getSessionFactory();
		sessionControl.setReturnValue(sf);
		session.setFlushMode(FlushMode.NEVER);
		sessionControl.setVoidCallable(1);
		session.close();
			sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateInterceptor interceptor = new HibernateInterceptor();
		interceptor.setFlushModeName("FLUSH_NEVER");
		interceptor.setSessionFactory(sf);
		try {
			interceptor.invoke(new TestInvocation(sf));
		}
		catch (Throwable t) {
			fail("Should not have thrown Throwable: " + t.getMessage());
		}

		sfControl.verify();
		sessionControl.verify();
	}

	public void testInterceptorWithPrebound() {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		session.getSessionFactory();
		sessionControl.setReturnValue(sf, 1);
		sfControl.replay();
		sessionControl.replay();

		TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
		HibernateInterceptor interceptor = new HibernateInterceptor();
		interceptor.setSessionFactory(sf);
		try {
			interceptor.invoke(new TestInvocation(sf));
		}
		catch (Throwable t) {
			fail("Should not have thrown Throwable: " + t.getMessage());
		}
		finally {
			TransactionSynchronizationManager.unbindResource(sf);
		}

		sfControl.verify();
		sessionControl.verify();
	}

	public void testInterceptorWithPreboundAndFlushEager() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		session.getSessionFactory();
		sessionControl.setReturnValue(sf, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		sfControl.replay();
		sessionControl.replay();

		TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
		HibernateInterceptor interceptor = new HibernateInterceptor();
		interceptor.setFlushMode(HibernateInterceptor.FLUSH_EAGER);
		interceptor.setSessionFactory(sf);
		try {
			interceptor.invoke(new TestInvocation(sf));
		}
		catch (Throwable t) {
			fail("Should not have thrown Throwable: " + t.getMessage());
		}
		finally {
			TransactionSynchronizationManager.unbindResource(sf);
		}

		sfControl.verify();
		sessionControl.verify();
	}

	public void testInterceptorWithFlushingFailure() throws Throwable {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.getSessionFactory();
		sessionControl.setReturnValue(sf, 1);
		SQLException sqlex = new SQLException("argh", "27");
		session.flush();
		sessionControl.setThrowable(new JDBCException(sqlex), 1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateInterceptor interceptor = new HibernateInterceptor();
		interceptor.setSessionFactory(sf);
		try {
			interceptor.invoke(new TestInvocation(sf));
			fail("Should have thrown DataIntegrityViolationException");
		}
		catch (DataIntegrityViolationException ex) {
			// expected
			assertEquals(sqlex, ex.getCause());
		}

		sfControl.verify();
		sessionControl.verify();
	}

	public void testInterceptorWithEntityInterceptor() throws HibernateException {
		MockControl interceptorControl = MockControl.createControl(net.sf.hibernate.Interceptor.class);
		net.sf.hibernate.Interceptor entityInterceptor = (net.sf.hibernate.Interceptor) interceptorControl.getMock();
		interceptorControl.replay();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		sf.openSession(entityInterceptor);
		sfControl.setReturnValue(session, 1);
		session.getSessionFactory();
		sessionControl.setReturnValue(sf, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		HibernateInterceptor interceptor = new HibernateInterceptor();
		interceptor.setSessionFactory(sf);
		interceptor.setEntityInterceptor(entityInterceptor);
		try {
			interceptor.invoke(new TestInvocation(sf));
		}
		catch (Throwable t) {
			fail("Should not have thrown Throwable: " + t.getMessage());
		}

		interceptorControl.verify();
		sfControl.verify();
		sessionControl.verify();
	}

	public void testInterceptorWithEntityInterceptorBeanName() throws HibernateException {
		MockControl interceptorControl = MockControl.createControl(net.sf.hibernate.Interceptor.class);
		net.sf.hibernate.Interceptor entityInterceptor = (net.sf.hibernate.Interceptor) interceptorControl.getMock();
		interceptorControl.replay();
		MockControl interceptor2Control = MockControl.createControl(net.sf.hibernate.Interceptor.class);
		net.sf.hibernate.Interceptor entityInterceptor2 = (net.sf.hibernate.Interceptor) interceptor2Control.getMock();
		interceptor2Control.replay();

		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		sf.openSession(entityInterceptor);
		sfControl.setReturnValue(session, 1);
		sf.openSession(entityInterceptor2);
		sfControl.setReturnValue(session, 1);
		session.getSessionFactory();
		sessionControl.setReturnValue(sf, 2);
		session.flush();
		sessionControl.setVoidCallable(2);
		session.close();
		sessionControl.setReturnValue(null, 2);
		sfControl.replay();
		sessionControl.replay();

		MockControl beanFactoryControl = MockControl.createControl(BeanFactory.class);
		BeanFactory beanFactory = (BeanFactory) beanFactoryControl.getMock();
		beanFactory.getBean("entityInterceptor", net.sf.hibernate.Interceptor.class);
		beanFactoryControl.setReturnValue(entityInterceptor, 1);
		beanFactory.getBean("entityInterceptor", net.sf.hibernate.Interceptor.class);
		beanFactoryControl.setReturnValue(entityInterceptor2, 1);
		beanFactoryControl.replay();

		HibernateInterceptor interceptor = new HibernateInterceptor();
		interceptor.setSessionFactory(sf);
		interceptor.setEntityInterceptorBeanName("entityInterceptor");
		interceptor.setBeanFactory(beanFactory);
		for (int i = 0; i < 2; i++) {
			try {
				interceptor.invoke(new TestInvocation(sf));
			}
			catch (Throwable t) {
				fail("Should not have thrown Throwable: " + t.getMessage());
			}
		}

		interceptorControl.verify();
		interceptor2Control.verify();
		sfControl.verify();
		sessionControl.verify();
	}


	private static class TestInvocation implements MethodInvocation {

		private SessionFactory sessionFactory;

		public TestInvocation(SessionFactory sessionFactory) {
			this.sessionFactory = sessionFactory;
		}

		public Object proceed() throws Throwable {
			if (!TransactionSynchronizationManager.hasResource(this.sessionFactory)) {
				throw new IllegalStateException("Session not bound");
			}
			return null;
		}


		public int getCurrentInterceptorIndex() {
			return 0;
		}

		public int getNumberOfInterceptors() {
			return 0;
		}

		public Interceptor getInterceptor(int i) {
			return null;
		}

		public Method getMethod() {
			return null;
		}

		public AccessibleObject getStaticPart() {
			return null;
		}

		public Object getArgument(int i) {
			return null;
		}

		public Object[] getArguments() {
			return null;
		}

		public void setArgument(int i, Object handler) {
		}

		public int getArgumentCount() {
			return 0;
		}

		public Object getThis() {
			return null;
		}

		public Object getProxy() {
			return null;
		}

		public Invocation cloneInstance() {
			return null;
		}

		public void release() {
		}
	}

}
