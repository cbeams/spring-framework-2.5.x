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

package org.springframework.web.servlet.tags.form;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.support.BindStatus;
import org.springframework.web.servlet.tags.NestedPathTag;

import javax.servlet.jsp.JspException;

/**
 * Base tag for all data-binding aware JSP form tags. Provides the common
 * {@link #setPath path} and {@link #setId id} properties. Provides sub-classes
 * with utility methods for accessing the {@link BindStatus} of their bound value
 * and also for {@link #writeOptionalAttribute interacting} with the {@link TagWriter}.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public abstract class AbstractDataBoundFormElementTag extends AbstractFormTag {

	/**
	 * The '<code>id</code>' attribute of the rendered HTML tag.
	 */
	public static final String ID_ATTRIBUTE = "id";

	/**
	 * The {@link BindStatus} of this tag.
	 */
	private BindStatus bindStatus;

	/**
	 * The value of the '<code>id</code>' attribute.
	 */
	private String id;

	/**
	 * The property path from the {@link FormTag#setCommandName command object}.
	 */
	private String path;

	/**
	 * Sets the property path from the {@link FormTag#setCommandName command object}.
	 * May be a runtime expression. Required.
	 */
	public void setPath(String path) {
		Assert.hasText(path, "'path' cannot be null or zero length.");
		this.path = path;
	}

	/**
	 * Sets the value of the '<code>id</code>' attribute. Defaults to the value of
	 * {@link #getName}. May be a runtime expression.
	 */
	public void setId(String id) {
		Assert.notNull(id, "'id' cannot be null.");
		this.id = id;
	}

	/**
	 * Gets the value of the '<code>id</code>' attribute.
	 * May be a runtime expression.
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Gets the {@link #evaluate resolved} property path from the
	 * {@link FormTag#setCommandName command object}.
	 */
	protected final String getPath() throws JspException {
		return (String) evaluate("path", this.path);
	}

	/**
	 * Writes the default set of attributes to the supplied {@link TagWriter}.
	 * Further abstract sub-classes should override this method to add in
	 * any additional default attributes but <strong>must</strong> remember
	 * to call the <code>super</code> method.
	 * <p/>Concrete sub-classes should call this method when/if they want
	 * to render default attributes.
	 */
	protected void writeDefaultAttributes(TagWriter tagWriter) throws JspException {
		String id = getId();
		if (StringUtils.hasText(id)) {
			tagWriter.writeAttribute(ID_ATTRIBUTE, ObjectUtils.getDisplayString(evaluate(ID_ATTRIBUTE, id)));
		}
		else {
			// write the default id which matches the name
			tagWriter.writeAttribute(ID_ATTRIBUTE, getName());
		}
		tagWriter.writeAttribute("name", getName());
	}

	/**
	 * Gets the value for the HTML '<code>name</code>' attribute. The default
	 * implementation simply delegates to {@link #getPath} to use the property
	 * path as the name. For the most part this is desirable as it links with
	 * the server-side expectation for databinding. However, some subclasses
	 * may wish to change the value of the '<code>name</code>' attribute without
	 * changing the bind path.
	 */
	protected String getName() throws JspException {
		return getPath();
	}

	/**
	 * Gets the bound value.
	 * @see #getBindStatus()
	 */
	protected final Object getBoundValue() throws JspException {
		return getBindStatus().getValue();
	}

	/**
	 * Gets the {@link BindStatus} for this tag.
	 */
	protected BindStatus getBindStatus() throws JspException {
		if (this.bindStatus == null) {
			String resolvedPropertyPath = getPath();
			String bindPath = getBindPath(resolvedPropertyPath);
			this.bindStatus = new BindStatus(getRequestContext(), bindPath, this.isHtmlEscape());
		}
		return this.bindStatus;
	}

	/**
	 * Gets the final bind path including the exposed {@link FormTag command name} and
	 * any {@link NestedPathTag nested paths}.
	 */
	private String getBindPath(String resolvedSubPath) {

		StringBuffer sb = new StringBuffer();
		sb.append(getCommandName()).append('.');

		String nestedPath = getNestedPath();
		if(nestedPath != null) {
			sb.append(nestedPath).append('.');
		}

		return sb.append(resolvedSubPath).toString();
	}

	/**
	 * Gets the value of the nested path that may have been exposed by the
	 * {@link NestedPathTag}.
	 */
	private String getNestedPath() {
		return (String) this.pageContext.getAttribute(NestedPathTag.NESTED_PATH_VARIABLE_NAME);
	}

	private String getCommandName() {
		return (String) this.pageContext.getAttribute(FormTag.COMMAND_NAME_VARIABLE_NAME);
	}

	public void doFinally() {
		super.doFinally();
		this.bindStatus = null;
	}

}
