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

import org.apache.ojb.broker.Identity;
import org.apache.ojb.broker.query.Query;

import org.springframework.dao.DataAccessException;

/**
 * Interface that specifies a basic set of OJB PersistenceBroker operations.
 * Implemented by PersistenceBrokerTemplate. Not often used, but a useful
 * option to enhance testability, as it can easily be mocked or stubbed.
 *
 * <p>Provides PersistenceBrokerTemplate's data access methods that mirror
 * various PersistenceBroker methods.
 *
 * @author Juergen Hoeller
 * @since 02.07.2004
 * @see PersistenceBrokerTemplate
 * @see org.apache.ojb.broker.PersistenceBroker
 */
public interface PersistenceBrokerOperations {

	Object execute(PersistenceBrokerCallback action) throws DataAccessException;

	Collection executeFind(PersistenceBrokerCallback action) throws DataAccessException;


	//-------------------------------------------------------------------------
	// Convenience methods for load, find, save, delete
	//-------------------------------------------------------------------------

	Object getObjectByIdentity(Identity id) throws DataAccessException;

	Object getObjectByQuery(Query query) throws DataAccessException;

	Collection getCollectionByQuery(Query query) throws DataAccessException;

	int getCount(Query query) throws DataAccessException;

	void removeFromCache(Object entityOrId) throws DataAccessException;

	void clearCache() throws DataAccessException;

	void store(Object entity) throws DataAccessException;

	void delete(Object entity) throws DataAccessException;

	void deleteByQuery(Query query) throws DataAccessException;

}
