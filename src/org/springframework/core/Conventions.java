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

package org.springframework.core;

import java.util.Collection;
import java.util.Iterator;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Provides methods to support various naming and other conventions used
 * throughout the framework. Mainly for internal use within the framework.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public abstract class Conventions {

	/**
	 * Suffix added to names when using arrays.
	 */
	private static final String PLURAL_SUFFIX = "List";


	/**
	 * Determine the conventional variable name for the supplied
	 * code>Object</code> based on its concrete type. The convention
	 * used is to return the uncapitalized short name of the <code>Class</code>.
	 * So, <code>com.myapp.Product</code> becomes <code>product</code>.
	 * <p>For arrays, we use the pluralized version of the array component type.
	 * For <code>Collection</code>s we attempt to 'peek ahead' in the
	 * <code>Collection</code> to determine the component type and
	 * return the pluralized version of that component type.
	 */
	public static String getVariableName(Object value) {
		Class valueClass = value.getClass();
		boolean pluralize = false;

		if (valueClass.isArray()) {
			valueClass = valueClass.getComponentType();
			pluralize = true;
		}
		else if (value instanceof Collection) {
			Collection collection = (Collection) value;
			if (collection.isEmpty()) {
				throw new IllegalArgumentException("Cannot generate variable name for an empty Collection");
			}
			valueClass = peekAhead(collection);
			pluralize = true;
		}

		String name = StringUtils.uncapitalize(ClassUtils.getShortName(valueClass));
		return (pluralize ? pluralize(name) : name);
	}

	/**
	 * Convert <code>String</code>s in attribute name format (lowercase, hyphens separating words)
	 * into property name format (camel-cased). For example, <code>transaction-manager</code> is
	 * converted into <code>transactionManager</code>.
	 */
	public static String attributeNameToPropertyName(String attributeName) {
		Assert.notNull(attributeName, "'attributeName' cannot be null.");
		if (attributeName.indexOf("-") == -1) {
			return attributeName;
		}
		char[] chars = attributeName.toCharArray();
		char[] result = new char[chars.length -1]; // not completely accurate but good guess
		int currPos = 0;
		boolean upperCaseNext = false;
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if(c == '-') {
				upperCaseNext = true;
				continue;
			} else if (upperCaseNext) {
				result[currPos++] = Character.toUpperCase(c);
				upperCaseNext = false;
			} else {
				result[currPos++] = c;
			}
		}
		return new String(result, 0, currPos);
	}

	/**
	 * Pluralize the given name.
	 */
	private static String pluralize(String name) {
		return name + PLURAL_SUFFIX;
	}

	/**
	 * Retrieve the <code>Class</code> of an element in the <code>Collection</code>.
	 * The exact element for which the <code>Class</code> is retreived will depend
	 * on the concrete <code>Collection</code> implementation.
	 */
	private static Class peekAhead(Collection collection) {
		Iterator it = collection.iterator();
		if (it.hasNext()) {
			return it.next().getClass();
		}
		else {
			throw new IllegalStateException(
					"Unable to peek ahead in non-empty collection - no element found");
		}
	}

	/**
	 * Return an attribute name qualified by the supplied enclosing {@link Class}. For example,
	 * the attribute name '<code>foo</code>' qualified by {@link Class} '<code>com.myapp.SomeClass</code>'
	 * would be '<code>com.myapp.SomeClass.foo</code>'
	 */
	public static String getQualifiedAttributeName(Class enclosingClass, String attributeName) {
		Assert.notNull(enclosingClass, "'enclosingClass' cannot be null.");
		Assert.notNull(attributeName, "'attributeName' cannot be null.");
		return enclosingClass.getName() + "." + attributeName;
	}

}
