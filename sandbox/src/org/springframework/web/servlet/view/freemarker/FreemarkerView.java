/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.web.servlet.view.freemarker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.context.ApplicationContextException;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;


/**
 * View using the FreeMarker template engine.
 *
 * <p>Exposes the following JavaBean properties:
 * <ul>
 * <li><b>url</b>: the location of the FreeMarker template to be wrapped,
 * relative to the FreeMarker template context (directory).
 * </ul>
 *
 * <p>Depends on a FreemarkerConfig object such as FreemarkerConfigurer
 * being accessible in the current web application context.
 * 
 * @author Darren Davison
 * @since 3/3/2004
 * @version $Id: FreemarkerView.java,v 1.1 2004-03-05 19:45:44 davison Exp $
 */
public class FreemarkerView extends AbstractUrlBasedView {

	private static final String DEFAULT_TEMPLATE_CONTEXT = "WEB-INF/freemarker";
	
	private String templateContext = DEFAULT_TEMPLATE_CONTEXT;

	private FreemarkerConfig freemarkerConfig;
	
    private Configuration configuration;
	
	private Template freemarkerTemplate;


    /**
	 * Invoked on startup. Looks for a single FreemarkerConfig bean to
	 * find the relevant Configuration for this factory.
	 * 
	 * <p>Method checks that the template for the default Locale can be found -
	 * FM will check non-Locale specific templates if a locale specific 
	 * one is not found.
	 * 
	 * @see freemarker.cache.TemplateCache.getTemplate
	 */
	protected void initApplicationContext() throws ApplicationContextException {
		super.initApplicationContext();

		logger.debug("Initializing applicationContext");
		try {
			freemarkerConfig = (FreemarkerConfig)
					BeanFactoryUtils.beanOfTypeIncludingAncestors(getWebApplicationContext(),
																  FreemarkerConfig.class, true, true);
			this.configuration = freemarkerConfig.getConfiguration();
			this.configuration.setServletContextForTemplateLoading(getServletContext(), templateContext);
			
			logger.debug("Freemarker Configuration set with template loading context [" + templateContext + "]");
		}
		catch (BeanDefinitionStoreException ex) {
			throw new ApplicationContextException("Must define a single FreemarkerConfig bean in this web application " +
												  "context (may be inherited): FreemarkerConfigurer is the usual implementation. " +
												  "This bean may be given any name.", ex);
		}

		loadTemplate(Locale.getDefault());
	}
		
    /**
     * attempt to load and parse the FreeMarker template for a given Locale
     */
    private void loadTemplate(Locale locale) throws ApplicationContextException {
		try {
			this.freemarkerTemplate = this.configuration.getTemplate(getUrl());
		}
		catch (FileNotFoundException ex) {
			handleException("Can't load FreeMarker template [" + getUrl() +
											"]  Is it available in the template directory?", ex);
		}
		catch (ParseException ex) {
			handleException("Error parsing FreeMarker template [" + getUrl() + "]", ex);
		}
		catch (IOException ex) {
			handleException("Error loading FreeMarker template [" + getUrl() + "]", ex);
		}		
    }
    
	/**
	 * Re-throw the given exception as ApplicationContextException with proper message.
	 */
	private void handleException(String message, Exception ex) throws ApplicationContextException {
		String actualMessage = "FreeMarker resource loader is [" +
				this.configuration.getTemplateLoader() + "]: " + message;
		throw new ApplicationContextException(actualMessage, ex);
	}

    /**
     * Process the model map by merging it with the FreeMarker template.  Output is
     * directed to the response.  This method can be overridden if custom behaviour 
     * is needed.
     * @see org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel
     */
    protected void renderMergedOutputModel(
        Map model,
        HttpServletRequest request,
        HttpServletResponse response)
        throws Exception {
        
        if (logger.isDebugEnabled())
        	logger.debug("Preparing to process model [" 
        				 + model + "] with FreeMarker Template [" + freemarkerTemplate.getName() + "]");
    	    	
		response.setContentType(getContentType());
		try {
			freemarkerTemplate.process(model, response.getWriter());
		}
		catch (TemplateException ex) {
			throw new ServletException("Error merging FreeMarker template with model data", ex);
		}
		
    }

    /**
     * returns the template context that will be searched for the 
     * FreeMarker template file.
     * @return templateContext the template directory relative to the ServletContext
     */
    public String getTemplateContext() {
        return templateContext;
    }

    /**
     * sets the template context that will be searched for the 
     * FreeMarker template file.
     * @param templateContext the template directory relative to the ServletContext
     */
    public void setTemplateContext(String templateContext) {
    	// FreeMarker doesn't want leading '/' in servlet context paths
    	if (templateContext.startsWith("/"))
    		templateContext = templateContext.substring(1);
        this.templateContext = templateContext;
    }

}
