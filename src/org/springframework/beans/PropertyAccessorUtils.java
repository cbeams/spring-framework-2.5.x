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

package org.springframework.beans;

/**
 * Utility methods for classes that perform bean property access
 * according to the PropertyAccessor interface.
 *
 * @author Juergen Hoeller
 * @since 1.2.6
 * @see PropertyAccessor
 */
public abstract class PropertyAccessorUtils {

	/**
	 * Return the actual property name for the given property path.
	 * @param propertyPath the property path to determine the property name
	 * for (can include property keys, for example for specifying a map entry)
	 * @return the actual property name, without any key elements
	 */
	public static String getPropertyName(String propertyPath) {
		int separatorIndex = propertyPath.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR);
		return (separatorIndex != -1 ? propertyPath.substring(0, separatorIndex) : propertyPath);
	}

	/**
	 * Determine the first nested property separator in the
	 * given property path, ignoring dots in keys (like "map[my.key]").
	 * @param propertyPath the property path to check
	 * @return the index of the nested property separator, or -1 if none
	 */
	public static int getFirstNestedPropertySeparatorIndex(String propertyPath) {
		return getNestedPropertySeparatorIndex(propertyPath, false);
	}

	/**
	 * Determine the first nested property separator in the
	 * given property path, ignoring dots in keys (like "map[my.key]").
	 * @param propertyPath the property path to check
	 * @return the index of the nested property separator, or -1 if none
	 */
	public static int getLastNestedPropertySeparatorIndex(String propertyPath) {
		return getNestedPropertySeparatorIndex(propertyPath, true);
	}

	/**
	 * Determine the first (or last) nested property separator in the
	 * given property path, ignoring dots in keys (like "map[my.key]").
	 * @param propertyPath the property path to check
	 * @param last whether to return the last separator rather than the first
	 * @return the index of the nested property separator, or -1 if none
	 */
	private static int getNestedPropertySeparatorIndex(String propertyPath, boolean last) {
		boolean inKey = false;
		final int length = propertyPath.length();
		int i = (last ? length - 1 : 0);
		while (last ? i >= 0 : i < length) {
			switch (propertyPath.charAt(i)) {
				case PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR:
				case PropertyAccessor.PROPERTY_KEY_SUFFIX_CHAR:
					inKey = !inKey;
					break;
				case PropertyAccessor.NESTED_PROPERTY_SEPARATOR_CHAR:
					if (!inKey) {
						return i;
					}
			}
			if (last)
				i--;
			else
				i++;
		}
		return -1;
	}

}
