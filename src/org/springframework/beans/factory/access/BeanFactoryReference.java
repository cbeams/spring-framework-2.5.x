/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.beans.factory.access;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;

/**
 * Class used t track a reference to a BeanFactory obtained through
 * {@link BeanFactoryLocator}
 * 
 * @version $Revision: 1.1 $
 * @author colin sampaleanu
 */
public interface BeanFactoryReference {
  
  /**
   * Returns the BeanFactory instance held by this reference
   */
  BeanFactory getFactory();
  
  /**
   * <p>Indicate that the BeanFactory instance referred to by this object is not
   * needed any longer by the client code which obtained the ref object. Depending
   * on the actual implementation of BeanFactoryLocator, and the actual type of
   * BeanFactory, this may possibly not actually do anything; alternately in the
   * case of a 'closeable' BeanFactory or derived class (such as ApplicationContext)
   * may 'close' it.</p>
   * <p>In an EJB usage scenario this would normally be called from ejbRemove and
   * ejbPassivate.
   * 
   * @throws FatalBeanException if the BeanFactory cannot be released
   * 
   * @see org.springframework.beans.factory.access.BeanFactoryLocator 
   */
  void release() throws FatalBeanException;

}
