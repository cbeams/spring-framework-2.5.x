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

package org.springframework.orm.jdo;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.springframework.dao.DataAccessException;

/**
 * Interface that specifies a basic set of JDO operations.
 * Implemented by JdoTemplate. Not often used, but a useful option
 * to enhance testability, as it can easily be mocked or stubbed.
 *
 * <p>Provides JdoTemplate's data access methods that mirror
 * various PersistenceManager methods.
 *
 * @author Juergen Hoeller
 * @since 12.06.2004
 * @see JdoTemplate
 * @see javax.jdo.PersistenceManager
 */
public interface JdoOperations {

	/**
	 * Execute the action specified by the given action object within a
	 * PersistenceManager. Application exceptions thrown by the action object
	 * get propagated to the caller (can only be unchecked). JDO exceptions
	 * are transformed into appropriate DAO ones. Allows for returning a
	 * result object, i.e. a domain object or a collection of domain objects.
	 * <p>Note: Callback code is not supposed to handle transactions itself!
	 * Use an appropriate transaction manager like JdoTransactionManager.
	 * @param action action object that specifies the JDO action
	 * @return a result object returned by the action, or null
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see JdoTransactionManager
	 * @see org.springframework.dao
	 * @see org.springframework.transaction
	 */
	Object execute(JdoCallback action) throws DataAccessException;

	/**
	 * Execute the specified action assuming that the result object is a
	 * Collection. This is a convenience method for executing JDO queries
	 * within an action.
	 * @param action action object that specifies the JDO action
	 * @return a result object returned by the action, or null
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 */
	Collection executeFind(JdoCallback action) throws DataAccessException;


	//-------------------------------------------------------------------------
	// Convenience methods for load, save, delete
	//-------------------------------------------------------------------------

	/**
	 * Return the persistent instance with the given JDO object id,
	 * throwing an exception if not found.
	 * <p>A JDO object id identifies both the persistent class and the id
	 * within the namespace of that class.
	 * @param objectId a JDO object id of the persistent instance
	 * @return the persistent instance
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#getObjectById
	 */
	Object getObjectById(Serializable objectId) throws DataAccessException;

	/**
	 * Return the persistent instance of the given entity class
	 * with the given id value, throwing an exception if not found.
	 * <p>The given id value is typically just unique within the namespace
	 * of the persistent class. Its toString value must correspond to the
	 * toString value of the corresponding JDO object id.
	 * <p>Usually, the passed-in value will have originated from the primary
	 * key field of a persistent object that uses JDO's application identity.
	 * @param entityClass a persistent class
	 * @param idValue an id value of the persistent instance
	 * @return the persistent instance
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#getObjectById
	 * @see javax.jdo.PersistenceManager#newObjectIdInstance
	 */
	Object getObjectById(Class entityClass, Serializable idValue) throws DataAccessException;

	/**
	 * Remove the given object from the PersistenceManager cache.
	 * @param entity the persistent instance to evict
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#evict
	 */
	void evict(Object entity) throws DataAccessException;

	/**
	 * Remove all objects from the PersistenceManager cache.
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#evictAll
	 */
	void evictAll() throws DataAccessException;

	/**
	 * Re-read the state of the given persistent instance.
	 * @param entity the persistent instance to re-read
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#refresh
	 */
	void refresh(Object entity) throws DataAccessException;

	/**
	 * Re-read the state of all persistent instances.
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#refreshAll
	 */
	void refreshAll() throws DataAccessException;

	/**
	 * Make the given transient instance persistent.
	 * @param entity the transient instance to make persistent
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#makePersistent
	 */
	void makePersistent(Object entity) throws DataAccessException;

	/**
	 * Delete the given persistent instance.
	 * @param entity the persistent instance to delete
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#deletePersistent
	 */
	void deletePersistent(Object entity) throws DataAccessException;

	/**
	 * Delete all given persistent instances.
	 * <p>This can be combined with any of the find methods to delete by query
	 * in two lines of code.
	 * @param entities the persistent instances to delete
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#deletePersistentAll
	 */
	void deletePersistentAll(Collection entities) throws DataAccessException;

