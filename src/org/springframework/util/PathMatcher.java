/*
 * Copyright 2002-2007 the original author or authors.
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
	 * method does not have to be used because direct equality comparisons
	 * on the static path Strings will lead to the same result.
	 * @param text the <code>string</code> to check
	 * @return <code>true</code> if the given <code>string</code> represents a pattern
	 */
	boolean isPattern(String text);

	/**
	 * Match the given <code>text</code> against the given <code>pattern</code>,
	 * according to this PathMatcher's matching strategy.
	 * @param pattern the pattern to match against
	 * @param text the string to test
	 * @return <code>true</code> if the supplied <code>text</code> matched,
	 * <code>false</code> if it didn't
	 */
	boolean match(String pattern, String text);

	/**
	 * Given a pattern and a full path, determine the pattern-mapped part.
	 * <p>This method is supposed to find out which part of the path is matched
	 * dynamically through an actual pattern, that is, it strips off a statically
	 * defined leading path from the given full path, returning only the actually
	 * pattern-matched part of the path.
	 * <p>For example: For "myroot/*.html" as pattern and "myroot/myfile.html"
	 * as full path, this method should return "myfile.html". The detailed
	 * determination rules are specified to this PathMatcher's matching strategy.
	 * <p>A simple implementation may return the given full path as-is in case
	 * of an actual pattern, and the empty String in case of the pattern not
	 * containing any dynamic parts (i.e. the <code>pattern</code> parameter being
	 * a static path that wouldn't qualify as an actual {@link #isPattern pattern}).
	 * A sophisticated implementation will differentiate between the static parts
	 * and the dynamic parts of the given path pattern.
	 * @param pattern the path pattern
	 * @param path the full path to introspect
	 * @return the pattern-mapped part of the given <code>path</code>
	 * (never <code>null</code>)
	 */
	String extractPathWithinPattern(String pattern, String path);

}
