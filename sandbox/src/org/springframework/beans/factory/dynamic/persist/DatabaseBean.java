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

import org.springframework.dao.DataAccessException;
import org.springframework.aop.target.dynamic.Refreshable;

/**
 * Interface to be implemented by objects that are backed by an object
 * mapped to an RDBMS row.
 * @author Rod Johnson
 */
public interface DatabaseBean extends Refreshable {

	long getPrimaryKey();

	void setPrimaryKey(long pk) throws DataAccessException;

	/**
	 * Return a readable String showing the details where this definition comes from,
	 * for use in logging etc.
	 * @return
	 */
	String getStoreDetails();
}
