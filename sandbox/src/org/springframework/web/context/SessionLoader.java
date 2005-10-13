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

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.target.scope.HttpSessionScopedBeanPostProcessor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * <p>Initializes a HTTP session (or session for short) scoped application
 * context. "/WEB-INF/sessionContext.xml" is the default config location
 * that's loaded by the application context.
 * 
 * <p>The "sessionClass" parameter at the web.xml context-param level
 * allows overriding the application context class type. By default this is
 * {@link org.springframework.web.context.support.XmlWebApplicationContext}.
 * 
 * <p>The "sessionConfigLocation" context parameter can hold config
 * locations for the application context. This value is handled in the same
 * way as the "contextConfigLocation" parameter in
 * {@link org.springframework.web.context.ContextLoader}.
 * 
 * <p>If an application context is available in the servlet context - loaded
 * by {@link org.springframework.web.context.ContextLoaderListener} or
 * {@link org.springframework.web.context.ContextLoaderServlet} -  this
 * will be used as a parent for the session scoped application context.
 * The session scoped application context is a singleton instance.
 * 
 * <p>Prototype beans in the session scoped application context will be created
 * once per session and stored in the session and are thus "session singletons". 
 * Singletons will not be stored in the session. They remain normal singletons
 * and are shared across sessions. 
 * 
 * <p>This class does not deal with the session directly. It's up to client
 * classes to store the application context in the session for easy access.
 * 
 * @author Steven Devijver
 * @since Sep 29, 2005
 */
public class SessionLoader {

	private static final String SESSION_WEB_APPLICATION_CONTEXT_ATTRIBUTE = WebApplicationContext.class + ".SESSION";

	private static Log log = LogFactory.getLog(SessionLoader.class);
	
	public static final  String SESSION_CONTEXT_PARAM = "sessionContext";
	
	public static final Class DEFAULT_SESSION_CONTEXT_CLASS = XmlWebApplicationContext.class;
	
	public static final String CONFIG_LOCATION_PARAM = "sessionConfigLocation";
	
	private static final String DEFAULT_CONFIG_LOCATION = "/WEB-INF/sessionContext.xml";

	private ConfigurableWebApplicationContext applicationContext = null;
	
	/**
	 * Returns an application context that creates prototype beans per session. Prototype instances are
	 * stored and retrieved from the session.
	 */
	public ConfigurableWebApplicationContext initWebApplicationContext(ServletContext servletContext) {
		try {
			if (applicationContext != null) {
				throw new IllegalStateException("Session application context has already been initialized!");
			}
			
			long startTime = System.currentTimeMillis();
			ApplicationContext parent = loadParentApplicationContext(servletContext);
			
			this.applicationContext = createWebApplicationContext(servletContext, parent);
			this.applicationContext.getBeanFactory().addBeanPostProcessor(new HttpSessionScopedBeanPostProcessor(this.applicationContext.getBeanFactory()));
			
			if (log.isInfoEnabled()) {
				log.info("Using context class [" + this.applicationContext.getClass().getName() +
						"] for session WebApplicationContext");
			}
			if (log.isInfoEnabled()) {
				long elapsedTime = System.currentTimeMillis() - startTime;
				log.info("Session WebApplicationContext: initialization completed in " + elapsedTime + " ms");
			}

			return this.applicationContext;
		} catch (RuntimeException e) {
			log.error("Session context initialization failed", e);
			servletContext.setAttribute(SESSION_WEB_APPLICATION_CONTEXT_ATTRIBUTE, e);
			throw e;
		} catch (Error e) {
			log.error("Session context initialization failed", e);
			servletContext.setAttribute(SESSION_WEB_APPLICATION_CONTEXT_ATTRIBUTE, e);
			throw e;
		}
	}
	
	/**
	 * <p>Gets the parent application context using {@link WebApplicationContextUtils#getWebApplicationContext(ServletContext)}.
	 */
	protected ApplicationContext loadParentApplicationContext(ServletContext servletContext) {
		return WebApplicationContextUtils.getWebApplicationContext(servletContext);
	}
	
	/**
	 * <p>Creates a WebApplicationContext instance. By default {@link XmlWebApplicationContext} is returned.
	 * Using the "sessionContext" context-parameter you can specify another class to instantiate. This
	 * class must implement the {@link ConfigurableWebApplicationContext} interface.
	 * 
	 * <p>If the "sessionConfigLocation" context-parameter is specified these locations are loaded. By default
	 * "/WEB-INF/sessionContext.xml" is loaded.
	 */
	protected ConfigurableWebApplicationContext createWebApplicationContext(ServletContext servletContext, ApplicationContext parent) {

		String sessionContextClassName = servletContext.getInitParameter(SESSION_CONTEXT_PARAM);
		Class contextClass = DEFAULT_SESSION_CONTEXT_CLASS;
		
		if (sessionContextClassName != null) {
			try {
				contextClass = ClassUtils.forName(sessionContextClassName);
			} catch (ClassNotFoundException e) {
				throw new ApplicationContextException("Failed to load context class [" + sessionContextClassName + "]", e);
			}
			if (!ConfigurableWebApplicationContext.class.isAssignableFrom(contextClass)) {
				throw new ApplicationContextException("Custom context class [" + sessionContextClassName +
				"] is not of type ConfigurableWebApplicationContext");
			}
		}
		
		ConfigurableWebApplicationContext wac = (ConfigurableWebApplicationContext)BeanUtils.instantiateClass(contextClass);
		wac.setParent(parent);
		wac.setServletContext(servletContext);
		String configLocation = servletContext.getInitParameter(CONFIG_LOCATION_PARAM);
		if (configLocation != null) {
			wac.setConfigLocations(StringUtils.tokenizeToStringArray(configLocation, ConfigurableWebApplicationContext.CONFIG_LOCATION_DELIMITERS));
		} else {
			wac.setConfigLocations(new String[] { DEFAULT_CONFIG_LOCATION });
		}
		wac.refresh();
		
		return wac;
	}
	
	/**
	 * Closes the session application context.
	 */
	public void closeWebApplicationContext(ServletContext servletContext) {
		this.applicationContext.close();
	}
}
