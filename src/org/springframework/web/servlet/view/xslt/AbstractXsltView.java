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

package org.springframework.web.servlet.view.xslt;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Node;

import org.springframework.context.ApplicationContextException;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.view.AbstractView;

/**
 * Convenient superclass for views rendered using an XSLT stylesheet.
 * Subclasses must provide the XML W3C document to transform.
 * They do not need to concern themselves with XSLT.
 *
 * <p>Properties:
 * <ul>
 * <li>stylesheet: no transform is null
 * <li>root: name of the root element
 * <li>uriResolver: URIResolver used in the transform
 * <li>cache (optional, default=true): debug setting only
 * </ul>
 *
 * <p>Setting cache to false will cause the templates object to be reloaded
 * for each rendering. This is useful during development, but will seriously
 * affect performance in production and isn't threadsafe.
 *
 * @author Rod Johnson
 * @author Darren Davison
 */
public abstract class AbstractXsltView extends AbstractView {

	public static final String DEFAULT_ROOT = "DocRoot";
	
	
	/** URL of stylesheet */
	private Resource stylesheetLocation;

	/** Document root element name, normally overridden in the view definition config */
	private String root = DEFAULT_ROOT;

	/** Custom URIResolver, set by subclass or as bean property */
	private URIResolver uriResolver;

	private boolean cache = true;

	private TransformerFactory transformerFactory;

	/** XSLT Template */
	private Templates templates;


	/**
	 * Set the location of the XSLT stylesheet.
	 * @param stylesheetLocation the location of the XSLT stylesheet
	 * @see org.springframework.context.ApplicationContext#getResource
	 */
	public void setStylesheetLocation(Resource stylesheetLocation) {
		this.stylesheetLocation = stylesheetLocation;
	}

	/** 
	 * Document root element name. Default is "DocRoot".
	 * Only used if we're not passed a single Node as model.
	 * @param root document root element name
	 * @see #DEFAULT_ROOT
	 */
	public void setRoot(String root) {
		this.root = root;
	}

	/**
	 * Set the URIResolver used in the transform. The URIResolver
	 * handles calls to the XSLT document() function.
	 * This method can be used by subclasses or as a bean property.
	 * @param uriResolver URIResolver to set. No URIResolver
	 * will be set if this is null (this is the default).
	 */
	public void setUriResolver(URIResolver uriResolver) {
		this.uriResolver = uriResolver;
	}
	
	/**
	 * Set whether to activate the cache. Default is true.
	 */
	public void setCache(boolean cache) {
		this.cache = cache;
	}


	/**
	 * Here we load our template, as we need the ApplicationContext to do it.
	 */
	protected final void initApplicationContext() throws ApplicationContextException {
		this.transformerFactory = TransformerFactory.newInstance();
		
		if (this.uriResolver != null) {
			if (logger.isInfoEnabled()) {
				logger.info("Using custom URIResolver [" + this.uriResolver + "] in XSLT view with name '" +
										getBeanName() + "'");
			}
			this.transformerFactory.setURIResolver(this.uriResolver);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("URL in view is " + this.stylesheetLocation);
		}

		cacheTemplates();
	}	

	private void cacheTemplates() throws ApplicationContextException {
		if (this.stylesheetLocation != null) {
			try {
				this.templates = this.transformerFactory.newTemplates(getStylesheetSource(this.stylesheetLocation));
				if (logger.isDebugEnabled()) {
					logger.debug("Loaded templates [" + this.templates + "] in XSLT view '" + getBeanName() + "'");
				}
			}
			catch (TransformerConfigurationException ex) {
				throw new ApplicationContextException(
					"Can't load stylesheet from " + this.stylesheetLocation + " in XSLT view '" + getBeanName() + "'", ex);
			}
		}
	}

	/** 
	 * Load the stylesheet. Subclasses can override this.
	 */
	protected Source getStylesheetSource(Resource stylesheetLocation) throws ApplicationContextException {
		if (logger.isDebugEnabled()) {
			logger.debug("Loading XSLT stylesheet from " + stylesheetLocation);
		}
		try {
			URL url = stylesheetLocation.getURL(); 
			String urlPath = url.toString(); 
			String systemId = urlPath.substring(0, urlPath.lastIndexOf('/') + 1); 
			return new StreamSource(url.openStream(), systemId);
		}
		catch (IOException ex) {
			throw new ApplicationContextException("Can't load XSLT stylesheet from " + stylesheetLocation, ex);
		}
	}

	protected final void renderMergedOutputModel(
			Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (!this.cache) {
			logger.warn("DEBUG SETTING: NOT THREADSAFE AND WILL IMPAIR PERFORMANCE: template will be refreshed");
			cacheTemplates();
		}

		if (this.templates == null) {
			if (this.transformerFactory == null) {
				throw new ServletException("XLST view is incorrectly configured. Templates AND TransformerFactory are null");
			}

			logger.warn("XSLT view is not configured: will copy XML input");
			response.setContentType("text/xml; charset=ISO-8859-1");
		}
		else {
			// normal case
			response.setContentType(getContentType());
		}

		Node dom = null;
		String docRoot = null;

		// value of a single element in the map, if there is one
		Object singleModel = null;

		if (model.size() == 1) {
			docRoot = (String) model.keySet().iterator().next();
			if (logger.isDebugEnabled()) {
				logger.debug("Single model object received, key [" + docRoot + "] will be used as root tag");
			}
			singleModel = model.get(docRoot);
		}

		// handle special case when we have a single node
		if (singleModel != null && (singleModel instanceof Node)) {
			// Don't domify if the model is already an XML node.
			// We don't need to worry about model name, either:
			// we leave the Node alone.
			logger.debug("No need to domify: was passed an XML node");
			dom = (Node) singleModel;
		}
		else 			
			// docRoot local variable takes precedence
			dom = createDomNode(model, (docRoot == null) ? this.root : docRoot, request, response);
		
		doTransform(model, dom, request, response);
	}

