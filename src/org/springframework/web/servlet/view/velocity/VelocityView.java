/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.view.velocity;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.io.VelocityWriter;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.util.SimplePool;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContextException;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

/**
 * View using Velocity template engine.
 * Based on code in the VelocityServlet shipped with Velocity.
 *
 * <p>Exposes the following JavaBean properties:
 * <ul>
 * <li><b>url</b>: location of the Velocity template to be wrapped,
 * relative to the Velocity resource loader path (see VelocityConfigurer).
 * <li><b>dateToolAttribute</b> (optional, default=null): set the name of the
 * DateTool helper object to expose in the Velocity context of this view,
 * or null if not needed. DateTool is from Velocity Tools.
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
 * @version $Id: VelocityView.java,v 1.18 2004-02-04 17:34:32 jhoeller Exp $
 * @see VelocityConfig
 * @see VelocityConfigurer
 */
public class VelocityView extends AbstractUrlBasedView {

	public static final int DEFAULT_WRITER_POOL_SIZE = 40;

	public static final int OUTPUT_BUFFER_SIZE = 4096;


	private String dateToolAttribute;

	private boolean cacheTemplate;

	/** Cache of writers */
	private SimplePool writerPool;

	/** The encoding to use when generating outputing */
	private String encoding = null;

	/** Instance of the VelocityEngine */
	private VelocityEngine velocityEngine;

	/** Velocity Template */
	private Template velocityTemplate;


	/**
	 * Set the name of the DateHool helper object to expose in the Velocity context
	 * of this view, or null if not needed. DateTool is from Velocity Tools.
	 */
	public void setDateToolAttribute(String dateToolAttribute) {
		this.dateToolAttribute = dateToolAttribute;
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
		exposeModelAsContextAttributes(model, velocityContext);
		exposeHelpers(velocityContext, request);

		// expose DateTool?
		if (this.dateToolAttribute != null) {
			velocityContext.put(this.dateToolAttribute, new LocaleAwareDateTool(request));
		}

		mergeTemplate(this.velocityTemplate, velocityContext, response);
		if (logger.isDebugEnabled()) {
			logger.debug("Merged with Velocity template '" + getUrl() + "' in VelocityView '" + getName() + "'");
		}
	}

	/**
	 * Expose the models in the given map as Velocity context attributes.
	 * Names will be taken from the map.
	 * @param model Map of model data to expose
	 * @param velocityContext VelocityContext to add data to
	 */
	private void exposeModelAsContextAttributes(Map model, Context velocityContext) {
		if (model != null) {
			Iterator itr = model.keySet().iterator();
			while (itr.hasNext()) {
				String modelName = (String) itr.next();
				Object modelObject = model.get(modelName);
				modelName = transformModelNameIfNecessary(modelName);
				if (logger.isDebugEnabled()) {
					logger.debug("Added model attribute with name '" + modelName + "' and value [" + modelObject +
											 "] to Velocity context in view '" + getName() + "'");
				}
				velocityContext.put(modelName, modelObject);
			}
		}
		else {
			logger.debug("Model is null. Nothing to expose to Velocity context in view with name '" + getName() + "'");
		}
	}
	
	/**
	 * If necessary, transform the model name into a legal Velocity model name.
	 * Velocity can't cope with ".s" in a variable name, so we change them to "_s".
	 * @param modelName
	 */
	protected String transformModelNameIfNecessary(String modelName) {
		return StringUtils.replace(modelName, ".", "_");
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
	 * Based on code from the VelocityServlet.
	 * @param template template object returned by the handleRequest() method
	 * @param context context the Velocity context
	 * @param response servlet reponse (use this to get the OutputStream or Writer)
	 */
	protected void mergeTemplate(Template template, Context context, HttpServletResponse response) throws Exception {
		ServletOutputStream output = response.getOutputStream();
		VelocityWriter vw = null;
		try {
			vw = (VelocityWriter) this.writerPool.get();
			if (vw == null) {
				vw = new VelocityWriter(new OutputStreamWriter(output, this.encoding), OUTPUT_BUFFER_SIZE, true);
			}
			else {
				vw.recycle(new OutputStreamWriter(output, this.encoding));
			}
			template.merge(context, vw);
		}
		finally {
			try {
				if (vw != null) {
					vw.flush();
					this.writerPool.put(vw);
					output.close();
				}
			}
			catch (IOException ex) {
				// do nothing
			}
		}
	}


	/**
	 * Subclass of DateTool from Velocity tools,
	 * using the RequestContext Locale instead of the default Locale.
	 * @see org.springframework.web.servlet.support.RequestContextUtils#getLocale
	 */
	private static class LocaleAwareDateTool extends DateTool {

		private HttpServletRequest request;

		private LocaleAwareDateTool(HttpServletRequest request) {
			this.request = request;
		}

		public Locale getLocale() {
			return RequestContextUtils.getLocale(this.request);
		}
	}

}
