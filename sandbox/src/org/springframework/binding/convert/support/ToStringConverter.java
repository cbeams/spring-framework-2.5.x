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
package org.springframework.binding.convert.support;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.binding.format.FormatterLocator;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.visitor.ReflectiveVisitorSupport;
import org.springframework.util.visitor.Visitor;

/**
 * Converts textual representations of numbers to a <code>Number</code>
 * specialization. Delegates to a synchronized formatter to parse text strings.
 * @author Keith Donald
 */
public class ToStringConverter extends AbstractFormattingConverter implements Visitor {

	private static final String EMPTY = "[empty]";

	private static final String NULL = "[null]";

	private static final String COLLECTION = "collection";

	private static final String SET = "set";

	private static final String LIST = "list";

	private static final String MAP = "map";

	private static final String ARRAY = "array";

	private ReflectiveVisitorSupport reflectiveVisitorSupport = new ReflectiveVisitorSupport();

	public ToStringConverter(FormatterLocator locator) {
		super(locator);
	}

	public Class[] getSourceClasses() {
		return new Class[] { Object.class };
	}

	public Class[] getTargetClasses() {
		return new Class[] { String.class };
	}

	protected Object doConvert(Object source, Class targetClass) throws Exception {
		return style(source);
	}

	String style(Object source) {
		return (String)reflectiveVisitorSupport.invokeVisit(this, source);
	}

	String visit(String value) {
		return ('\'' + value + '\'');
	}

	String visit(Number value) {
		return getFormatterLocator().getNumberFormatter(value.getClass()).formatValue(value);
	}

	String visit(Class clazz) {
		return ClassUtils.getShortName(clazz);
	}

	String visit(Method method) {
		return method.getName() + "@" + ClassUtils.getShortName(method.getDeclaringClass());
	}

	String visit(Map value) {
		StringBuffer buffer = new StringBuffer(value.size() * 8 + 16);
		buffer.append(MAP + "[");
		for (Iterator i = value.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry)i.next();
			buffer.append(style(entry));
			if (i.hasNext()) {
				buffer.append(',').append(' ');
			}
		}
		if (value.isEmpty()) {
			buffer.append(EMPTY);
		}
		buffer.append("]");
		return buffer.toString();
	}

	String visit(Map.Entry value) {
		return style(value.getKey()) + " -> " + style(value.getValue());
	}

	String visit(Collection value) {
		StringBuffer buffer = new StringBuffer(value.size() * 8 + 16);
		buffer.append(getCollectionTypeString(value) + "[");
		for (Iterator i = value.iterator(); i.hasNext();) {
			buffer.append(style(i.next()));
			if (i.hasNext()) {
				buffer.append(',').append(' ');
			}
		}
		if (value.isEmpty()) {
			buffer.append(EMPTY);
		}
		buffer.append("]");
		return buffer.toString();
	}

	private String getCollectionTypeString(Collection value) {
		if (value instanceof List) {
			return LIST;
		}
		else if (value instanceof Set) {
			return SET;
		}
		else {
			return COLLECTION;
		}
	}

	String visit(Object value) {
		if (value.getClass().isArray()) {
			return styleArray(getObjectArray(value));
		}
		else {
			return String.valueOf(value);
		}
	}

	String visitNull() {
		return NULL;
	}

	private String styleArray(Object[] array) {
		StringBuffer buffer = new StringBuffer(array.length * 8 + 16);
		buffer.append(ARRAY + "<" + StringUtils.delete(ClassUtils.getShortName(array.getClass()), ";") + ">[");
		for (int i = 0; i < array.length - 1; i++) {
			buffer.append(style(array[i]));
			buffer.append(',').append(' ');
		}
		if (array.length > 0) {
			buffer.append(style(array[array.length - 1]));
		}
		else {
			buffer.append(EMPTY);
		}
		buffer.append("]");
		return buffer.toString();
	}

	private Object[] getObjectArray(Object value) {
		if (value.getClass().getComponentType().isPrimitive()) {
			return ObjectUtils.toObjectArray(value);
		}
		else {
			return (Object[])value;
		}
	}
}