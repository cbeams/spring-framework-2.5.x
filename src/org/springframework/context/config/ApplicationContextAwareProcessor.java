package org.springframework.context.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * BeanPostProcessor implementation that passes the application context
 * to beans that implement the ApplicationContextAware interface.
 *
 * <p>Application contexts will automatically register this with their
 * underlying bean factory. Applications do not use this directly.
 *
 * @author Juergen Hoeller
 * @since 10.10.2003
 * @see org.springframework.context.ApplicationContextAware
 * @see org.springframework.context.support.AbstractApplicationContext#refresh
 */
public class ApplicationContextAwareProcessor implements BeanPostProcessor {

	private ApplicationContext applicationContext;

	/**
	 * Create a new ApplicationContextAwareProcessor for the given context.
	 */
	public ApplicationContextAwareProcessor(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public Object postProcessBean(Object bean, String name) throws BeansException {
		if (bean instanceof ApplicationContextAware) {
			((ApplicationContextAware) bean).setApplicationContext(this.applicationContext);
		}
		return bean;
	}

}
