package org.springframework.context.support;

import java.util.Locale;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;

/**
 * ApplicationContext to allow concrete registration of Java objects
 * in code, rather than from external configuration sources.
 * Mainly useful for testing.
 * @author Rod Johnson
 */
public class StaticApplicationContext extends AbstractApplicationContext {

	private DefaultListableBeanFactory beanFactory;

	/**
	 * Create new StaticApplicationContext.
	 */
	public StaticApplicationContext() throws BeansException {
		this(null);
	}

	/**
	 * Create new StaticApplicationContext with the given parent.
	 * @param parent the parent application context
	 */
	public StaticApplicationContext(ApplicationContext parent) throws BeansException {
		super(parent);

		// create bean factory with parent
		this.beanFactory = new DefaultListableBeanFactory(parent);

		// Register the message source bean
		registerSingleton(MESSAGE_SOURCE_BEAN_NAME, StaticMessageSource.class, null);
	}

	/**
	 * Return the underlying bean factory of this context.
	 */
	public DefaultListableBeanFactory getDefaultListableBeanFactory() {
		return beanFactory;
	}

	/**
	 * Return underlying bean factory for super class.
	 */
	public ConfigurableListableBeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Do nothing: We rely on callers to update our public methods.
	 */
	protected void refreshBeanFactory() {
	}

	/**
	 * Register a singleton bean with the default bean factory.
	 */
	public void registerSingleton(String name, Class clazz, MutablePropertyValues pvs) throws BeansException {
		this.beanFactory.registerBeanDefinition(name, new RootBeanDefinition(clazz, pvs));
	}

	/**
	 * Register a prototype bean with the default bean factory.
	 */
	public void registerPrototype(String name, Class clazz, MutablePropertyValues pvs) throws BeansException {
		this.beanFactory.registerBeanDefinition(name, new RootBeanDefinition(clazz, pvs, false));
	}

	/**
	 * Associate the given message with the given code.
	 * @param code lookup code
	 * @param locale locale message should be found within
	 * @param defaultMessage message associated with this lookup code
	 */
	public void addMessage(String code, Locale locale, String defaultMessage) {
		StaticMessageSource messageSource = (StaticMessageSource) getBean(MESSAGE_SOURCE_BEAN_NAME);
		messageSource.addMessage(code, locale, defaultMessage);
	}

}
