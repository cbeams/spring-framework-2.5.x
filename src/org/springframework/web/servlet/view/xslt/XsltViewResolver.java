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

import org.springframework.util.Assert;
import org.springframework.util.xml.SimpleTransformErrorListener;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.URIResolver;
import java.util.Properties;

/**
 * {@link org.springframework.web.servlet.ViewResolver} implementation that
 * resolves instances of {@link XsltView} by translating the supplied view name into
 * the URL of the XSLT stylesheet.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class XsltViewResolver extends UrlBasedViewResolver {

	private ErrorListener errorListener = new SimpleTransformErrorListener(logger);

	private URIResolver uriResolver;

	private boolean cacheTemplates = true;

	private String sourceKey;

	private Properties outputProperties;

	private boolean indent = true;

	public XsltViewResolver() {
		setViewClass(XsltView.class);
	}

	/**
	 * @see XsltView#setCacheTemplates
	 */
	public void setCacheTemplates(boolean cacheTemplates) {
		this.cacheTemplates = cacheTemplates;
	}

	/**
	 * @see XsltView#setUriResolver
	 */
	public void setUriResolver(URIResolver uriResolver) {
		this.uriResolver = uriResolver;
	}

	/**
	 * @see XsltView#setErrorListener
	 */
	public void setErrorListener(ErrorListener errorListener) {
		Assert.notNull(errorListener, "'errorListener' cannot be null.");
		this.errorListener = errorListener;
	}

	/**
	 * @see XsltView#setSourceKey
	 */
	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
	}

	/**
	 * @see XsltView#setOutputProperties
	 */
	public void setOutputProperties(Properties outputProperties) {
		this.outputProperties = outputProperties;
	}

	/**
	 * @see XsltView#setIndent(boolean)
	 */
	public void setIndent(boolean indent) {
		this.indent = indent;
	}

	protected Class requiredViewClass() {
		return XsltView.class;
	}

	protected AbstractUrlBasedView buildView(String viewName) throws Exception {
		XsltView view = (XsltView) super.buildView(viewName);
		view.setUriResolver(this.uriResolver);
		view.setErrorListener(this.errorListener);
		view.setSourceKey(this.sourceKey);
		view.setOutputProperties(this.outputProperties);
		view.setIndent(this.indent);
		return view;
	}
}
