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

package org.springframework.dao.support;

import java.util.Collection;

import org.springframework.dao.IncorrectResultSizeDataAccessException;

/**
 * Miscellaneous utility methods for DAO implementations.
 * Useful with any data access technology.
 * @author Juergen Hoeller
 * @since 26.05.2004
 */
public abstract class DataAccessUtils {

	/**
	 * Return a unique result object from the given Collection.
	 * Returns null if 0 result objects found; throws an exception
	 * if more than 1 found.
	 * @param results the result Collection
	 * @return the unique result object, or null if none
	 * @throws IncorrectResultSizeDataAccessException if more than one
	 * result object has been found in the given Collection
	 */
	public static Object uniqueResult(Collection results) throws IncorrectResultSizeDataAccessException {
		int size = results.size();
		if (size == 0) {
			return null;
		}
		if (size > 1) {
			throw new IncorrectResultSizeDataAccessException(1, size);
		}
		return results.iterator().next();
	}

	/**
	 * Return a unique result object from the given Collection.
	 * Throws an exception if 0 or more than 1 result objects found.
	 * @param results the result Collection
	 * @return the unique result object, or null if none
	 * @throws IncorrectResultSizeDataAccessException if more than one
	 * result object has been found in the given Collection
	 */
	public static Object requiredUniqueResult(Collection results) throws IncorrectResultSizeDataAccessException {
		Object result = uniqueResult(results);
		if (result == null) {
			throw new IncorrectResultSizeDataAccessException(1, 0);
		}
		return result;
	}

}
