/*
 * Copyright 2002-2004 the original author or authors.
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

/**
 * Miscellaneous object utility methods. Mainly for internal use
 * within the framework; consider Jakarta's Commons Lang for a more
 * comprehensive suite of object utilities.
 * @author Juergen Hoeller
 * @since 19.03.2004
 * @see org.apache.commons.lang.ObjectUtils
 */
public abstract class ObjectUtils {

	/**
	 * Determine if the given Objects are equal, returning true if both
	 * are null respectively false if only one is null.
	 * @param o1 first Object to compare
	 * @param o2 second Object to compare
	 * @return whether the given Objects are equal
	 */
	public static boolean nullSafeEquals(Object o1, Object o2) {
		return (o1 == o2 || (o1 != null && o1.equals(o2)));
	}

}
