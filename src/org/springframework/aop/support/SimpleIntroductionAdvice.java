/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.support;

import java.util.HashSet;
import java.util.Set;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.InterceptionIntroductionAdvisor;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.framework.AopConfigException;

/**
 * Simple IntroductionAdvice implementation that by default applies to any class.
 * @author Rod Johnson
 * @since 11-Nov-2003
 * @version $Id: SimpleIntroductionAdvice.java,v 1.1 2003-11-16 12:54:58 johnsonr Exp $
 */
public class SimpleIntroductionAdvice implements InterceptionIntroductionAdvisor, ClassFilter {
	
	private IntroductionInterceptor interceptor;
	
	private Set interfaces = new HashSet();
	
	public SimpleIntroductionAdvice(IntroductionInterceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public SimpleIntroductionAdvice(IntroductionInterceptor interceptor, Class clazz) throws AopConfigException {
		this(interceptor);
		addInterface(clazz);
	}
	
	/**
	 * Wrap this interceptor and introduce all interfaces	
	 */
	public SimpleIntroductionAdvice(DelegatingIntroductionInterceptor dii) {
		this((IntroductionInterceptor) dii);
		for (int i = 0; i < dii.getIntroducedInterfaces().length; i++) {
			Class intf = dii.getIntroducedInterfaces()[i];
			addInterface(intf);
		}
	}
	
	public void addInterface(Class intf) throws AopConfigException {
		this.interfaces.add(intf);
	}

	/**
	 * @see org.springframework.aop.IntroductionAdvice#getClassFilter()
	 */
	public ClassFilter getClassFilter() {
		return this;
	}

	/**
	 * @see org.springframework.aop.IntroductionAdvice#getIntroductionInterceptor()
	 */
	public IntroductionInterceptor getIntroductionInterceptor() {
		return interceptor;
	}

	/**
	 * @see org.springframework.aop.IntroductionAdvice#getInterfaces()
	 */
	public Class[] getInterfaces() {
		return (Class[]) this.interfaces.toArray(new Class[this.interfaces.size()]);
	}

	/**
	 * @see org.springframework.aop.ClassFilter#matches(java.lang.Class)
	 */
	public boolean matches(Class clazz) {
		return true;
	}

	/**
	 * Default for an introduction is per-instance interception
	 * @see org.springframework.aop.Advice#isPerInstance()
	 */
	public boolean isPerInstance() {
		return true;
	}

}
