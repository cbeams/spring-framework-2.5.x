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

package org.springframework.web.servlet.view.velocity;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.tools.VelocityFormatter;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.NumberTool;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.AbstractTemplateView;

/**
 * View using the Velocity template engine.
 * Based on code in the VelocityServlet shipped with Velocity.
 *
 * <p>Exposes the following JavaBean properties:
 * <ul>
 * <li><b>url</b>: the location of the Velocity template to be wrapped,
 * relative to the Velocity resource loader path (see VelocityConfigurer).
 * <li><b>encoding</b> (optional, default is determined by Velocity configuration):
 * the encoding of the Velocity template file
 * <li><b>velocityFormatterAttribute</b> (optional, default=null): the name of
 * the VelocityFormatter helper object to expose in the Velocity context of this
 * view, or null if not needed. VelocityFormatter is part of standard Velocity.
 * <li><b>dateToolAttribute</b> (optional, default=null): the name of the
 * DateTool helper object to expose in the Velocity context of this view,
 * or null if not needed. DateTool is part of Velocity Tools 1.0.
 * <li><b>numberToolAttribute</b> (optional, default=null): the name of the
 * NumberTool helper object to expose in the Velocity context of this view,
 * or null if not needed. NumberTool is part of Velocity Tools 1.1.
 * <li><b>cacheTemplate</b> (optional, default=false): whether or not the Velocity
 * template should be cached. It should normally be true in production, but setting
 * this to false enables us to modify Velocity templates without restarting the
 * application (similar to JSPs). Note that this is a minor optimization only,
 * as Velocity itself caches templates in a modification-aware fashion.
 * </ul>
 *
 * <p>Depends on a VelocityConfig object such as VelocityConfigurer being
 * accessible in the current web application context, with any bean name.
 * Alternatively, you can set the VelocityEngine object as bean property.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see VelocityConfig
 * @see VelocityConfigurer
 * @see #setUrl
 * @see #setExposeSpringMacroHelpers
 * @see #setEncoding
 * @see #setVelocityEngine
 * @see VelocityConfig
 * @see VelocityConfigurer
 */
public class VelocityView extends AbstractTemplateView {

	private String encoding = null;

	private String velocityFormatterAttribute;

	private String dateToolAttribute;

	private String numberToolAttribute;
	
	private boolean cacheTemplate;

	private VelocityEngine velocityEngine;

	private Template template;


	/**
	 * Set the encoding of the Velocity template file. Default is determined
	 * by the VelocityEngine: "ISO-8859-1" if not specified otherwise.
	 * <p>Specify the encoding in the VelocityEngine rather than per template
	 * if all your templates share a common encoding.
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Return the encoding for the Velocity template.
	 */
	protected String getEncoding() {
		return encoding;
	}

	/**
	 * Set the name of the VelocityFormatter helper object to expose in the
	 * Velocity context of this view, or null if not needed.
	 * VelocityFormatter is part of the standard Velocity distribution.
	 * @see org.apache.velocity.app.tools.VelocityFormatter
	 */
	public void setVelocityFormatterAttribute(String velocityFormatterAttribute) {
		this.velocityFormatterAttribute = velocityFormatterAttribute;
	}

	/**
	 * Set the name of the DateTool helper object to expose in the Velocity context
	 * of this view, or null if not needed. DateTool is part of Velocity Tools 1.0.
	 * @see org.apache.velocity.tools.generic.DateTool
	 */
	public void setDateToolAttribute(String dateToolAttribute) {
		this.dateToolAttribute = dateToolAttribute;
	}

	/**
	 * Set the name of the NumberTool helper object to expose in the Velocity context
	 * of this view, or null if not needed. NumberTool is part of Velocity Tools 1.1.
	 * @see org.apache.velocity.tools.generic.NumberTool
	 */
	public void setNumberToolAttribute(String numberToolAttribute) {
		this.numberToolAttribute = numberToolAttribute;
	}
	
    /**
	 * Set whether the Velocity template should be cached. Default is false.
	 * It should normally be true in production, but setting this to false enables us to
	 * modify Velocity templates without restarting the application (similar to JSPs).
	 * <p>Note that this is a minor optimization only, as Velocity itself caches
	 * templates in a modification-aware fashion.
	 */
	public void setCacheTemplate(boolean cacheTemplate) {
		this.cacheTemplate = cacheTemplate;
	}

