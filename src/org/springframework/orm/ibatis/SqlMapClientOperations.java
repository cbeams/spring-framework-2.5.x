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

package org.springframework.orm.ibatis;

import java.util.List;
import java.util.Map;

import com.ibatis.common.util.PaginatedList;
import com.ibatis.sqlmap.client.event.RowHandler;

import org.springframework.dao.DataAccessException;

/**
 * Interface that specifies a basic set of iBATIS SqlMapClient operations.
 * Implemented by SqlMapClientTemplate. Not often used, but a useful option
 * to enhance testability, as it can easily be mocked or stubbed.
 *
 * <p>Provides SqlMapClientTemplate's convenience methods that mirror SqlMapSession's
 * execution methods. See the SqlMapSession javadocs for details on those methods.
 *
 * <p>NOTE: The SqlMapClient/SqlMapSession API is the API of iBATIS SQL Maps 2.
 * With SQL Maps 1.x, the SqlMap/MappedStatement API has to be used.
 *
 * @author Juergen Hoeller
 * @since 24.02.2004
 * @see SqlMapClientTemplate
 * @see com.ibatis.sqlmap.client.SqlMapClient
 */
public interface SqlMapClientOperations {

	Object queryForObject(String statementName, Object parameterObject) throws DataAccessException;

	Object queryForObject(String statementName, Object parameterObject,	Object resultObject)
			throws DataAccessException;

	List queryForList(String statementName, Object parameterObject) throws DataAccessException;

	List queryForList(String statementName, Object parameterObject, int skipResults,
										int maxResults) throws DataAccessException;

	void queryWithRowHandler(String statementName, Object parameterObject, RowHandler rowHandler)
		throws DataAccessException;

	/**
	 * @deprecated
	 */
	List queryForList(String statementName, Object parameterObject, RowHandler rowHandler)
			throws DataAccessException;

	PaginatedList queryForPaginatedList(String statementName, Object parameterObject, int pageSize)
			throws DataAccessException;

	Map queryForMap(String statementName, Object parameterObject, String keyProperty)
			throws DataAccessException;

	Map queryForMap(String statementName, Object parameterObject, String keyProperty,
									String valueProperty) throws DataAccessException;

	Object insert(String statementName, Object parameterObject) throws DataAccessException;

	int update(String statementName, Object parameterObject) throws DataAccessException;

	int delete(String statementName, Object parameterObject) throws DataAccessException;

}
