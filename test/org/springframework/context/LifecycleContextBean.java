/*
 * Created on Jul 2, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.springframework.context;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.LifecycleBean;

/**
 * @author colin
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class LifecycleContextBean extends LifecycleBean implements ApplicationContextAware {
	
	protected ApplicationContext owningContext;

	public void setBeanFactory(BeanFactory beanFactory) {
		super.setBeanFactory(beanFactory);
		if (this.owningContext != null)
			throw new RuntimeException("Factory called setBeanFactory after setApplicationContext");
	}
	
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (this.owningFactory == null)
			throw new RuntimeException("Factory called setApplicationContext before setBeanFactory");
			
		this.owningContext = applicationContext;
	}
	
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		if (this.owningContext == null)
			throw new RuntimeException("Factory didn't call setAppliationContext before afterPropertiesSet on lifecycle bean");
	}

}
