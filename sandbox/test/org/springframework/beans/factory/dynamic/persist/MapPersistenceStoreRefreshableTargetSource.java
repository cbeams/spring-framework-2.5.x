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

package org.springframework.beans.factory.dynamic.persist;

import java.util.HashMap;

import org.springframework.dao.DataAccessException;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * 
 * @author Rod Johnson
 */
public class MapPersistenceStoreRefreshableTargetSource extends AbstractPersistenceStoreRefreshableTargetSource {
	
	private HashMap map = new HashMap();
	
	public void put(long pk, Object o) {
		map.put(new Long(pk), o);
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.persist.AbstractPersistenceStoreRefreshableTargetSource#loadFromPersistentStore()
	 */
	protected Object loadFromPersistentStore() throws DataAccessException {
		Long key = new Long(getPrimaryKey());
		Object o = map.get(key);
		if (o == null) {
			throw new ObjectRetrievalFailureException(getPersistentClass(), key);
		}
		return o;
	}
	
	protected String storeDetails() {
		return "MAP";
	}

	public boolean isModified() {
		return true;
	}

}
