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

package org.springframework.web.servlet.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.support.RequestContext;

/**
 * Abstract View superclass. Standard framework View implementations
 * and application-specific custom Views can extend this class
 * to simplify their implementation. Subclasses should be JavaBeans.
 *
 * <p>Extends WebApplicationObjectSupport, which will be helpful to some views.
 * Handles static attributes, and merging static with dynamic attributes.
 * Subclasses just need to implement the actual rendering.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #renderMergedOutputModel
 */
public abstract class AbstractView extends WebApplicationObjectSupport implements View, BeanNameAware {

	/** Default content type. Overridable as bean property. */
	public static final String DEFAULT_CONTENT_TYPE = "text/html; charset=ISO-8859-1";


	private String beanName;

	private String contentType = DEFAULT_CONTENT_TYPE;

	private String requestContextAttribute;

	/** Map of static attributes, keyed by attribute name (String) */
	private final Map	staticAttributes = new HashMap();


	/**
	 * Set the view's name. Helpful for traceability.
	 * Framework code must call this when constructing views.
	 * @param beanName the view's name. May not be null.
	 * Views should use this for log messages.
	 */
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	/**
	 * Return the view's name. Should never be null,
	 * if the view was correctly configured.
	 */
	public String getBeanName() {
		return beanName;
	}

	/**
	 * Set the content type for this view.
	 * Default is "text/html; charset=ISO-8859-1".
	 * <p>May be ignored by subclasses if the view itself is assumed
	 * to set the content type, e.g. in case of JSPs.
	 * @param contentType content type for this view
	 * @see #DEFAULT_CONTENT_TYPE
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Return the content type for this view.
	 */
	public String getContentType() {
		return this.contentType;
	}

	/**
	 * Set the name of the RequestContext attribute for this view.
	 * Default is none.
	 */
	public void setRequestContextAttribute(String requestContextAttribute) {
		this.requestContextAttribute = requestContextAttribute;
	}

	/**
	 * Return the name of the RequestContext attribute, if any.
	 */
	public String getRequestContextAttribute() {
			return requestContextAttribute;
	}
    
	/**
	 * Set static attributes as a CSV string.
	 * Format is: attname0={value1},attname1={value1}
	 */
	public void setAttributesCSV(String propString) throws IllegalArgumentException {
		if (propString == null) {
			// leave static attributes unchanged
			return;
		}

		StringTokenizer st = new StringTokenizer(propString, ",");
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			int eqIdx = tok.indexOf("=");
			if (eqIdx == -1) {
				throw new IllegalArgumentException("Expected = in attributes CSV string '" + propString + "'");
			}
			if (eqIdx >= tok.length() - 2) {
				throw new IllegalArgumentException(
						"At least 2 characters ([]) required in attributes CSV string '" + propString + "'");
			}
			String name = tok.substring(0, eqIdx);
			String value = tok.substring(eqIdx + 1);

			// celete first and last characters of value: { and }
			value = value.substring(1);
			value = value.substring(0, value.length() - 1);

			if (logger.isDebugEnabled()) {
				logger.debug("Set static attribute with name '" + name + "' and value [" + value + "] on view");
			}
			addStaticAttribute(name, value);
		}
	}

	/**
	 * Set static attributes from a java.util.Properties object. This is
	 * the most convenient way to set static attributes. Note that static
	 * attributes can be overridden by dynamic attributes, if a value
	 * with the same name is included in the model.
	 * <p>Can be populated with a String "value" (parsed via PropertiesEditor)
	 * or a "props" element in XML bean definitions.
	 * @see org.springframework.beans.propertyeditors.PropertiesEditor
	 */
	public void setAttributes(Properties props) {
		setAttributesMap(props);
	}

	/**
	 * Set static attributes from a Map. This allows to set any kind
	 * of attribute values, for example bean references.
	 * <p>Can be populated with a "map" or "props" element in XML bean
	 * definitions.
	 * @param attributes Map with name Strings as keys and attribute
	 * objects as values
	 */
	public void setAttributesMap(Map attributes) {
		if (attributes != null) {
			Iterator it = attributes.keySet().iterator();
			while (it.hasNext()) {
				String name = (String) it.next();
				Object value = attributes.get(name);
				addStaticAttribute(name, value);
			}
		}
	}

	/**
	 * Allow Map access to the static attributes of this view,
	 * with the option to add or override specific entries.
	 * <p>Useful for specifying entries directly, for example via
	 * "attributesMap[myKey]". This is particularly useful for
	 * adding or overriding entries in child view definitions.
	 */
	public Map getAttributesMap() {
		return this.staticAttributes;
	}

	/**
	 * Add static data to this view, exposed in each view.
	 * <p>Must be invoked before any calls to render().
	 * @param name name of attribute to expose
	 * @param value object to expose
	 */
	public void addStaticAttribute(String name, Object value) {
		this.staticAttributes.put(name, value);
		if (logger.isDebugEnabled()) {
			logger.debug("Set static attribute with name '" + name + "' and value [" + value + "] on view");
		}
	}

	/**
	 * Return the static attributes held in this view. Handy for testing.
	 * <p>Returns an unmodifiable Map, as this is not intended for
	 * manipulating the Map but rather just for checking the contents.
	 * @return the static attributes in this view
	 */
	public Map getStaticAttributes() {
		return Collections.unmodifiableMap(this.staticAttributes);
	}


	/**
	 * Prepares the view given the specified model, merging it with static
	 * attributes and a RequestContext attribute, if necessary.
	 * Delegates to renderMergedOutputModel for the actual rendering.
	 * @see #renderMergedOutputModel
	 */
	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Rendering view with name '" + this.beanName + "' with model " + model +
				" and static attributes " + this.staticAttributes);
		}

		// consolidate static and dynamic model attributes
		Map mergedModel = new HashMap(this.staticAttributes.size() + (model != null ? model.size() : 0));
		mergedModel.putAll(this.staticAttributes);
		if (model != null) {
			mergedModel.putAll(model);
		}

		// expose RequestContext?
		if (this.requestContextAttribute != null) {
			mergedModel.put(this.requestContextAttribute, createRequestContext(request, mergedModel));
		}

		renderMergedOutputModel(mergedModel, request, response);
	}

	/**
	 * Create a RequestContext to expose under the specified attribute name.
	 * <p>Default implementation creates a standard RequestContext instance for the
	 * given request and model. Can be overridden in subclasses for custom instances.
	 * @param request current HTTP request
	 * @param model combined output Map, with dynamic values taking precedence
	 * over static attributes
	 * @return the RequestContext instance
	 * @see #setRequestContextAttribute
	 * @see org.springframework.web.servlet.support.RequestContext
	 */
	protected RequestContext createRequestContext(HttpServletRequest request, Map model) {
		return new RequestContext(request, model);
	}

	/**
	 * Subclasses must implement this method to actually render the view.
	 * <p>The first step will be preparing the request: In the JSP case,
	 * this would mean setting model objects as request attributes.
	 * The second step will be the actual rendering of the view,
	 * for example including the JSP via a RequestDispatcher.
	 * @param model combined output Map, with dynamic values taking precedence
	 * over static attributes
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws Exception if rendering failed
	 */
	protected abstract void renderMergedOutputModel(
			Map model, HttpServletRequest request, HttpServletResponse response) throws Exception;

}
