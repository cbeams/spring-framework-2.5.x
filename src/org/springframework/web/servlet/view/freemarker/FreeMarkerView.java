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
 * Alternatively, you can set the Freemarker Configuration object as bean property.
 *
 * <p>Note: Spring's FreeMarker support requires FreeMarker 2.3 or higher.
 *
 * @author Darren Davison
 * @author Juergen Hoeller
 * @since 3/3/2004
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
	public void setConfiguration(Configuration configration) {
		this.configuration = configration;
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

		if (this.configuration == null) {
			try {
				FreeMarkerConfig freemarkerConfig = (FreeMarkerConfig)
						BeanFactoryUtils.beanOfTypeIncludingAncestors(
								getApplicationContext(), FreeMarkerConfig.class, true, true);
				this.configuration = freemarkerConfig.getConfiguration();
			}
			catch (NoSuchBeanDefinitionException ex) {
				throw new ApplicationContextException(
						"Must define a single FreeMarkerConfig bean in this web application context " +
						"(may be inherited): FreeMarkerConfigurer is the usual implementation. " +
						"This bean may be given any name.", ex);
			}
		}

		try {
			// check that we can get the template, even if we might subsequently get it again
			getTemplate(this.configuration.getLocale());
		}
		catch (ParseException ex) {
		    throw new ApplicationContextException("Failed to parse FreeMarker template for URL [" + 
			        getUrl() + "]", ex);
		}
		catch (IOException ex) {
			throw new ApplicationContextException("Cannot load FreeMarker template for URL [" + 
			        getUrl() + "]", ex);
		}
	}


	/**
	 * Process the model map by merging it with the FreeMarker template. Output is
	 * directed to the response. This method can be overridden if custom behavior
	 * is needed.
	 */
	protected void renderMergedTemplateModel(
			Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
	   
		// grab the locale-specific version of the template
		Template template = getTemplate(RequestContextUtils.getLocale(request));
		if (logger.isDebugEnabled()) {
			logger.debug("Preparing to process FreeMarker template [" + template.getName() +
									 "] with model [" + model + "] ");
		}
		response.setContentType(getContentType());
		exposeHelpers(model, request);
		processTemplate(template, model, response);
	}

	/**
	 * Retrieve the FreeMarker template for the given locale.
	 * @param locale the current locale
	 * @return the FreeMarker template to process
	 * @throws IOException if the template file could not be retrieved
	 */
	protected Template getTemplate(Locale locale) throws IOException {
		return (this.encoding != null ? this.configuration.getTemplate(getUrl(), locale, this.encoding) :
				this.configuration.getTemplate(getUrl(), locale));
	}

	/**
	 * Expose helpers unique to each rendering operation. This is necessary so that
	 * different rendering operations can't overwrite each other's formats etc.
	 * <p>Called by renderMergedTemplateModel. The default implementations is empty.
	 * This method can be overridden to add custom helpers to the model.
	 * @param model The model that will be passed to the template at merge time
	 * @param request current HTTP request
	 * @throws Exception if there's a fatal error while we're adding information to the context
	 * @see #renderMergedTemplateModel
	 */
	protected void exposeHelpers(Map model, HttpServletRequest request) throws Exception {
	}

	/**
	 * Process the FreeMarker template to the servlet response.
	 * Can be overridden to customize the behavior.
	 * @param template the template to process
	 * @param model the model for the template
	 * @param response servlet response (use this to get the OutputStream or Writer)
	 * @see freemarker.template.Template#process(Object, java.io.Writer)
	 */
	protected void processTemplate(Template template, Map model, HttpServletResponse response)
			throws IOException, TemplateException {
		template.process(model, response.getWriter());
	}

}
