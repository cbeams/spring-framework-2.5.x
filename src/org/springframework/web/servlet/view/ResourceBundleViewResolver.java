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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceEditor;
import org.springframework.web.servlet.View;

/**
 * Implementation of ViewResolver that uses bean definitions in a
 * ResourceBundle, specified by the bundle basename. The bundle is
 * typically defined in a properties file, located in the class path.
 * The default bundle basename is "views".
 *
 * <p>This ViewResolver supports localized view definitions,
 * using the default support of java.util.PropertyResourceBundle.
 *
 * <p>Note: This ViewResolver implements the Ordered interface to allow for
 * flexible participation in ViewResolver chaining. For example, some special
 * views could be defined via this ViewResolver (giving it 0 as "order" value),
 * while all remaining views could be resolved by a UrlBasedViewResolver.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see java.util.ResourceBundle#getBundle
 * @see java.util.PropertyResourceBundle
 * @see UrlBasedViewResolver
 */
public class ResourceBundleViewResolver extends AbstractCachingViewResolver implements Ordered, DisposableBean {

	/** Default if no other basename is supplied */
	public final static String DEFAULT_BASENAME = "views";

	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered

	private String[] basenames = new String[] {DEFAULT_BASENAME};

	private String defaultParentView;

	/** Locale -> BeanFactory */
	private final Map cachedFactories = new HashMap();


	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	/**
	 * Set the basename, as defined in the java.util.ResourceBundle documentation.
	 * ResourceBundle supports different suffixes. For example, a base name of
	 * "views" might map to ResourceBundle files "views", "views_en_au" and "views_de".
	 * <p>The default is "views".
	 * @param basename the ResourceBundle basename
	 * @see #setBasenames
	 * @see java.util.ResourceBundle
	 */
	public void setBasename(String basename) {
		setBasenames(new String[] {basename});
	}

	/**
	 * Set multiple ResourceBundle basenames.
	 * @param basenames multiple ResourceBundle basenames
	 * @see #setBasename
	 */
	public void setBasenames(String[] basenames) {
		this.basenames = basenames;
	}

	/**
	 * Set the default parent for views defined in the ResourceBundle.
	 * This avoids repeated "yyy1.parent=xxx", "yyy2.parent=xxx" definitions
	 * in the bundle, especially if all defined views share the same parent.
	 * <p>The parent will typically define the view class and common attributes.
	 * Concrete views might simply consist of an URL definition then:
	 * a la "yyy1.url=/my.jsp", "yyy2.url=/your.jsp".
	 * <p>View definitions that define their own parent or carry their own
	 * class can still override this. Strictly speaking, the rule that a
	 * default parent setting does not apply to a bean definition that
	 * carries a class is there for backwards compatiblity reasons.
	 * It still matches the typical use case.
	 * @param defaultParentView the default parent view
	 */
	public void setDefaultParentView(String defaultParentView) {
		this.defaultParentView = defaultParentView;
	}


	protected View loadView(String viewName, Locale locale) throws MissingResourceException, BeansException {
		try {
			return (View) initFactory(locale).getBean(viewName, View.class);
		}
		catch (NoSuchBeanDefinitionException ex) {
			// to allow for ViewResolver chaining
			return null;
		}
	}

	/**
	 * Initialize the BeanFactory from the ResourceBundle, for the given locale.
	 * Synchronized because of access by parallel threads.
	 */
	protected synchronized BeanFactory initFactory(Locale locale) throws MissingResourceException, BeansException {
		BeanFactory parsedBundle = isCache() ? (BeanFactory) this.cachedFactories.get(locale) : null;
		if (parsedBundle != null) {
			return parsedBundle;
		}

		DefaultListableBeanFactory factory = new DefaultListableBeanFactory(getApplicationContext());
		PropertiesBeanDefinitionReader reader = new PropertiesBeanDefinitionReader(factory);
		reader.setDefaultParentBean(this.defaultParentView);
		for (int i = 0; i < this.basenames.length; i++) {
			ResourceBundle bundle = ResourceBundle.getBundle(this.basenames[i], locale,
																											 Thread.currentThread().getContextClassLoader());
			reader.registerBeanDefinitions(bundle);
		}
		factory.registerCustomEditor(Resource.class, new ResourceEditor(getApplicationContext()));

		if (isCache()) {
			factory.preInstantiateSingletons();
			this.cachedFactories.put(locale, factory);
		}
		return factory;
	}

	public void destroy() throws BeansException {
		for (Iterator it = this.cachedFactories.values().iterator(); it.hasNext();) {
			ConfigurableBeanFactory factory = (ConfigurableBeanFactory) it.next();
			factory.destroySingletons();
		}
		this.cachedFactories.clear();
	}

}
