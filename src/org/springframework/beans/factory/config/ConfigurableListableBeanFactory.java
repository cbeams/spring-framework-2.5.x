package org.springframework.beans.factory.config;

import org.springframework.beans.factory.ListableBeanFactory;

/**
 * SPI interface to be implemented by most if not all listable bean factories.
 * In addition to ConfigurableBeanFactory, provides a way to pre-instantiate singletons.
 *
 * <p>Allows for framework-internal plug'n'play, e.g. in AbstractApplicationContext.
 *
 * @author Juergen Hoeller
 * @since 03.11.2003
 * @see org.springframework.context.support.AbstractApplicationContext#getBeanFactory
 */
public interface ConfigurableListableBeanFactory
		extends ListableBeanFactory, ConfigurableBeanFactory, AutowireCapableBeanFactory {

	/**
	 * Ensure that all non-lazy-init singletons are instantiated, also considering
	 * FactoryBeans. Typically invoked at the end of factory setup, if desired.
	 */
	void preInstantiateSingletons();

}
