package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

/**
 * Allows for custom modification of new bean instances, e.g.
 * checking for marker interfaces or wrapping them with proxies.
 *
 * <p>Application contexts can auto-detect BeanPostProcessor beans in their
 * bean definitions and apply them before any other beans get created.
 *
 * @author Juergen Hoeller
 * @since 10.10.2003
 * @see ConfigurableBeanFactory#addBeanPostProcessor
 * @see BeanFactoryPostProcessor
 */
public interface BeanPostProcessor {

	/**
	 * Apply this BeanPostProcessor to the given new bean instance,
	 * being already populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * @param bean the new bean instance
	 * @param name the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	Object postProcessBean(Object bean, String name) throws BeansException;

}
