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
 * An interfaces used by the StoredProcedure class for mapping returned result sets.
 * Implementations of this interface perform the actual work of mapping
 * rows, but don't need to worry about exception handling. SQLExceptions
 * will be caught and handled correctly by the JdbcTemplate class.
 * @author Thomas Risberg
 */
public interface RowMapper {
	
	/** 
	 * Implementations must implement this method to map each row of data
	 * in the ResultSet. This method should not call next() on the ResultSet,
	 * but extract the current values. 
	 * @param rs the ResultSet to map
	 * @param rowNum The number of the current row
	 * @throws SQLException if a SQLException is encountered getting
	 * column values (that is, there's no need to catch SQLException)
	 */
	Object mapRow(ResultSet rs, int rowNum) throws SQLException; 

}
 