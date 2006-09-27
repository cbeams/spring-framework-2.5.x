/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.web.portlet.handler;

import javax.portlet.PortletRequest;

import org.springframework.beans.BeansException;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.Ordered;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.portlet.HandlerExecutionChain;
import org.springframework.web.portlet.HandlerInterceptor;
import org.springframework.web.portlet.HandlerMapping;

/**
 * Abstract base class for HandlerMapping implementations.
 * Supports ordering, a default handler, and handler interceptors.
 *
 * @author Juergen Hoeller
 * @author John A. Lewis
 * @since 2.0
 * @see #getHandlerInternal
 * @see org.springframework.web.portlet.HandlerInterceptor
 */
public abstract class AbstractHandlerMapping extends ApplicationObjectSupport
    implements HandlerMapping, Ordered {

	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	private Object defaultHandler;

	private Object[] interceptors;

	private boolean applyWebRequestInterceptorsToRenderPhaseOnly = true;

	private HandlerInterceptor[] adaptedInterceptors;


	public final void setOrder(int order) {
	  this.order = order;
	}

	public final int getOrder() {
	  return order;
	}

	/**
	 * Set the default handler for this handler mapping.
	 * This handler will be returned if no specific mapping was found.
	 * <p>Default is <code>null</code>, indicating no default handler.
	 * @param defaultHandler default handler instance, or null if none
	 */
	public final void setDefaultHandler(Object defaultHandler) {
		this.defaultHandler = defaultHandler;
		if (logger.isInfoEnabled()) {
			logger.info("Default mapping to handler [" + this.defaultHandler + "]");
		}
	}

	/**
	 * Return the default handler for this handler mapping.
	 * @return the default handler instance, or <code>null</code> if none
	 */
	protected final Object getDefaultHandler() {
		return defaultHandler;
	}

	/**
	 * Set the interceptors to apply for all handlers mapped by this handler mapping.
	 * <p>Supported interceptor types are HandlerInterceptor and WebRequestInterceptor.
	 * Each given WebRequestInterceptor will be wrapped in a WebRequestHandlerInterceptorAdapter.
	 * @param interceptors array of handler interceptors, or <code>null</code> if none
	 * @see #adaptInterceptor
	 * @see org.springframework.web.portlet.HandlerInterceptor
	 * @see org.springframework.web.context.request.WebRequestInterceptor
	 */
	public final void setInterceptors(Object[] interceptors) {
		this.interceptors = interceptors;
	}

	/**
	 * Specify whether to apply WebRequestInterceptors to the Portlet render phase
	 * only ("true<", or whether to apply them to the Portlet action phase as well
	 * ("false").
	 * <p>Default is "true", since WebRequestInterceptors are usually built for
	 * MVC-style handler execution plus rendering process (which is, for example,
	 * the primary target scenario for "Open Session in View" interceptors,
	 * offering lazy loading of persistent objects during view rendering).
	 * Set this to "false" to have WebRequestInterceptors apply to the action
	 * phase as well (for example, in case of an "Open Session in View" interceptor,
	 * to allow for lazy loading outside of a transaction during the action phase).
	 * @see #setInterceptors
	 * @see org.springframework.web.context.request.WebRequestInterceptor
	 * @see WebRequestHandlerInterceptorAdapter#WebRequestHandlerInterceptorAdapter(WebRequestInterceptor, boolean)
	 * @see org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor
	 */
	public void setApplyWebRequestInterceptorsToRenderPhaseOnly(boolean applyWebRequestInterceptorsToRenderPhaseOnly) {
		this.applyWebRequestInterceptorsToRenderPhaseOnly = applyWebRequestInterceptorsToRenderPhaseOnly;
	}


	/**
	 * Calls the <code>initInterceptors()</code> method.
	 * @see #initInterceptors()
	 */
	protected void initApplicationContext() throws BeansException {
		initInterceptors();
	}

	/**
	 * Initialize the specified interceptors, adapting them where necessary.
	 * @see #setInterceptors
	 * @see #adaptInterceptor
	 */
	protected void initInterceptors() {
		if (this.interceptors != null) {
			this.adaptedInterceptors = new HandlerInterceptor[this.interceptors.length];
			for (int i = 0; i < this.interceptors.length; i++) {
				if (this.interceptors[i] == null) {
					throw new IllegalArgumentException("Entry number " + i + " in interceptors array is null");
				}
				this.adaptedInterceptors[i] = adaptInterceptor(this.interceptors[i]);
			}
		}
	}

	/**
	 * Adapt the given interceptor object to the HandlerInterceptor interface.
	 * <p>Supported interceptor types are HandlerInterceptor and WebRequestInterceptor.
	 * Each given WebRequestInterceptor will be wrapped in a WebRequestHandlerInterceptorAdapter.
	 * Can be overridden in subclasses.
	 * @param interceptor the specified interceptor object
	 * @return the interceptor wrapped as HandlerInterceptor
	 * @see #setApplyWebRequestInterceptorsToRenderPhaseOnly
	 * @see org.springframework.web.portlet.HandlerInterceptor
	 * @see org.springframework.web.context.request.WebRequestInterceptor
	 * @see WebRequestHandlerInterceptorAdapter
	 */
	protected HandlerInterceptor adaptInterceptor(Object interceptor) {
		if (interceptor instanceof HandlerInterceptor) {
			return (HandlerInterceptor) interceptor;
		}
		else if (interceptor instanceof WebRequestInterceptor) {
			return new WebRequestHandlerInterceptorAdapter(
					(WebRequestInterceptor) interceptor, this.applyWebRequestInterceptorsToRenderPhaseOnly);
		}
		else {
			throw new IllegalArgumentException("Interceptor type not supported: " + interceptor.getClass().getName());
		}
	}


	/**
	 * Look up a handler for the given request, falling back to the default
	 * handler if no specific one is found.
	 * @param request current HTTP request
	 * @return the corresponding handler instance, or the default handler
	 * @see #getHandlerInternal
	 */
	public final HandlerExecutionChain getHandler(PortletRequest request) throws Exception {
		Object handler = getHandlerInternal(request);
		if (handler == null) {
			handler = this.defaultHandler;
		}
		if (handler == null) {
			return null;
		}
		// Bean name or resolved handler?
		if (handler instanceof String) {
			String handlerName = (String) handler;
			handler = getApplicationContext().getBean(handlerName);
		}
		return new HandlerExecutionChain(handler, this.adaptedInterceptors);
	}

	/**
	 * Look up a handler for the given request, returning <code>null</code> if no
	 * specific one is found. This method is called by <code>getHandler<code>;
	 * a <code>null</code> return value will lead to the default handler, if one is set.
	 * @param request current portlet request
	 * @return the corresponding handler instance, or <code>null</code> if none found
	 * @throws Exception if there is an internal error
	 * @see #getHandler
	 */
	protected abstract Object getHandlerInternal(PortletRequest request) throws Exception;

}
