/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ejb.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;

/**
 * Convenient superclass for stateful session beans.
 * SFSBs should extend this class, leaving them to implement the
 * ejbActivate() and ejbPassivate() lifecycle methods to comply
 * with the requirements of the EJB specification.
 *
 * <p><b>Note: Subclasses should invoke the loadBeanFactory() method in
 * their custom ejbCreate and ejbActivate methods, and should invoke
 * the unloadBeanFactory() method in their ejbPassivate method.</b>
 * 
 * <p><b>Note: The default BeanFactoryLocator used by this class's superclass
 * (ContextJndiBeanFactoryLocator) is <b>not</b> serializable. Therefore,
 * when using the default BeanFactoryLocator, or another variant which is
 * not serializable, subclasses must call setBeanFactoryLocator(null) in
 * ejbPassivate, with a corresponding call to setBeanFactoryLocator(xxx)
 * in ejbActivate unless relying on the default locator.
 * 
 * @author Rod Johnson
 * @author Colin Sampaleanu
 * @see org.springframework.context.access.ContextJndiBeanFactoryLocator
 */
public abstract class AbstractStatefulSessionBean extends AbstractSessionBean {

	/**
	 * Load a Spring BeanFactory namespace. Exposed for subclasses
	 * to load a BeanFactory in their ejbCreate() methods. Those 
	 * callers would normally want to catch BeansException and
	 * rethrow it as {@link javax.ejb.CreateException}. Unless
	 * the BeanFactory is known to be serializable, this method
	 * must also be called from ejbActivate(), to reload a context
	 * removed via a call to unloadBeanFactory from ejbPassivate.
	 */
	protected void loadBeanFactory() throws BeansException {
		super.loadBeanFactory();
	}
	
	/**
	 * Unload the Spring BeanFactory instance.
	 * The default ejbRemove method invokes this method, but subclasses
	 * which override ejbRemove must invoke this method themselves.
	 * Unless the BeanFactory is known to be serializable, this method
	 * must also be called from ejbPassivate, with a corresponding call
	 * to loadBeanFactory from ejbActivate.
	 */
	protected void unloadBeanFactory() throws FatalBeanException {
		super.unloadBeanFactory();
	}

}
