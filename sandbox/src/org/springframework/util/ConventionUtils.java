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

package org.springframework.util;

import java.util.Collection;
import java.util.Iterator;

/**
 * Provides methods to support various naming and other conventions used
 * throughout the framework.
 *
 * @author Rob Harrop
 * @since 2.0
 */
public abstract class ConventionUtils {

	/**
	 * Retrieves the conventional variable name for the supplied <code>Object</code> based on its concrete type.
	 * The convention used is to return the uncapitalized short name of the <code>Class</code>. So,
	 * <code>com.myapp.Product</code> becomes <code>product</code>.
	 * <p/>
	 * For arrays, we use the pluralized version of the array component type.
	 * <p/>
	 * For <code>Collection</code>s we attempt to 'peak ahead' in the <code>Collection</code> to determine the
	 * component type and return the pluralized version of that component type.
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

			if (!collection.isEmpty()) {
				valueClass = peekAhead(collection);
				pluralize = true;
			}
		}

		String name = StringUtils.uncapitalize(ClassUtils.getShortName(valueClass));
		return (pluralize ? pluralize(name) : name);
	}

	/**
	 * Pluralize the given name.
	 */
	private static String pluralize(String name) {
		return Pluralizer.pluralize(name);
	}

	/**
	 * Retreives the <code>Class</code> of an element in the <code>Collection</code>. The exact element
	 * for which the <code>Class</code> is retreived will depend on the concrete <code>Collection</code>
	 * implementation.
	 */
	private static Class peekAhead(Collection collection) {
		Iterator itr = collection.iterator();
		if (itr.hasNext()) {
			return itr.next().getClass();
		}
		else {
			throw new IllegalStateException("Unable to peek ahead in non-empty collection - hasNext() returns false.");
		}
	}

}
