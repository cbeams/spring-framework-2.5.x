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
 * <li>pattern: Java 1.4 regular expression for the fully-qualified method names to match
 * <li>patterns: alternative property taking a String array of patterns. The result will
 * be the union of these patterns. 
 *
 * <p>Note: the regular expressions must be a match. For example,
 * <code>.*get.*</code> will match com.mycom.Foo.getBar().
 * <code>get.*</code> will not.
 *
 * Does require J2SE 1.4
 *
 * @author Dmitriy Kopylenko
 * @since 1.1
 * @see org.springframework.aop.support.AbstractRegexpMethodPointcut
 */
public class Jdk14RegexpMethodPointcut extends AbstractRegexpMethodPointcut { 
	
	/** 
	 * Java 1.4 compiled form of this pattern. 
	 */
	private transient Pattern[] compiledPatterns = new Pattern[0];

	/**
	 * Initialize Java 1.4 Patterns field from patterns String[].
	 */
	protected void initPatternRepresentation() throws PatternSyntaxException{
		this.compiledPatterns = new Pattern[getPatterns().length];
		
		for (int i = 0; i < getPatterns().length; i++) {
			this.compiledPatterns[i] = Pattern.compile(getPatterns()[i]);
		} 
	}

	/**
	 * Match this pattern against the ith compiled pattern
	 * @param patt string to match
	 * @param i index from 0 of compiled pattern
	 * @return whetehr the pattern maches
	 */
	protected boolean matches(String patt, int i) { 
		Matcher matcher = this.compiledPatterns[i].matcher(patt);   
	    boolean matched = matcher.matches();
		if (logger.isDebugEnabled()) {
			logger.debug("Candidate is: '" + patt + "'; pattern is " + this.compiledPatterns[i].pattern() +
			             "; matched=" + matched);
		}
		return matched;
	}
}
