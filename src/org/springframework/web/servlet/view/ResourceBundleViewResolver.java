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
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.PropertiesBeanDefinitionReader;
import org.springframework.context.support.ContextResourceEditor;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.View;

/**
 * Implementation of ViewResolver that uses bean definitions in a
 * ResourceBundle, specified by the bundle basename. The bundle is
 * typically defined in a properties file, located in the classpath.
 *
 * <p>This ViewResolver supports internationalization,
 * using the default support of java.util.PropertyResourceBundle.
 *
 * <p>Extends AbstractCachingViewResolver for decent performance.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see java.util.ResourceBundle#getBundle
 * @see java.util.PropertyResourceBundle
 */
public class ResourceBundleViewResolver extends AbstractCachingViewResolver {

	/** Default if no other basename is supplied */
	public final static String DEFAULT_BASENAME = "views";

	private String basename = DEFAULT_BASENAME;

	private String defaultParentView;

	/** Locale -> BeanFactory */
	private Map cachedFactories = new HashMap();

	/**
	 * Set the basename, as defined in the java.util.ResourceBundle documentation.
	 * ResourceBundle supports different suffixes. For example, a base name of
	 * "views" might map to ResourceBundle files "views", "views_en_au" and "views_de".
	 * <p>The default is "views".
	 * @param basename the ResourceBundle base name
	 * @see java.util.ResourceBundle
	 */
	public void setBasename(String basename) {
		this.basename = basename;
	}

	/**
	 * Set the default parent for views defined in the ResourceBundle.
	 * This avoids repeated "yyy1.parent=xxx", "yyy2.parent=xxx" definitions
	 * in the bundle, especially if all defined views share the same parent.
	 * The parent will typically define the view class and common attributes.
	 * Concrete views might simply consist of an URL definition then:
	 * a la "yyy1.url=/my.jsp", "yyy2.url=/your.jsp".
	 * @param defaultParentView the default parent view
	 */
	public void setDefaultParentView(String defaultParentView) {
		this.defaultParentView = defaultParentView;
	}

	protected View loadView(String viewName, Locale locale) throws MissingResourceException, BeansException {
		return (View) initFactory(locale).getBean(viewName, View.class);
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
		ResourceBundle bundle = ResourceBundle.getBundle(this.basename, locale,
																										 Thread.currentThread().getContextClassLoader());
		DefaultListableBeanFactory lbf = new DefaultListableBeanFactory(getApplicationContext());
		PropertiesBeanDefinitionReader reader = new PropertiesBeanDefinitionReader(lbf);
		reader.setDefaultParentBean(this.defaultParentView);
		reader.registerBeanDefinitions(bundle);
		lbf.registerCustomEditor(Resource.class, new ContextResourceEditor(getApplicationContext()));
		lbf.preInstantiateSingletons();
		if (isCache()) {
			this.cachedFactories.put(locale, lbf);
		}
		return lbf;
	}

}
