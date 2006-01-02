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

package org.springframework.web.servlet.view.xslt;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.ErrorListener;
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
import org.springframework.util.xml.SimpleTransformErrorListener;
import org.springframework.web.servlet.view.AbstractView;
import org.springframework.web.util.NestedServletException;

/**
 * Convenient superclass for views rendered using an XSLT stylesheet.
 * Subclasses must <b>either</b> provide the XML W3C Document or Node to
 * transform by overriding <code>createDomNode()</code>, <b>or</b> provide the
 * <code>Source</code> to transform by overriding <code>createXsltSource()</code>.
 *
 * <p>Note that <code>createXsltSource()</code> is the preferred method which all
 * new subclasses should override since Spring 1.2. <code>createDomNode()</code>
 * has been deprecated and may be removed in a future version.
 *
 * <p>Subclasses do not need to concern themselves with XSLT other than providing
 * a valid stylesheet location.
 *
 * <p>Properties:
 * <ul>
 * <li>stylesheetLocation: a Spring <code>Resource</code> pointing to the
 * XSLT stylesheet
 * <li>root: name of the root element, defaults to "DocRoot"
 * <li>uriResolver: URIResolver used in the transform
 * <li>errorListener (optional): ErrorListener implementation for custom
 * handling of warnings and errors during TransformerFactory operations
 * <li>indent (optional, default=true): whether additional whitespace
 * may be added when outputting the result true
 * <li>cache (optional, default=true): debug setting only
 * </ul>
 *
 * <p>Note that setting "cache" to "false" will cause the template objects
 * to be reloaded for each rendering. This is useful during development,
 * but will seriously affect performance in production and isn't thread-safe.
 *
 * @author Rod Johnson
 * @author Darren Davison
 * @author Juergen Hoeller
 */
public abstract class AbstractXsltView extends AbstractView {

	public static final String DEFAULT_ROOT = "DocRoot";


	private Resource stylesheetLocation;

	private String root = DEFAULT_ROOT;

	private boolean useSingleModelNameAsRoot = true;

	private URIResolver uriResolver;

	private ErrorListener errorListener = new SimpleTransformErrorListener(logger);

	private boolean indent = true;

	private Properties outputProperties;

	private boolean cache = true;

	private TransformerFactory transformerFactory;

	private Templates templates;


	/**
	 * Set the location of the XSLT stylesheet.
	 * @param stylesheetLocation the location of the XSLT stylesheet
	 * @see org.springframework.context.ApplicationContext#getResource
	 */
	public void setStylesheetLocation(Resource stylesheetLocation) {
		this.stylesheetLocation = stylesheetLocation;
		// Re-cache templates if transformer factory already initialized.
		if (this.transformerFactory != null) {
			cacheTemplates();
		}
	}

	/**
	 * Document root element name. Default is "DocRoot".
	 * Only used if we're not passed a single Node as model.
	 * @param root the document root element name
	 * @see #DEFAULT_ROOT
	 */
	public void setRoot(String root) {
		this.root = root;
	}

