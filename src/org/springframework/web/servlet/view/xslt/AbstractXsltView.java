/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.web.servlet.view.xslt;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
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
 * @version $Id: AbstractXsltView.java,v 1.4 2003-12-12 19:46:14 jhoeller Exp $
 */
public abstract class AbstractXsltView extends AbstractView {

	/** URL of stylesheet */
	private String stylesheet;

	/** Document root element name */
	private String root;

	/** Custom URIResolver, set by subclass or as bean property */
	private URIResolver uriResolver;

	private boolean cache = true;

	private TransformerFactory transformerFactory;

	/** XSLT Template */
	private Templates templates;

	public AbstractXsltView() {
	}

	/**
	 * Set the URL of the XSLT stylesheet.
	 * @param url the URL of the XSLT stylesheet
	 */
	public final void setStylesheet(String url) {
		this.stylesheet = url;
	}

	/** 
	 * Document root element name.
	 * Only used if we're not passed a single Node as model.
	 * @param root document root element name
	 */
	public final void setRoot(String root) {
		this.root = root;
	}

	/**
	 * Set the URIResolver used in the transform. The URIResolver
	 * handles calls to the XSLT document() function.
	 * This method can be used by subclasses or as a bean property
	 * @param uriResolver URIResolver to set. No URIResolver
	 * will be set if this is null (this is the default).
	 */
	public final void setUriResolver(URIResolver uriResolver) {
		this.uriResolver = uriResolver;
	}
	
	/**
	 * Activate or deactivate the cache.
	 * @param cache whether to activate the cache
	 */
	public final void setCache(boolean cache) {
		this.cache = cache;
	}
	
	/**
	 * Here we load our template, as we need the ApplicationContext to do it.
	 */
	protected final void initApplicationContext() throws ApplicationContextException {
		this.transformerFactory = TransformerFactory.newInstance();
		
		if (this.uriResolver != null) {
			logger.info("Using custom URIResolver [" + this.uriResolver + "] in XSLT view with name '" + getName() + "'");
			this.transformerFactory.setURIResolver(this.uriResolver);
		}
		logger.debug("Url in view is " + stylesheet);
		cacheTemplates();
	}	

	private void cacheTemplates() throws ApplicationContextException {
		if (this.stylesheet != null && !"".equals(this.stylesheet)) {
			try {
				this.templates = this.transformerFactory.newTemplates(getStylesheetSource(this.stylesheet));
				logger.debug("Loaded templates [" + this.templates + "] in XSLT view '" + getName() + "'");
			}
			catch (TransformerConfigurationException ex) {
				throw new ApplicationContextException(
					"Can't load stylesheet at [" + this.stylesheet + "] in XSLT view '" + getName() + "'", ex);
			}
		}
	}

	/** 
	 * Load the stylesheet. Subclasses can override this.
	 */
	protected Source getStylesheetSource(String url) throws ApplicationContextException {
		logger.debug("Loading XSLT stylesheet '" + url + "'");
		try {
			return new StreamSource(getApplicationContext().getResourceAsStream(url));
		}
		catch (IOException ex) {
			throw new ApplicationContextException("Can't load XSLT stylesheet from [" + url + "]", ex);
		}
	}

	protected final void renderMergedOutputModel(Map model, HttpServletRequest request,
	                                             HttpServletResponse response) throws Exception {
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
			// Normal case
			response.setContentType(getContentType());
		}

		if (model == null)
			throw new ServletException("Cannot do XSLT transform on null model");

		Node dom = null;
		String docRoot = null;

		// Value of a single element in the map, if there is one
		Object singleModel = null;

		if (model.size() == 1) {
			docRoot = (String) model.keySet().iterator().next();
			singleModel = model.get(docRoot);
		}

		// Handle special case when we have a single node
		if (singleModel != null && (singleModel instanceof Node)) {
			// Don't domify if the model is already an XML node
			// We don't need to worry about model name, either:
			// we leave the Node alone
			logger.debug("No need to domify: was passed an XML node");
			dom = (Node) singleModel;
		}
		else {
			if (this.root == null && docRoot == null) {
				throw new ServletException(
					"Cannot domify multiple non-Node objects without a root element name in XSLT view with name='" + getName() + "'");
			}
			
			// docRoot local variable takes precedence
			dom = createDomNode(model, (docRoot == null) ? this.root : docRoot, request, response);
		}

		doTransform(response, dom);
	}

	/**
	 * Return the XML node to transform.
	 * Subclasses must implement this method.
	 * @param model the model Map
	 * @param root name for root element
	 * @param request HTTP request. Subclasses won't normally use this, as
	 * request processing should have been complete. However, we might to
	 * create a RequestContext to expose as part of the model.
	 * @param response HTTP response. Subclasses won't normally use this,
	 * however there may sometimes be a need to set cookies.
	 * @throws Exception we let this method throw any exception; the
	 * AbstractXlstView superclass will catch exceptions
	 */
	protected abstract Node createDomNode(Map model, String root, HttpServletRequest request,
	                                      HttpServletResponse response) throws Exception;

	/**
	 * Use TrAX to perform the transform.
	 */
	protected void doTransform(HttpServletResponse response, Node dom) throws ServletException, IOException {
		try {
			Transformer trans = (this.templates != null) ?
			    this.templates.newTransformer() : // we have a stylesheet
						this.transformerFactory.newTransformer(); // just a copy

			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			// Xalan-specific, but won't do any harm in other XSLT engines
			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			trans.transform(new DOMSource(dom), new StreamResult(new BufferedOutputStream(response.getOutputStream())));

			logger.debug("XSLT transformed OK with stylesheet '" + this.stylesheet + "'");
		}
		catch (TransformerConfigurationException ex) {
			throw new ServletException(
				"Couldn't create XSLT transformer for stylesheet '" + this.stylesheet +
				"' in XSLT view with name='" + getName() + "'", ex);
		}
		catch (TransformerException ex) {
			throw new ServletException(
				"Couldn't perform transform with stylesheet '" + this.stylesheet +
				"' in XSLT view with name='" + getName() + "'", ex);
		}
	}

}
