package org.springframework.web.servlet.tags.form;

import javax.servlet.jsp.JspException;

/**
 * Abstract base class to provide common methods for
 * implementing databinding-aware JSP tags for rendering an HTML '<code>input</code>'
 * element with a '<code>type</code>' of '<code>checkbox</code>'.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Thomas Risberg
 * @since 2.5
 */
public abstract class AbstractCheckboxTag extends AbstractHtmlInputElementTag {

	/**
	 * Writes the '<code>input(checkbox)</code>' to the supplied {@link org.springframework.web.servlet.tags.form.TagWriter},
	 * marking it as 'checked' if appropriate.
	 */
	protected abstract int writeTagContent(TagWriter tagWriter) throws JspException;

	/**
	 * Render the '<code>input(checkbox)</code>' with the supplied value, marking the
	 * '<code>input</code>' element as 'checked' if the supplied value matches the
	 * bound value.
	 */
	protected void renderSingleValue(Object resolvedValue, TagWriter tagWriter) throws JspException {
		tagWriter.writeAttribute("value", getDisplayString(resolvedValue, getPropertyEditor()));
		if (SelectedValueComparator.isSelected(getBindStatus(), resolvedValue)) {
			tagWriter.writeAttribute("checked", "checked");
		}
	}

	/**
	 * Render the '<code>input(checkbox)</code>' with the supplied value, marking
	 * the '<code>input</code>' element as 'checked' if the supplied value is
	 * present in the bound Collection value.
	 */
	protected void renderFromCollection(Object resolvedValue, TagWriter tagWriter) throws JspException {
		tagWriter.writeAttribute("value", getDisplayString(resolvedValue, getPropertyEditor()));
		if (SelectedValueComparator.isSelected(getBindStatus(), resolvedValue)) {
			tagWriter.writeAttribute("checked", "checked");
		}
	}

	/**
	 * Render the '<code>input(checkbox)</code>' with the supplied value, marking
	 * the '<code>input</code>' element as 'checked' if the supplied Boolean is
	 * <code>true</code>.
	 */
	protected void renderFromBoolean(Boolean boundValue, TagWriter tagWriter) throws JspException {
		tagWriter.writeAttribute("value", "true");
		if (boundValue.booleanValue()) {
			tagWriter.writeAttribute("checked", "checked");
		}
	}

	/**
	 * Return a unique ID for the bound name within the current PageContext.
	 */
	protected String autogenerateId() throws JspException {
		return TagIdGenerator.nextId(getName(), this.pageContext);
	}

}