	/**
	 * Return the XML node to transform.
	 * Subclasses must implement this method.
	 * @param model the model Map
	 * @param root name for root element. This can be supplied as a bean property
	 * to concrete subclasses within the view definition file, but will be overridden
	 * in the case of a single object in the model map to be the key for that object.
	 * If no root property is specified and multiple model objects exist, a default
	 * root tag name will be supplied. 
	 * @param request HTTP request. Subclasses won't normally use this, as
	 * request processing should have been complete. However, we might to
	 * create a RequestContext to expose as part of the model.
	 * @param response HTTP response. Subclasses won't normally use this,
	 * however there may sometimes be a need to set cookies.
	 * @return the XML node to transform
	 * @throws Exception we let this method throw any exception; the
	 * AbstractXlstView superclass will catch exceptions
	 */
	protected abstract Node createDomNode(
			Map model, String root, HttpServletRequest request, HttpServletResponse response)
			throws Exception;

	/**
	 * Perform the actual transformation, writing to the HTTP response.
	 * <p>Default implementation delegates to the doTransform version
	 * that takes a Result argument, building a StreamResult for the
	 * ServletResponse OutputStream.
	 * @param model the model Map
	 * @param dom the XNL node to transform
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws Exception we let this method throw any exception; the
	 * AbstractXlstView superclass will catch exceptions
	 * @see #doTransform(Node, Map, Result, String)
	 * @see javax.xml.transform.stream.StreamResult
	 * @see javax.servlet.ServletResponse#getOutputStream
	 */
	protected void doTransform(Map model, Node dom, HttpServletRequest request, HttpServletResponse response)
	    throws Exception {
		Map parameters = getParameters(request);
		doTransform(
				dom, parameters,
				new StreamResult(new BufferedOutputStream(response.getOutputStream())),
				response.getCharacterEncoding());
	}

	/**
	 * Perform the actual transformation, writing to the given result.
	 * @param dom the XML node to transform
	 * @param parameters a Map of parameters to be applied to the stylesheet
	 * @param result the result to write to
	 * @throws Exception we let this method throw any exception; the
	 * AbstractXlstView superclass will catch exceptions
	 */
	protected void doTransform(Node dom, Map parameters, Result result, String encoding)
	    throws Exception {
		try {
			Transformer trans = (this.templates != null) ?
			    this.templates.newTransformer() : // we have a stylesheet
					this.transformerFactory.newTransformer(); // just a copy
				
			// apply any subclass supplied parameters to the transformer
			if (parameters != null) {
				for (Iterator iter = parameters.entrySet().iterator(); iter.hasNext();) {
					Map.Entry entry = (Map.Entry) iter.next();
					trans.setParameter(entry.getKey().toString(), entry.getValue());
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Added parameters [" + parameters + "] to transformer object");
				}
			}

			trans.setOutputProperty(OutputKeys.ENCODING, encoding);
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			// Xalan-specific, but won't do any harm in other XSLT engines
			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			trans.transform(new DOMSource(dom), result);
			if (logger.isDebugEnabled()) {
				logger.debug("XSLT transformed with stylesheet [" + this.stylesheetLocation + "]");
			}
		}
		catch (TransformerConfigurationException ex) {
			throw new ServletException(
				"Couldn't create XSLT transformer for stylesheet [" + this.stylesheetLocation +
				"] in XSLT view with name [" + getBeanName() + "]", ex);
		}
		catch (TransformerException ex) {
			throw new ServletException(
				"Couldn't perform transform with stylesheet [" + this.stylesheetLocation +
				"] in XSLT view with name [" + getBeanName() + "]", ex);
		}
	}

	/**
	 * Return a Map of parameters to be applied to the stylesheet.
	 * Subclasses can override this method in order to apply one or more
	 * parameters to the transformation process.
	 * <p>Default implementation delegates to simple getParameter version.
	 * @param request current HTTP request
	 * @return a Map of parameters to apply to the transformation process
	 * @see #getParameters()
	 * @see javax.xml.transform.Transformer#setParameter
	 */
	protected Map getParameters(HttpServletRequest request) {
		return getParameters();
	}

	/**
	 * Return a Map of parameters to be applied to the stylesheet.
	 * Subclasses can override this method in order to apply one or more
	 * parameters to the transformation process.
	 * <p>Default implementation delegates simply returns null.
	 * @return a Map of parameters to apply to the transformation process
	 * @see #getParameters(HttpServletRequest)
	 * @see javax.xml.transform.Transformer#setParameter
	 */
	protected Map getParameters() {
		return null;
	}

}
