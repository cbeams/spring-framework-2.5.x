/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
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
 