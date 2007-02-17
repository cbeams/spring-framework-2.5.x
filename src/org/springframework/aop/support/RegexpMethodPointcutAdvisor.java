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

package org.springframework.aop.support;

import java.io.Serializable;

import org.aopalliance.aop.Advice;

import org.springframework.aop.Pointcut;
import org.springframework.core.JdkVersion;
import org.springframework.util.ObjectUtils;

/**
 * Convenient class for regexp method pointcuts that hold an Advice,
 * making them an {@link org.springframework.aop.Advisor}.
 *
 * <p>Configure this class using the "pattern" and "patterns"
 * pass-through properties. These are analogous to the pattern
 * and patterns properties of {@link AbstractRegexpMethodPointcut}.
 *
 * <p>Can delegate to any {@link AbstractRegexpMethodPointcut} subclass,
 * like {@link Perl5RegexpMethodPointcut} or {@link JdkRegexpMethodPointcut}.
 * To choose a specific one, either override the {@link #createPointcut}
 * method or set the "perl5" flag accordingly.
 *
 * <p>By default, {@link JdkRegexpMethodPointcut} will be used on JDK 1.4+,
 * falling back to {@link Perl5RegexpMethodPointcut} on JDK 1.3 (requiring
 * Jakarta ORO on the classpath). The use of Perl5RegexpMethodPointcut
 * can be enforced through specifying the "perl5" property.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #setPattern
 * @see #setPatterns
 * @see #setPerl5
 * @see #createPointcut
 * @see Perl5RegexpMethodPointcut
 * @see JdkRegexpMethodPointcut
 */
public class RegexpMethodPointcutAdvisor extends AbstractGenericPointcutAdvisor {

	private String[] patterns;

	private boolean perl5 = false;

	private AbstractRegexpMethodPointcut pointcut;

	private final Object pointcutMonitor = new SerializableMonitor();


	/**
	 * Create an empty RegexpMethodPointcutAdvisor.
	 * @see #setPattern
	 * @see #setPatterns
	 * @see #setPerl5
	 * @see #setAdvice
	 */
	public RegexpMethodPointcutAdvisor() {
	}

	/**
	 * Create a RegexpMethodPointcutAdvisor for the given advice.
	 * The pattern still needs to be specified afterwards.
	 * @param advice the advice to use
	 * @see #setPattern
	 * @see #setPatterns
	 * @see #setPerl5
	 */
	public RegexpMethodPointcutAdvisor(Advice advice) {
		setAdvice(advice);
	}

	/**
	 * Create a RegexpMethodPointcutAdvisor for the given advice.
	 * @param pattern the pattern to use
	 * @param advice the advice to use
	 * @see #setPerl5
	 */
	public RegexpMethodPointcutAdvisor(String pattern, Advice advice) {
		setPattern(pattern);
		setAdvice(advice);
	}

	/**
	 * Create a RegexpMethodPointcutAdvisor for the given advice.
	 * @param patterns the patterns to use
	 * @param advice the advice to use
	 * @see #setPerl5
	 */
	public RegexpMethodPointcutAdvisor(String[] patterns, Advice advice) {
		setPatterns(patterns);
		setAdvice(advice);
	}


	/**
	 * Set the regular expression defining methods to match.
	 * <p>Use either this method or {@link #setPatterns}, not both.
	 * @see #setPatterns
	 */
	public void setPattern(String pattern) {
		setPatterns(new String[] {pattern});
	}

	/**
	 * Set the regular expressions defining methods to match.
	 * To be passed through to the pointcut implementation.
	 * <p>Matching will be the union of all these; if any of the
	 * patterns matches, the pointcut matches.
	 * @see AbstractRegexpMethodPointcut#setPatterns
	 */
	public void setPatterns(String[] patterns) {
		this.patterns = patterns;
	}

	/**
	 * Set whether to enforce Perl5 regexp syntax. Default is "false".
	 * <p>Turn this flag on to use {@link Perl5RegexpMethodPointcut}
	 * (delegating to Jakarta ORO). Else, {@link JdkRegexpMethodPointcut} will
	 * be used on JDK 1.4+, falling back to Perl5RegexpMethodPointcut on JDK 1.3.
	 * <p>Alternatively, override the {@link #createPointcut} method.
	 * @see #createPointcut
	 * @see Perl5RegexpMethodPointcut
	 * @see JdkRegexpMethodPointcut
	 */
	public void setPerl5(boolean perl5) {
		this.perl5 = perl5;
	}


	/**
	 * Initialize the singleton Pointcut held within this Advisor.
	 */
	public Pointcut getPointcut() {
		synchronized (this.pointcutMonitor) {
			if (this.pointcut == null) {
				this.pointcut = createPointcut();
				this.pointcut.setPatterns(this.patterns);
			}
			return pointcut;
		}
	}

	/**
	 * Create the actual pointcut: By default, a {@link Perl5RegexpMethodPointcut}
	 * will be created if Perl5 syntax is enforced or when running on JDK 1.3.
	 * Else, a {@link JdkRegexpMethodPointcut} (JDK 1.4+) will be used.
	 * @return the Pointcut instance (never <code>null</code>)
	 * @see #setPerl5
	 * @see Perl5RegexpMethodPointcut
	 * @see JdkRegexpMethodPointcut
	 */
	protected AbstractRegexpMethodPointcut createPointcut() {
		if (this.perl5 || JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			// needs Jakarta ORO on the classpath
			return Perl5RegexpPointcutFactory.createPerl5RegexpPointcut();
		}
		else {
			// needs to run on JDK >= 1.4
			return new JdkRegexpMethodPointcut();
		}
	}

	public String toString() {
		return getClass().getName() + ": advice [" + getAdvice() +
				"], pointcut patterns " + ObjectUtils.nullSafeToString(this.patterns);
	}


	/**
	 * Inner factory class used to just introduce an ORO dependency
	 * when actually creating a Perl5 regexp pointcut.
	 */
	private static class Perl5RegexpPointcutFactory {

		public static AbstractRegexpMethodPointcut createPerl5RegexpPointcut() {
			return new Perl5RegexpMethodPointcut();
		}
	}


	/**
	 * Empty class used for a serializable monitor object.
	 */
	private static class SerializableMonitor implements Serializable {
	}

}
