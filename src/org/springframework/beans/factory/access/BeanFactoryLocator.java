/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.access;

import org.springframework.beans.FatalBeanException;

/**
 * <p>An interface for a class used to lookup/use, and optionally allow the
 * release of a BeanFactory, or BeanFactory subclass such as ApplicationContext.
 *
 * <p>Where this interface is implemented as a singleton class such as
 * KeyedSingletonBeanFactoryLocator, the Spring team <strong>strongly</strong>
 * suggests that it be used sparingly and with caution. By far the vast majority
 * of the code inside an application is best written in a Dependency Injection
 * style, where that code is served out of a BeanFactory/ApplicationContext
 * container, and has its own dependencies supplied by the container when it is
 * created. However, even such a singleton implementation sometimes has its use
 * in the small glue layers of code that is sometimes needed to tie other code
 * together. For example, third party code may try to construct new objects
 * directly, without the ability to force it to get these objects out of a 
 * beanfactory. If the object constructed by the third party code is just a
 * small stub or proxy, which then uses an implementation of this class to get a
 * beanfactory from which it gets the real object, to which it delegates, then 
 * proper Dependency Injection has been achieved. As another example, in a complex
 * J2EE app with multiple layers, with each layer having its own
 * ApplicationContext definition (in a hierarchy), a class like
 * KeyedSingletonBeanFactoryLocator may be used to demand load these contexts.  
 * 
 * @author colin sampaleanu
 * @version $Revision: 1.2 $
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.context.access.LocatorFactory
 * @see org.springframework.context.ApplicationContext
 */
public interface BeanFactoryLocator {

  /**
   * Use the BeanFactory (or derived class such as ApplicationContext) specified
   * by the factoryKey parameter. The definition is possibly loaded/created as needed.
   * @param factoryKey a resourceName specifying which BeanFactory the BeanFactoryLocator
   * should return for usage. The actual meaning of the resourceName is specific to the
   * actual implementation of BeanFactoryLocator.
   * @return the BeanFactory instance, wrapped as a {@link BeanFactoryReference} object
   * @throws FatalBeanException if there is an error loading or accessing the BeanFactory
   */
  BeanFactoryReference useFactory(String factoryKey) throws FatalBeanException;

}
