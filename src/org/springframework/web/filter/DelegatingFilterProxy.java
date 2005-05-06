/* Copyright 2004, 2005 Acegi Technology Pty Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Proxy for a standard Servlet 2.3 Filter, delegating to a Spring-managed
 * bean that implements the Filter interface.
 *
 * <p><code>web.xml</code> will usually contain a DelegatingFilterProxy definition,
 * with the specified <code>filter-name</code> corresponding to a bean name in
 * Spring's root application context. All calls to the filter proxy will then
 * be delegated to that bean in the Spring context, which is required to implement
 * the standard Servlet 2.3 Filter interface.
 *
 * <p>This approach is particularly useful for Filter implementation with complex
 * setup needs, allowing to apply the full Spring bean definition machinery to
 * Filter instances. Alternatively, consider standard Filter setup in combination
 * with looking up service beans from the Spring root application context.
 *
 * <p>This class is inspired by Acegi Security's FilterToBeanProxy class,
 * written by Ben Alex.
 *
 * @author Juergen Hoeller
 * @since 1.2
 */
public class DelegatingFilterProxy extends GenericFilterBean {

	private String targetBeanName;

	private Filter delegate;


	/**
	 * Set the name of the target bean in the Spring application context.
	 * The target bean must implement the standard Servlet 2.3 Filter interface.
	 * <p>By default, the <code>filter-name</code> as specified for the
	 * DelegatingFilterProxy in <code>web.xml</code> will be used.
	 */
	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	/**
	 * Set the name of the target bean in the Spring application context.
	 */
	protected String getTargetBeanName() {
		return targetBeanName;
	}


	protected void initFilterBean() throws ServletException {
		// If no target bean name specified, use filter name.
		if (this.targetBeanName == null) {
			this.targetBeanName = getFilterName();
		}
		// Fetch Spring root application context and initialize the delegate early,
		// if possible. If the root application context will be started after this
		// filter proxy, we'll have to resort to lazy initialization.
		WebApplicationContext wac =
				WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		if (wac != null) {
			this.delegate = initDelegate(wac);
		}
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// Lazily initialize the delegate if necessary.
		if (this.delegate == null) {
			WebApplicationContext wac =
					WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
			this.delegate = initDelegate(wac);
		}

		// Let the delegate perform the actual doFilter opeation.
		this.delegate.doFilter(request, response, filterChain);
	}

	public void destroy() {
		if (this.delegate != null) {
			destroyDelegate(this.delegate);
		}
	}


	/**
	 * Initialize the Filter delegate, defined as bean the given Spring
	 * application context.
	 * <p>Default implementation fetches the bean from the application context
	 * and calls the standard <code>Filter.init</code> method on it, passing
	 * in the FilterConfig of this Filter proxy.
	 * @param wac the root application context
	 * @return the initialized delegate Filter
	 * @throws ServletException if thrown by the Filter
	 * @see #getTargetBeanName()
	 * @see #getFilterConfig()
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	protected Filter initDelegate(WebApplicationContext wac) throws ServletException {
		Filter delegate = (Filter) wac.getBean(getTargetBeanName(), Filter.class);
		delegate.init(getFilterConfig());
		return delegate;
	}

	/**
	 * Destroy the Filter delegate.
	 * Default implementation simply calls <code>Filter.destroy</code> on it.
	 * @param delegate the Filter delegate (never null)
	 * @see javax.servlet.Filter#destroy()
	 */
	protected void destroyDelegate(Filter delegate) {
		this.delegate.destroy();
	}

}
