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

package org.springframework.context.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationContextException;

/**
 * Convenient superclass for application objects that want to be aware of
 * the application context, e.g. for custom lookup of collaborating beans
 * or for context-specific resource access. It saves the application
 * context reference and provides an initialization callback method.
 * Furthermore, it offers numerous convenience methods for message lookup.
 *
 * <p>There is no requirement to subclass this class: It just makes things
 * a little easier if you need access to the context, e.g. for access to
 * file resources or to the message source. Note that many application
 * objects do not need to be aware of the application context at all,
 * as they can receive collaborating beans via bean references.
 *
 * <p>Many framework classes are derived from this class, particularly
 * within the web support.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public abstract class ApplicationObjectSupport implements ApplicationContextAware {
	
	/** Logger that is available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());
	
	/** ApplicationContext this object runs in */
	private ApplicationContext applicationContext;

	/** MessageSourceAccessor for easy message access */
	private MessageSourceAccessor messageSourceAccessor;

	/**
	 * Constructor for bean usage via subclassing.
	 */
	public ApplicationObjectSupport() {
	}

	/**
	 * Constructor for usage as helper to delegate to.
	 * @param context ApplicationContext object to be used by this object
	 */
	public ApplicationObjectSupport(ApplicationContext context) {
		this.applicationContext = context;
	}

	public final void setApplicationContext(ApplicationContext context) throws BeansException {
		if (this.applicationContext == null) {
			if (!requiredContextClass().isInstance(context)) {
				throw new ApplicationContextException("Invalid application context: needs to be of type '" +
				                                      requiredContextClass().getName() + "'");
			}
			this.applicationContext = context;
			this.messageSourceAccessor = new MessageSourceAccessor(context);
			initApplicationContext();
		}
		else {
			// ignore reinitialization if same context passed in
			if (this.applicationContext != context) {
				throw new ApplicationContextException("Cannot reinitialize with different application context");
			}
		}
	}
	
	/**
	 * Determine the context class that any context passed to
	 * setApplicationContext must be an instance of.
	 * Can be overridden in subclasses.
	 */
	protected Class requiredContextClass() {
		return ApplicationContext.class;
	}

	/**
	 * Subclasses can override this for custom initialization behavior.
	 * Gets called by setApplicationContext() after setting the context instance.
	 * <p>Note: Does </i>not</i> get called on reinitialization of the context.
	 * @throws ApplicationContextException in case of initialization errors
	 * @throws BeansException if thrown by application context methods
	 */
	protected void initApplicationContext() throws BeansException {
	}

	/**
	 * Return the ApplicationContext instance used by this object.
	 */
	public final ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * Return a MessageSourceAccessor for the application context
	 * used by this object, for easy message access.
	 */
	protected final MessageSourceAccessor getMessageSourceAccessor() {
		return this.messageSourceAccessor;
	}

}
