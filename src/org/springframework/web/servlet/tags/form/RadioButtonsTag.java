package org.springframework.web.servlet.tags.form;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import javax.servlet.jsp.JspException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Databinding-aware JSP tag for rendering an HTML '<code>input</code>'
 * element with a '<code>type</code>' of '<code>radio</code>'.
 *
 * <p>Rendered elements are marked as 'checked' if the configured
 * {@link #setItems(Object) value} matches the {@link #getValue bound value}.
 *
 * <p>A typical usage pattern will involved multiple tag instances bound
 * to the same property but with different values.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public class RadioButtonsTag extends AbstractHtmlInputElementTag {

	/**
	 * The {@link java.util.Collection}, {@link java.util.Map} or array of
	 * objects used to generate the '<code>input type="checkbox"</code>' tags.
	 */
	private Object items;

	/**
	 * The name of the property mapped to the '<code>value</code>' attribute
	 * of the '<code>input type="checkbox"</code>' tag.
	 */
	private String itemValue;

	/**
	 * The value to be displayed as part
	 * of the '<code>input type="checkbox"</code>' tag.
	 */
	private String itemLabel;

	/**
	 * The HTML '<code>span</code>' tag.
	 */
	public static final String SPAN_TAG = "span";

	private String element = SPAN_TAG;

	/**
	 * Delimiter to use between each '<code>input type="checkbox"</code>' tag.
	 */
	private String delimiter;


	/**
	 * Set the {@link java.util.Collection}, {@link java.util.Map} or array
	 * of objects used to generate the '<code>input(checkbox)</code>' tags.
	 * <p>Typically a runtime expression.
	 * @param items said items
	 * @throws IllegalArgumentException if the supplied <code>items</code> instance is <code>null</code>
	 */
	public void setItems(Object items) {
		this.items = items;
	}

	/**
	 * Get the {@link java.util.Collection}, {@link java.util.Map} or array
	 * of objects used to generate the '<code>input(checkbox)</code>' tags.
	 * <p>Typically a runtime expression.
	 */
	protected Object getItems() {
		return this.items;
	}

	/**
	 * Set the name of the property mapped to the '<code>value</code>' attribute
	 * of the '<code>input(checkbox)</code>' tag.
	 * <p>May be a runtime expression.
	 */
	public void setItemValue(String itemValue) {
		Assert.hasText(itemValue, "'itemValue' must not be empty");
		this.itemValue = itemValue;
	}

	protected String getItemValue() {
		return this.itemValue;
	}

	/**
	 * Set the value to be displayed as part
	 * of the '<code>input(checkbox)</code>' tag.
	 * <p>May be a runtime expression.
	 */
	public void setItemLabel(String itemLabel) {
		Assert.hasText(itemLabel, "'itemLabel' must not be empty");
		this.itemLabel = itemLabel;
	}

	/**
	 * Get the value to be displayed as part
	 * of the '<code>input(checkbox)</code>' tag.
	 * <p>May be a runtime expression.
	 */
	protected String getItemLabel() {
		return this.itemLabel;
	}

	/**
	 * Set the delimiter to be used between each
	 * '<code>input type="checkbox"</code>' tag.
	 * <p>By default, there is <em>no</em> delimiter.
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * Return the delimiter to be used between each
	 * '<code>input type="checkbox"</code>' tag.
	 */
	public String getDelimiter() {
		return this.delimiter;
	}

	/**
	 * Set the HTML element used to enclose the
	 * '<code>input type="checkbox"</code>' tag.
	 * <p>Defaults to an HTML '<code>&lt;span/&gt;</code>' tag.
	 */
	public void setElement(String element) {
		Assert.hasText(element, "'element' cannot be null or blank");
		this.element = element;
	}

	/**
	 * Get the HTML element used to enclose
	 * '<code>input type="checkbox"</code>' tag.
	 */
	public String getElement() {
		return this.element;
	}


	/**
	 * Renders the '<code>input(radio)</code>' element with the configured
	 * {@link #setItems(Object) value}. Marks the element as checked if the
	 * value matches the {@link #getValue bound value}.
	 */
	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		Object items = getItems();
		Object itemsObject = (items instanceof String ? evaluate("items", (String) items) : items);

		String itemValue = getItemValue();
		String itemLabel = getItemLabel();
		String valueProperty =
				(itemValue != null ? ObjectUtils.getDisplayString(evaluate("itemValue", itemValue)) : null);
		String labelProperty =
				(itemLabel != null ? ObjectUtils.getDisplayString(evaluate("itemLabel", itemLabel)) : null);

		if (itemsObject == null) {
			throw new IllegalArgumentException("Attribute 'items' is required and must be a Collection, an Array or a Map");
		}

		if (itemsObject.getClass().isArray()) {
			Object[] itemsArray = (Object[]) itemsObject;
			for (int i = 0; i < itemsArray.length; i++) {
				Object item = itemsArray[i];
				writeObjectEntry(tagWriter, valueProperty, labelProperty, item, i);
			}
		}
		else if (itemsObject instanceof Collection) {
			final Collection optionCollection = (Collection) itemsObject;
			int itemIndex = 0;
			for (Iterator it = optionCollection.iterator(); it.hasNext(); itemIndex++) {
				Object item = it.next();
				writeObjectEntry(tagWriter, valueProperty, labelProperty, item, itemIndex);
			}
		}
		else if (itemsObject instanceof Map) {
			final Map optionMap = (Map) itemsObject;
			int itemIndex = 0;
			for (Iterator it = optionMap.entrySet().iterator(); it.hasNext(); itemIndex++) {
				Map.Entry entry = (Map.Entry) it.next();
				writeMapEntry(tagWriter, valueProperty, labelProperty, entry, itemIndex);
			}
		}
		else {
			throw new IllegalArgumentException("Attribute 'items' must be a Collection, an Array or a Map");
		}

//		if (!isDisabled()) {
//			// Write out the 'field was present' marker.
//			tagWriter.startTag("input");
//			tagWriter.writeAttribute("type", "hidden");
//			tagWriter.writeAttribute("name", WebDataBinder.DEFAULT_FIELD_MARKER_PREFIX + getName());
//			tagWriter.writeAttribute("value", "on");
//			tagWriter.endTag();
//		}

		return EVAL_PAGE;
	}

	private void writeObjectEntry(TagWriter tagWriter, String valueProperty,
			String labelProperty, Object item, int itemIndex) throws JspException {
		BeanWrapper wrapper = new BeanWrapperImpl(item);
		Object renderValue = (valueProperty != null ? wrapper.getPropertyValue(valueProperty) : item);
		Object renderLabel = (labelProperty != null ? wrapper.getPropertyValue(labelProperty) : item);
		writeCheckboxTag(tagWriter, renderValue, renderLabel, itemIndex);
	}

	private void writeMapEntry(TagWriter tagWriter, String valueProperty,
			String labelProperty, Map.Entry entry, int itemIndex) throws JspException {
		Object mapKey = entry.getKey();
		Object mapValue = entry.getValue();
		BeanWrapper mapKeyWrapper = new BeanWrapperImpl(mapKey);
		BeanWrapper mapValueWrapper = new BeanWrapperImpl(mapValue);
		Object renderValue = (valueProperty != null ? mapKeyWrapper.getPropertyValue(valueProperty)
				: mapKey.toString());
		Object renderLabel = (labelProperty != null ? mapValueWrapper.getPropertyValue(labelProperty)
				: mapValue.toString());
		writeCheckboxTag(tagWriter, renderValue, renderLabel, itemIndex);
	}

	private void writeCheckboxTag(TagWriter tagWriter, Object value, Object label, int itemIndex) throws JspException {
		tagWriter.startTag(getElement());
		if (itemIndex > 0 && this.getDelimiter() != null) {
			tagWriter.appendValue(ObjectUtils.getDisplayString(evaluate("delimiter", this.getDelimiter())));
		}
		tagWriter.startTag("input");
		writeDefaultAttributes(tagWriter);
		tagWriter.writeAttribute("type", "radio");
		tagWriter.writeAttribute("value", getDisplayString(value, getPropertyEditor()));
		if (SelectedValueComparator.isSelected(getBindStatus(), value)) {
			tagWriter.writeAttribute("checked", "checked");
		}
		tagWriter.appendValue(label.toString());
		tagWriter.endTag();
		tagWriter.endTag();
	}

	/**
	 * Return a unique ID for the bound name within the current PageContext.
	 */
	protected String autogenerateId() throws JspException {
		return TagIdGenerator.nextId(getName(), this.pageContext);
	}

}
