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

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * Perl5 regular expression pointcut bean. JavaBean properties are:
 * <li>pattern: Perl5 regular expression for the fully-qualified method names to match
 * <li>patterns: alternative property taking a String array of patterns. The result will
 * be the union of these patterns. 
 *
 * <p>Note: the regular expressions must be a match. For example,
 * <code>.*get.*</code> will match com.mycom.Foo.getBar().
 * <code>get.*</code> will not.
 *
 * <p>Currently uses Jakarta ORO regular expression library.
 * Does not require J2SE 1.4, although it runs under 1.4.
 *
 * @author Rod Johnson
 * @since 1.1
 * @see org.springframework.aop.support.AbstractRegexpMethodPointcut
 */
public class Perl5RegexpMethodPointcut extends AbstractRegexpMethodPointcut { 
	
	/** 
	 * ORO's compiled form of this pattern.
	 * ORO fields are transient as they're not serializable.
	 * They will be reinitialized on deserialization by
	 * the initPatternRepresentation() method. 
	 */
	private transient Pattern[] compiledPatterns = new Pattern[0];
	
	/** ORO pattern matcher to use */
	private transient PatternMatcher matcher;

	/**
	 * Initialize ORO fields from patterns String[].
	 */
	protected void initPatternRepresentation() throws MalformedPatternException {
		this.compiledPatterns = new Pattern[getPatterns().length];
		
		Perl5Compiler compiler = new Perl5Compiler();
		for (int i = 0; i < getPatterns().length; i++) {
			// Compile the pattern to be threadsafe
			this.compiledPatterns[i] = compiler.compile(getPatterns()[i], Perl5Compiler.READ_ONLY_MASK);
		}
		this.matcher = new Perl5Matcher();
	}

	/**
	 * Match this pattern against the ith compiled pattern
	 * @param patt string to match
	 * @param i index from 0 of compiled pattern
	 * @return whetehr the pattern maches
	 */
	protected boolean matches(String patt, int i) { 
		boolean matched = this.matcher.matches(patt, this.compiledPatterns[i]);
		if (logger.isDebugEnabled()) {
			logger.debug("Candidate is: '" + patt + "'; pattern is " + this.compiledPatterns[i].getPattern() +
			             "; matched=" + matched);
		}
		return matched;
	}

}
