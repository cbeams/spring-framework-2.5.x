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

package org.springframework.orm.hibernate;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.LockMode;
import net.sf.hibernate.type.Type;

import org.springframework.dao.DataAccessException;

/**
 * Interface that specifies a basic set of Hibernate operations.
 * Implemented by HibernateTemplate. Not often used, but a useful option
 * to enhance testability, as it can easily be mocked or stubbed.
 *
 * <p>Provides HibernateTemplate's data access methods that mirror
 * various Session methods.
 *
 * @author Juergen Hoeller
 * @since 05.02.2004
 * @see HibernateTemplate
 * @see net.sf.hibernate.Session
 */
public interface HibernateOperations {

	/**
	 * Execute the action specified by the given action object within a session.
	 * Application exceptions thrown by the action object get propagated to the
	 * caller (can only be unchecked). Hibernate exceptions are transformed into
	 * appropriate DAO ones. Allows for returning a result object, i.e. a domain
	 * object or a collection of domain objects.
	 * <p>Note: Callback code is not supposed to handle transactions itself!
	 * Use an appropriate transaction manager like HibernateTransactionManager.
	 * Generally, callback code must not touch any Session lifecycle methods,
	 * like close, disconnect, or reconnect, to let the template do its work.
	 * @param action callback object that specifies the Hibernate action
	 * @return a result object returned by the action, or null
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see HibernateTransactionManager
	 * @see org.springframework.dao
	 * @see org.springframework.transaction
	 */
	Object execute(HibernateCallback action) throws DataAccessException;

	/**
	 * Execute the specified action assuming that the result object is a List.
	 * This is a convenience method for executing Hibernate find calls or
	 * queries within an action.
	 * @param action action object that specifies the Hibernate action
	 * @return a result object returned by the action, or null
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 */
	List executeFind(HibernateCallback action) throws DataAccessException;


	//-------------------------------------------------------------------------
	// Convenience methods for loading individual objects
	//-------------------------------------------------------------------------

	/**
	 * Return the persistent instance of the given entity class
	 * with the given identifier, or null if not found.
	 * @param entityClass a persistent class
	 * @param id an identifier of the persistent instance
	 * @return the persistent instance, or null if not found
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#get(Class, java.io.Serializable)
	 */
	Object get(Class entityClass, Serializable id) throws DataAccessException;

	/**
	 * Return the persistent instance of the given entity class
	 * with the given identifier, or null if not found.
	 * Obtains the specified lock mode if the instance exists.
	 * @param entityClass a persistent class
	 * @param id an identifier of the persistent instance
	 * @param lockMode the lock mode to obtain
	 * @return the persistent instance, or null if not found
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#get(Class, java.io.Serializable, net.sf.hibernate.LockMode)
	 */
	Object get(Class entityClass, Serializable id, LockMode lockMode)
			throws DataAccessException;

	/**
	 * Return the persistent instance of the given entity class
	 * with the given identifier, throwing an exception if not found.
	 * @param entityClass a persistent class
	 * @param id an identifier of the persistent instance
	 * @return the persistent instance
	 * @throws HibernateObjectRetrievalFailureException if the instance could not be found
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#load(Class, java.io.Serializable)
	 */
	Object load(Class entityClass, Serializable id) throws DataAccessException;

	/**
	 * Return the persistent instance of the given entity class
	 * with the given identifier, throwing an exception if not found.
	 * Obtains the specified lock mode if the instance exists.
	 * @param entityClass a persistent class
	 * @param id an identifier of the persistent instance
	 * @param lockMode the lock mode to obtain
	 * @return the persistent instance
	 * @throws HibernateObjectRetrievalFailureException if the instance could not be found
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#load(Class, java.io.Serializable)
	 */
	Object load(Class entityClass, Serializable id, LockMode lockMode)
			throws DataAccessException;

	/**
	 * Return all persistent instances of the given entity class.
	 * Note: Use queries or criteria for retrieving a specific subset. 
	 * @param entityClass a persistent class
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException if there is a Hibernate error
	 * @see net.sf.hibernate.Session#createCriteria
	 */
	List loadAll(Class entityClass) throws DataAccessException;

	/**
	 * Re-read the state of the given persistent instance.
	 * @param entity the persistent instance to re-read
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#refresh(Object)
	 */
	void refresh(Object entity) throws DataAccessException;

	/**
	 * Re-read the state of the given persistent instance.
	 * Obtains the specified lock mode for the instance.
	 * @param entity the persistent instance to re-read
	 * @param lockMode the lock mode to obtain
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#refresh(Object, net.sf.hibernate.LockMode)
	 */
	void refresh(Object entity, LockMode lockMode) throws DataAccessException;

