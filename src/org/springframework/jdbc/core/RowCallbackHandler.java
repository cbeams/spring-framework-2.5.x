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

package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Callback interface used by JdbcTemplate's query methods.
 * Implementations of this interface perform the actual work of extracting
 * results, but don't need to worry about exception handling. SQLExceptions
 * will be caught and handled correctly by the JdbcTemplate class.
 *
 * <p>In contrast to a ResultSetExtractor, a RowCallbackHandler object is
 * typically stateful: It keeps the result state within the object, to be
 * available for later inspection. See RowCountCallbackHandler's javadoc
 * for a usage example with JdbcTemplate.
 *
 * <p>The ResultReader subinterface allows to make a results list available
 * in a uniform manner. JdbcTemplate's query methods will return the results
 * list in that case, else returning null (-> result state is solely
 * available from RowCallbackHandler object).
 *
 * <p>A convenient out-of-the-box implementation of RowCallbackHandler is the
 * RowMapperResultReader adapter which delegates row mapping to a RowMapper.
 * Note that a RowMapper object is typically stateless and thus reusable;
 * just the RowMapperResultReader adapter is stateful.
 *
 * @author Rod Johnson
 * @see ResultSetExtractor
 * @see RowCountCallbackHandler
 * @see ResultReader
 * @see RowMapperResultReader
 * @see RowMapper
 */
public interface RowCallbackHandler {

	/**
	 * Implementations must implement this method to process each row of data
	 * in the ResultSet. This method should not call next() on the ResultSet,
	 * but extract the current values. Exactly what the implementation chooses
	 * to do is up to it; a trivial implementation might simply count rows,
	 * while another implementation might build an XML document.
	 * @param rs the ResultSet to process
	 * @throws SQLException if a SQLException is encountered getting
	 * column values (that is, there's no need to catch SQLException)
	 */
	void processRow(ResultSet rs) throws SQLException;

}
