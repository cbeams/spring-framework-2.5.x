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

package org.springframework.web.servlet.view;

import java.util.Locale;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.web.servlet.View;

/**
 * Simple implementation of ViewResolver that allows for direct resolution of
 * symbolic view names to URLs, without explicit mapping definition. This is
 * appropriate if your symbolic names match the names of your view resources
 * in a straightforward manner, without the need for arbitrary mappings.
 *
 * <p>Supports AbstractUrlBasedView subclasses like InternalResourceView,
 * VelocityView and FreeMarkerView. The view class for all views generated
 * by this resolver can be specified via the "viewClass" property.
 *
 * <p>View names can either be resource URLs themselves, or get augmented by a
 * specified prefix and/or suffix. Exporting an attribute that holds the
 * RequestContext to all views is explicitly supported.
 *
 * <p>Example: prefix="/WEB-INF/jsp/", suffix=".jsp", viewname="test" ->
 * "/WEB-INF/jsp/test.jsp"
 *
 * <p>As a special feature, redirect URLs can be specified via the "redirect:"
 * prefix. E.g.: "redirect:myAction.do" will trigger a redirect to the given
 * URL, rather than resolution as standard view name.
 *
 * <p>Note: This class does not support localized resolution, i.e. resolving
 * a symbolic view name to different resources depending on the current locale.
 *
 * <p>Note: When chaining ViewResolvers, a UrlBasedViewResolver always needs
 * to be last, as it will attempt to resolve any view name, no matter whether
 * the underlying resource actually exists.
 *
 * @author Juergen Hoeller
 * @since 13.12.2003
 * @see #setViewClass
 * @see #setPrefix
 * @see #setSuffix
 * @see #setRequestContextAttribute
 * @see #REDIRECT_URL_PREFIX
 * @see AbstractUrlBasedView
 * @see InternalResourceView
 * @see org.springframework.web.servlet.view.velocity.VelocityView
 * @see org.springframework.web.servlet.view.freemarker.FreeMarkerView
 */
public class UrlBasedViewResolver extends AbstractCachingViewResolver {

	public static final String REDIRECT_URL_PREFIX = "redirect:";


	private Class viewClass;

	private String prefix = "";

	private String suffix = "";

	private String contentType;

	private String requestContextAttribute;

	private boolean redirectContextRelative = true;

	private boolean redirectHttp10Compatible = true;


	/**
	 * Set the view class that should be used to create views.
	 * @param viewClass class that is assignable to the required view class
	 * (by default, AbstractUrlBasedView)
	 * @see AbstractUrlBasedView
	 */
	public void setViewClass(Class viewClass) {
		if (viewClass == null || !requiredViewClass().isAssignableFrom(viewClass)) {
			throw new IllegalArgumentException(
			    "Given view class [" + (viewClass != null ? viewClass.getName() : null) +
					"] is not of type [" + requiredViewClass().getName() + "]");
		}
		this.viewClass = viewClass;
	}

	/**
	 * Return the required type of view for this resolver.
	 * This implementation returns AbstractUrlBasedView.
	 * @see AbstractUrlBasedView
	 */
	protected Class requiredViewClass() {
		return AbstractUrlBasedView.class;
	}

	/**
	 * Set the prefix that gets applied to view names when building a URL.
	 * @param prefix view name prefix
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Set the suffix that gets applied to view names when building a URL.
	 * @param suffix view name suffix
	 */
	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	/**
	 * Set the content type for all views.
	 * May be ignored by view classes if the view itself is assumed
	 * to set the content type, e.g. in case of JSPs.
	 * @param contentType the content type
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Set the name of the RequestContext attribute for all views.
	 * @param requestContextAttribute name of the RequestContext attribute
	 * @see AbstractView#setRequestContextAttribute
	 */
	public void setRequestContextAttribute(String requestContextAttribute) {
		this.requestContextAttribute = requestContextAttribute;
	}

	/**
	 * Set whether to interpret a given redirect URL that starts with a
	 * slash ("/") as relative to the current ServletContext, i.e. as
	 * relative to the web application root.
	 * <p>Default is true: A redirect URL that starts with a slash will be
	 * interpreted as relative to the web application root, i.e. the context
	 * path will be prepended to the URL.
	 * <p><b>Redirect URLs can be specified via the "redirect:" prefix.</b>
	 * E.g.: "redirect:myAction.do"
	 * @see RedirectView#setContextRelative
	 * @see #REDIRECT_URL_PREFIX
	 */
	public void setRedirectContextRelative(boolean redirectContextRelative) {
		this.redirectContextRelative = redirectContextRelative;
	}

	/**
	 * Set whether redirects should stay compatible with HTTP 1.0 clients.
	 * <p>In the default implementation, this will enforce HTTP status code 302
	 * in any case, i.e. delegate to <code>HttpServletResponse.sendRedirect</code>.
	 * Turning this off will send HTTP status code 303, which is the correct
	 * code for HTTP 1.1 clients, but not understood by HTTP 1.0 clients.
	 * <p>Many HTTP 1.1 clients treat 302 just like 303, not making any
	 * difference. However, some clients depend on 303 when redirecting
	 * after a POST request; turn this flag off in such a scenario.
	 * <p><b>Redirect URLs can be specified via the "redirect:" prefix.</b>
	 * E.g.: "redirect:myAction.do"
	 * @see RedirectView#setHttp10Compatible
	 * @see #REDIRECT_URL_PREFIX
	 */
	public void setRedirectHttp10Compatible(boolean redirectHttp10Compatible) {
		this.redirectHttp10Compatible = redirectHttp10Compatible;
	}

	protected void initApplicationContext() {
		super.initApplicationContext();
		if (this.viewClass == null) {
			throw new IllegalArgumentException("viewClass is required");
		}
	}


	/**
	 * This implementation returns just the view name,
	 * as this ViewResolver doesn't support localized resolution.
	 */
	protected String getCacheKey(String viewName, Locale locale) {
		return viewName;
	}

	/**
	 * Overridden to implement check for "redirect:" prefix.
	 * <p>Not possible in loadView, as overridden loadView versions in
	 * subclasses might rely on the superclass always creating instances
	 * of the required view class.
	 * @see #loadView
	 * @see #requiredViewClass
	 */
	protected View createView(String viewName, Locale locale) throws Exception {
		// check for special "redirect:" prefix
		if (viewName.startsWith(REDIRECT_URL_PREFIX)) {
			String redirectUrl = viewName.substring(REDIRECT_URL_PREFIX.length());
			return new RedirectView(
			    redirectUrl, this.redirectContextRelative, this.redirectHttp10Compatible);
		}
		// else fall back to superclass implementation: calling loadView
		return super.createView(viewName, locale);
	}

	/**
	 * Creates a new instance of the specified view class and configures it.
	 * Does <i>not</i> perform any lookup for pre-defined View instances.
	 */
	protected View loadView(String viewName, Locale locale) throws BeansException {
		AbstractUrlBasedView view = (AbstractUrlBasedView) BeanUtils.instantiateClass(this.viewClass);
		view.setBeanName(viewName);
		view.setUrl(this.prefix + viewName + this.suffix);
		if (this.contentType != null) {
			view.setContentType(this.contentType);
		}
		view.setRequestContextAttribute(this.requestContextAttribute);
		return view;
	}

}
