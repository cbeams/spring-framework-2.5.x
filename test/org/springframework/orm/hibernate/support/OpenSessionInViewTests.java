package org.springframework.orm.hibernate.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import com.mockobjects.servlet.MockFilterChain;
import com.mockobjects.servlet.MockFilterConfig;
import junit.framework.TestCase;
import net.sf.hibernate.FlushMode;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import org.easymock.MockControl;

import org.springframework.orm.hibernate.HibernateAccessor;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.mock.MockHttpServletRequest;
import org.springframework.web.mock.MockHttpServletResponse;
import org.springframework.web.mock.MockServletContext;

/**
 * @author Juergen Hoeller
 * @since 06.12.2003
 */
public class OpenSessionInViewTests extends TestCase {

	public void testOpenSessionInViewInterceptor() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();

		OpenSessionInViewInterceptor interceptor = new OpenSessionInViewInterceptor();
		interceptor.setSessionFactory(sf);
		MockServletContext sc = new MockServletContext();
		MockHttpServletRequest request = new MockHttpServletRequest(sc, "GET", "/test");
		MockHttpServletResponse response = new MockHttpServletResponse();

		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.setFlushMode(FlushMode.NEVER);
		sessionControl.setVoidCallable(1);
		sfControl.replay();
		sessionControl.replay();
		interceptor.preHandle(request, response, "handler");
		assertTrue(TransactionSynchronizationManager.hasResource(sf));
		sfControl.verify();
		sessionControl.verify();

		sfControl.reset();
		sessionControl.reset();
		sfControl.replay();
		sessionControl.replay();
		interceptor.postHandle(request, response, "handler", null);
		assertTrue(TransactionSynchronizationManager.hasResource(sf));
		sfControl.verify();
		sessionControl.verify();

		sfControl.reset();
		sessionControl.reset();
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		interceptor.afterCompletion(request, response, "handler", null);
		assertFalse(TransactionSynchronizationManager.hasResource(sf));
		sfControl.verify();
		sessionControl.verify();
	}

	public void testOpenSessionInViewInterceptorWithFlush() throws HibernateException {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();

		OpenSessionInViewInterceptor interceptor = new OpenSessionInViewInterceptor();
		interceptor.setSessionFactory(sf);
		interceptor.setFlushMode(HibernateAccessor.FLUSH_AUTO);
		MockServletContext sc = new MockServletContext();
		MockHttpServletRequest request = new MockHttpServletRequest(sc, "GET", "/test");
		MockHttpServletResponse response = new MockHttpServletResponse();

		sf.openSession();
		sfControl.setReturnValue(session, 1);
		sfControl.replay();
		sessionControl.replay();
		interceptor.preHandle(request, response, "handler");
		assertTrue(TransactionSynchronizationManager.hasResource(sf));
		sfControl.verify();
		sessionControl.verify();

		sfControl.reset();
		sessionControl.reset();
		session.flush();
		sessionControl.setVoidCallable(1);
		sfControl.replay();
		sessionControl.replay();
		interceptor.postHandle(request, response, "handler", null);
		assertTrue(TransactionSynchronizationManager.hasResource(sf));
		sfControl.verify();
		sessionControl.verify();

		sfControl.reset();
		sessionControl.reset();
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();
		interceptor.afterCompletion(request, response, "handler", null);
		assertFalse(TransactionSynchronizationManager.hasResource(sf));
		sfControl.verify();
		sessionControl.verify();
	}

	public void testOpenSessionInViewFilter() throws Exception {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();

		MockServletContext sc = new MockServletContext();
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(sc);
		wac.getListableBeanFactory().registerSingleton(OpenSessionInViewFilter.DEFAULT_SESSION_FACTORY_BEAN_NAME, sf);
		wac.refresh();
		WebApplicationContextUtils.publishWebApplicationContext(wac);
		MockHttpServletRequest request = new MockHttpServletRequest(sc, "GET", "/test");
		MockHttpServletResponse response = new MockHttpServletResponse();

		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.setFlushMode(FlushMode.NEVER);
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		MockFilterConfig filterConfig = new MockFilterConfig() {
			public Enumeration getInitParameterNames() {
				return Collections.enumeration(new ArrayList());
			}
		};
		filterConfig.setupGetServletContext(wac.getServletContext());
		MockFilterChain filterChain = new MockFilterChain();

		OpenSessionInViewFilter filter = new OpenSessionInViewFilter();
		filter.init(filterConfig);
		assertFalse(TransactionSynchronizationManager.hasResource(sf));
		filter.doFilter(request, response, filterChain);
		assertFalse(TransactionSynchronizationManager.hasResource(sf));
		sfControl.verify();
		sessionControl.verify();

		wac.close();
	}

	public void testOpenSessionInViewFilterWithCustomBeanName() throws Exception {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();

		MockServletContext sc = new MockServletContext();
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(sc);
		wac.getListableBeanFactory().registerSingleton("mySessionFactory", sf);
		wac.refresh();
		WebApplicationContextUtils.publishWebApplicationContext(wac);
		MockHttpServletRequest request = new MockHttpServletRequest(sc, "GET", "/test");
		MockHttpServletResponse response = new MockHttpServletResponse();

		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.setFlushMode(FlushMode.NEVER);
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		MockFilterConfig filterConfig = new MockFilterConfig() {
			public Enumeration getInitParameterNames() {
				return Collections.enumeration(new ArrayList());
			}
		};
		filterConfig.setupGetServletContext(wac.getServletContext());
		MockFilterChain filterChain = new MockFilterChain();

		OpenSessionInViewFilter filter = new OpenSessionInViewFilter();
		filter.setSessionFactoryBeanName("mySessionFactory");
		filter.init(filterConfig);
		assertFalse(TransactionSynchronizationManager.hasResource(sf));
		filter.doFilter(request, response, filterChain);
		assertFalse(TransactionSynchronizationManager.hasResource(sf));
		sfControl.verify();
		sessionControl.verify();
	}

}
