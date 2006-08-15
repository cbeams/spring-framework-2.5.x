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

package org.springframework.aop.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Java 1.4+ regular expression pointcut. JavaBean properties are:
 * <ul>
 * <li>pattern: Java 1.4 regular expression for the fully-qualified method names to match
 * <li>patterns: alternative property taking a String array of patterns. The result will
 * be the union of these patterns.
 * </ul>
 *
 * <p>Note: the regular expressions must be a match. For example,
 * <code>.*get.*</code> will match com.mycom.Foo.getBar().
 * <code>get.*</code> will not.
 *
 * <p>Requires JDK 1.4+, as it builds on the <code>java.util.regex</code> package.
 * As alternative on JDK 1.3 or for Perl5-style regular expression parsing,
 * consider Perl5RegexpMethodPointcut.
 *
 * @author Dmitriy Kopylenko
 * @author Rob Harrop
 * @since 1.1
 * @see Perl5RegexpMethodPointcut
 */
public class JdkRegexpMethodPointcut extends AbstractRegexpMethodPointcut {
	
	/** 
	 * Java 1.4 compiled form of the patterns.
	 */
	private transient Pattern[] compiledPatterns = new Pattern[0];

	/** 
	 * Java 1.4 compiled form of the exclusion patterns.
	 */
	private transient Pattern[] compiledExclusionPatterns = new Pattern[0];

	/**
	 * Initialize Java 1.4 {@link Pattern Patterns} from the supplied <code>String[]</code>.
	 */
	protected void initPatternRepresentation(String[] patterns) throws PatternSyntaxException {
		this.compiledPatterns = compilePatterns(patterns);
	}

	/**
	 * Returns <code>true</code> if the {@link Pattern} at index <code>patternIndex</code>
	 * matches the supplied candidate <code>String</code>.
	 */
	protected boolean matches(String pattern, int patternIndex) {
		Matcher matcher = this.compiledPatterns[patternIndex].matcher(pattern);
		return matcher.matches();
	}

	/**
	 * Initialize Java 1.4 exclusion {@link Pattern Patterns} from the supplied <code>String[]</code>.
	 */
	protected void initExcludedPatternRepresentation(String[] excludedPatterns) throws IllegalArgumentException {
		this.compiledExclusionPatterns = compilePatterns(excludedPatterns);
	}

	/**
	 * Returns <code>true</code> if the exclusion {@link Pattern} at index <code>patternIndex</code>
	 * matches the supplied candidate <code>String</code>.
	 */
	protected boolean matchesExclusion(String candidate, int patternIndex) {
		Matcher matcher = this.compiledExclusionPatterns[patternIndex].matcher(candidate);
		return matcher.matches();
	}

	/**
	 * Compiles the supplied <code>String[]</code> into an array of
	 * {@link Pattern} objects and returns that array.
	 */
	private Pattern[] compilePatterns(String[] source) {
		Pattern[] destination = new Pattern[source.length];
		for (int i = 0; i < source.length; i++) {
			destination[i] = Pattern.compile(source[i]);
		}
		return destination;
	}

}
