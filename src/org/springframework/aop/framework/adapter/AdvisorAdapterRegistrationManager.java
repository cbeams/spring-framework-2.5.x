package org.springframework.aop.framework.adapter;

import org.springframework.aop.framework.adapter.AdvisorAdapter;
import org.springframework.aop.framework.adapter.GlobalAdvisorAdapterRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * BeanPostProcessor implementation that "registers" instances of any non-default AdvisorAdapters
 * with GlobalAdvisorAdapterRegistry.
 * <p>
 * The only requirement for it to work is that it needs to be defined in application context
 * along with any arbitrary "non-native" Spring AdvisorAdapters that need to be "recognized" by
 * SpringAOP module.
 * 
 * @author Dmitriy Kopylenko
 * @version $Id: AdvisorAdapterRegistrationManager.java,v 1.1 2004-02-27 14:28:27 dkopylenko Exp $
 */
public class AdvisorAdapterRegistrationManager implements BeanPostProcessor {

	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
	 */
	public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
		return bean;
	}

	/**
	 * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String)
	 */
	public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
		if(bean instanceof AdvisorAdapter){
			GlobalAdvisorAdapterRegistry.getInstance().registerAdvisorAdapter((AdvisorAdapter)bean);
		}
		
		return bean;
	}
}
