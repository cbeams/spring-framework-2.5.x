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
 * Strategy interface for <code>String</code>-based path matching.
 * 
 * <p>Used by {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver},
 * {@link org.springframework.web.servlet.handler.AbstractUrlHandlerMapping},
 * {@link org.springframework.web.servlet.mvc.multiaction.PropertiesMethodNameResolver},
 * and {@link org.springframework.web.servlet.mvc.WebContentInterceptor}.
 *
 * <p>The default implementation is {@link AntPathMatcher}, supporting the
 * Ant-style pattern syntax.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see AntPathMatcher
 */
public interface PathMatcher {

	/**
	 * Does the given <code>string</code> represent a pattern that can be matched
	 * by an implementation of this interface?
	 * <p>If the return value is <code>false</code>, then the {@link #match}
	 * method does not have to be used because direct equality comparisons will
	 * be sufficient.
	 * @param text the <code>string</code> to check
	 * @return <code>true</code> if the given <code>string</code> represents a pattern
	 */
	boolean isPattern(String text);

	/**
	 * Match the given <code>text</code> against the given <code>pattern</code>.
	 * @param pattern the pattern to match against
	 * @param text the string to test
	 * @return <code>true</code> if the supplied <code>text</code> matched
	 */
	boolean match(String pattern, String text);

	/**
	 * Given a pattern and a full path, returns the non-pattern mapped part.
	 * @param pattern the path pattern
	 * @param path the full path
	 * @return the non-pattern mapped part of the given <code>path</code>
	 */
	String extractPathWithinPattern(String pattern, String path);
}
