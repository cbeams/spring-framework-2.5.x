package org.springframework.beans.factory;

import org.springframework.beans.FatalBeanException;

/**
 * Exception that a bean implementation is suggested to throw if its own
 * factory-aware initialization code fails. BeansExceptions thrown by
 * bean factory methods themselves should simply be propagated as-is.
 *
 * <p>Note that non-factory-aware initialization methods like afterPropertiesSet()
 * or a custom "init-method" can throw any exception.
 *
 * @author Juergen Hoeller
 * @since 13.11.2003
 * @see BeanFactoryAware#setBeanFactory
 * @see InitializingBean#afterPropertiesSet
 */
public class BeanInitializationException extends FatalBeanException {

	public BeanInitializationException(String msg) {
		super(msg);
	}

	public BeanInitializationException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
