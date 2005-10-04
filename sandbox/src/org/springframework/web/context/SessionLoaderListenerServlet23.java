/*
 * Copyright 2004-2005 the original author or authors.
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
package org.springframework.web.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

/**
 * <p>Listener for Servlet 2.3 containers that creates and destroys a session application context.
 * 
 * <p>When a session is created or activated (de-serialized) the application context is published in the session.
 * 
 * <p>When a session is being destroyed or passivated (serialized) the application context is removed from the session.
 * 
 * @author Steven Devijver
 * @since Oct 3, 2005
 */
public class SessionLoaderListenerServlet23 implements ServletContextListener,
		HttpSessionActivationListener, HttpSessionListener {

	private static final String SESSION_WEB_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.class + ".SESSION";

	private static Log log = LogFactory.getLog(SessionLoaderListenerServlet23.class);
	
	private SessionLoader sessionLoader = new SessionLoader();
	private ApplicationContext applicationContext = null;
	
	public SessionLoaderListenerServlet23() {
		super();
	}

	/**
	 * <p>Web application is starting.
	 * 
	 * @see SessionLoader#initWebApplicationContext(ServletContext)
	 */
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		this.applicationContext = this.sessionLoader.initWebApplicationContext(servletContextEvent.getServletContext());
	}

	/**
	 * <p>Web application is stopping.
	 * 
	 * @see SessionLoader#closeWebApplicationContext(ServletContext)
	 */
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		this.sessionLoader.closeWebApplicationContext(servletContextEvent.getServletContext());
	}

	/**
	 * <p>HTTP session has been created. This method adds the application context to the session for easy access.
	 * 
	 *  @see WebApplicationContext#SESSION_WEB_APPLICATION_CONTEXT_ATTRIBUTE
	 */
	public void sessionCreated(HttpSessionEvent httpSessionEvent) {
		HttpSession session = httpSessionEvent.getSession();
		if (log.isDebugEnabled()) {
			log.debug("Session has been created");
		}
		addToSession(session, this.applicationContext);
	}
	
	/**
	 * <p>HTTP session is being destroyed. This method removes the application context from the session.
	 * 
	 * @see WebApplicationContext#SESSION_WEB_APPLICATION_CONTEXT_ATTRIBUTE
	 */
	public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
		HttpSession session = httpSessionEvent.getSession();
		if (log.isDebugEnabled()) {
			log.debug("Session is being destroyed");
		}
		removeFromSession(session);
	}
	
	/**
	 * <p>HTTP session has been activated (de-serialized). This method adds the application context to the session for easy access.
	 * 
	 * @see WebApplicationContext#SESSION_WEB_APPLICATION_CONTEXT_ATTRIBUTE
	 */
	public void sessionDidActivate(HttpSessionEvent httpSessionEvent) {
		HttpSession session = httpSessionEvent.getSession();
		if (log.isDebugEnabled()) {
			log.debug("Session has been activated");
		}
		addToSession(session, this.applicationContext);
	}
	
	/**
	 * <p>HTTP session is being passivated. This method removes the application context from the session.
	 * 
	 * <p>VM's configured in a cluster each have their own application context instance. If the configuration for the session application context
	 * on each VM is equal (which is the expected scenario) each VM publishes its own application context instance in the session. Prototype instances
	 * that have been created in the scope of a session are stored in the session and should be serializable in clustered environments.
	 * 
	 * @see WebApplicationContext#SESSION_WEB_APPLICATION_CONTEXT_ATTRIBUTE
	 */
	public void sessionWillPassivate(HttpSessionEvent httpSessionEvent) {
		HttpSession session = httpSessionEvent.getSession();
		if (log.isDebugEnabled()) {
			log.debug("Session is being passivated");
		}
		removeFromSession(session);
	}
	
	private void addToSession(HttpSession session, ApplicationContext applicationContext) {
		session.setAttribute(WebApplicationContext.SESSION_WEB_APPLICATION_CONTEXT_ATTRIBUTE, this.applicationContext);
		if (log.isDebugEnabled()) {
			log.debug("Published session WebApplicationContext [" + this.applicationContext +
					"] as Session attribute with name [" +
					WebApplicationContext.SESSION_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "]");
		}		
	}
	
	private void removeFromSession(HttpSession session) {
		session.removeAttribute(WebApplicationContext.SESSION_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		if (log.isDebugEnabled()) {
			log.debug("Removed session WebApplicationContext [" + this.applicationContext +
					"] with attribute name [" +
					WebApplicationContext.SESSION_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "] from Session");
		}
	}
}
