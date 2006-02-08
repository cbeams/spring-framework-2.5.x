package org.springframework.web.servlet.tags.form;

import org.springframework.util.ObjectUtils;

import javax.servlet.jsp.JspException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Rob Harrop
 */
public class CheckboxTag extends AbstractHtmlInputElementTag {

	private String value;

	public void setValue(String value) {
		this.value = value;
	}

	protected int writeTagContent(TagWriter tagWriter) throws JspException {
		tagWriter.startTag("input");
		writeDefaultAttributes(tagWriter);
		tagWriter.writeAttribute("type", "checkbox");

		Object boundValue = getValue();

		if (boundValue instanceof Boolean) {
			renderFromBoolean((Boolean) boundValue, tagWriter);
		}
		else if(boundValue != null) {

			if (this.value == null) {
				throw new IllegalArgumentException("Attribute 'value' is required when binding to non-Boolean values.");
			}

			Object resolvedValue = evaluate("value", this.value);

			if (boundValue.getClass().isArray()) {
				renderFromCollection(resolvedValue, toList(boundValue), tagWriter);
			}
			else if (boundValue instanceof Collection) {
				renderFromCollection(resolvedValue, (Collection) boundValue, tagWriter);
			}
			else {
				renderSingleValue(resolvedValue, boundValue, tagWriter);
			}
		}

		tagWriter.endTag();

		// write out the marker field
		tagWriter.startTag("input");
		tagWriter.writeAttribute("type", "hidden");
		tagWriter.writeAttribute("boundValue", "1");
		tagWriter.writeAttribute("name", "_" + getPath());
		tagWriter.endTag();

		return EVAL_PAGE;
	}

	private void renderSingleValue(Object resolvedValue, Object boundValue, TagWriter tagWriter) throws JspException {
		tagWriter.writeAttribute("value", ObjectUtils.nullSafeToString(resolvedValue));

		if (boundValue != null && resolvedValue.equals(boundValue)) {
			tagWriter.writeAttribute("checked", "true");
		}
	}

	private void renderFromCollection(Object resolvedValue, Collection boundValue, TagWriter tagWriter) throws JspException {
		tagWriter.writeAttribute("value", ObjectUtils.nullSafeToString(resolvedValue));

		if (boundValue.contains(resolvedValue)) {
			tagWriter.writeAttribute("checked", "true");
		}
	}

	private void renderFromBoolean(Boolean b, TagWriter tagWriter) throws JspException {
		tagWriter.writeAttribute("value", "true");
		if (b.booleanValue()) {
			tagWriter.writeAttribute("checked", "true");
		}
	}

	private List toList(Object boundValue) {
		List values = new ArrayList();
		boolean primitive = boundValue.getClass().getComponentType().isPrimitive();
		Object[] array = (primitive ? ObjectUtils.toObjectArray(boundValue) : (Object[]) boundValue);
		values.addAll(Arrays.asList(array));
		return values;
	}
}
