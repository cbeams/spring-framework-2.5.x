package org.springframework.beans.factory.access;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * Default implementation of BeanFactoryReference, wrapping a newly
 * created BeanFactory, destroying its singletons on release.
 * @author Juergen Hoeller
 * @since 13.02.2004
 */
public class DefaultBeanFactoryReference implements BeanFactoryReference {

	private final BeanFactory beanFactory;

	public DefaultBeanFactoryReference(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public BeanFactory getFactory() {
		return beanFactory;
	}

	public void release() {
		if (this.beanFactory instanceof ConfigurableBeanFactory) {
			((ConfigurableBeanFactory) this.beanFactory).destroySingletons();
		}
	}

}
