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

package org.springframework.web.servlet.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * Convenient superclass for view resolvers.
 * Caches views once resolved: This means that view resolution won't be a
 * performance problem, no matter how costly initial view retrieval is.
 *
 * <p>View retrieval is deferred to subclasses via the <code>loadView</code>
 * template method.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #loadView
 */
public abstract class AbstractCachingViewResolver extends WebApplicationObjectSupport implements ViewResolver {

	/** Whether we should cache views, once resolved */
	private boolean cache = true;

	/** View name --> View instance */
	private final Map viewCache = Collections.synchronizedMap(new HashMap());


	/**
	 * Enable or disable caching. Disable this only for debugging and development.
	 * Default is for caching to be enabled.
	 * <p><b>Warning: Disabling caching can severely impact performance.</b>
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
		if (!isCache()) {
			logger.warn("View caching is SWITCHED OFF -- DEVELOPMENT SETTING ONLY: This can severely impair performance");
			return createView(viewName, locale);
		}
		else {
			String cacheKey = getCacheKey(viewName, locale);
			// No synchronization, as we can live with occasional double caching.
			View view = (View) this.viewCache.get(cacheKey);
			if (view == null) {
				// Ask the subclass to create the View object.
				view = createView(viewName, locale);
				this.viewCache.put(cacheKey, view);
				if (logger.isDebugEnabled()) {
					logger.debug("Cached view '" + cacheKey + "'");
				}
			}
			return view;
		}
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
	 * Provides functionality to clear the cache for a certain view.
	 * This can be handy in case developer are able to modify views
	 * (e.g. Velocity templates) at runtime after which you'd need to
	 * clear the cache for the specified view.
	 * @param viewName the view name for which the cached view object
	 * (if any) needs to be removed
	 * @param locale the locale for which the view object should be removed
	 */
	public void removeFromCache(String viewName, Locale locale) {
		if (!this.cache) {
			logger.warn("View caching is SWITCHED OFF -- removal not necessary");			
		}
		else {
			String cacheKey = getCacheKey(viewName, locale);
			if (this.viewCache.remove(cacheKey) == null) {
				// Some debug output might be useful...
				if (logger.isDebugEnabled()) {
					logger.debug("No cached instance for view '" + cacheKey + "' was found");
				}
			} 
			else {
				if (logger.isDebugEnabled()) {
					logger.debug("Cache for view " + cacheKey + " has been cleared");
				}
			}
		}
	}

	/**
	 * Clear the entire view cache, removing all cached view objects.
	 * Subsequent resolve calls will lead to recreation of demanded view objects.
	 */
	public void clearCache() {
		logger.debug("Clearing entire view cache");
		synchronized (this.viewCache) {
			this.viewCache.clear();
		}
	}


	/**
	 * Create the actual View object.
	 * Default implementation delegates to <code>loadView</code>.
	 * <p>Can be overridden to resolve certain view names in a special fashion,
	 * before delegating to the actual <code>loadView</code> implementation
	 * provided by the subclass.
	 * @param viewName the name of the view to retrieve
	 * @param locale the Locale to retrieve the view for
	 * @return the View instance, or <code>null</code> if not found
	 * (optional, to allow for ViewResolver chaining)
	 * @throws Exception if the view couldn't be resolved
	 * @see #loadView
	 */
	protected View createView(String viewName, Locale locale) throws Exception {
		return loadView(viewName, locale);
	}

	/**
	 * Subclasses must implement this method. There need be no concern
	 * for efficiency, as this class will cache views.
	 * <p>Not all subclasses may support internationalization:
	 * A subclass that doesn't can simply ignore the locale parameter.
	 * <p><b>NOTE:</b> As of Spring 1.2, subclasses are supposed to
	 * fully initialize the View objects before returning them,
	 * also applying bean container initialization callbacks.
	 * @param viewName the name of the view to retrieve
	 * @param locale the Locale to retrieve the view for
	 * @return the View instance, or <code>null</code> if not found
	 * (optional, to allow for ViewResolver chaining)
	 * @throws Exception if the view couldn't be resolved
	 * @see #resolveViewName
	 */
	protected abstract View loadView(String viewName, Locale locale) throws Exception;

}
