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
 * Simple IntroductionAdvisor implementation that by default applies to any class.
 * @author Rod Johnson
 * @since 11-Nov-2003
 * @version $Id: DefaultInterceptionIntroductionAdvisor.java,v 1.1 2004-01-21 20:21:35 johnsonr Exp $
 */
public class DefaultInterceptionIntroductionAdvisor implements InterceptionIntroductionAdvisor, ClassFilter {
	
	private IntroductionInterceptor interceptor;
	
	private Set interfaces = new HashSet();
	
	public DefaultInterceptionIntroductionAdvisor(IntroductionInterceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public DefaultInterceptionIntroductionAdvisor(IntroductionInterceptor interceptor, Class clazz) throws AopConfigException {
		this(interceptor);
		addInterface(clazz);
	}
	
	/**
	 * Wrap this interceptor and introduce all interfaces.
	 */
	public DefaultInterceptionIntroductionAdvisor(DelegatingIntroductionInterceptor dii) {
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
	
	public void validateInterfaces() throws AopConfigException {
		 for (int i = 0; i < getInterfaces().length; i++) {
			 if (!getInterfaces()[i].isInterface()) {
				 throw new AopConfigException("Class '" + getInterfaces()[i].getName() + "' is not an interface; cannot be used in an introduction");
			 }
			  if (!getIntroductionInterceptor().implementsInterface(getInterfaces()[i])) {
				 throw new AopConfigException("IntroductionInterceptor [" + getIntroductionInterceptor() + "] " +
						 "does not implement interface '" + getInterfaces()[i].getName() + "' specified in introduction advice");
			  }
		  }
	}

}
