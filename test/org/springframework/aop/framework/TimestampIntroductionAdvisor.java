/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.aop.support.DefaultIntroductionAdvisor;

/**
 * 
 * @author Rod Johnson
 * @version $Id: TimestampIntroductionAdvisor.java,v 1.3 2004-02-22 09:48:54 johnsonr Exp $
 */
public class TimestampIntroductionAdvisor extends DefaultIntroductionAdvisor {

	/**
	 * @param dii
	 */
	public TimestampIntroductionAdvisor() {
		super(new DelegatingIntroductionInterceptor(new TimestampIntroductionInterceptor()));
	}

}
