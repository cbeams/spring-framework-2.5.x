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

package org.springframework.aop.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Java 1.4 regular expression pointcut bean. JavaBean properties are:
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
 * <p>Requires J2SE 1.4, as it builds on the <code>java.util.regex</code> package.
 *
 * @author Dmitriy Kopylenko
 * @since 1.1
 * @see org.springframework.aop.support.AbstractRegexpMethodPointcut
 */
public class JdkRegexpMethodPointcut extends AbstractRegexpMethodPointcut {
	
	/** 
	 * Java 1.4 compiled form of this pattern. 
	 */
	private transient Pattern[] compiledPatterns = new Pattern[0];

	/**
	 * Initialize Java 1.4 Patterns field from patterns String[].
	 */
	protected void initPatternRepresentation(String[] patterns) throws PatternSyntaxException {
		this.compiledPatterns = new Pattern[patterns.length];
		for (int i = 0; i < patterns.length; i++) {
			this.compiledPatterns[i] = Pattern.compile(patterns[i]);
		} 
	}

	protected boolean matches(String pattern, int patternIndex) {
		Matcher matcher = this.compiledPatterns[patternIndex].matcher(pattern);
		boolean matched = matcher.matches();
		if (logger.isDebugEnabled()) {
			logger.debug("Candidate is: '" + pattern + "'; pattern is '" +
					this.compiledPatterns[patternIndex].pattern() + "'; matched=" + matched);
		}
		return matched;
	}

}
