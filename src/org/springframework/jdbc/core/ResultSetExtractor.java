
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

import org.springframework.dao.DataAccessException;

/** 
 * Callback interface used by JdbcTemplate's query methods.
 * Implementations of this interface perform the actual work of extracting
 * results, but don't need to worry about exception handling. SQLExceptions
 * will be caught and handled correctly by the JdbcTemplate class.
 *
 * <p>This interface is mainly used within the JDBC framework.
 * A RowCallbackHandler is usually a simpler choice for ResultSet processing,
 * in particular a RowMapperResultReader in combination with a RowMapper.
 *
 * <p>Note: In contrast to a RowCallbackHandler, a ResultSetExtractor object
 * is typically stateless and thus reusable, as long as it doesn't access
 * stateful resources (like output streams when streaming LOB contents)
 * or keep result state within the object.
 *
 * @author Rod Johnson
 * @since April 24, 2003
 * @see JdbcTemplate
 * @see RowCallbackHandler
 * @see RowMapperResultReader
 * @see org.springframework.jdbc.core.support.AbstractLobStreamingResultSetExtractor
 */
public interface ResultSetExtractor {
	
	/** 
	 * Implementations must implement this method to process
	 * all rows in the ResultSet.
	 * @param rs ResultSet to extract data from. Implementations should
	 * not close this: it will be closed by the JdbcTemplate.
	 * @return an arbitrary result object, or null if none
	 * (the extractor will typically be stateful in the latter case).
	 * @throws SQLException if a SQLException is encountered getting column
	 * values or navigating (that is, there's no need to catch SQLException)
	 * @throws DataAccessException in case of custom exceptions
	 */
	Object extractData(ResultSet rs) throws SQLException, DataAccessException;

}
