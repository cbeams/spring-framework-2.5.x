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
import java.util.Iterator;

import net.sf.hibernate.CallbackException;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Support class for Hibernate interceptors that participate in a chain. 
 * Suggested by Seth Ladd and based by code contributed
 * by Oliver Hutchison.
 * Necessary as Hibernate does not support native chaining of
 * interceptors.
 * <br>
 * Implements all Interceptor methods as no ops.
 * Abstract as it will do nothing unless a subclass
 * overrides one or more methods.
 * Any methods that are not handled by the subclass
 * will delegate to the next interceptor in the chain.
 * 
 * @author Rod Johnson
 * @see Interceptor
 * @since 1.2
 */
public abstract class ChainedInterceptorSupport implements Interceptor {

	protected final Log log = LogFactory.getLog(getClass());

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

	protected Interceptor next() {
		return nextInterceptor;
	}

	/**
	 * @see net.sf.hibernate.Interceptor#instantiate(java.lang.Class, java.io.Serializable)
	 */
	public Object instantiate(Class beanClass, Serializable id) throws CallbackException {
		return (nextInterceptor != null) ? nextInterceptor.instantiate(beanClass, id) : null;
	}

	/**
	 * 
	 * @see net.sf.hibernate.Interceptor#onLoad(java.lang.Object, java.io.Serializable,
	 *      java.lang.Object[], java.lang.String[], net.sf.hibernate.type.Type[])
	 */
	public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
		if (nextInterceptor != null) {
			return nextInterceptor.onLoad(entity, id, state, propertyNames, types);
		}
		else {
			return false;
		}
	}

	/**
	 * @see net.sf.hibernate.Interceptor#onFlushDirty(java.lang.Object, java.io.Serializable,
	 *      java.lang.Object[], java.lang.Object[], java.lang.String[],
	 *      net.sf.hibernate.type.Type[])
	 */
	public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
			String[] propertyNames, Type[] types) throws CallbackException {
		if (nextInterceptor != null) {
			return nextInterceptor.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
		}
		else {
			return false;
		}
	}

	/**
	 * @see net.sf.hibernate.Interceptor#onSave(java.lang.Object, java.io.Serializable,
	 *      java.lang.Object[], java.lang.String[], net.sf.hibernate.type.Type[])
	 */
	public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
		if (nextInterceptor != null) {
			return nextInterceptor.onSave(entity, id, state, propertyNames, types);
		}
		else {
			return false;
		}
	}

	/**
	 * @see net.sf.hibernate.Interceptor#onDelete(java.lang.Object, java.io.Serializable,
	 *      java.lang.Object[], java.lang.String[], net.sf.hibernate.type.Type[])
	 */
	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types)
			throws CallbackException {
		if (nextInterceptor != null) {
			nextInterceptor.onDelete(entity, id, state, propertyNames, types);
		}
	}

	/**
	 * @see net.sf.hibernate.Interceptor#preFlush(java.util.Iterator)
	 */
	public void preFlush(Iterator entities) throws CallbackException {
		if (nextInterceptor != null) {
			nextInterceptor.preFlush(entities);
		}
	}

	/**
	 * @see net.sf.hibernate.Interceptor#postFlush(java.util.Iterator)
	 */
	public void postFlush(Iterator entities) throws CallbackException {
		if (nextInterceptor != null) {
			nextInterceptor.postFlush(entities);
		}
	}

	/**
	 * @see net.sf.hibernate.Interceptor#isUnsaved(java.lang.Object)
	 */
	public Boolean isUnsaved(Object entity) {
		if (nextInterceptor != null) {
			return nextInterceptor.isUnsaved(entity);
		}
		else {
			return null;
		}
	}

	/**
	 * @see net.sf.hibernate.Interceptor#findDirty(java.lang.Object, java.io.Serializable,
	 *      java.lang.Object[], java.lang.Object[], java.lang.String[],
	 *      net.sf.hibernate.type.Type[])
	 */
	public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames,
			Type[] types) {
		if (nextInterceptor != null) {
			return nextInterceptor.findDirty(entity, id, currentState, previousState, propertyNames, types);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Convenience methods subclasses can use to apply the id to a new entity if they override
	 * the instantiate method.
	 * @param persistentClass
	 * @param id
	 * @param newEntity
	 * @throws CallbackException
	 */
	protected void setIdOnNewEntity(SessionFactory sessionFactory, Class persistentClass, Serializable id, Object newEntity) throws CallbackException {
		// Now we must set the id property on the object
		// to the PK value given as a parameter
		try {
			BeanWrapper wrapper = new BeanWrapperImpl(newEntity);

			wrapper.setPropertyValue(sessionFactory.getClassMetadata(persistentClass).getIdentifierPropertyName(), id);
		}
		catch (HibernateException ex) {
			throw new CallbackException("Error getting identifier property for class " + persistentClass, ex);
		}
	}
}