/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.view.velocity;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
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
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.util.SimplePool;
import org.springframework.beans.factory.support.BeanFactoryUtils;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.AbstractView;

/**
 * View using Velocity template engine.
 * Based on code in the VelocityServlet shipped with Velocity.
 *
 * <p>Exposes the following JavaBean properties:
 * <ul>
 * <li>templateName: name of the Velocity template to be cached
 * <li>poolSize (optional, default=40): number of Velocity writers (refer to
 * Velocity documentation to see exactly what this means)
 * <li>cache: whether or not Velocity templates should be cached. They always should
 * be in production (the default), but setting this to false enables us to modify
 * Velocity templates without restarting the application
 * <li>exposeDateFormatter: whether to expose a date formatter helper object in the
 * Velocity context. Defaults to false, as it creates an object that is only needed
 * if we do date formatting. Velocity is currently weak in this area.
 * <li>exposeCurrencyFormatter: whether to expose a Currency formatter helper object
 * </ul>
 * 
 * <p>
 * Depends on a VelocityConfiguration object such as VelocityConfigurer
 * being accessible in the current web application context.
 
 * @author Rod Johnson
 * @version $Id: VelocityView.java,v 1.2 2003-09-20 16:32:40 johnsonr Exp $
 * @see VelocityConfigurer
 */
public class VelocityView extends AbstractView {
	
	/** Helper context name */
	public static final String DATE_FORMAT_KEY = "simpleDateFormat";
	
	/** Helper context name */
	public static final String CURRENCY_FORMAT_KEY = "currencyFormat";

	/** Encoding for the output stream */
	public static final String DEFAULT_OUTPUT_ENCODING = "ISO-8859-1";


	/** Instance of Velocity */
	private VelocityEngine velocityEngine;

	private int poolSize = 40;

	private boolean cache;

	private boolean exposeDateFormatter;

	private boolean exposeCurrencyFormatter;

	/** Name of the Velocity template */
	private String templateName;

	/** Velocity Template */
	private Template velocityTemplate;

	/** The encoding to use when generating outputing */
	private String encoding = null;

	/** Cache of writers */
	private SimplePool writerPool = new SimplePool(40);


	/**
 	* Invoked on startup. Looks for a single VelocityConfiguration bean to
 	* find the relevant VelocityEngine for this factory.
 	*/
	protected void initApplicationContext() throws ApplicationContextException {
		Collection c = BeanFactoryUtils.beansOfTypeIncludingAncestors(VelocityConfiguration.class, getWebApplicationContext());
		
		if (c.size() == 1) {
			// We need exactly one VelocityConfiguration bean
			VelocityConfiguration vconfig = (VelocityConfiguration) c.iterator().next();
			this.velocityEngine = vconfig.getVelocityEngine();
		}
		else {
			throw new ApplicationContextException("Must define a VelocityConfiguration bean in this web application context (may be inherited):" +				"VelocityConfigurer is the usual implementation. " +				"This bean may be given any name.");
		}
		 
		// TODO remove this dependence on RuntimeSingleton
		encoding = RuntimeSingleton.getString(VelocityEngine.OUTPUT_ENCODING, DEFAULT_OUTPUT_ENCODING);
		
		// Check that we can get the template, even if we might subsequently get it again
		loadTemplate();
	}
	

	public void setPoolSize(int sz) {
		this.poolSize = sz;
		this.writerPool = null;
		writerPool = new SimplePool(poolSize);
	}

	public void setCache(boolean cache) {
		this.cache = cache;
	}
	
	/**
	 * Set whether to expose a date formatter.
	 */
	public void setExposeDateFormatter(boolean exposeDateFormatter) {
		this.exposeDateFormatter = exposeDateFormatter;
	}

	/**
	 * Set whether to expose a currency formatter.
	 */
	public void setExposeCurrencyFormatter(boolean exposeCurrencyFormatter) {
		this.exposeCurrencyFormatter = exposeCurrencyFormatter;
	}

	/**
	 * Set the name of the wrapped Velocity template.
	 * This will cause the template to be loaded.
	 * @param templateName the name of the wrapped Velocity template,
	 * relative to the Velocity template root. For example,
	 * "/ic/interestResult.vm".
	 */
	public void setTemplateName(String templateName) throws ServletException {
		this.templateName = templateName;
	}
	
	
	/**
	 * Load the Velocity template that is to be cached in this class.
	 */
	private void loadTemplate() throws ApplicationContextException {
		String mesg = "Velocity resource loader is: [" + this.velocityEngine.getProperty("class.resource.loader.class") + "]; ";
		try {
			this.velocityTemplate = this.velocityEngine.getTemplate(this.templateName);
		}
		catch (ResourceNotFoundException ex) {
			mesg += "Can't load Velocity template '" + this.templateName + "': is it on the classpath, under /WEB-INF/classes?";
			logger.error(mesg, ex);
			throw new ApplicationContextException(mesg, ex);
		} 
		catch (ParseErrorException ex) {
			mesg += "Error parsing Velocity template '" + this.templateName + "'";
			logger.error(mesg, ex);
			throw new ApplicationContextException(mesg, ex);
		} 
		catch (Exception ex) {
			mesg += "Unexpected error getting Velocity template '" + this.templateName + "'";
			logger.error(mesg, ex);
			throw new ApplicationContextException(mesg, ex);
		}
	}
	