	/**
	 * Flush all transactional modifications to the database.
	 * <p>Only invoke this for selective eager flushing, for example when JDBC code
	 * needs to see certain changes within the same transaction. Else, it's preferable
	 * to rely on auto-flushing at transaction completion.
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see JdoDialect#flush
	 */
	void flush() throws DataAccessException;


	//-------------------------------------------------------------------------
	// Convenience finder methods
	//-------------------------------------------------------------------------

	/**
	 * Return all persistent instances of the given class.
	 * @param entityClass a persistent class
	 * @return the persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newQuery(Class)
	 */
	Collection find(Class entityClass) throws DataAccessException;

	/**
	 * Return all persistent instances of the given class that match the given
	 * JDOQL filter.
	 * @param entityClass a persistent class
	 * @param filter the JDOQL filter to match
	 * @return the persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newQuery(Class, String)
	 */
	Collection find(Class entityClass, String filter) throws DataAccessException;

	/**
	 * Return all persistent instances of the given class that match the given
	 * JDOQL filter, with the given result ordering.
	 * @param entityClass a persistent class
	 * @param filter the JDOQL filter to match
	 * @param ordering the ordering of the result
	 * @return the persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newQuery(Class, String)
	 * @see javax.jdo.Query#setOrdering
	 */
	Collection find(Class entityClass, String filter, String ordering)
			throws DataAccessException;

	/**
	 * Return all persistent instances of the given class that match the given
	 * JDOQL filter, using the given parameter declarations and parameter values.
	 * @param entityClass a persistent class
	 * @param filter the JDOQL filter to match
	 * @param parameters the JDOQL parameter declarations
	 * @param values the corresponding parameter values
	 * @return the persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newQuery(Class, String)
	 * @see javax.jdo.Query#declareParameters
	 * @see javax.jdo.Query#executeWithArray
	 */
	Collection find(Class entityClass, String filter, String parameters, Object[] values)
			throws DataAccessException;

	/**
	 * Return all persistent instances of the given class that match the given
	 * JDOQL filter, using the given parameter declarations and parameter values,
	 * with the given result ordering.
	 * @param entityClass a persistent class
	 * @param filter the JDOQL filter to match
	 * @param parameters the JDOQL parameter declarations
	 * @param values the corresponding parameter values
	 * @param ordering the ordering of the result
	 * @return the persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newQuery(Class, String)
	 * @see javax.jdo.Query#declareParameters
	 * @see javax.jdo.Query#executeWithArray
	 * @see javax.jdo.Query#setOrdering
	 */
	Collection find(Class entityClass, String filter, String parameters, Object[] values,
									String ordering) throws DataAccessException;

	/**
	 * Return all persistent instances of the given class that match the given
	 * JDOQL filter, using the given parameter declarations and parameter values.
	 * @param entityClass a persistent class
	 * @param filter the JDOQL filter to match
	 * @param parameters the JDOQL parameter declarations
	 * @param values a Map with parameter names as keys and parameter values
	 * @return the persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newQuery(Class, String)
	 * @see javax.jdo.Query#declareParameters
	 * @see javax.jdo.Query#executeWithMap
	 */
	Collection find(Class entityClass, String filter, String parameters, Map values)
			throws DataAccessException;

	/**
	 * Return all persistent instances of the given class that match the given
	 * JDOQL filter, using the given parameter declarations and parameter values,
	 * with the given result ordering.
	 * @param entityClass a persistent class
	 * @param filter the JDOQL filter to match
	 * @param parameters the JDOQL parameter declarations
	 * @param values a Map with parameter names as keys and parameter values
	 * @param ordering the ordering of the result
	 * @return the persistent instances
	 * @throws org.springframework.dao.DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newQuery(Class, String)
	 * @see javax.jdo.Query#declareParameters
	 * @see javax.jdo.Query#executeWithMap
	 * @see javax.jdo.Query#setOrdering
	 */
	Collection find(Class entityClass, String filter, String parameters, Map values,
									String ordering) throws DataAccessException;

}
