/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/** 
 * Implement this interface when parameters need to be customized based
 * on the connection. We might need to do this to make
 * use of proprietary features, available only with a specific
 * Connection type.
 * @author Rod Johnson
 * @author Thomas Risberg
 */

public interface ParameterMapper {

	/**
	 * @param conn JDBC connection. This is useful (and the purpose
	 * of this interface) if we need to do something RDBMS-specific
	 * with a proprietary Connection implementation. This class conceals
	 * such proprietary details. However, it is best to avoid using
	 * such proprietary RDBMS features if possible.
	 * @throws SQLException if a SQLException is encountered setting
	 * parameter values (that is, there's no need to catch SQLException)
	 * @return Map of input parameters, keyed by name
	 */
	Map createMap(Connection conn) throws SQLException;
	
}
