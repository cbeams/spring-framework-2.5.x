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

package org.springframework.orm.hibernate.support;

import java.io.Serializable;

import net.sf.hibernate.CallbackException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.SessionFactory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.DependencyInjectionAspectSupport;

/**
 * Allows Spring to create and wire up objects that come from Hibernate. 
 * This enables richer domain models, with domain objects able to access
 * business objects.
 * <p> 
 * Based on a constribution by
 * Oliver Hutchison. Thanks also to Seth Ladd.
 * <p>
 * This is a factory bean as we want to extend DependencyInjectionAspectSupport yet be usable
 * as a Hibernate Interceptor. 
 * <p>
 * 
 * @author Oliver Hutchison
 * @author Rod Johnson
 * @see Interceptor
 * @since 1.2
 */
public class DependencyInjectionInterceptorFactoryBean extends DependencyInjectionAspectSupport implements FactoryBean {

	private SessionFactory sessionFactory;

	/**
	 * We need this to work out identifier property name to set PK on object
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}


	protected void validateProperties() {
		if (sessionFactory == null) {
			throw new IllegalArgumentException("Hibernate SessionFactory is required");
		}
	}

	protected class DependencyInjectionInterceptor extends ChainedInterceptorSupport {

		/**
		 * @see net.sf.hibernate.Interceptor#instantiate(java.lang.Class, java.io.Serializable)
		 */
		public Object instantiate(Class clazz, Serializable id) throws CallbackException {
			try {
				Object newEntity = createAndConfigure(clazz);
				setIdOnNewEntity(sessionFactory, clazz, id, newEntity);
				return newEntity;
			}
			catch (NoAutowiringConfigurationForClassException ex) {
				// Delegate to next interceptor in the chain if we didn't find an instantiation rule
				return super.instantiate(clazz, id);
			}
		}
	}

	protected SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws Exception {
		return new DependencyInjectionInterceptor();
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class getObjectType() {
		return Interceptor.class;
	}

	/**
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return true;
	}
	
}