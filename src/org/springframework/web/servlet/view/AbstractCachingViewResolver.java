/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.view;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * Convenient superclass for view resolvers. Caches views once resolved.
 * This means that view resolution won't be a performance problem,
 * no matter how costly initial view retrieval is.
 * View retrieval is deferred to subclasses.
 * @author Rod Johnson
 */
public abstract class AbstractCachingViewResolver extends WebApplicationObjectSupport implements ViewResolver {

	/** View name --> View instance */
	private Map viewMap = new HashMap();

	/** Whether we should cache views, once resolved */
	private boolean cache = true;

	/**
	 * Enable caching. Disable this only for debugging and development.
	 * Default is for caching to be enabled.
	 * <p><b>Warning: Disabling caching severely impacts performance.</b>
	 * Tests indicate that turning caching off reduces performance by at least 20%.
	 * Increased object churn probably eventually makes the problem even worse.
	 */
	public final void setCache(boolean cache) {
		this.cache = cache;
	}

	/**
	 * Return if caching is enabled.
	 */
	public final boolean isCache() {
		return cache;
	}

	public final View resolveViewName(String viewName, Locale locale) throws ServletException {
		View view = null;
		if (!this.cache) {
			logger.warn("View caching is SWITCHED OFF -- DEVELOPMENT SETTING ONLY: This will severely impair performance");
			view = loadAndConfigureView(viewName, locale);
		}
		else {
			// We're caching - don't really need synchronization
			view = (View) this.viewMap.get(getCacheKey(viewName, locale));
			if (view == null) {
				// Ask the subclass to load the View
				view = loadAndConfigureView(viewName, locale);
			}
		}
		return view;
	}

	/**
	 * Configure the given View. Only invoked once per View.
	 * Configuration means giving the View its name, and 
	 * setting the ApplicationContext on the View if necessary.
	 */
	private View loadAndConfigureView(String viewName, Locale locale) throws ServletException {
		// Ask the subclass to load the view
		View view = loadView(viewName, locale);
		if (view == null)
			throw new ServletException("Cannot resolve view name '" + viewName + "'");
			
		// Configure view
		view.setName(viewName);

		// Give the view access to the ApplicationContext if it needs it
		if (view instanceof ApplicationContextAware) {
			try {
				((ApplicationContextAware) view).setApplicationContext(getApplicationContext());
			}
			catch (BeansException ex) {
				throw new ServletException("Error initializing View '" + viewName + "': " + ex.getMessage(), ex);
			}

			String cacheKey = getCacheKey(viewName, locale);
			logger.info("Cached view '" + cacheKey + "'");
			this.viewMap.put(cacheKey, view);
		}

		return view;
	}

	/**
	 * Return the cache key for the given viewName and the given locale.
	 * Needs to regard the locale, as a different locale can lead to a different view!
	 */
	private String getCacheKey(String viewName, Locale locale) {
		return viewName + "_" + locale;
	}

	/**
	 * Subclasses must implement this method. There need be no concern for efficiency,
	 * as this class will cache views. Not all subclasses may support internationalization:
	 * A subclass that doesn't can ignore the locale parameter.
	 * @param viewName name of the view to retrieve
	 * @param locale Locale to retrieve the view for
	 * @throws ServletException if there is an error trying to resolve the view
	 * @return the View if it can be resolved, or null
	 */
	protected abstract View loadView(String viewName, Locale locale) throws ServletException;

}
