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

package org.springframework.util;

/**
 * Utility methods for simple pattern matching, in particular for
 * Spring's typical "xxx*", "*xxx" and "*xxx*" pattern styles.
 *
 * @author Juergen Hoeller
 * @since 2.0
 */
public abstract class PatternMatchUtils {

	/**
	 * Match a String against the given pattern, supporting the following simple
	 * pattern styles: "xxx*", "*xxx" and "*xxx*" matches, as well as direct equality.
	 * @param pattern the pattern to match against
	 * @param str the String to match
	 * @return whether the String matches the given pattern
	 */
	public static boolean simpleMatch(String pattern, String str) {
		if (ObjectUtils.nullSafeEquals(pattern, str) || "*".equals(pattern)) {
			return true;
		}
		if (pattern == null || str == null) {
			return false;
		}
		if (pattern.startsWith("*") && pattern.endsWith("*") &&
				str.indexOf(pattern.substring(1, pattern.length() - 1)) != -1) {
			return true;
		}
		if (pattern.startsWith("*") && str.endsWith(pattern.substring(1, pattern.length()))) {
			return true;
		}
		if (pattern.endsWith("*") && str.startsWith(pattern.substring(0, pattern.length() - 1))) {
			return true;
		}
		return false;
	}

	/**
	 * Match a String against the given patterns, supporting the following simple
	 * pattern styles: "xxx*", "*xxx" and "*xxx*" matches, as well as direct equality.
	 * @param patterns the patterns to match against
	 * @param str the String to match
	 * @return whether the String matches any of the given patterns
	 */
	public static boolean simpleMatch(String[] patterns, String str) {
		if (patterns != null) {
			for (int i = 0; i < patterns.length; i++) {
				if (simpleMatch(patterns[i], str)) {
					return true;
				}
			}
		}
		return false;
	}

}
