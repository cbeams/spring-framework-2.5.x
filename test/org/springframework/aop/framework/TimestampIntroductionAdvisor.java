/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.aop.support.DefaultInterceptionIntroductionAdvisor;

/**
 * 
 * @author Rod Johnson
 * @version $Id: TimestampIntroductionAdvisor.java,v 1.2 2004-01-21 20:21:34 johnsonr Exp $
 */
public class TimestampIntroductionAdvisor extends DefaultInterceptionIntroductionAdvisor {

	/**
	 * @param dii
	 */
	public TimestampIntroductionAdvisor() {
		super(new DelegatingIntroductionInterceptor(new TimestampIntroductionInterceptor()));
	}

}
