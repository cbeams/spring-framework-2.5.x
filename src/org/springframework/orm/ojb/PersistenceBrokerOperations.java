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

package org.springframework.orm.ojb;

import java.util.Collection;
import java.util.Iterator;

import org.apache.ojb.broker.query.Query;

import org.springframework.dao.DataAccessException;

/**
 * Interface that specifies a basic set of OJB PersistenceBroker operations.
 * Implemented by PersistenceBrokerTemplate. Not often used, but a useful
 * option to enhance testability, as it can easily be mocked or stubbed.
 *
 * <p>Provides PersistenceBrokerTemplate's data access methods that mirror
 * various PersistenceBroker methods. See the PersistenceBroker javadocs
 * for details on those methods.
 *
 * @author Juergen Hoeller
 * @since 02.07.2004
 * @see PersistenceBrokerTemplate
 * @see org.apache.ojb.broker.PersistenceBroker
 */
public interface PersistenceBrokerOperations {

	/**
	 * Execute the action specified by the given action object within a
	 * PersistenceBroker. Application exceptions thrown by the action object
	 * get propagated to the caller (can only be unchecked). OJB exceptions
	 * are transformed into appropriate DAO ones. Allows for returning a
	 * result object, i.e. a domain object or a collection of domain objects.
	 * <p>Note: Callback code is not supposed to handle transactions itself!
	 * Use an appropriate transaction manager like PersistenceBrokerTransactionManager.
	 * @param action action object that specifies the OJB action
	 * @return a result object returned by the action, or null
	 * @throws org.springframework.dao.DataAccessException in case of OJB errors
	 * @see PersistenceBrokerTransactionManager
	 * @see org.springframework.dao
	 * @see org.springframework.transaction
	 */
	Object execute(PersistenceBrokerCallback action) throws DataAccessException;

	/**
	 * Execute the specified action assuming that the result object is a
	 * Collection. This is a convenience method for executing OJB queries
	 * within an action.
	 * @param action action object that specifies the OJB action
	 * @return a result object returned by the action, or null
	 * @throws org.springframework.dao.DataAccessException in case of OJB errors
	 */
	Collection executeFind(PersistenceBrokerCallback action) throws DataAccessException;


	//-------------------------------------------------------------------------
	// Convenience methods for load, find, save, delete
	//-------------------------------------------------------------------------

	Object getObjectByQuery(Query query) throws DataAccessException;

	Collection getCollectionByQuery(Query query) throws DataAccessException;

	Iterator getIteratorByQuery(final Query query) throws DataAccessException;

	Iterator getReportQueryIteratorByQuery(Query query);
	
	int getCount(Query query) throws DataAccessException;

	void removeFromCache(Object entityOrId) throws DataAccessException;

	void clearCache() throws DataAccessException;

	void store(Object entity) throws DataAccessException;

	void delete(Object entity) throws DataAccessException;

	void deleteByQuery(Query query) throws DataAccessException;

}
