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

package org.springframework.core.style;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ReflectiveVisitorHelper;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * Converts objects to string form, generally for debugging purposes,
 * using Spring's <code>toString</code> styling conventions.
 *
 * <p>Uses the reflective visitor pattern underneath the hood to nicely
 * encapsulate styling algorithms for each type of styled object.
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 1.2.2
 */
public class DefaultValueStyler implements ValueStyler {

	private static final String EMPTY = "[empty]";
	private static final String NULL = "[null]";
	private static final String COLLECTION = "collection";
	private static final String SET = "set";
	private static final String LIST = "list";
	private static final String MAP = "map";
	private static final String ARRAY = "array";


	private final ReflectiveVisitorHelper reflectiveVisitorHelper = new ReflectiveVisitorHelper();


	public String style(Object value) {
		return (String) reflectiveVisitorHelper.invokeVisit(this, value);
	}

	String visit(String value) {
		return ('\'' + value + '\'');
	}

	String visit(Number value) {
		return String.valueOf(value);
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
			return styleArray(ObjectUtils.toObjectArray(value));
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
		buffer.append(ARRAY + "<" + ClassUtils.getShortName(array.getClass().getComponentType()) + ">[");
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

}
