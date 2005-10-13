/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.web.servlet.view.freemarker;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.AbstractTemplateView;

/**
 * View using the FreeMarker template engine.
 *
 * <p>Exposes the following JavaBean properties:
 * <ul>
 * <li><b>url</b>: the location of the FreeMarker template to be wrapped,
 * relative to the FreeMarker template context (directory).
 * <li><b>encoding</b> (optional, default is determined by FreeMarker configuration):
 * the encoding of the FreeMarker template file
 * </ul>
 *
 * <p>Depends on a single FreeMarkerConfig object such as FreeMarkerConfigurer
 * being accessible in the current web application context, with any bean name.
 * Alternatively, you can set the FreeMarker Configuration object as bean property.
 *
 * <p>Note: Spring's FreeMarker support requires FreeMarker 2.3 or higher.
 *
 * @author Darren Davison
 * @author Juergen Hoeller
 * @since 03.03.2004
 * @see #setUrl
 * @see #setExposeSpringMacroHelpers
 * @see #setEncoding
 * @see #setConfiguration
 * @see FreeMarkerConfig
 * @see FreeMarkerConfigurer
 */
public class FreeMarkerView extends AbstractTemplateView {

	private String encoding;

	private Configuration configuration;


	/**
	 * Set the encoding of the FreeMarker template file. Default is determined
	 * by the FreeMarker Configuration: "ISO-8859-1" if not specified otherwise.
	 * <p>Specify the encoding in the FreeMarker Configuration rather than per
	 * template if all your templates share a common encoding.
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Return the encoding for the FreeMarker template.
	 */
	protected String getEncoding() {
		return encoding;
	}

	/**
	 * Set the FreeMarker Configuration to be used by this view.
	 * If this is not set, the default lookup will occur: A single FreeMarkerConfig
	 * is expected in the current web application context, with any bean name.
	 * @see FreeMarkerConfig
	 */
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Return the FreeMarker configuration used by this view.
	 */
	protected Configuration getConfiguration() {
		return configuration;
	}


	/**
	 * Invoked on startup. Looks for a single FreeMarkerConfig bean to
	 * find the relevant Configuration for this factory.
	 * <p>Checks that the template for the default Locale can be found:
	 * FreeMarker will check non-Locale-specific templates if a
	 * locale-specific one is not found.
	 * @see freemarker.cache.TemplateCache#getTemplate
	 */
	protected void initApplicationContext() throws BeansException {
		super.initApplicationContext();

		if (getConfiguration() == null) {
			// No explicit Configuration instance: try to autodetect one.
			setConfiguration(autodetectConfiguration());
		}

		checkTemplate();
	}

	/**
	 * Autodetect a FreeMarker Configuration object via the ApplicationContext.
	 * Called if no explicit Configuration instance has been specified.
	 * @return the Configuration instance to use for FreeMarkerViews
	 * @throws BeansException if no Configuration instance could be found
	 * @see #getApplicationContext
	 * @see #setConfiguration
	 */
	protected Configuration autodetectConfiguration() throws BeansException {
		try {
			FreeMarkerConfig freemarkerConfig = (FreeMarkerConfig)
					BeanFactoryUtils.beanOfTypeIncludingAncestors(
							getApplicationContext(), FreeMarkerConfig.class, true, false);
			return freemarkerConfig.getConfiguration();
		}
		catch (NoSuchBeanDefinitionException ex) {
			throw new ApplicationContextException(
					"Must define a single FreeMarkerConfig bean in this web application context " +
					"(may be inherited): FreeMarkerConfigurer is the usual implementation. " +
					"This bean may be given any name.", ex);
		}
	}

	/**
	 * Check that the FreeMarker template used for this view exists and is valid.
	 * <p>Can be overridden to customize the behavior, for example in case of
	 * multiple templates to be rendered into a single view.
	 * @throws ApplicationContextException if the template cannot be found or is invalid
	 */
	protected void checkTemplate() throws ApplicationContextException {
		try {
			// Check that we can get the template, even if we might subsequently get it again.
			getTemplate(getConfiguration().getLocale());
		}
		catch (ParseException ex) {
			throw new ApplicationContextException(
					"Failed to parse FreeMarker template for URL [" +  getUrl() + "]", ex);
		}
		catch (IOException ex) {
			throw new ApplicationContextException(
					"Could not load FreeMarker template for URL [" + getUrl() + "]", ex);
		}
	}


