package org.springframework.web.multipart.support;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

/**
 * Servlet 2.3 Filter that resolves multipart requests via a MultipartResolver
 * in the root web application context. Supports a "multipartResolverBeanName"
 * filter init param; the default bean name is "filterMultipartResolver".
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
 * @see #getMultipartResolver
 * @see org.springframework.web.multipart.MultipartResolver
 * @see org.springframework.web.servlet.DispatcherServlet
 */
public class MultipartFilter implements Filter {

	public static final String MULTIPART_RESOLVER_BEAN_NAME_PARAM = "multipartResolverBeanName";

	public static final String DEFAULT_MULTIPART_RESOLVER_BEAN_NAME = "filterMultipartResolver";

	protected final Log logger = LogFactory.getLog(getClass());

	private MultipartResolver multipartResolver;

	/**
	 * Fetches a reference to the MultipartResolver via getMultipartResolver
	 * and stores it for use in doFilter.
	 * @see #getMultipartResolver
	 */
	public void init(FilterConfig filterConfig) throws ServletException {
		this.multipartResolver = getMultipartResolver(filterConfig);
		if (this.multipartResolver == null) {
			throw new ServletException("Could not determine MultipartResolver");
		}
	}

	/**
	 * Looks for a MultipartResolver bean in the root web application context.
	 * Supports a "multipartResolverBeanName" filter init param; the default
	 * bean name is "filterMultipartResolver".
	 * <p>This can be overridden to use a custom MultipartResolver instance,
	 * for example if not using a Spring web application context.
	 * @return the MultipartResolver instance, or null if none found
	 * @see org.springframework.web.context.support.WebApplicationContextUtils#getWebApplicationContext
	 * @see #MULTIPART_RESOLVER_BEAN_NAME_PARAM
	 * @see #DEFAULT_MULTIPART_RESOLVER_BEAN_NAME
	 */
	protected MultipartResolver getMultipartResolver(FilterConfig filterConfig) {
		WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
		String beanName = filterConfig.getInitParameter(MULTIPART_RESOLVER_BEAN_NAME_PARAM);
		if (beanName == null) {
			beanName = DEFAULT_MULTIPART_RESOLVER_BEAN_NAME;
		}
		logger.info("Using multipart resolver '" + beanName + "' for multipart filter");
		return (MultipartResolver) wac.getBean(beanName);
	}

	/**
	 * Checks for a multipart request via this filter's MultipartResolver,
	 * and wraps the original request with a MultipartHttpServletRequest if appropriate.
	 * <p>All later elements in the filter chain, most importantly servlets, benefit
	 * from proper parameter extraction in the multipart case, and are able to cast to
	 * MultipartHttpServletRequest if they need to.
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
	    throws IOException, ServletException {
		HttpServletRequest processedRequest = (HttpServletRequest) request;
		if (this.multipartResolver.isMultipart(processedRequest)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Resolving request [" + processedRequest.getRequestURI() + "] with multipart filter");
			}
			processedRequest = this.multipartResolver.resolveMultipart(processedRequest);
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

	public void destroy() {
	}

}
