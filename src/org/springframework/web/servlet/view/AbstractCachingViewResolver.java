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

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * Convenient superclass for view resolvers.
 * Caches views once resolved: This means that view resolution won't be a
 * performance problem, no matter how costly initial view retrieval is.
 *
 * <p>View retrieval is deferred to subclasses via the loadView template method.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #loadView
 */
public abstract class AbstractCachingViewResolver extends WebApplicationObjectSupport implements ViewResolver {

	/** View name --> View instance */
	private final Map viewMap = Collections.synchronizedMap(new HashMap());

	/** Whether we should cache views, once resolved */
	private boolean cache = true;

	/**
	 * Enable respectively disable caching. Disable this only for debugging
	 * and development. Default is for caching to be enabled.
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
		View view = null;
		if (!this.cache) {
			logger.warn("View caching is SWITCHED OFF -- DEVELOPMENT SETTING ONLY: This can severely impair performance");
			view = loadAndConfigureView(viewName, locale);
		}
		else {
			String cacheKey = getCacheKey(viewName, locale);
			view = (View) this.viewMap.get(cacheKey);
			if (view == null) {
				// ask the subclass to load the View
				view = loadAndConfigureView(viewName, locale);
				this.viewMap.put(cacheKey, view);
				logger.info("Cached view '" + cacheKey + "'");
			}
		}
		return view;
	}

	/**
	 * Load and configure the given View. Only invoked once per View.
	 * Delegates to the loadView template method for actual loading.
	 * <p>Sets the ApplicationContext on the View if necessary.
	 * @see #loadView
	 */
	private View loadAndConfigureView(String viewName, Locale locale) throws Exception {
		View view = loadView(viewName, locale);
		if (view instanceof ApplicationContextAware) {
			((ApplicationContextAware) view).setApplicationContext(getApplicationContext());
		}
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
	 * A subclass that doesn't can simply ignore the locale parameter.
	 * @param viewName the name of the view to retrieve
	 * @param locale the Locale to retrieve the view for
	 * @return the View instance
	 * @throws Exception if the view couldn't be resolved
	 */
	protected abstract View loadView(String viewName, Locale locale) throws Exception;

}
