package org.springframework.beans.factory.support;

import org.springframework.beans.BeansException;

/**
 * Exception if the validation of a bean definition failed.
 * @author Juergen Hoeller
 * @since 21.11.2003
 * @see AbstractBeanDefinition#validate
 */
public class BeanDefinitionValidationException extends BeansException {

	public BeanDefinitionValidationException(String msg) {
		super(msg);
	}

	public BeanDefinitionValidationException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
