/*
 * Copyright 2002-2006 the original author or authors.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.xml.SimpleTransformErrorListener;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

/**
 * XSLT-driven View that allows for response context to be rendered as the
 * result of an XSLT transformation.
 *
 * <p>The XSLT Source object is supplied as a parameter in the model and then
 * {@link #locateSource detected} during response rendering. Users can either specify
 * a specific entry in the model via the {@link #setSourceKey sourceKey} property or
 * have Spring locate the Source object. This class also provides basic conversion
 * of objects into Source implementations. See {@link #getSourceTypes() here}
 * for more details.
 *
 * <p>All model parameters are passed to the XSLT Transformer as parameters.
 * In addition the user can configure {@link #setOutputProperties output properties}
 * to be passed to the Transformer.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class XsltView extends AbstractUrlBasedView {

	private TransformerFactory transformerFactory = TransformerFactory.newInstance();

	private ErrorListener errorListener = new SimpleTransformErrorListener(logger);

	private URIResolver uriResolver;

	private boolean cacheTemplates = true;

	private Templates cachedTemplates;

	private String sourceKey;

	private Properties outputProperties;

	private boolean indent = true;


	/**
	 * Turns on/off the caching of the XSLT {@link Templates} instance. The default
	 * value is <code>true</code>. Only set this to <code>false</code> in development
	 * as not caching seriously impacts performance.
	 */
	public void setCacheTemplates(boolean cacheTemplates) {
		this.cacheTemplates = cacheTemplates;
	}

	/**
	 * Sets a custom {@link URIResolver} to use for processing the transformation
	 * and loading the
	 * @param uriResolver
	 */
	public void setUriResolver(URIResolver uriResolver) {
		this.uriResolver = uriResolver;
	}

	public void setErrorListener(ErrorListener errorListener) {
		Assert.notNull(errorListener, "'errorListener' cannot be null.");
		this.errorListener = errorListener;
	}

	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
	}

	public void setOutputProperties(Properties outputProperties) {
		this.outputProperties = outputProperties;
	}

	public void setIndent(boolean indent) {
		this.indent = indent;
	}

	protected final TransformerFactory getTransformerFactory() {
		return this.transformerFactory;
	}

	protected void initApplicationContext() throws BeansException {
		this.getTransformerFactory().setErrorListener(this.errorListener);

		if (this.uriResolver != null) {
			if (logger.isInfoEnabled()) {
				logger.info("Using custom URIResolver '" + this.uriResolver
								+ "' in XSLT view with URL '" + getUrl() + "'");
			}
			this.getTransformerFactory().setURIResolver(this.uriResolver);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("URL in view is '" + getUrl() + "'");
		}

		if (this.cacheTemplates) {
			this.cachedTemplates = loadTemplates();
		}
	}

	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Templates templates = this.cachedTemplates;
		if (templates == null) {
			logger.warn("DEBUG SETTING: WILL IMPAIR PERFORMANCE: template will be refreshed");
			templates = loadTemplates();
		}

		Transformer transformer = createTransformer(templates);
		configureTransformer(model, response, transformer);
		configureResponse(model, response, transformer);
		Source source = null;
		try {
			source = locateSource(model);
			if(source == null) {
				throw new IllegalArgumentException("Unable to locate Source object in model.");
			}
			transformer.transform(source, createResult(response));
		}
		finally {
			if (source != null) {
				closeSourceIfNecessary(source);
			}
		}
	}

	/**
	 * Creates the XSLT {@link Result} used to render the result of the transformation.
	 * Default implementation creates a {@link StreamResult} wrapping the supplied
	 * {@link HttpServletResponse}.
	 */
	protected Result createResult(HttpServletResponse response) throws Exception {
		return new StreamResult(response.getOutputStream());
	}

	/**
	 * Locates the {@link Source} object in the supplied model, converting objects as required.
	 * Default implementation first attempts to look under the configured {@link #setSourceKey source key},
	 * if any, before attempting to locate an object of {@link #getSourceTypes() supported type}.
	 */
	protected Source locateSource(Map model) throws Exception {
		if (this.sourceKey != null) {
			return convertSource(model.get(this.sourceKey));
		}

		Object source = CollectionUtils.findValueOfType(model.values(), getSourceTypes());
		return (source != null ? convertSource(source) : null);
	}

	/**
	 * Returns the array of {@link Class Classes} that are supported when converting to an
	 * XSLT {@link Source}. Current supports {@link Source}, {@link Document}, {@link Node},
	 * {@link InputStream}, {@link Reader} and {@link Resource}.
	 */
	protected Class[] getSourceTypes() {
		return new Class[]{Source.class, Document.class, Node.class, InputStream.class, Reader.class, Resource.class};
	}

	/**
	 * Converts the supplied {@link Object} into an XSLT {@link Source} if the
	 * {@link Object} type is {@link #getSourceTypes() supported}.
	 *
	 * @throws IllegalArgumentException if the {@link Object} cannot if not of a supported type.
	 */
	protected Source convertSource(Object source) throws Exception {
		if (source instanceof Source) {
			return (Source) source;
		}
		else if (source instanceof Document) {
			return new DOMSource(((Document) source).getDocumentElement());
		}
		else if (source instanceof Node) {
			return new DOMSource((Node) source);
		}
		else if (source instanceof InputStream) {
			return new StreamSource((InputStream) source);
		}
		else if (source instanceof Reader) {
			return new StreamSource((Reader) source);
		}
		else if (source instanceof Resource) {
			return new StreamSource(((Resource) source).getInputStream());
		}
		else {
			throw new IllegalArgumentException("Value '" + source + "' cannot be converted to Source.");
		}
	}

	/**
	 * Configures the supplied {@link Transformer} instance. Default implementation copies parameters from
	 * the model into the {@link Transformer} {@link Transformer#setParameter parameter set}.
	 * This implementation also copies the {@link #setOutputProperties output properties}
	 * into the {@link Transformer} {@link Transformer#setOutputProperty output properties}.
	 * Indentation properties are also set by this implementation.
	 *
	 * @see #copyModelParameters(Map, Transformer)
	 * @see #copyOutputProperties(Transformer)
	 * @see #configureIndentation(Transformer)
	 */
	protected void configureTransformer(Map model, HttpServletResponse response, Transformer transformer) {
		copyModelParameters(model, transformer);
		copyOutputProperties(transformer);
		configureIndentation(transformer);
	}

	/**
	 * Configures the indentation settings for the supplied {@link Transformer}.
	 */
	protected final void configureIndentation(Transformer transformer) {
		if (this.indent) {
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			// Xalan-specific but doesn't interfere with other processors.
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		}
		else {
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
		}
	}

	/**
	 * Copies the configured output {@link Properties}, if any, into the
	 * {@link Transformer#setOutputProperty output property set} of the supplied
	 * {@link Transformer}.
	 */
	protected final void copyOutputProperties(Transformer transformer) {
		if (this.outputProperties != null) {
			Enumeration en = this.outputProperties.propertyNames();
			while (en.hasMoreElements()) {
				String name = (String) en.nextElement();
				transformer.setOutputProperty(name, this.outputProperties.getProperty(name));
			}
		}
	}

	/**
	 * Copies all entries from the supplied Map into the
	 * {@link Transformer#setParameter(String, Object) parameter set}
	 * of the supplied {@link Transformer}.
	 */
	protected final void copyModelParameters(Map model, Transformer transformer) {
		copyMapEntriesToTransformerParameters(model, transformer);
	}

	/**
	 * Configures the supplied {@link HttpServletResponse}. The default implementation of this
	 * method sets the {@link HttpServletResponse#setContentType content type} and
	 * {@link HttpServletResponse#setCharacterEncoding	encoding} from properties specified
	 * in the {@link Transformer}.
	 */
	protected void configureResponse(Map model, HttpServletResponse response, Transformer transformer) {
		response.setContentType(transformer.getOutputProperty(OutputKeys.MEDIA_TYPE));
		response.setCharacterEncoding(transformer.getOutputProperty(OutputKeys.ENCODING));
	}

	/**
	 * Load the {@link Templates} instance for the stylesheet at the configured location.
	 */
	private Templates loadTemplates() throws ApplicationContextException {
		Source stylesheetSource = getStylesheetSource();
		try {
			Templates templates = getTransformerFactory().newTemplates(stylesheetSource);
			if (logger.isDebugEnabled()) {
				logger.debug("Loading templates '" + templates + "'");
			}
			return templates;
		}
		catch (TransformerConfigurationException ex) {
			throw new ApplicationContextException("Can't load stylesheet from '" + getUrl() + "'", ex);
		}
		finally {
			closeSourceIfNecessary(stylesheetSource);
		}
	}

	/**
	 * Creates the {@link Transformer} instance used to prefer the XSLT transformation. Default implementation
	 * simply calls {@link Templates#newTransformer()}. Configures the {@link Transformer} with the custom
	 * {@link URIResolver} if specified.
	 */
	protected Transformer createTransformer(Templates templates) throws TransformerConfigurationException {
		Transformer transformer = templates.newTransformer();
		if (this.uriResolver != null) {
			transformer.setURIResolver(this.uriResolver);
		}
		return transformer;
	}

	/**
	 * Gets the XSLT {@link Source} for the XSLT template under the {@link #setUrl configured URL}.
	 */
	protected Source getStylesheetSource() {
		String url = getUrl();
		if (logger.isDebugEnabled()) {
			logger.debug("Loading XSLT stylesheet from '" + url + "'");
		}
		try {
			Resource stylesheetResource = getApplicationContext().getResource(url);
			String systemId = url.substring(0, url.lastIndexOf('/') + 1);
			return new StreamSource(stylesheetResource.getInputStream(), systemId);
		}
		catch (IOException ex) {
			throw new ApplicationContextException("Can't load XSLT stylesheet from '" + url + "'", ex);
		}
	}

	/**
	 * Copies all {@link Map.Entry entries} from the supplied {@link Map} into the
	 * {@link Transformer#setParameter(String, Object) parameter set} of the supplied
	 * {@link Transformer}.
	 */
	private void copyMapEntriesToTransformerParameters(Map map, Transformer transformer) {
		for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry entry = (Map.Entry) iterator.next();
			transformer.setParameter(ObjectUtils.nullSafeToString(entry.getKey()), entry.getValue());
		}
	}

	/**
	 * Closes the underlying resource managed by the supplied {@link Source} if applicable.
	 * Only works for {@link StreamSource StreamSources}.
	 */
	private void closeSourceIfNecessary(Source source) {
		if (source instanceof StreamSource) {
			StreamSource streamSource = (StreamSource) source;

			if (streamSource.getInputStream() != null) {
				try {
					streamSource.getInputStream().close();
				}
				catch (IOException e) {
					logger.warn("Unable to close InputStream '" + streamSource.getInputStream() + "'");
				}
			}
			else if (streamSource.getReader() != null) {
				try {
					streamSource.getReader().close();
				}
				catch (IOException e) {
					logger.warn("Unable to close Reader '" + streamSource.getReader() + "'");
				}
			}
		}
	}
}