	/**
	 * Set the VelocityEngine to be used by this view.
	 * If this is not set, the default lookup will occur: A single VelocityConfig
	 * is expected in the current web application context, with any bean name.
	 * @see VelocityConfig
	 */
	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}

	/**
	 * Return the VelocityEngine used by this view.
	 */
	protected VelocityEngine getVelocityEngine() {
		return velocityEngine;
	}


	/**
 	 * Invoked on startup. Looks for a single VelocityConfig bean to
 	 * find the relevant VelocityEngine for this factory.
 	 */
	protected void initApplicationContext() throws BeansException {
		super.initApplicationContext();

		if (this.velocityEngine == null) {
			try {
				VelocityConfig velocityConfig = (VelocityConfig)
						BeanFactoryUtils.beanOfTypeIncludingAncestors(
								getApplicationContext(), VelocityConfig.class, true, true);
				this.velocityEngine = velocityConfig.getVelocityEngine();
			}
			catch (NoSuchBeanDefinitionException ex) {
				throw new ApplicationContextException(
						"Must define a single VelocityConfig bean in this web application context " +
						"(may be inherited): VelocityConfigurer is the usual implementation. " +
						"This bean may be given any name.", ex);
			}
		}

		try {
			// check that we can get the template, even if we might subsequently get it again
			this.template = getTemplate();
		}
		catch (ResourceNotFoundException ex) {
			throw new ApplicationContextException("Cannot find Velocity template for URL [" + getUrl() +
				"]: Did you specify the correct resource loader path?", ex);
		}
		catch (Exception ex) {
			throw new ApplicationContextException("Cannot load Velocity template for URL [" + getUrl() + "]", ex);
		}
	}

	/**
	 * Process the model map by merging it with the Velocity template. Output is directed
	 * to the response. This method can be overridden if custom behavior is needed.
	 */
	protected void renderMergedTemplateModel(
			Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		// We already hold a reference to the template, but we might want to load it
		// if not caching. As Velocity itself caches templates, so our ability to
		// cache templates in this class is a minor optimization only.
		Template template = this.template;
		if (!this.cacheTemplate) {
			template = getTemplate();
		}

		response.setContentType(getContentType());

		exposeHelpers(model, request);

		// create context from model
		Context velocityContext = new VelocityContext(model);
		exposeHelpers(velocityContext, request);

		if (this.velocityFormatterAttribute != null) {
			velocityContext.put(this.velocityFormatterAttribute, new VelocityFormatter(velocityContext));
		}

		if (this.dateToolAttribute != null || this.numberToolAttribute != null) {
			Locale locale = RequestContextUtils.getLocale(request);
			if (this.dateToolAttribute != null) {
				velocityContext.put(this.dateToolAttribute, new LocaleAwareDateTool(locale));
			}
			if (this.numberToolAttribute != null) {
				velocityContext.put(this.numberToolAttribute, new LocaleAwareNumberTool(locale));
			}
		}
		
		mergeTemplate(template, velocityContext, response);
		if (logger.isDebugEnabled()) {
			logger.debug("Merged with Velocity template '" + getUrl() + "' in VelocityView '" + getBeanName() + "'");
		}
	}

	/**
	 * Retrieve the Velocity template.
	 * @return the Velocity template to process
	 * @throws Exception if thrown by Velocity
	 */
	protected Template getTemplate() throws Exception {
		return (this.encoding != null ? this.velocityEngine.getTemplate(getUrl(), this.encoding) :
				this.velocityEngine.getTemplate(getUrl()));
	}

	/**
	 * Expose helpers unique to each rendering operation. This is necessary so that
	 * different rendering operations can't overwrite each other's formats etc.
	 * <p>Called by renderMergedTemplateModel. The default implementation is empty.
	 * This method can be overridden to add custom helpers to the model.
	 * @param model the model that will be passed to the template at merge time
	 * @param request current HTTP request
	 * @throws Exception if there's a fatal error while we're adding model attributes
	 * @see #renderMergedTemplateModel
	 */
	protected void exposeHelpers(Map model, HttpServletRequest request) throws Exception {
	}

	/**
	 * Expose helpers unique to each rendering operation. This is necessary so that
	 * different rendering operations can't overwrite each other's formats etc.
	 * <p>Called by renderMergedTemplateModel. The default implementations is empty.
	 * This method can be overridden to add custom helpers to the Velocity context.
	 * @param velocityContext Velocity context that will be passed to the template at merge time
	 * @param request current HTTP request
	 * @throws Exception if there's a fatal error while we're adding model attributes
	 * @deprecated in favor of exposeHelpers(Map, HttpServletRequest)
	 * @see #exposeHelpers(Map, HttpServletRequest)
	 */
	protected void exposeHelpers(Context velocityContext, HttpServletRequest request) throws Exception {
	}

	/**
	 * Merge the template with the context.
	 * Can be overridden to customize the behavior.
	 * @param template the template to merge
	 * @param context the Velocity context
	 * @param response servlet response (use this to get the OutputStream or Writer)
	 * @see org.apache.velocity.Template#merge
	 */
	protected void mergeTemplate(Template template, Context context, HttpServletResponse response)
			throws Exception {
		template.merge(context, response.getWriter());
	}


	/**
	 * Subclass of DateTool from Velocity tools,
	 * using the RequestContext Locale instead of the default Locale.
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getLocale
	 */
	private static class LocaleAwareDateTool extends DateTool {

		private Locale locale;

		private LocaleAwareDateTool(Locale locale) {
			this.locale = locale;
		}

		public Locale getLocale() {
			return this.locale;
		}
	}


	/**
	 * Subclass of NumberTool from Velocity tools,
	 * using the RequestContext Locale instead of the default Locale.
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getLocale
	 */
	private static class LocaleAwareNumberTool extends NumberTool {

		private Locale locale;

		private LocaleAwareNumberTool(Locale locale) {
			this.locale = locale;
		}

		public Locale getLocale() {
			return this.locale;
		}
	}

}
