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

package org.springframework.web.multipart.support;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

/**
 * Servlet 2.3 Filter that resolves multipart requests via a MultipartResolver
 * in the root web application context. Supports a "multipartResolverBeanName"
 * filter init-param; the default bean name is "filterMultipartResolver".
 *
 * <p>MultipartResolver determination is customizable: Override this filter's
 * getMultipartResolver method to use a custom MultipartResolver instance,
 * for example if not using a Spring web application context.
 *
 * <p>Note: This filter is an <b>alternative</b> to using DispatcherServlet's
 * MultipartResolver support, for example for web applications with custom
 * web views that do not use Spring's web MVC. It should not be combined with
 * servlet-specific multipart resolution.
 *
 * @author Juergen Hoeller
 * @since 08.10.2003
 * @see org.springframework.web.multipart.MultipartResolver
 * @see org.springframework.web.servlet.DispatcherServlet
 */
public class MultipartFilter extends OncePerRequestFilter {

	public static final String DEFAULT_MULTIPART_RESOLVER_BEAN_NAME = "filterMultipartResolver";

	private String multipartResolverBeanName = DEFAULT_MULTIPART_RESOLVER_BEAN_NAME;

	private MultipartResolver multipartResolver;

	/**
	 * Set the bean name of the MultipartResolver to fetch from Spring's
	 * root application context.
	 */
	public void setMultipartResolverBeanName(String multipartResolverBeanName) {
		this.multipartResolverBeanName = multipartResolverBeanName;
	}

	/**
	 * Return the bean name of the MultipartResolver to fetch from Spring's
	 * root application context.
	 */
	protected String getMultipartResolverBeanName() {
		return multipartResolverBeanName;
	}

	/**
	 * Fetch a reference to the MultipartResolver via lookupMultipartResolver
	 * and stores it for use in this filter.
	 * @see #lookupMultipartResolver
	 */
	protected void initFilterBean() throws ServletException {
		this.multipartResolver = lookupMultipartResolver();
		if (this.multipartResolver == null) {
			throw new ServletException("Could not determine MultipartResolver");
		}
	}

	/**
	 * Look for a MultipartResolver bean in the root web application context.
	 * Supports a "multipartResolverBeanName" filter init param; the default
	 * bean name is "filterMultipartResolver".
	 * <p>This can be overridden to use a custom MultipartResolver instance,
	 * for example if not using a Spring web application context.
	 * @return the MultipartResolver instance, or null if none found
	 */
	protected MultipartResolver lookupMultipartResolver() {
		logger.info("Using MultipartResolver '" + getMultipartResolverBeanName() + "' for MultipartFilter");
		WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		return (MultipartResolver) wac.getBean(getMultipartResolverBeanName());
	}

	/**
	 * Check for a multipart request via this filter's MultipartResolver,
	 * and wrap the original request with a MultipartHttpServletRequest if appropriate.
	 * <p>All later elements in the filter chain, most importantly servlets, benefit
	 * from proper parameter extraction in the multipart case, and are able to cast to
	 * MultipartHttpServletRequest if they need to.
	 */
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
																	FilterChain filterChain) throws ServletException, IOException {
		HttpServletRequest processedRequest = request;
		if (this.multipartResolver.isMultipart(processedRequest)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Resolving multipart request [" + processedRequest.getRequestURI() +
				             "] with MultipartFilter");
			}
			processedRequest = this.multipartResolver.resolveMultipart(processedRequest);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Request [" + processedRequest.getRequestURI() + "] is not a multipart request");
			}
		}
		try {
			filterChain.doFilter(processedRequest, response);
		}
		finally {
			if (processedRequest instanceof MultipartHttpServletRequest) {
				this.multipartResolver.cleanupMultipart((MultipartHttpServletRequest) processedRequest);
			}
		}
	}

}
