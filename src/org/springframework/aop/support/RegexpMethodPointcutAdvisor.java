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

/**
 * Convenient class for regexp method pointcuts that hold an Advice,
 * making them an Advisor.
 * <p>
 * Configure this class using the pattern and patterns
 * pass-through properties. These are analogous to the pattern
 * and patterns properties of AbstractRegexpMethodPointcut.
 * <p>
 * Can delegate to any type of regexp pointcut.
 * Currently only Perl5 pointcuts are supported.
 * Pointcut property must be of class AbstractRegexpMethodPointcut.
 * This should not normally be set directly.
 * @author Dmitriy Kopylenko
 * @author Rod Johnson
 */
public class RegexpMethodPointcutAdvisor extends DefaultPointcutAdvisor {
	
	public RegexpMethodPointcutAdvisor() {
		initPointcut();
	}

	public RegexpMethodPointcutAdvisor(Advice advice) {
		super(advice);
		initPointcut();
	}
	
	/**
	 * Create the pointcut.
	 * This will be of type AbstractRegexpMethodPointcut.
	 */
	protected void initPointcut() {
		// TODO could support different types driven by properties
		setPointcut(new Perl5RegexpMethodPointcut());
	}
	
	public void setPattern(String pattern) throws Exception {
		// We know that the Pointcut in this object is of type
		// AbstractRegexpMethodPointcut, as we set it
		AbstractRegexpMethodPointcut armp = (AbstractRegexpMethodPointcut) getPointcut();
		armp.setPattern(pattern);
	}
	
	public void setPatterns(String[] patterns) throws Exception {
		AbstractRegexpMethodPointcut armp = (AbstractRegexpMethodPointcut) getPointcut();
		armp.setPatterns(patterns);
	}
}
