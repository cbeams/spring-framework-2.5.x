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
 * Miscellaneous collection utility methods.
 * Mainly for internal use within the framework.
 * @author Juergen Hoeller
 * @since 1.1.3
 */
public abstract class CollectionUtils {

	/**
	 * Find a value of the given type in the given collection.
	 * @param coll the collection to search
	 * @param type the type to look for
	 * @return a value of the given type found, or null if none
	 * @throws IllegalArgumentException if more than one value
	 * of the given type found
	 */
	public static Object findValueOfType(Collection coll, Class type) throws IllegalArgumentException {
		Object value = null;
		for (Iterator it = coll.iterator(); it.hasNext();) {
			Object obj = it.next();
			if (type.isInstance(obj)) {
				if (value != null) {
					throw new IllegalArgumentException("More than one value of type [" + type.getName() + "] found");
				}
				value = obj;
			}
		}
		return value;
	}

	/**
	 * Find a value of one of the given types in the given collection:
	 * searching the collection for a value of the first type, then
	 * searching for a value of the second type, etc.
	 * @param coll the collection to search
	 * @param types the types to look for, in prioritized order
	 * @return a of one of the given types found, or null if none
	 * @throws IllegalArgumentException if more than one value
	 * of the given type found
	 */
	public static Object findValueOfType(Collection coll, Class[] types) throws IllegalArgumentException {
		for (int i = 0; i < types.length; i++) {
			Object value = findValueOfType(coll, types[i]);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

}
