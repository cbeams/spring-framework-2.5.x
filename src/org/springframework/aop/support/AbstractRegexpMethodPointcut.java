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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Method;

import org.aopalliance.aop.AspectException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.aop.ClassFilter;

/**
 * Abstract base regular expression pointcut bean. JavaBean properties are:
 * <ul>
 * <li>pattern: regular expression for the fully-qualified method names to match.
 * The exact regexp syntax will depend on the subclass (e.g. Perl5 regular expressions)
 * <li>patterns: alternative property taking a String array of patterns. The result will
 * be the union of these patterns.
 * </ul>
 * 
 * <p>Note: the regular expressions must be a match. For example,
 * <code>.*get.*</code> will match com.mycom.Foo.getBar().
 * <code>get.*</code> will not.
 * 
 * <p>This base class is serializable. Subclasses should declare all fields transient
 * - the initPatternRepresentation method in this class will be invoked again on the
 * client side on deserialization.
 * 
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 * @see Perl5RegexpMethodPointcut
 * @see JdkRegexpMethodPointcut
 */
public abstract class AbstractRegexpMethodPointcut extends StaticMethodMatcherPointcut
		implements ClassFilter, Serializable {
	
	/** Transient as it's reinitialized on deserialization. */
	protected transient Log logger = LogFactory.getLog(getClass());
	
	/** Regular expressions to match */
	private String[] patterns = new String[0];


	/**
	 * Convenience method when we have only a single pattern.
	 * Use either this method or setPatterns(), not both.
	 * @see #setPatterns
	 */
	public void setPattern(String pattern) {
		setPatterns(new String[] { pattern });
	}

	/**
	 * Set the regular expressions defining methods to match.
	 * Matching will be the union of all these; if any match,
	 * the pointcut matches.
	 * @param patterns Perl5 regular expressions describing methods
	 * to match
	 */
	public void setPatterns(String[] patterns) {
		this.patterns = patterns;
		initPatternRepresentation(patterns);
	}

	/**
	 * Return the regular expressions for method matching.
	 */
	public String[] getPatterns() {
		return patterns;
	}


	/**
	 * Subclasses must implement this to initialize regexp pointcuts.
	 * Can be invoked multiple times.
	 * <p>This method will be invoked from the setPatterns method,
	 * and also on deserialization.
	 * @param patterns the patterns to initialize
	 * @throws IllegalArgumentException in case of an invalid pattern
	 */
	protected abstract void initPatternRepresentation(String[] patterns) throws IllegalArgumentException;

	/**
	 * Does the pattern at the given index match this string?
	 * @param pattern string pattern to match
	 * @param patternIndex index of pattern from 0
	 * @return whether there's a match
	 */
	protected abstract boolean matches(String pattern, int patternIndex);


	/**
	 * Try to match the regular expression against the fully qualified name
	 * of the method's declaring class, plus the name of the method.
	 * Note that the declaring class is that class that originally declared
	 * the method, not necessarily the class that's currently exposing it.
	 * <p>For example, <code>java.lang.Object.hashCode</code> matches
	 * any subclass of object's hashCode() method.
	 */
	public final boolean matches(Method m, Class targetClass) { 
		// TODO use target class here?
		String patt = m.getDeclaringClass().getName() + "." + m.getName();
		for (int i = 0; i < this.patterns.length; i++) {
			boolean matched = matches(patt, i);
			if (matched) {
				return true;
			}
		}
		return false;
	}

	public boolean matches(Class clazz) {
		// TODO do with regexp
		return true;
	}
	
	public final ClassFilter getClassFilter() {
		return this;
	}

	
	//	---------------------------------------------------------------------
	// Serialization support
	//---------------------------------------------------------------------
	
	private void readObject(ObjectInputStream ois) throws IOException {
		// Rely on default serialization, just initialize state after deserialization.
		try {
			ois.defaultReadObject();
		}
		catch (ClassNotFoundException ex) {
			throw new AspectException("Failed to deserialize AOP regular expression pointcut: " +
					"Check that Spring AOP libraries are available on the client side", ex);
		}
		
		// initialize transient fields
		this.logger = LogFactory.getLog(getClass());

		// ask subclass to reinitialize
		try {
			initPatternRepresentation(this.patterns);
		}
		catch (Throwable ex) {
			throw new AspectException("Failed to deserialize AOP regular expression pointcut: " +
					"Check that the necessary regular expression libraries are available on the client side", ex);
		}
	}
	
}
