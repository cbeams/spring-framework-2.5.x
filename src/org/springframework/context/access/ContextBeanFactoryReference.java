package org.springframework.context.access;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * ApplicationContext-specific implementation of BeanFactoryReference,
 * wrapping a newly created ApplicationContext, closing it on release.
 * @author Juergen Hoeller
 * @since 13.02.2004
 */
public class ContextBeanFactoryReference implements BeanFactoryReference {

	private final ApplicationContext applicationContext;

	public ContextBeanFactoryReference(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public BeanFactory getFactory() {
		return applicationContext;
	}

	public void release() {
		if (this.applicationContext instanceof ConfigurableApplicationContext) {
			((ConfigurableApplicationContext) this.applicationContext).close();
		}
	}

}