	/**
	 * Render the view given the model to output.
	 * @param model combined output Map, with dynamic values
	 * taking precedence over static attributes
	 * @param request HttpServetRequest
	 * @param response HttpServletResponse
	 * @throws ServletException if there is any other error
	 */
	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
		throws ServletException {

		if (this.velocityTemplate == null)
			throw new ServletException(
				"FastVelocityView with name '" + getName() + "' is not configured: templateName must have been set");
				
		// We already hold a reference to the template, but we might want to load it
		// if not caching. As Velocity itself caches templates, so our ability to
		// cache templates in this class is a minor optimization only.
		if (!this.cache) {
			loadTemplate();
		}

		response.setContentType(getContentType());

		try {
			// Create Velocity context
			Context context = new VelocityContext();
			exposeModelsAsContextAttributes(model, context);
			exposeHelpers(context, request);
			mergeTemplate(this.velocityTemplate, context, response);

			if (logger.isDebugEnabled())
				logger.debug("Merged OK with Velocity template '" + templateName + "' in VelocityView with name '" + getName() + "'");
		}
		catch (IOException ex) {
			String mesg =
				"Couldn't write to response trying to merge Velocity template with name '"
					+ this.templateName
					+ "' in VelocityView with name '"
					+ getName()
					+ "'";
			logger.error(mesg, ex);
			throw new ServletException(mesg, ex);
		}
		catch (ParseErrorException ex) {
			String mesg =
				"Velocity template with name '"
					+ this.templateName
					+ "' appears to be invalid in VelocityView with name '"
					+ getName()
					+ "'";
			logger.error(mesg, ex);
			throw new ServletException(mesg, ex);
		}
		catch (Exception ex) {
			String mesg =
				"Unknown error trying to merge Velocity template with name '"
					+ this.templateName
					+ "' in VelocityView with name '"
					+ getName()
					+ "'";
			logger.error(mesg, ex);
			throw new ServletException(mesg, ex);
		}
	}

	/**
	 * Expose the models in the given map as Velocity context attributes.
	 * Names will be taken from the map.
	 * @param model Map of model data to expose
	 * @param vContext VelocityContext to add data to
	 */
	private void exposeModelsAsContextAttributes(Map model, Context vContext) {
		if (model != null) {
			Set keys = model.keySet();
			Iterator itr = keys.iterator();
			while (itr.hasNext()) {
				String modelname = (String) itr.next();
				Object val = model.get(modelname);

				if (logger.isDebugEnabled())
					logger.debug("Added model with name '" + modelname
							+ "' and class "
							+ val.getClass()
							+ " to Velocity context in view with name '"
							+ getName()
							+ "'");

				vContext.put(modelname, val);
			}
		}
		else {
			logger.debug("Model is null. Nothing to expose to FastVelocity context in view with name '" + getName() + "'");
		}
	}
	
	/**
	 * Expose helpers unique to each rendring operation. 
	 * This is necessary so that different rendering operations can't overwrite each other's formats etc.
	 */
	private void exposeHelpers(Context vContext, HttpServletRequest request) throws ServletException {
		Locale locale = RequestContextUtils.getLocale(request);

		if (this.exposeDateFormatter) {
			// Javadocs indicate that this cast will work in most locales
			SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
			vContext.put(DATE_FORMAT_KEY, df);
			logger.debug("Adding date helper to context");
		}
		
		if (this.exposeCurrencyFormatter) {
			NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
			vContext.put(CURRENCY_FORMAT_KEY, nf);
			logger.debug("Adding currency helper to context");
		}
	}
	
	/**
	 * Based on code from the VelocityServlet.
	 * Merges the template with the context. Only override this if you really, really,
	 * really need to. (And don't call us with questions if it breaks :)
	 * @param template template object returned by the handleRequest() method
	 * @param context context created by the createContext() method
	 * @param response servlet reponse (use this to get the OutputStream or Writer)
	 */
	protected void mergeTemplate(Template template, Context context, HttpServletResponse response) throws Exception {
		ServletOutputStream output = response.getOutputStream();
		VelocityWriter vw = null;
		try {
			vw = (VelocityWriter) this.writerPool.get();
			if (vw == null) {
				vw = new VelocityWriter(new OutputStreamWriter(output, encoding), 4 * 1024, true);
			}
			else {
				vw.recycle(new OutputStreamWriter(output, encoding));
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

}
