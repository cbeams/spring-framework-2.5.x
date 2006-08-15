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

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * Perl5-style regular expression pointcut. JavaBean properties are:
 * <ul>
 * <li>pattern: Perl5 regular expression for the fully-qualified method names to match
 * <li>patterns: alternative property taking a String array of patterns.
 * The result will be the union of these patterns.
 * </ul>
 *
 * <p>Note: the regular expressions must be a match. For example,
 * <code>.*get.*</code> will match com.mycom.Foo.getBar().
 * <code>get.*</code> will not.
 *
 * <p>Currently uses the <a href="http://jakarta.apache.org/oro">Jakarta ORO</a>
 * regular expression library. Does not require JDK 1.4+, in contrast to
 * JdkRegexpMethodPointcut.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @since 1.1
 * @see JdkRegexpMethodPointcut
 */
public class Perl5RegexpMethodPointcut extends AbstractRegexpMethodPointcut { 
	
	/** 
	 * ORO's compiled form of this pattern.
	 * ORO fields are transient as they're not serializable.
	 * They will be reinitialized on deserialization by
	 * the initPatternRepresentation() method. 
	 */
	private transient Pattern[] compiledPatterns = new Pattern[0];

	private transient Pattern[] compiledExclusionPatterns = new Pattern[0];

	/** ORO pattern matcher to use */
	private transient PatternMatcher matcher;


	/**
	 * Initializes the {@link Pattern ORO representation} of the supplied exclusion patterns.
	 */
	protected void initPatternRepresentation(String[] patterns) throws IllegalArgumentException {
		this.compiledPatterns = compilePatterns(patterns);
		this.matcher = new Perl5Matcher();
	}

	/**
	 * Returns <code>true</code> if the {@link Pattern} at index <code>patternIndex</code>
	 * matches the supplied candidate <code>String</code>.
	 */
	protected boolean matches(String pattern, int patternIndex) {
		return this.matcher.matches(pattern, this.compiledPatterns[patternIndex]);
	}

	/**
	 * Initializes the {@link Pattern ORO representation} of the supplied exclusion patterns.
	 */
	protected void initExcludedPatternRepresentation(String[] excludedPatterns) throws IllegalArgumentException {
		this.compiledExclusionPatterns = compilePatterns(excludedPatterns);
	}

	/**
	 * Returns <code>true</code> if the exclusion {@link Pattern} at index <code>patternIndex</code>
	 * matches the supplied candidate <code>String</code>.
	 */
	protected boolean matchesExclusion(String pattern, int patternIndex) {
		return this.matcher.matches(pattern, this.compiledExclusionPatterns[patternIndex]);
	}

	/**
	 * Compiles the supplied pattern sources into a {@link Pattern} array.
	 */
	private Pattern[] compilePatterns(String[] source) {
		Perl5Compiler compiler = new Perl5Compiler();
		Pattern[] destination = new Pattern[source.length];
		for (int i = 0; i < source.length; i++) {
			// compile the pattern to be thread-safe
			try {
				destination[i] = compiler.compile(source[i], Perl5Compiler.READ_ONLY_MASK);
			}
			catch (MalformedPatternException ex) {
				throw new IllegalArgumentException(ex.getMessage());
			}
		}
		return destination;
	}

}
