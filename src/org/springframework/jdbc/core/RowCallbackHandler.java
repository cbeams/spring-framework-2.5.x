/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/** 
 * One of the two central callback interfaces used by the JdbcTemplate class.
 * Implementations of this interface perform the actual work of extracting
 * results, but don't need to worry about exception handling. SQLExceptions
 * will be caught and handled correctly by the JdbcTemplate class.
 * @author Rod Johnson
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
