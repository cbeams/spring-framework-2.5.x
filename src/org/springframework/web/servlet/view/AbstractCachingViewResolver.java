/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.view;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * Convenient superclass for view resolvers. Caches views once resolved.
 * This means that view resolution won't be a performance problem,
 * no matter how costly initial view retrieval is.
 *
 * <p>View retrieval is deferred to subclasses via the loadView template method.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #loadView
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
	public void setCache(boolean cache) {
		this.cache = cache;
	}

	/**
	 * Return if caching is enabled.
	 */
	public boolean isCache() {
		return cache;
	}

	public View resolveViewName(String viewName, Locale locale) throws Exception {
		View view = null;
		if (!this.cache) {
			logger.warn("View caching is SWITCHED OFF -- DEVELOPMENT SETTING ONLY: This will severely impair performance");
			view = loadAndCacheView(viewName, locale);
		}
		else {
			// we're caching - don't really need synchronization
			view = (View) this.viewMap.get(getCacheKey(viewName, locale));
			if (view == null) {
				// ask the subclass to load the View
				view = loadAndCacheView(viewName, locale);
			}
		}
		return view;
	}

	/**
	 * Configure the given View. Only invoked once per View.
	 * Configuration means giving the View its name, and 
	 * setting the ApplicationContext on the View if necessary.
	 */
	private View loadAndCacheView(String viewName, Locale locale) throws Exception {
		View view = loadView(viewName, locale);
		if (view instanceof ApplicationContextAware) {
			((ApplicationContextAware) view).setApplicationContext(getApplicationContext());
		}
		String cacheKey = getCacheKey(viewName, locale);
		logger.info("Cached view '" + cacheKey + "'");
		this.viewMap.put(cacheKey, view);
		return view;
	}

	/**
	 * Return the cache key for the given viewName and the given locale.
	 * Needs to regard the locale in general, as a different locale can lead to a
	 * different view! Can be overridden in subclasses.
	 */
	protected String getCacheKey(String viewName, Locale locale) {
		return viewName + "_" + locale;
	}

	/**
	 * Subclasses must implement this method. There need be no concern for efficiency,
	 * as this class will cache views. Not all subclasses may support internationalization:
	 * A subclass that doesn't can ignore the locale parameter.
	 * @param viewName name of the view to retrieve
	 * @param locale Locale to retrieve the view for
	 * @return the View instance
	 * @throws Exception if the view couldn't be resolved
	 */
	protected abstract View loadView(String viewName, Locale locale) throws Exception;

}
