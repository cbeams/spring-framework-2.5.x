
/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/** 
 * Callback interfaces used by the JdbcTemplate class's doWithResultSetXXXX() methods.
 * Implementations of this interface perform the actual work of extracting results,
 * but don't need to worry about exception handling. SQLExceptions
 * will be caught and handled correctly by the JdbcTemplate class.
 *
 * <p>The RowCallbackHandler is usually a simpler choice for passing to callback methods.
 *
 * @author Rod Johnson
 * @version $Id: ResultSetExtractor.java,v 1.1 2003-08-22 08:18:33 jhoeller Exp $
 * @since April 24, 2003
 * @see org.springframework.jdbc.core.RowCallbackHandler
 */
public interface ResultSetExtractor {
	
	/** 
	* Implementations must implement this method to process
	* all rows in the ResultSet.
	* @param rs ResultSet to extract data from.
	* Implementations should not close this: it will be closed
	* by the JdbcTemplate.
	* @throws SQLException if a SQLException is encountered getting
	* column values or navigating (that is, there's no need to catch SQLException)
	*/
	void extractData(ResultSet rs) throws SQLException; 

}
