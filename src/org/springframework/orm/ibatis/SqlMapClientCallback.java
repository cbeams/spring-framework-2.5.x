package org.springframework.orm.ibatis;

import java.sql.SQLException;

import com.ibatis.sqlmap.client.SqlMapSession;

/**
 * Callback interface for data access code that works on an iBATIS Database Layer
 * SqlMapSession. To be used with SqlMapClientTemplate's execute method,
 * assumably often as anonymous classes within a method implementation.
 *
 * <p>NOTE: The SqlMapClient/SqlMapSession API is the API of iBATIS SQL Maps 2.
 * With SQL Maps 1.x, the SqlMap/MappedStatement API has to be used.
 *
 * @author Juergen Hoeller
 * @since 24.02.2004
 */
public interface SqlMapClientCallback {

	Object doInSqlMapSession(SqlMapSession session) throws SQLException;

}
