/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.view;

import java.util.Locale;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.support.ContextResourceEditor;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.View;

/**
 * Implementation of ViewResolver that uses bean definitions in an XML
 * file, specified by location (URL or relative path, according to the
 * ApplicationContext implementation).
 * The file will typically be located in the WEB-INF directory.
 *
 * <p>This ViewResolver does not support internationalization.
 * Consider ResourceBundleViewResolver if you need to apply
 * different view resources per locale.
 *
 * <p>Extends AbstractCachingViewResolver for decent performance.
 *
 * @author Juergen Hoeller
 * @since 18.06.2003
 * @see org.springframework.context.ApplicationContext#getResource
 * @see ResourceBundleViewResolver
 */
public class XmlViewResolver extends AbstractCachingViewResolver {

	/** Default if no other location is supplied */
	public final static String DEFAULT_LOCATION = "/WEB-INF/views.xml";

	private Resource location;

	private BeanFactory cachedFactory;

	/**
	 * Set the location of the XML file that defines the view beans.
	 * <p>The default is "/WEB-INF/views.xml".
	 * @param location the location of the XML file.
	 */
	public void setLocation(Resource location) {
		this.location = location;
	}

	/**
	 * Pre-initialize the factory from the XML file.
	 * Only effective if caching is enabled.
	 */
	protected void initApplicationContext() throws BeansException {
		if (isCache()) {
			initFactory();
		}
	}

	/**
	 * This implementation returns just the view name,
	 * as XmlViewResolver doesn't support localized resolution.
	 */
	protected String getCacheKey(String viewName, Locale locale) {
		return viewName;
	}

	protected View loadView(String viewName, Locale locale) throws BeansException {
		return (View) initFactory().getBean(viewName, View.class);
	}

	/**
	 * Initialize the BeanFactory from the XML file.
	 * Synchronized because of access by parallel threads.
	 * @throws BeansException in case of initialization errors
	 */
	protected synchronized BeanFactory initFactory() throws BeansException {
		if (this.cachedFactory != null) {
			return this.cachedFactory;
		}
		Resource actualLocation = this.location;
		if (actualLocation == null) {
			actualLocation = getApplicationContext().getResource(DEFAULT_LOCATION);
		}
		XmlBeanFactory xbf = new XmlBeanFactory(actualLocation, getApplicationContext());
		xbf.registerCustomEditor(Resource.class, new ContextResourceEditor(getApplicationContext()));
		xbf.preInstantiateSingletons();
		if (isCache()) {
			this.cachedFactory = xbf;
		}
		return xbf;
	}

}
