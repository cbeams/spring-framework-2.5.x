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

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.ojb.broker.PBKey;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerException;
import org.apache.ojb.broker.accesslayer.LookupException;
import org.apache.ojb.broker.query.Query;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Helper class that simplifies OJB PersistenceBroker data access code,
 * and converts PersistenceBrokerExceptions into exceptions compatible
 * to the org.springframework.dao exception hierarchy.
 *
 * <p>The central method is "execute", supporting OJB code implementing
 * the PersistenceBrokerCallback interface. It provides PersistenceBroker handling
 * such that neither the PersistenceBrokerCallback implementation nor the calling
 * code needs to explicitly care about retrieving/closing PersistenceBrokers,
 * or handling OJB lifecycle exceptions.
 *
 * <p>Typically used to implement data access or business logic services that
 * use OJB within their implementation but are OJB-agnostic in their interface.
 * The latter resp. code calling the latter only have to deal with business
 * objects, query objects, and org.springframework.dao exceptions.
 *
 * @author Juergen Hoeller
 * @since 02.07.2004
 * @see PersistenceBrokerCallback
 */
public class PersistenceBrokerTemplate extends OjbAccessor implements PersistenceBrokerOperations {

	/**
	 * Create a new PersistenceBrokerTemplate,
	 * sing the default connection configured for OJB.
	 */
	public PersistenceBrokerTemplate() {
	}

	/**
	 * Create a new PersistenceBrokerTemplate.
	 * @param jcdAlias the JDBC Connection Descriptor alias
	 * of the PersistenceBroker configuration to use
	 */
	public PersistenceBrokerTemplate(String jcdAlias) {
		setJcdAlias(jcdAlias);
	}

	/**
	 * Create a new PersistenceBrokerTemplate.
	 * @param pbKey the PBKey of the PersistenceBroker configuration to use
	 */
	public PersistenceBrokerTemplate(PBKey pbKey) {
		setPbKey(pbKey);
	}


	public Object execute(PersistenceBrokerCallback action) throws DataAccessException {
		PersistenceBroker pb = OjbFactoryUtils.getPersistenceBroker(getPbKey());
		try {
			return action.doInPersistenceBroker(pb);
		}
		catch (PersistenceBrokerException ex) {
			throw convertOjbAccessException(ex);
		}
		catch (LookupException ex) {
			throw new DataAccessResourceFailureException("Could not retrieve resource", ex);
		}
		catch (SQLException ex) {
			throw convertJdbcAccessException(ex);
		}
		catch (RuntimeException ex) {
			// callback code threw application exception
			throw ex;
		}
		finally {
			OjbFactoryUtils.closePersistenceBrokerIfNecessary(pb, getPbKey());
		}
	}

	public Collection executeFind(PersistenceBrokerCallback action) throws DataAccessException {
		return (Collection) execute(action);
	}


	public Object getObjectByQuery(final Query query) throws DataAccessException {
		return execute(new PersistenceBrokerCallback() {
			public Object doInPersistenceBroker(PersistenceBroker pb) throws PersistenceBrokerException {
				return pb.getObjectByQuery(query);
			}
		});
	}

	public Collection getCollectionByQuery(final Query query) throws DataAccessException {
		return executeFind(new PersistenceBrokerCallback() {
			public Object doInPersistenceBroker(PersistenceBroker pb) throws PersistenceBrokerException {
				return pb.getCollectionByQuery(query);
			}
		});
	}

	public Iterator getIteratorByQuery(final Query query) throws DataAccessException {
		return (Iterator) execute(new PersistenceBrokerCallback() {
			public Object doInPersistenceBroker(PersistenceBroker pb) throws PersistenceBrokerException {
				return pb.getIteratorByQuery(query);
			}
		});
	}

	public int getCount(final Query query) throws DataAccessException {
		Integer count = (Integer) execute(new PersistenceBrokerCallback() {
			public Object doInPersistenceBroker(PersistenceBroker pb) throws PersistenceBrokerException {
				return new Integer(pb.getCount(query));
			}
		});
		return count.intValue();
	}

	public void removeFromCache(final Object entityOrId) throws DataAccessException {
		execute(new PersistenceBrokerCallback() {
			public Object doInPersistenceBroker(PersistenceBroker pb) throws PersistenceBrokerException {
				pb.removeFromCache(entityOrId);
				return null;
			}
		});
	}

	public void clearCache() throws DataAccessException {
		execute(new PersistenceBrokerCallback() {
			public Object doInPersistenceBroker(PersistenceBroker pb) throws PersistenceBrokerException {
				pb.clearCache();
				return null;
			}
		});
	}

	public void store(final Object entity) throws DataAccessException {
		execute(new PersistenceBrokerCallback() {
			public Object doInPersistenceBroker(PersistenceBroker pb) throws PersistenceBrokerException {
				pb.store(entity);
				return null;
			}
		});
	}

	public void delete(final Object entity) throws DataAccessException {
		execute(new PersistenceBrokerCallback() {
			public Object doInPersistenceBroker(PersistenceBroker pb) throws PersistenceBrokerException {
				pb.delete(entity);
				return null;
			}
		});
	}

	public void deleteByQuery(final Query query) throws DataAccessException {
		execute(new PersistenceBrokerCallback() {
			public Object doInPersistenceBroker(PersistenceBroker pb) throws PersistenceBrokerException {
				pb.deleteByQuery(query);
				return null;
			}
		});
	}

}
