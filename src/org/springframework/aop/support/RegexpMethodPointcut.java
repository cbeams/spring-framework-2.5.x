/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.springframework.aop.ClassFilter;

/**
 * Perl5 regular expression pointcut bean. JavaBean properties are:
 * <li>pattern: Perl5 regular expression for the fully-qualified method names to match
 * <li>patterns: alternative property taking a String array of patterns. The result will
 * be the union of these patterns. 
 * <li>interceptor: AOP Alliance interceptor to invoke if the pointcut matches
 * Matching is based purely on method name.
 *
 * <p>Note: the regular expressions must be a match. For example,
 * <code>.*get.*</code> will match com.mycom.Foo.getBar().
 * <code>get.*</code> will not.
 *
 * <p>Currently uses Jakarta ORO regular expression library.
 * Does not require J2SE 1.4, although it runs under 1.4.
 *
 * @author Rod Johnson
 * @since July 22, 2003
 * @version $Id: RegexpMethodPointcut.java,v 1.4 2004-01-13 22:33:49 johnsonr Exp $
 */
public class RegexpMethodPointcut extends StaticMethodMatcherPointcut implements ClassFilter { 
	
	private Log logger = LogFactory.getLog(getClass());
	
	/** Regular expression to match */
	private String[] patterns = new String[0];
	
	/** ORO's compiled form of this pattern */
	private Pattern[] compiledPatterns = new Pattern[0];
	
	/** ORO pattern matcher to use */
	private PatternMatcher matcher;

	/**
	 * @return the regular expressions for method matching
	 */
	public String[] getPatterns() {
		return patterns;
	}
	
	/**
	 * Convenience method when we have only a single pattern.
	 * Use either this method or setPatterns(), not both.
	 * @param pattern
	 * @throws MalformedPatternException
	 */
	public void setPattern(String pattern) throws MalformedPatternException {
		setPatterns(new String[] { pattern });
	}

	/**
	 * Set the regular expressions defining methods to match.
	 * Matching will be the union of all these; if any match,
	 * the pointcut matches.
	 * @param patterns Perl5 regular expressions describing methods
	 * to match
	 */
	public void setPatterns(String[] patterns) throws MalformedPatternException {
		this.patterns = patterns;
		this.compiledPatterns = new Pattern[patterns.length];
		
		Perl5Compiler compiler = new Perl5Compiler();
		for (int i = 0; i < patterns.length; i++) {
			// Compile the pattern to be threadsafe
			this.compiledPatterns[i] = compiler.compile(patterns[i], Perl5Compiler.READ_ONLY_MASK);
		}
		this.matcher = new Perl5Matcher();
	}

	/**
	 * Try to match the regular expression against the fully qualified name
	 * of the method's declaring class, plus the name of the method.
	 * Note that the declaring class is that class that originally declared
	 * the method, not necessarily the class that's currently exposing it.
	 * <p>For example, <code>java.lang.Object.hashCode</code> matches
	 * any subclass of object's hashCode() method.
	 */
	public boolean matches(Method m, Class targetClass) { 
		// TODO use target class here?
		String patt = m.getDeclaringClass().getName() + "." + m.getName();
		for (int i = 0; i < this.compiledPatterns.length; i++) {
			boolean matched = this.matcher.matches(patt, this.compiledPatterns[i]);
			if (logger.isDebugEnabled())
				logger.debug("Candidate is: '" + patt + "'; pattern is " + this.compiledPatterns[i].getPattern() + "; matched=" + matched);
			if (matched)
				return true;
		}
		return false;
	}

	public boolean matches(Class clazz) {
		// TODO do with regexp
		return true;
	}
	
	public ClassFilter getClassFilter() {
		return this;
	}

}
