/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.aop.support.SimpleIntroductionAdvisor;

/**
 * 
 * @author Rod Johnson
 * @version $Id: TimestampIntroductionAdvisor.java,v 1.1 2003-12-09 16:43:30 johnsonr Exp $
 */
public class TimestampIntroductionAdvisor extends SimpleIntroductionAdvisor {

	/**
	 * @param dii
	 */
	public TimestampIntroductionAdvisor() {
		super(new DelegatingIntroductionInterceptor(new TimestampIntroductionInterceptor()));
	}

}
