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

package org.springframework.jdbc.core.support;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;

/** 
 * An implementation of the KeyRetriever interface.
 * 
 * @author Thomas Risberg
 * @see KeyHolder
 * @see JdbcTemplate
 * @see org.springframework.jdbc.object.SqlUpdate
 */
public class GeneratedKeyHolder implements KeyHolder {
	private List keyList = new LinkedList();

	/* Return single key value
	 * @see org.springframework.jdbc.core.support.KeyRetriever#getKey()
	 */
	public Object getKey() throws InvalidDataAccessApiUsageException, DataRetrievalFailureException {
		if (keyList.size() == 0)
			return null;
		if (keyList.size() > 1 || ((Map)keyList.get(0)).size() > 1)
			throw new 
				InvalidDataAccessApiUsageException("The getKey method should only be used when a single key is returned.  " +
						"The current key entry contains multiple keys: " + 
						keyList);
		Iterator keyIter = ((Map)keyList.get(0)).values().iterator();
		if (keyIter.hasNext()) {
			return keyIter.next();
		}
		else {
			throw new DataRetrievalFailureException("Unable to retrieve the generated key.  Check that the table has the an identity column enabled");
		}
	}
	/* Return the Map of keys
	 * @see org.springframework.jdbc.core.support.KeyRetriever#getKeys()
	 */
	public Map getKeys() throws InvalidDataAccessApiUsageException {
		if (keyList.size() == 0)
			return null;
		if (keyList.size() > 1)
			throw new 
				InvalidDataAccessApiUsageException("The getKeys method should only be used when keys for a single row are returned.  " + 
						"The current key list contains keys for multiple rows: " +
						keyList);
		return (Map)keyList.get(0);
	}
	/* Return the List containing the keys
	 * @see org.springframework.jdbc.core.support.KeyRetriever#getKeyList()
	 */
	public List getKeyList() {
		return keyList;
	}
}
