/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import java.lang.reflect.Method;

import org.aopalliance.intercept.AspectException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import org.springframework.aop.ClassFilter;
import org.springframework.beans.factory.InitializingBean;

/**
 * Perl5 regular expression pointcut bean. JavaBean properties are:
 * <li>pattern: Perl5 regular expression for the fully-qualified method names to match
 * <li>interceptor: interceptor to invoke if the pointcut matches
 * Matching is based purely on method name.
 *
 * <p>Note: the regular expression must be a match. For example,
 * <code>.*get.*</code> will match com.mycom.Foo.getBar().
 * <code>get.*</code> will not.
 *
 * <p>Currently using Jakarta ORO regular expression library.
 * Does not require J2SE 1.4, although it runs under 1.4.
 *
 * @author Rod Johnson
 * @since July 22, 2003
 * @version $Id: RegexpMethodPointcut.java,v 1.3 2004-01-13 18:47:48 johnsonr Exp $
 */
public class RegexpMethodPointcut extends StaticMethodMatcherPointcut implements ClassFilter, InitializingBean { 
	
	private Log logger = LogFactory.getLog(getClass());
	
	/** Regular expression to match */
	private String pattern;
	
	/** ORO's compiled form of this pattern */
	private Pattern compiledPattern;
	
	/** ORO pattern matcher to use */
	private PatternMatcher matcher;

	/**
	 * @return the regular expression for method matching
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Set the regular expression defining methods to match
	 * @param pattern Perl5 regular expression describing methods
	 * to match
	 */
	public void setPattern(String pattern) throws MalformedPatternException {
		this.pattern = pattern;
		Perl5Compiler compiler = new Perl5Compiler();
		// Compile the pattern to be threadsafe
		this.compiledPattern = compiler.compile(pattern, Perl5Compiler.READ_ONLY_MASK);
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
		boolean matched =  this.matcher.matches(patt, this.compiledPattern);
		if (logger.isDebugEnabled())
			logger.debug("Candidate is: '" + patt + "'; pattern is " + this.compiledPattern.getPattern() + "; matched=" + matched);
		return matched;
	}

	public boolean matches(Class clazz) {
		// TODO do with regexp
		return true;
	}
	
	public ClassFilter getClassFilter() {
		return this;
	}

	/**
	 * We implement initializing bean only to do this validation. 
	 * It doesn't matter if callers other than the Spring IoC container
	 * don't bother to call this method.
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		if (pattern == null)
			throw new AspectException("Pattern must be supplied in " + getClass().getName());
	}

}
