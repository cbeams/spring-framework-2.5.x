package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

/**
 * Subinterface of BeanPostProcessor that adds a before-destruction callback.
 *
 * <p>The typical usage will be to invoke custom destruction callbacks on
 * specific bean types, matching corresponding initialization callbacks.
 *
 * @author Juergen Hoeller
 * @since 09.04.2004
 */
public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor {

	/**
	 * Apply this BeanPostProcessor to the given new bean instance before
	 * its destruction. Can invoke custom destruction callbacks.
	 * @param bean the new bean instance
	 * @param name the name of the bean
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	void postProcessBeforeDestruction(Object bean, String name) throws BeansException;

}
