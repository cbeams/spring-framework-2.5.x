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

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;

/**
 * Convenient class for regexp method pointcuts that hold an Advice,
 * making them an Advisor.
 *
 * <p>Configure this class using the "pattern" and "patterns"
 * pass-through properties. These are analogous to the pattern
 * and patterns properties of AbstractRegexpMethodPointcut.
 *
 * <p>Can delegate to any AbstractRegexpMethodPointcut subclass.
 * To choose a specific one, either override <code>createPointcut</code>
 * or set the "perl5" flag accordingly.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #createPointcut
 * @see #setPerl5
 * @see AbstractRegexpMethodPointcut
 */
public class RegexpMethodPointcutAdvisor extends DefaultPointcutAdvisor implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private boolean perl5 = true;

	private String[] patterns;

	public RegexpMethodPointcutAdvisor() {
	}

	public RegexpMethodPointcutAdvisor(Advice advice) {
		super(advice);
	}
	
	/**
	 * Set whether to use Perl5 regexp syntax. If on, Perl5RegexpMethodPointcut
	 * will be used (delegating to Jakarta ORO); if off, the JdkRegexpMethodPointcut
	 * (delegating to the JDK 1.4 regexp package).
	 * <p>Alternatively, override the <code>createPointcut</code> method.
	 * @see #createPointcut
	 * @see Perl5RegexpMethodPointcut
	 * @see JdkRegexpMethodPointcut
	 */
	public void setPerl5(boolean perl5) {
		this.perl5 = perl5;
	}

	/**
	 * Convenience method when we have only a single pattern.
	 * Use either this method or setPatterns, not both.
	 * <p>To be passed through to the pointcut implementation.
	 * @see #setPatterns
	 */
	public void setPattern(String pattern) {
		setPatterns(new String[] { pattern });
	}

	/**
	 * Set the regular expressions defining methods to match.
	 * Matching will be the union of all these; if any match,
	 * the pointcut matches.
	 * <p>To be passed through to the pointcut implementation.
	 * @param patterns regular expressions describing methods to match
	 */
	public void setPatterns(String[] patterns) {
		this.patterns = patterns;
	}

	/**
	 * Creates the pointcut and initializes it with the patterns.
	 * @see #createPointcut
	 * @see #setPatterns
	 */
	public void afterPropertiesSet() {
		AbstractRegexpMethodPointcut pointcut = createPointcut();
		pointcut.setPatterns(this.patterns);
		setPointcut(pointcut);
	}

	/**
	 * Create the default pointcut: a Perl5RegexpMethodPointcut.
	 * <p>Alternatively, set the "perl5" flag.
	 * @see #setPerl5
	 * @see AbstractRegexpMethodPointcut
	 * @see Perl5RegexpMethodPointcut
	 * @see JdkRegexpMethodPointcut
	 */
	protected AbstractRegexpMethodPointcut createPointcut() {
		if (this.perl5) {
			logger.info("Using Perl5RegexpMethodPointcut for RegexpMethodPointcutAdvisor");
			return new Perl5RegexpMethodPointcut();
		}
		else {
			logger.info("Using JdkRegexpMethodPointcut for RegexpMethodPointcutAdvisor");
			return new JdkRegexpMethodPointcut();
		}
	}

}
