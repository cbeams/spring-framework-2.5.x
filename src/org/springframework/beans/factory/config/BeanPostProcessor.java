package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

/**
 * Allows for custom modification of new bean instances, e.g.
 * checking for marker interfaces or wrapping them with proxies.
 *
 * <p>Application contexts can auto-detect BeanPostProcessor beans in their
 * bean definitions and apply them before any other beans get created.
 * Plain bean factories allow for programmatic registration of post-processors.
 *
 * <p>Typically, post-processors that populate beans via marker interfaces
 * or the like will implement postProcessBeforeInitialization, and post-processors
 * that wrap beans with proxies will normally implement postProcessAfterInitialization.
 *
 * @author Juergen Hoeller
 * @since 10.10.2003
 * @see ConfigurableBeanFactory#addBeanPostProcessor
 * @see BeanFactoryPostProcessor
 */
public interface BeanPostProcessor {

	/**
	 * Apply this BeanPostProcessor to the given new bean instance <i>before</i> any bean
	 * initialization callbacks (like InitializingBean's afterPropertiesSet or a custom
	 * init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * @param bean the new bean instance
	 * @param name the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	Object postProcessBeforeInitialization(Object bean, String name) throws BeansException;

	/**
	 * Apply this BeanPostProcessor to the given new bean instance <i>after</i> any bean
	 * initialization callbacks (like InitializingBean's afterPropertiesSet or a custom
	 * init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * @param bean the new bean instance
	 * @param name the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one
	 * @throws org.springframework.beans.BeansException in case of errors
	 */
	Object postProcessAfterInitialization(Object bean, String name) throws BeansException;

}
