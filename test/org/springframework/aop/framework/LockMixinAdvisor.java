/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.aop.framework;

import org.springframework.aop.support.DefaultIntroductionAdvisor;

/**
 * Advisor for use with a LockMixin. Applies to all classes.
 * @author Rod Johnson
 * @version $Id: LockMixinAdvisor.java,v 1.3 2004-02-22 09:48:53 johnsonr Exp $
 */
public class LockMixinAdvisor extends DefaultIntroductionAdvisor {
	
	public LockMixinAdvisor() {
		super(new LockMixin(), Lockable.class);
	}

}