	/**
	 * Check whether the given object is in the Session cache.
	 * @param entity the persistence instance to check
	 * @return whether the given object is in the Session cache
	 * @throws org.springframework.dao.DataAccessException if there is a Hibernate error
	 * @see net.sf.hibernate.Session#contains
	 */
	boolean contains(Object entity) throws DataAccessException;

	/**
	 * Remove the given object from the Session cache.
	 * @param entity the persistent instance to evict
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#evict
	 */
	void evict(Object entity) throws DataAccessException;


	//-------------------------------------------------------------------------
	// Convenience methods for storing individual objects
	//-------------------------------------------------------------------------

	/**
	 * Obtain the specified lock level upon the given object, implicitly
	 * checking whether the corresponding database entry still exists
	 * (throwing an OptimisticLockingFailureException if not found).
	 * @param entity the persistent instance to lock
	 * @param lockMode the lock mode to obtain
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see HibernateOptimisticLockingFailureException
	 * @see net.sf.hibernate.Session#lock(Object, net.sf.hibernate.LockMode)
	 */
	void lock(Object entity, LockMode lockMode) throws DataAccessException;

	/**
	 * Persist the given transient instance.
	 * @param entity the transient instance to persist
	 * @return the generated identifier
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#save(Object)
	 */
	Serializable save(Object entity) throws DataAccessException;

	/**
	 * Persist the given transient instance with the given identifier.
	 * @param entity the transient instance to persist
	 * @param id the identifier to assign
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#save(Object, java.io.Serializable)
	 */
	void save(Object entity, Serializable id) throws DataAccessException;

	/**
	 * Save respectively update the given persistent instance,
	 * according to its ID (matching the configured "unsaved-value"?).
	 * @param entity the persistent instance to save respectively update
	 * (to be associated with the Hibernate Session)
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#saveOrUpdate(Object)
	 */
	void saveOrUpdate(Object entity) throws DataAccessException;

	/**
	 * Save respectively update the contents of given persistent object,
	 * according to its ID (matching the configured "unsaved-value"?).
	 * Will copy the contained fields to an already loaded instance
	 * with the same ID, if appropriate.
	 * @param entity the persistent object to save respectively update
	 * (<i>not</i> necessarily to be associated with the Hibernate Session)
	 * @return the actually associated persistent object
	 * (either an already loaded instance with the same ID, or the given object)
	 * @throws DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#saveOrUpdateCopy(Object)
	 */
	Object saveOrUpdateCopy(Object entity) throws DataAccessException;

	/**
	 * Update the given persistent instance.
	 * @param entity the persistent instance to update
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#update(Object)
	 */
	void update(Object entity) throws DataAccessException;

	/**
	 * Update the given persistent instance.
	 * <p>Obtains the specified lock mode if the instance exists, implicitly
	 * checking whether the corresponding database entry still exists
	 * (throwing an OptimisticLockingFailureException if not found).
	 * @param entity the persistent instance to update
	 * @param lockMode the lock mode to obtain
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see HibernateOptimisticLockingFailureException
	 * @see net.sf.hibernate.Session#update(Object)
	 */
	void update(Object entity, LockMode lockMode) throws DataAccessException;

	/**
	 * Delete the given persistent instance.
	 * @param entity the persistent instance to delete
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#delete(Object)
	 */
	void delete(Object entity) throws DataAccessException;

	/**
	 * Delete the given persistent instance.
	 * <p>Obtains the specified lock mode if the instance exists, implicitly
	 * checking whether the corresponding database entry still exists
	 * (throwing an OptimisticLockingFailureException if not found).
	 * @param entity the persistent instance to delete
	 * @param lockMode the lock mode to obtain
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see HibernateOptimisticLockingFailureException
	 * @see net.sf.hibernate.Session#delete(Object)
	 */
	void delete(Object entity, LockMode lockMode) throws DataAccessException;

	/**
	 * Delete all given persistent instances.
	 * <p>This can be combined with any of the find methods to delete by query
	 * in two lines of code, similar to Session's delete by query methods.
	 * @param entities the persistent instances to delete
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#delete(String)
	 */
	void deleteAll(Collection entities) throws DataAccessException;

	/**
	 * Flush all pending saves, updates and deletes to the database.
	 * <p>Only invoke this for selective eager flushing, for example when JDBC code
	 * needs to see certain changes within the same transaction. Else, it's preferable
	 * to rely on auto-flushing at transaction completion.
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#flush
	 */
	void flush() throws DataAccessException;

	/**
	 * Remove all objects from the Session cache, and cancel all pending saves,
	 * updates and deletes.
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#clear
	 */
	void clear() throws DataAccessException;


