/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
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
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.apache.velocity.util.SimplePool;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContextException;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

/**
 * View using the Velocity template engine.
 * Based on code in the VelocityServlet shipped with Velocity.
 *
 * <p>Exposes the following JavaBean properties:
 * <ul>
 * <li><b>url</b>: the location of the Velocity template to be wrapped,
 * relative to the Velocity resource loader path (see VelocityConfigurer).
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
 * <li><b>writerPoolSize</b> (optional, default=40): number of Velocity writers
 * (refer to Velocity documentation to see exactly what this means)
 * </ul>
 *
 * <p>Depends on a VelocityConfig object such as VelocityConfigurer
 * being accessible in the current web application context.

 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: VelocityView.java,v 1.22 2004-03-04 00:10:31 davison Exp $
 * @see VelocityConfig
 * @see VelocityConfigurer
 */
public class VelocityView extends AbstractUrlBasedView {

	public static final int DEFAULT_WRITER_POOL_SIZE = 40;

	public static final int OUTPUT_BUFFER_SIZE = 4096;


	private String velocityFormatterAttribute;

	private String dateToolAttribute;

	private String numberToolAttribute;

	private boolean cacheTemplate;

	/** Cache of writers */
	private SimplePool writerPool;

	/** The encoding to use when generating output */
	private String encoding = null;

	/** Instance of the VelocityEngine */
	private VelocityEngine velocityEngine;

	/** Velocity Template */
	private Template velocityTemplate;


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
	 * Set the number of Velocity writers. Default is 40.
	 * Refer to Velocity documentation to see exactly what this means.
	 */
	public void setWriterPoolSize(int writerPoolSize) {
		this.writerPool = new SimplePool(writerPoolSize);
	}


	/**
 	 * Invoked on startup. Looks for a single VelocityConfig bean to
 	 * find the relevant VelocityEngine for this factory.
 	 */
	protected void initApplicationContext() throws ApplicationContextException {
		super.initApplicationContext();

		try {
			VelocityConfig vconfig = (VelocityConfig)
					BeanFactoryUtils.beanOfTypeIncludingAncestors(getWebApplicationContext(),
					                                              VelocityConfig.class, true, true);
			this.velocityEngine = vconfig.getVelocityEngine();
		}
		catch (BeanDefinitionStoreException ex) {
			throw new ApplicationContextException("Must define a single VelocityConfig bean in this web application " +
			                                      "context (may be inherited): VelocityConfigurer is the usual implementation. " +
			                                      "This bean may be given any name.", ex);
		}

		if (this.writerPool == null) {
			this.writerPool = new SimplePool(DEFAULT_WRITER_POOL_SIZE);
		}

		this.encoding = (String) this.velocityEngine.getProperty(VelocityEngine.OUTPUT_ENCODING);
		if (this.encoding == null) {
			this.encoding = VelocityEngine.ENCODING_DEFAULT;
		}

		// check that we can get the template, even if we might subsequently get it again
		loadTemplate();
	}

	/**
	 * Load the Velocity template to back this view.
	 */
	private void loadTemplate() throws ApplicationContextException {
		try {
			this.velocityTemplate = this.velocityEngine.getTemplate(getUrl());
		}
		catch (ResourceNotFoundException ex) {
			handleException("Can't load Velocity template '" + getUrl() +
											"': is it available in the template directory?", ex);
		}
		catch (ParseErrorException ex) {
			handleException("Error parsing Velocity template '" + getUrl() + "'", ex);
		}
		catch (Exception ex) {
			handleException("Unexpected error getting Velocity template '" + getUrl() + "'", ex);
		}
	}

	/**
	 * Re-throw the given exception as ApplicationContextException with proper message.
	 */
	private void handleException(String message, Exception ex) throws ApplicationContextException {
		String actualMessage = "Velocity resource loader is '" +
				this.velocityEngine.getProperty(VelocityEngine.RESOURCE_LOADER) + "': " + message;
		throw new ApplicationContextException(actualMessage, ex);
	}

	protected void renderMergedOutputModel(Map model, HttpServletRequest request,
	                                       HttpServletResponse response) throws Exception {

		// We already hold a reference to the template, but we might want to load it
		// if not caching. As Velocity itself caches templates, so our ability to
		// cache templates in this class is a minor optimization only.
		if (!this.cacheTemplate) {
			loadTemplate();
		}

		response.setContentType(getContentType());
		Context velocityContext = new VelocityContext();
		VelocityEngineUtils.exposeModelAsContextAttributes(model, velocityContext);
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

		mergeTemplate(this.velocityTemplate, velocityContext, response);
		if (logger.isDebugEnabled()) {
			logger.debug("Merged with Velocity template '" + getUrl() + "' in VelocityView '" + getBeanName() + "'");
		}
	}

	/**
	 * Expose helpers unique to each rendering operation. This is necessary so that
	 * different rendering operations can't overwrite each other's formats etc.
	 * <p>Called by renderMergedOutputModel. The default implementations is empty.
	 * This method can be overridden to add custom helpers to the Velocity context.
	 * @param velocityContext Velocity context that will be passed to the template at merge time
	 * @param request current HTTP request
	 * @throws Exception if there's a fatal error while we're adding information to the context
	 * @see #renderMergedOutputModel
	 */
	protected void exposeHelpers(Context velocityContext, HttpServletRequest request) throws Exception {
	}

	/**
	 * Merge the template with the context.
	 * Can be overridden if custom behaviour needs to be defined.
	 * @param template template object returned by the handleRequest() method
	 * @param context context the Velocity context
	 * @param response servlet reponse (use this to get the OutputStream or Writer)
	 */
	protected void mergeTemplate(Template template, Context context, HttpServletResponse response) throws Exception {
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
