/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.aop;


/** 
 * Base interface holding AOP <b>advice</b> (action to take at a joinpoint)
 * and a filter determining the applicability of the advice (such as 
 * a pointcut). <i>This interface is not for use by Spring users, but to
 * allow for commonality in support for different types of advice.</i>
 * <br>
 * Spring currently supports <b>around advice</b> delivered via method
 * <b>interception</b>. However, this interface is intended to allow support for
 * different types of advice, such as <b>before</b> and <b>after</b> advice,
 * which need not be implemented using interception.
 * Note that these advice types can be implemented using around advice
 * at present. 
 * @see org.springframework.aop.InterceptionAroundAdvisor
 * @see org.springframework.aop.InterceptionIntroductionAdvisor
 * @author Rod Johnson
 * @version $Id: Advisor.java,v 1.2 2003-11-20 10:43:59 johnsonr Exp $
 */
public abstract interface Advisor {
	
	
	// Aspect getAspect();
	
	/**
	 * @return wheter this advice is associated with a particular instance
	 * (for example, creating a mixin) or is it shared with all instances of
	 * the advised class obtained from the same Spring bean factory?
	 * <b>Note that this method is not currently used by the framework</b>. Use
	 * singleton/prototype bean definitions or appropriate programmatic
	 * proxy creation to ensure that Advisors have the correct lifecycle model. 
	 */
	boolean isPerInstance();

}