	/**
	 * Set whether to use the name of a given single model object
	 * as document root element name.
	 * <p>Default is "true": If you pass in a model with a single object
	 * named "myElement", then the document root will be named "myElement"
	 * as well. Set this flag to "false" if you want to pass in a single
	 * model object while still using the root element name configured
	 * through the "root" property.
	 * @see #setRoot
	 */
	public void setUseSingleModelNameAsRoot(boolean useSingleModelNameAsRoot) {
		this.useSingleModelNameAsRoot = useSingleModelNameAsRoot;
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
	 * Set an implementation of the <code>javax.xml.transform.ErrorListener</code>
	 * interface for custom handling of transformation errors and warnings.
	 * <p>If not set, a default SimpleTransformErrorListener is used that simply
	 * logs warnings using the logger instance of the view class,
	 * and rethrows errors to discontinue the XML transformation.
	 * @see org.springframework.util.xml.SimpleTransformErrorListener
	 */
	public void setErrorListener(ErrorListener errorListener) {
		this.errorListener = errorListener;
	}

	/**
	 * Set whether the XSLT transformer may add additional whitespace when
	 * outputting the result tree. Default is on; turn this off to not
	 * specify an "indent" key, leaving the choice up to the stylesheet.
	 * @see javax.xml.transform.OutputKeys#INDENT
	 */
	public void setIndent(boolean indent) {
		this.indent = indent;
	}

	/**
	 * Set arbitrary transformer output properties to be applied to the stylesheet.
	 * <p>Any values specified here will override defaults that this view sets
	 * programmatically.
	 * @param outputProperties output properties to apply to the transformation process
	 * @see javax.xml.transform.Transformer#setOutputProperty
	 */
	public void setOutputProperties(Properties outputProperties) {
		this.outputProperties = outputProperties;
	}

	/**
	 * Set whether to activate the cache. Default is on.
	 */
	public void setCache(boolean cache) {
		this.cache = cache;
	}


	/**
	 * Here we load our template, as we need the ApplicationContext to do it.
	 */
	protected final void initApplicationContext() throws ApplicationContextException {
		this.transformerFactory = TransformerFactory.newInstance();
		this.transformerFactory.setErrorListener(this.errorListener);

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

	private synchronized void cacheTemplates() throws ApplicationContextException {
		if (this.stylesheetLocation != null) {
			try {
				this.templates = this.transformerFactory.newTemplates(getStylesheetSource(this.stylesheetLocation));
				if (logger.isDebugEnabled()) {
					logger.debug("Loaded templates [" + this.templates + "] in XSLT view '" + getBeanName() + "'");
				}
			}
			catch (TransformerConfigurationException ex) {
				throw new ApplicationContextException("Can't load stylesheet from " + this.stylesheetLocation +
						" in XSLT view '" + getBeanName() + "'", ex);
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

		/*
		 * The preferred method has subclasses creating a Source rather than a Node for
		 * transformation. Support for Nodes is retained for backwards compatibility.
		 */
		Source source = null;
		Node dom = null;
		String docRoot = null;

		// Value of a single element in the map, if there is one.
		Object singleModel = null;

		if (this.useSingleModelNameAsRoot && model.size() == 1) {
			docRoot = (String) model.keySet().iterator().next();
			if (logger.isDebugEnabled()) {
				logger.debug("Single model object received, key [" + docRoot + "] will be used as root tag");
			}
			singleModel = model.get(docRoot);
		}

		// Handle special case when we have a single node.
		if (singleModel instanceof Node || singleModel instanceof Source) {
			// Don't domify if the model is already an XML node/source.
			// We don't need to worry about model name, either:
			// we leave the Node alone.
			logger.debug("No need to domify: was passed an XML Node or Source");
			source = (singleModel instanceof Node ? new DOMSource((Node) singleModel) : (Source) singleModel);
		}
		else {
			// docRoot local variable takes precedence
			dom = createDomNode(model, (docRoot != null ? docRoot : this.root), request, response);
			if (dom != null) {
				source = new DOMSource(dom);
			}
			else {
				source = createXsltSource(model, (docRoot != null ? docRoot : this.root), request, response);
			}
		}

		doTransform(model, source, request, response);
	}

	/**
	 * Return the XML <code>Source</code> to transform. Subclasses must implement
	 * <b>either</b> this method <b>or</b> <code>createDomNode</code>, which is
	 * retained only for backward compatibility.
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
	 * @return the XSLT Source to transform
	 * @throws Exception we let this method throw any exception; the
	 * AbstractXlstView superclass will catch exceptions
	 */
	protected Source createXsltSource(
			Map model, String root, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		return null;
	}

	/**
	 * Return the XML <code>Node</code> to transform.
	 * <p>
	 * This method is deprecated from version 1.2 with the preferred extension point
	 * being <code>createXsltSource(Map, String, HttpServletRequest, HttpServletResponse)</code>
	 * instead.  Code that previously implemented this method can now override the preferred
	 * method, returning <code>new DOMSource(node)</code> in place of returning <code>node</code>
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
	 * @deprecated in favor of <code>createXsltSource(Map, String, HttpServletRequest, HttpServletResponse)</code>
	 */
	protected Node createDomNode(Map model, String root, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		return null;
	}

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
	 * @see #doTransform(Map, Source, HttpServletRequest, HttpServletResponse)
	 * @deprecated the preferred method is <code>doTransform</code> with a Source argument
	 * @see #doTransform(java.util.Map, javax.xml.transform.Source, HttpServletRequest, HttpServletResponse)
	 */
	protected void doTransform(Map model, Node dom, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		doTransform(new DOMSource(dom), getParameters(request),
				new StreamResult(new BufferedOutputStream(response.getOutputStream())),
				response.getCharacterEncoding());
	}

	/**
	 * Perform the actual transformation, writing to the HTTP response.
	 * <p>Default implementation delegates to the doTransform version
	 * that takes a Result argument, building a StreamResult for the
	 * ServletResponse OutputStream.
	 * @param model the model Map
	 * @param source the Source to transform
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws Exception we let this method throw any exception; the
	 * AbstractXlstView superclass will catch exceptions
	 * @see #doTransform(Node, Map, Result, String)
	 * @see javax.xml.transform.stream.StreamResult
	 * @see javax.servlet.ServletResponse#getOutputStream
	 */
	protected void doTransform(Map model, Source source, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		doTransform(source, getParameters(request),
				new StreamResult(new BufferedOutputStream(response.getOutputStream())),
				response.getCharacterEncoding());
	}

	/**
	 * Perform the actual transformation, writing to the given result.
	 * Simply delegates to the
	 * <code>doTransform(Source, Map, Result, String)</code> version.
	 * @param dom the XML node to transform
	 * @param parameters a Map of parameters to be applied to the stylesheet
	 * @param result the result to write to
	 * @throws Exception we let this method throw any exception; the
	 * AbstractXlstView superclass will catch exceptions
	 * @see #doTransform(Source, Map, Result, String)
	 * @deprecated the preferred method is
	 * <code>doTransform(Source source, Map parameters, Result result, String encoding)</code>
	 */
	protected void doTransform(Node dom, Map parameters, Result result, String encoding)
			throws Exception {

		doTransform(new DOMSource(dom), parameters, result, encoding);
	}

	/**
	 * Perform the actual transformation, writing to the given result.
	 * @param source the Source to transform
	 * @param parameters a Map of parameters to be applied to the stylesheet
	 * @param result the result to write to
	 * @throws Exception we let this method throw any exception; the
	 * AbstractXlstView superclass will catch exceptions
	 */
	protected void doTransform(Source source, Map parameters, Result result, String encoding)
			throws Exception {

		try {
			Transformer trans = (this.templates != null) ?
					this.templates.newTransformer() : // we have a stylesheet
					this.transformerFactory.newTransformer(); // just a copy

			// Explicitly apply URIResolver to every created Transformer.
			if (this.uriResolver != null) {
				trans.setURIResolver(this.uriResolver);
			}

			// Apply any subclass supplied parameters to the transformer.
			if (parameters != null) {
				for (Iterator it = parameters.entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					trans.setParameter(entry.getKey().toString(), entry.getValue());
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Added parameters [" + parameters + "] to transformer object");
				}
			}

			// Specify default output properties.
			trans.setOutputProperty(OutputKeys.ENCODING, encoding);
			if (this.indent) {
				trans.setOutputProperty(OutputKeys.INDENT, "yes");
			}
			// Xalan-specific, but won't do any harm in other XSLT engines.
			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

			// Apply any arbitrary output properties, if specified.
			if (this.outputProperties != null) {
				Enumeration propsEnum = this.outputProperties.propertyNames();
				while (propsEnum.hasMoreElements()) {
					String propName = (String) propsEnum.nextElement();
					trans.setOutputProperty(propName, this.outputProperties.getProperty(propName));
				}
			}

			// Perform the actual XSLT transformation.
			trans.transform(source, result);
			if (logger.isDebugEnabled()) {
				logger.debug("XSLT transformed with stylesheet [" + this.stylesheetLocation + "]");
			}
		}
		catch (TransformerConfigurationException ex) {
			throw new NestedServletException("Couldn't create XSLT transformer for stylesheet [" +
					this.stylesheetLocation + "] in XSLT view with name [" + getBeanName() + "]", ex);
		}
		catch (TransformerException ex) {
			throw new NestedServletException("Couldn't perform transform with stylesheet [" +
					this.stylesheetLocation + "] in XSLT view with name [" + getBeanName() + "]", ex);
		}
	}

	/**
	 * Return a Map of transformer parameters to be applied to the stylesheet.
	 * Subclasses can override this method in order to apply one or more
	 * parameters to the transformation process.
	 * <p>Default implementation delegates to simple
	 * <code>getParameters</code> version.
	 * @param request current HTTP request
	 * @return a Map of parameters to apply to the transformation process
	 * @see #getParameters()
	 * @see javax.xml.transform.Transformer#setParameter
	 */
	protected Map getParameters(HttpServletRequest request) {
		return getParameters();
	}

	/**
	 * Return a Map of transformer parameters to be applied to the stylesheet.
	 * Subclasses can override this method in order to apply one or more
	 * parameters to the transformation process.
	 * <p>Default implementation delegates simply returns <code>null</code>.
	 * @return a Map of parameters to apply to the transformation process
	 * @see #getParameters(HttpServletRequest)
	 * @see javax.xml.transform.Transformer#setParameter
	 */
	protected Map getParameters() {
		return null;
	}

}
