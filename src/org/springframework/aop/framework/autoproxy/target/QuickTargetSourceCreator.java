/*
 * The Spring Framework is published under the terms of the Apache Software License.
 */

package org.springframework.aop.framework.autoproxy.target;

import org.springframework.aop.target.AbstractPrototypeTargetSource;
import org.springframework.aop.target.CommonsPoolTargetSource;
import org.springframework.aop.target.PrototypeTargetSource;
import org.springframework.aop.target.ThreadLocalTargetSource;
import org.springframework.beans.factory.BeanFactory;

/**
 * Convenient TargetSourceCreator using bean name prefixes to create one of three
 * well-known TargetSource types: 
 * <li>: CommonsPoolTargetSource
 * <li>% ThreadLocalTargetSource
 * <li>! PrototypeTargetSource
 * 
 * @author Rod Johnson
 * @version $Id: QuickTargetSourceCreator.java,v 1.2 2003-12-30 01:07:11 jhoeller Exp $
 */
public class QuickTargetSourceCreator extends AbstractPrototypeTargetSourceCreator {

	protected final AbstractPrototypeTargetSource createPrototypeTargetSource(Object bean, String beanName, BeanFactory factory) {
		if (beanName.startsWith(":")) {
			CommonsPoolTargetSource cpts = new CommonsPoolTargetSource();
			cpts.setMaxSize(25);
			return cpts;
			
		}
		else if (beanName.startsWith("%")) {
			ThreadLocalTargetSource tlts = new ThreadLocalTargetSource();
			return tlts;
		}
		else if (beanName.startsWith("!")) {
			PrototypeTargetSource pts = new PrototypeTargetSource();
			return pts;
		}
		else {
			// No match. Don't create a custom target source.
			return null;
		}
	}
	
}