package org.springframework.beans.factory.config;

import org.springframework.beans.factory.ListableBeanFactory;

/**
 * SPI interface to be implemented by most if not all listable bean factories.
 * Currently just a combination of ListableBeanFactory and ConfigurableBeanFactory.
 * 
 * <p>Allows for framework-internal plug'n'play, e.g. in AbstractApplicationContext.
 *
 * @author Juergen Hoeller
 * @since 03.11.2003
 * @see org.springframework.context.support.AbstractApplicationContext#getBeanFactory
 */
public interface ConfigurableListableBeanFactory extends ListableBeanFactory, ConfigurableBeanFactory {

}
