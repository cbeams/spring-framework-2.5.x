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
 * This is a factory bean, as we want to extend DependencyInjectionAspectSupport yet be usable
 * as a Hibernate Interceptor. 
 * <p>
 * Typically, the Hibernate SessionFactory will be set via the sessionFactoryName
 * property: this avoids a circular dependency between SessionFactory (requiring an
 * Interceptor) and this Interceptor (requiring a SessionFactory).
 * 
 * @author Oliver Hutchison
 * @author Rod Johnson
 * @see Interceptor
 * @since 1.2
 */
public class DependencyInjectionInterceptorFactoryBean extends DependencyInjectionAspectSupport implements FactoryBean {

	private SessionFactory sessionFactory;
	
	private String sessionFactoryName;
	
	private Interceptor nextInterceptor;
	
	/**
	 * As Hibernate doesn't support chaining of interceptors natively, we add the ability for
	 * chaining via a delegate.
	 * 
	 * @param delegateInterceptor
	 */
	public void setNextInterceptor(Interceptor nextInterceptor) {
		this.nextInterceptor = nextInterceptor;
	}

	/**
	 * @return the next Interceptor in the chain, or null if this is
	 * the only interceptor
	 */
	public Interceptor getNextInterceptor() {
		return nextInterceptor;
	}

	/**
	 * We need the Hibernate SessionFactory to work out identifier property name to set PK on object
	 * @param sessionFactory bean name of the Hibernate SessionFactory
	 * this interceptor should configure persistent object instances for
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * Alternative to the sessionFactory property.
	 * Use this property to avoid circular dependencies between interceptor
	 * and SessionFactory. The session factory will be looked up on validation.
	 * @param sessionFactoryName bean name of the Hibernate SessionFactory
	 * this interceptor should configure persistent object instances for
	 */
	public void setSessionFactoryName(String sessionFactoryName) {
		this.sessionFactoryName = sessionFactoryName;
	}


	protected void validateProperties() {
		if (sessionFactory == null) {
			if (sessionFactoryName == null) {
				throw new IllegalArgumentException("Either sessionFactory or sessionFactoryName property must be set");
			}
			else {
				// look up the session factory bean
				this.sessionFactory = (SessionFactory) getBeanFactory().getBean(sessionFactoryName, SessionFactory.class);
			}
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


	/**
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws Exception {
		DependencyInjectionInterceptor dii = new DependencyInjectionInterceptor();
		dii.setNextInterceptor(nextInterceptor);
		return dii;
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