	/**
	 * Process the model map by merging it with the FreeMarker template.
	 * Output is directed to the servlet response.
	 * <p>This method can be overridden if custom behavior is needed.
	 */
	protected void renderMergedTemplateModel(
			Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		response.setContentType(getContentType());
		exposeHelpers(model, request);
		doRender(model, request, response);
	}

	/**
	 * Expose helpers unique to each rendering operation. This is necessary so that
	 * different rendering operations can't overwrite each other's formats etc.
	 * <p>Called by <code>renderMergedTemplateModel</code>. The default implementation
	 * is empty. This method can be overridden to add custom helpers to the model.
	 * @param model The model that will be passed to the template at merge time
	 * @param request current HTTP request
	 * @throws Exception if there's a fatal error while we're adding information to the context
	 * @see #renderMergedTemplateModel
	 */
	protected void exposeHelpers(Map model, HttpServletRequest request) throws Exception {
	}

	/**
	 * Render the FreeMarker view to the given response, using the given model
	 * map which contains the complete template model to use.
	 * <p>The default implementation renders the template specified by the "url"
	 * bean property, retrieved via <code>getTemplate</code>. It delegates to the
	 * <code>processTemplate</code> method to merge the template instance with
	 * the given template model.
	 * <p>Can be overridden to customize the behavior, for example to render
	 * multiple templates into a single view.
	 * @param model the template model to use for rendering
	 * @param request current HTTP request
	 * @param response servlet response (use this to get the OutputStream or Writer)
	 * @throws IOException if the template file could not be retrieved
	 * @throws Exception if rendering failed
	 * @see #setUrl
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getLocale
	 * @see #getTemplate(java.util.Locale)
	 * @see #processTemplate
	 */
	protected void doRender(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Rendering FreeMarker template [" + getUrl() + "] in FreeMarkerView '" + getBeanName() + "'");
		}
		// Grab the locale-specific version of the template.
		Locale locale = RequestContextUtils.getLocale(request);
		processTemplate(getTemplate(locale), model, response);
	}

	/**
	 * Retrieve the FreeMarker template for the given locale,
	 * to be rendering by this view.
	 * <p>By default, the template specified by the "url" bean property
	 * will be retrieved.
	 * @param locale the current locale
	 * @return the FreeMarker template to render
	 * @throws IOException if the template file could not be retrieved
	 * @see #setUrl
	 * @see #getTemplate(String, java.util.Locale)
	 */
	protected Template getTemplate(Locale locale) throws IOException {
		return getTemplate(getUrl(), locale);
	}

	/**
	 * Retrieve the FreeMarker template specified by the given name,
	 * using the encoding specified by the "encoding" bean property.
	 * <p>Can be called by subclasses to retrieve a specific template,
	 * for example to render multiple templates into a single view.
	 * @param name the file name of the desired template
	 * @param locale the current locale
	 * @return the FreeMarker template
	 * @throws IOException if the template file could not be retrieved
	 */
	protected Template getTemplate(String name, Locale locale) throws IOException {
		return (getEncoding() != null ?
				getConfiguration().getTemplate(name, locale, getEncoding()) :
				getConfiguration().getTemplate(name, locale));
	}

	/**
	 * Process the FreeMarker template to the servlet response.
	 * <p>Can be overridden to customize the behavior.
	 * @param template the template to process
	 * @param model the model for the template
	 * @param response servlet response (use this to get the OutputStream or Writer)
	 * @throws IOException if the template file could not be retrieved
	 * @throws TemplateException if thrown by FreeMarker
	 * @see freemarker.template.Template#process(Object, java.io.Writer)
	 */
	protected void processTemplate(Template template, Map model, HttpServletResponse response)
			throws IOException, TemplateException {

		template.process(model, response.getWriter());
	}

}
