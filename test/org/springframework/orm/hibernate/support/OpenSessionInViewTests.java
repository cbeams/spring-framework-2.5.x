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

package org.springframework.orm.hibernate.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.mockobjects.servlet.MockFilterConfig;
import junit.framework.TestCase;
import net.sf.hibernate.FlushMode;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import org.easymock.MockControl;

import org.springframework.orm.hibernate.HibernateAccessor;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
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

		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.setFlushMode(FlushMode.NEVER);
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		MockControl sf2Control = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf2 = (SessionFactory) sf2Control.getMock();
		MockControl session2Control = MockControl.createControl(Session.class);
		Session session2 = (Session) session2Control.getMock();

		sf2.openSession();
		sf2Control.setReturnValue(session2, 1);
		session2.setFlushMode(FlushMode.NEVER);
		session2Control.setVoidCallable(1);
		session2.close();
		session2Control.setReturnValue(null, 1);
		sf2Control.replay();
		session2Control.replay();

		MockServletContext sc = new MockServletContext();
		StaticWebApplicationContext wac = new StaticWebApplicationContext();
		wac.setServletContext(sc);
		wac.getDefaultListableBeanFactory().registerSingleton("sessionFactory", sf);
		wac.getDefaultListableBeanFactory().registerSingleton("mySessionFactory", sf2);
		wac.refresh();
		sc.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, wac);
		MockHttpServletRequest request = new MockHttpServletRequest(sc, "GET", "/test");
		MockHttpServletResponse response = new MockHttpServletResponse();

		MockFilterConfig filterConfig = new MockFilterConfig() {
			public Enumeration getInitParameterNames() {
				return Collections.enumeration(new ArrayList());
			}
		};
		filterConfig.setupGetServletContext(wac.getServletContext());

		MockFilterConfig filterConfig2 = new MockFilterConfig() {
			public Enumeration getInitParameterNames() {
				return Collections.enumeration(Arrays.asList(new String[] {"sessionFactoryBeanName"}));
			}
			public String getInitParameter(String s) {
				return ("sessionFactoryBeanName".equals(s) ? "mySessionFactory" : null);
			}
		};
		filterConfig2.setupGetServletContext(wac.getServletContext());

		final OpenSessionInViewFilter filter = new OpenSessionInViewFilter();
		filter.init(filterConfig);
		final OpenSessionInViewFilter filter2 = new OpenSessionInViewFilter();
		filter2.init(filterConfig2);

		final FilterChain filterChain = new FilterChain() {
			public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
			    throws IOException, ServletException {
				assertTrue(TransactionSynchronizationManager.hasResource(sf));
				servletRequest.setAttribute("invoked", Boolean.TRUE);
			}
		};

		final FilterChain filterChain2 = new FilterChain() {
			public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
			    throws IOException, ServletException {
				assertTrue(TransactionSynchronizationManager.hasResource(sf2));
				filter.doFilter(servletRequest, servletResponse, filterChain);
			}
		};

		FilterChain filterChain3 = new FilterChain() {
			public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
			    throws IOException, ServletException {
				filter2.doFilter(servletRequest, servletResponse, filterChain2);
			}
		};

		assertFalse(TransactionSynchronizationManager.hasResource(sf));
		assertFalse(TransactionSynchronizationManager.hasResource(sf2));
		filter2.doFilter(request, response, filterChain3);
		assertFalse(TransactionSynchronizationManager.hasResource(sf));
		assertFalse(TransactionSynchronizationManager.hasResource(sf2));
		assertNotNull(request.getAttribute("invoked"));

		sfControl.verify();
		sessionControl.verify();
		sf2Control.verify();
		session2Control.verify();

		wac.close();
	}

}
