/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.context;

import org.springframework.beans.BeansException;

/**
 * Interface to be implemented by any object that wishes to be notified
 * of the application context it runs in.
 *
 * <p>Implementing this interface makes sense when an object requires access to
 * file resources, i.e. wants to call ApplicationContext.getResource, or access
 * to the MessageSource. Configuration via bean references is preferable to
 * implementing this interface just for bean lookup purposes.
 *
 * <p>Note that Resource dependencies can also be exposed as bean properties
 * of type Resource, populated via Strings with automatic type conversion by
 * the bean factory or via ContextResourceFactoryBean. This removes the need
 * for implementing this interface just for resource access purposes.
 *
 * <p>ApplicationObjectSupport is a convenience base class for
 * application objects, implementing this interface.
 *
 * @author Rod Johnson
 * @see org.springframework.context.ApplicationContext#getResource
 * @see org.springframework.core.io.Resource
 * @see org.springframework.context.config.ContextResourceFactoryBean
 * @see org.springframework.context.MessageSource
 * @see org.springframework.context.support.ApplicationObjectSupport
 */
public interface ApplicationContextAware {
	
	/** 
	 * Set the application context used by this object.
	 * Normally this call will be used to initialize the object.
	 * <p>Note that this call can occur multiple times if the context
	 * is reloadable. The implementation must check itself if it is
	 * already initialized resp. if it wants to perform reinitialization.
	 * @param context ApplicationContext object to be used by this object
	 * @throws ApplicationContextException in case of context initialization errors
	 * @throws BeansException if thrown by application context methods
	 * @see org.springframework.beans.factory.BeanInitializationException
	 */
	void setApplicationContext(ApplicationContext context) throws BeansException;

}
