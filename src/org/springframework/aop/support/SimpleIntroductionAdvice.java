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
 * @version $Id: SimpleIntroductionAdvice.java,v 1.2 2003-11-21 22:45:29 jhoeller Exp $
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
	 * Wrap this interceptor and introduce all interfaces.
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

	public ClassFilter getClassFilter() {
		return this;
	}

	public IntroductionInterceptor getIntroductionInterceptor() {
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

}
