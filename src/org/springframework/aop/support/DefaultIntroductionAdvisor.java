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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.aopalliance.aop.Advice;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.core.Ordered;

/**
 * Simple IntroductionAdvisor implementation that by default applies to any class.
 * @author Rod Johnson
 * @since 11-Nov-2003
 * @version $Id: DefaultIntroductionAdvisor.java,v 1.6 2004-04-21 17:49:37 jhoeller Exp $
 */
public class DefaultIntroductionAdvisor implements IntroductionAdvisor, ClassFilter, Ordered {

	private int order = Integer.MAX_VALUE;

	private IntroductionInterceptor interceptor;
	
	private Set interfaces = new HashSet();
	
	public DefaultIntroductionAdvisor(IntroductionInterceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public DefaultIntroductionAdvisor(IntroductionInterceptor interceptor, Class clazz) {
		this(interceptor);
		addInterface(clazz);
	}
	
	/**
	 * Wrap the given interceptor and introduce all interfaces.
	 */
	public DefaultIntroductionAdvisor(DelegatingIntroductionInterceptor dii) {
		this((IntroductionInterceptor) dii);
		Class[] introducedInterfaces = dii.getIntroducedInterfaces();
		for (int i = 0; i < introducedInterfaces.length; i++) {
			addInterface(introducedInterfaces[i]);
		}
	}
	
	public void setOrder(int order) {
		this.order = order;
	}

	public int getOrder() {
		return order;
	}

	public void addInterface(Class intf) {
		this.interfaces.add(intf);
	}

	public ClassFilter getClassFilter() {
		return this;
	}

	public Advice getAdvice() {
		return interceptor;
	}

	public Class[] getInterfaces() {
		return (Class[]) this.interfaces.toArray(new Class[this.interfaces.size()]);
	}

	public boolean matches(Class clazz) {
		return true;
	}

	/**
	 * Default for an introduction is per-instance interception.
	 */
	public boolean isPerInstance() {
		return true;
	}
	
	public void validateInterfaces() throws IllegalArgumentException {
		for (Iterator ut = this.interfaces.iterator(); ut.hasNext();) {
			Class intf = (Class) ut.next();
			if (!intf.isInterface()) {
			 throw new IllegalArgumentException("Class '" + intf.getName() +
																					"' is not an interface; cannot be used in an introduction");
			}
			if (!this.interceptor.implementsInterface(intf)) {
			 throw new IllegalArgumentException("IntroductionInterceptor [" + this.interceptor + "] " +
					 "does not implement interface '" + intf.getName() + "' specified in introduction advice");
			}
		}
	}

}
