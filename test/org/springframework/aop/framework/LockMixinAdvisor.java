/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import org.springframework.aop.support.DefaultInterceptionIntroductionAdvisor;

/**
 * Advisor for use with a LockMixin. Applies to all classes.
 * @author Rod Johnson
 * @version $Id: LockMixinAdvisor.java,v 1.2 2004-01-21 20:21:34 johnsonr Exp $
 */
public class LockMixinAdvisor extends DefaultInterceptionIntroductionAdvisor {
	
	public LockMixinAdvisor() {
		super(new LockMixin(), Lockable.class);
	}

}
