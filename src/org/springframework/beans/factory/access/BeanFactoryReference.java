/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.access;

import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;

/**
 * Class used to track a reference to a BeanFactory obtained through
 * a BeanFactoryLocator.
 *
 * <p>It is safe to call {@link #release()} multiple times, but
 * {@link #getFactory()} must not be called after calling release.
 *
 * @author Colin Sampaleanu
 * @see BeanFactoryLocator
 * @see org.springframework.context.access.ContextBeanFactoryReference
 */
public interface BeanFactoryReference {
  
  /**
   * Returns the BeanFactory instance held by this reference.
   * @throws IllegalStateException if invoked after release() has been called
   */
  BeanFactory getFactory();
  
  /**
   * Indicate that the BeanFactory instance referred to by this object is not
   * needed any longer by the client code which obtained the ref object. Depending
   * on the actual implementation of BeanFactoryLocator, and the actual type of
   * BeanFactory, this may possibly not actually do anything; alternately in the
   * case of a 'closeable' BeanFactory or derived class (such as ApplicationContext)
   * may 'close' it, or may 'close' it once no more references remain.
   * <p>In an EJB usage scenario this would normally be called from ejbRemove and
   * ejbPassivate.
   * <p>This is safe to call multiple times.
   * @throws FatalBeanException if the BeanFactory cannot be released
   * @see org.springframework.beans.factory.access.BeanFactoryLocator
	 * @see org.springframework.context.access.ContextBeanFactoryReference
	 * @see org.springframework.context.ConfigurableApplicationContext#close
   */
  void release() throws FatalBeanException;

}
