/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
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
 * Abstract view superclass. Standard framework view implementations
 * and application-specific custom views can extend this class
 * to simplify their implementation. Subclasses should be JavaBeans.
 *
 * <p>Extends ApplicationObjectSupport, which will be helpful to some views.
 * Handles static attributes, and merging static with dynamic attributes.
 * Subclasses just need to implement the actual rendering.
 *
 * <p>It's recommended that subclasses <b>don't</b> cache anything, in the
 * quest for efficiency. This class offers caching. However, it's possible
 * to disable this class's caching, which is useful during development.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @version $Id: AbstractView.java,v 1.7 2004-02-07 00:18:27 jhoeller Exp $
 * @see #renderMergedOutputModel
 */
public abstract class AbstractView extends WebApplicationObjectSupport implements View, BeanNameAware {

	/** The name by which this View is known */
	private String beanName;

	/** Default content type. Overridable as bean property. */
	private String contentType = "text/html; charset=ISO-8859-1";

	/** Name of request context attribute, or null if not needed */
	private String requestContextAttribute;

	/** Map of static attributes, keyed by attribute name (String) */
	private final Map	staticAttributes = new HashMap();


	/**
	 * Set the view's name. Helpful for traceability.
	 * Framework code must call this when constructing views.
	 * @param beanName the view's name. May not be null.
	 * Views should use this for log messages.
	 */
	public final void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	/**
	 * Return the view's name. Should never be null,
	 * if the view was correctly configured.
	 * @return the view's name
	 */
	public final String getBeanName() {
		return beanName;
	}

	/**
	 * Set the content type for this view.
	 * May be ignored by subclasses if the view itself is assumed
	 * to set the content type, e.g. in case of JSPs.
	 * @param contentType content type for this view
	 */
	public final void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * Return the content type for this view.
	 * @return content type for this view
	 */
	protected final String getContentType() {
		return this.contentType;
	}

	/**
	 * Set the name of the RequestContext attribute for this view,
	 * or null if not needed.
	 * @param requestContextAttribute name of the RequestContext attribute
	 */
	public final void setRequestContextAttribute(String requestContextAttribute) {
		this.requestContextAttribute = requestContextAttribute;
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
	public final void setAttributes(Properties props) {
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
	public final void setAttributesMap(Map attributes) {
		if (attributes != null) {
			Iterator itr = attributes.keySet().iterator();
			while (itr.hasNext()) {
				String name = (String) itr.next();
				Object value = attributes.get(name);
				addStaticAttribute(name, value);
			}
		}
	}

	/**
	 * Set static attributes as a CSV string.
	 * Format is attname0={value1},attname1={value1}
	 */
	public final void setAttributesCSV(String propString) throws IllegalArgumentException {
		if (propString == null)
			// Leave static attributes unchanged
			return;

		StringTokenizer st = new StringTokenizer(propString, ",");
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			int eqindx = tok.indexOf("=");
			if (eqindx == -1)
				throw new IllegalArgumentException("Expected = in View string '" + propString + "'");

			if (eqindx >= tok.length() - 2)
				throw new IllegalArgumentException("At least 2 characters ([]) required in View string '" + propString + "'");

			String name = tok.substring(0, eqindx);
			String val = tok.substring(eqindx + 1);

			// Delete first and last characters of value: { and }
			val = val.substring(1);
			val = val.substring(0, val.length() - 1);

			if (logger.isDebugEnabled()) {
				logger.info("Set static attribute with name '" + name + "' and value [" + val + "] on view");
			}
			addStaticAttribute(name, val);
		}
	}

	/**
	 * Add static data to this view, exposed in each view.
	 * <p>Must be invoked before any calls to render().
	 * @param name name of attribute to expose
	 * @param value object to expose
	 */
	public final void addStaticAttribute(String name, Object value) {
		logger.debug("Set static attribute with name '" + name + "' and value [" + value + "] on view");
		this.staticAttributes.put(name, value);
	}

	/**
	 * Handy for testing. Return the static attributes held in this view.
	 * @return the static attributes in this view
	 */
	public final Map getStaticAttributes() {
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
			logger.debug("Rendering view with name '" + this.beanName + "' with model=[" + model +
				"] and static attributes=[" + this.staticAttributes + "]");
		}

		// Consolidate static and dynamic model attributes
		Map mergedModel = new HashMap(this.staticAttributes);
		mergedModel.putAll(model);

		// expose RequestContext?
		if (this.requestContextAttribute != null) {
			mergedModel.put(this.requestContextAttribute, new RequestContext(request, mergedModel));
		}

		renderMergedOutputModel(mergedModel, request, response);
	}

	/** 
	 * Subclasses must implement this method to render the view.
	 * <p>The first take will be preparing the request: This may include setting
	 * the model elements as request attributes, e.g. in the case of a JSP view.
	 * @param model combined output Map, with dynamic values taking precedence
	 * over static attributes
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @throws Exception if rendering failed
	 */
	protected abstract void renderMergedOutputModel(Map model, HttpServletRequest request,
	                                                HttpServletResponse response) throws Exception;

}
