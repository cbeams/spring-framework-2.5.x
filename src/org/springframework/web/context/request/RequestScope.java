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

package org.springframework.web.context.request;

/**
 * Request-backed Scope implementation. Relies on a thread-bound
 * RequestAttributes instance, which can be exported through
 * RequestContextListener, RequestContextFilter or DispatcherServlet.
 *
 * <p>This Scope will also work for Portlet environments,
 * through an alternate RequestAttributes implementation
 * (as exposed out-of-the-box by Spring's DispatcherPortlet).
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 2.0
 * @see org.springframework.web.context.request.RequestContextHolder#currentRequestAttributes()
 * @see RequestAttributes#SCOPE_REQUEST
 * @see org.springframework.web.context.request.RequestContextListener
 * @see org.springframework.web.filter.RequestContextFilter
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see org.springframework.web.portlet.DispatcherPortlet
 */
public class RequestScope extends AbstractRequestAttributesScope {

	protected int getScope() {
		return RequestAttributes.SCOPE_REQUEST;
	}

	public String getConversationId() {
		// There is no conversation id concept for a request.
		return null;
	}

}
