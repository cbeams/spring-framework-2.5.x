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

/**
 * Strategy interface for String-based path matching.
 * Used by PathMatchingResourcePatternResolver, AbstractUrlHandlerMapping,
 * PropertiesMethodNameResolver, WebContentInterceptor.
 *
 * <p>The default implementation is AntPathMatcher,
 * supporting Ant-style pattern syntax.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see AntPathMatcher
 * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
 * @see org.springframework.web.servlet.handler.AbstractUrlHandlerMapping
 * @see org.springframework.web.servlet.mvc.multiaction.PropertiesMethodNameResolver
 * @see org.springframework.web.servlet.mvc.WebContentInterceptor
 */
public interface PathMatcher {

	/**
	 * Return if the given string represents a pattern to be matched
	 * via this class: If not, the "match" method does not have to be
	 * used because direct equality comparisons are sufficient.
	 * @param str the string to check
	 * @return whether the given string represents a pattern
	 * @see #match
	 */
	boolean isPattern(String str);

	/**
	 * Match a string against the given pattern.
	 * @param pattern the pattern to match against
	 * @param str the string to test
	 * @return whether the arguments matched
	 */
	boolean match(String pattern, String str);

}