	//-------------------------------------------------------------------------
	// Convenience finder methods for HQL strings
	//-------------------------------------------------------------------------

	/**
	 * Execute a query for persistent instances.
	 * @param queryString a query expressed in Hibernate's query language
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String)
	 * @see net.sf.hibernate.Session#createQuery
	 */
	List find(String queryString) throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding
	 * one value to a "?" parameter in the query string.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param value the value of the parameter
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String, Object, net.sf.hibernate.type.Type)
	 * @see net.sf.hibernate.Session#createQuery
	 */
	List find(String queryString, Object value) throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding one value
	 * to a "?" parameter of the given type in the query string.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param value the value of the parameter
	 * @param type Hibernate type of the parameter
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String, Object, net.sf.hibernate.type.Type)
	 * @see net.sf.hibernate.Session#createQuery
	 */
	List find(String queryString, Object value, Type type) throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding a
	 * number of values to "?" parameters in the query string.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param values the values of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String, Object[], net.sf.hibernate.type.Type[])
	 * @see net.sf.hibernate.Session#createQuery
	 */
	List find(String queryString, Object[] values) throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding a number of
	 * values to "?" parameters of the given types in the query string.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param values the values of the parameters
	 * @param types Hibernate types of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String, Object[], net.sf.hibernate.type.Type[])
	 * @see net.sf.hibernate.Session#createQuery
	 */
	List find(String queryString, Object[] values, Type[] types) throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding
	 * one value to a ":" named parameter in the query string.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param paramName the name of parameter
	 * @param value the value of the parameter
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String, Object, net.sf.hibernate.type.Type)
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedParam(String queryName, String paramName, Object value)
			throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding
	 * one value to a ":" named parameter in the query string.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param paramName the name of the parameter
	 * @param value the value of the parameter
	 * @param type Hibernate type of the parameter
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String, Object, net.sf.hibernate.type.Type)
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedParam(String queryName, String paramName, Object value, Type type)
			throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding a
	 * number of values to ":" named parameters in the query string.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param paramNames the names of the parameters
	 * @param values the values of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String, Object[], net.sf.hibernate.type.Type[])
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedParam(String queryString, String[] paramNames, Object[] values)
			throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding a
	 * number of values to ":" named parameters in the query string.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param paramNames the names of the parameters
	 * @param values the values of the parameters
	 * @param types Hibernate types of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String, Object[], net.sf.hibernate.type.Type[])
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedParam(String queryString, String[] paramNames, Object[] values, Type[] types)
			throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding the properties
	 * of the given bean to <i>named</i> parameters in the query string.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param valueBean the values of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Query#setProperties
	 * @see net.sf.hibernate.Session#createQuery
	 */
	List findByValueBean(String queryString, Object valueBean) throws DataAccessException;


	//-------------------------------------------------------------------------
	// Convenience finder methods for named queries
	//-------------------------------------------------------------------------

	/**
	 * Execute a named query for persistent instances.
	 * A named query is defined in a Hibernate mapping file.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String)
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedQuery(String queryName) throws DataAccessException;

	/**
	 * Execute a named query for persistent instances, binding
	 * one value to a "?" parameter in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String, Object, net.sf.hibernate.type.Type)
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedQuery(String queryName, Object value) throws DataAccessException;

	/**
	 * Execute a named query for persistent instances, binding
	 * one value to a "?" parameter in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param type Hibernate type of the parameter
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String, Object, net.sf.hibernate.type.Type)
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedQuery(String queryName, Object value, Type type) throws DataAccessException;

	/**
	 * Execute a named query for persistent instances, binding a
	 * number of values to "?" parameters in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param values the values of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String, Object[], net.sf.hibernate.type.Type[])
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedQuery(String queryName, Object[] values) throws DataAccessException;

	/**
	 * Execute a named query for persistent instances, binding a
	 * number of values to "?" parameters in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param values the values of the parameters
	 * @param types Hibernate types of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String, Object[], net.sf.hibernate.type.Type[])
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedQuery(String queryName, Object[] values, Type[] types)
			throws DataAccessException;

	/**
	 * @deprecated in favor of findByNamedQueryAndNamedParam,
	 * to avoid parameter overloading ambiguities
	 * @see #findByNamedQueryAndNamedParam
	 */
	List findByNamedQuery(String queryName, String paramName, Object value)
			throws DataAccessException;

	/**
	 * @deprecated in favor of findByNamedQueryAndNamedParam,
	 * to avoid parameter overloading ambiguities
	 * @see #findByNamedQueryAndNamedParam
	 */
	List findByNamedQuery(String queryName, String paramName, Object value, Type type)
			throws DataAccessException;

	/**
	 * @deprecated in favor of findByNamedQueryAndNamedParam,
	 * to avoid parameter overloading ambiguities
	 * @see #findByNamedQueryAndNamedParam
	 */
	List findByNamedQuery(String queryName, String[] paramNames, Object[] values)
			throws DataAccessException;

	/**
	 * @deprecated in favor of findByNamedQueryAndNamedParam,
	 * to avoid parameter overloading ambiguities
	 * @see #findByNamedQueryAndNamedParam
	 */
	List findByNamedQuery(String queryName, String[] paramNames, Object[] values, Type[] types)
			throws DataAccessException;

	/**
	 * Execute a named query for persistent instances, binding
	 * one value to a ":" named parameter in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param paramName the name of parameter
	 * @param value the value of the parameter
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String, Object, net.sf.hibernate.type.Type)
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedQueryAndNamedParam(String queryName, String paramName, Object value)
			throws DataAccessException;

	/**
	 * Execute a named query for persistent instances, binding
	 * one value to a ":" named parameter in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param paramName the name of the parameter
	 * @param value the value of the parameter
	 * @param type Hibernate type of the parameter
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String, Object, net.sf.hibernate.type.Type)
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedQueryAndNamedParam(String queryName, String paramName, Object value, Type type)
			throws DataAccessException;

	/**
	 * Execute a named query for persistent instances, binding a
	 * number of values to ":" named parameters in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param paramNames the names of the parameters
	 * @param values the values of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String, Object[], net.sf.hibernate.type.Type[])
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedQueryAndNamedParam(String queryName, String[] paramNames, Object[] values)
			throws DataAccessException;

	/**
	 * Execute a named query for persistent instances, binding a
	 * number of values to ":" named parameters in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param paramNames the names of the parameters
	 * @param values the values of the parameters
	 * @param types Hibernate types of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String, Object[], net.sf.hibernate.type.Type[])
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedQueryAndNamedParam(String queryName, String[] paramNames, Object[] values, Type[] types)
			throws DataAccessException;

	/**
	 * Execute a named query for persistent instances, binding the properties
	 * of the given bean to ":" named parameters in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param valueBean the values of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Query#setProperties
	 * @see net.sf.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedQueryAndValueBean(String queryName, Object valueBean)
			throws DataAccessException;


	//-------------------------------------------------------------------------
	// Convenience query methods for iterate and delete
	//-------------------------------------------------------------------------

	/**
	 * Execute a query for persistent instances.
	 * <p>Returns the results as Iterator. Entities returned are initialized
	 * on demand. See Hibernate docs for details.
	 * @param queryString a query expressed in Hibernate's query language
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#iterate(String)
	 * @see net.sf.hibernate.Session#createQuery
	 */
	Iterator iterate(String queryString) throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding one value
	 * to a "?" parameter of the given type in the query string.
	 * <p>Returns the results as Iterator. Entities returned are initialized
	 * on demand. See Hibernate docs for details.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param value the value of the parameter
	 * @param type Hibernate type of the parameter
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#iterate(String, Object, net.sf.hibernate.type.Type)
	 * @see net.sf.hibernate.Session#createQuery
	 */
	Iterator iterate(String queryString, Object value, Type type) throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding a number of
	 * values to "?" parameters of the given types in the query string.
	 * <p>Returns the results as Iterator. Entities returned are initialized
	 * on demand. See Hibernate docs for details.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param values the values of the parameters
	 * @param types Hibernate types of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#find(String, Object[], net.sf.hibernate.type.Type[])
	 * @see net.sf.hibernate.Session#createQuery
	 */
	Iterator iterate(String queryString, Object[] values, Type[] types) throws DataAccessException;

	/**
	 * Delete all objects returned by the query. Return the number of objects deleted.
	 * @param queryString a query expressed in Hibernate's query language
	 * @return the number of instances deleted
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#delete(String)
	 */
	int delete(String queryString) throws DataAccessException;

	/**
	 * Delete all objects returned by the query. Return the number of objects deleted.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param value the value of the parameter
	 * @param type Hibernate type of the parameter
	 * @return the number of instances deleted
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#delete(String, Object, net.sf.hibernate.type.Type)
	 */
	int delete(String queryString, Object value, Type type) throws DataAccessException;

	/**
	 * Delete all objects returned by the query. Return the number of objects deleted.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param values the values of the parameters
	 * @param types Hibernate types of the parameters
	 * @return the number of instances deleted
	 * @throws org.springframework.dao.DataAccessException in case of Hibernate errors
	 * @see net.sf.hibernate.Session#delete(String, Object[], net.sf.hibernate.type.Type[])
	 */
	int delete(String queryString, Object[] values, Type[] types) throws DataAccessException;

}
