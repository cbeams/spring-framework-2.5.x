package org.springframework.orm.ibatis;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.ibatis.db.sqlmap.MappedStatement;
import com.ibatis.db.sqlmap.SqlMap;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcAccessor;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Helper class that simplifies data access via the MappedStatement API of the iBATIS
 * Database Layer, and converts checked SQLExceptions into unchecked DataAccessExceptions,
 * compatible to the org.springframework.dao exception hierarchy.
 * Uses the same SQLExceptionTranslator mechanism as JdbcTemplate.
 *
 * <p>The main method is executeInMappedStatement, taking the name of a mapped statement
 * defined in the iBATIS SqlMap config file and a callback implementation that
 * represents a data access action on the specified statement.
 *
 * @author Juergen Hoeller
 * @since 28.11.2003
 */
public class MappedStatementTemplate extends JdbcAccessor {

	private SqlMap sqlMap;

	/**
	 * Set the iBATIS Database Layer SqlMap to work with.
	 * @param sqlMap the SqlMap instance to work with
	 */
	public void setSqlMap(SqlMap sqlMap) {
		this.sqlMap = sqlMap;
	}

	/**
	 * Execute the given data access action on the given mapped statement.
	 * @param statementName name of the statement mapped in the iBATIS SqlMap config file
	 * @param action callback object that specifies the data access action
	 * @throws DataAccessException in case of Hibernate errors
	 */
	public Object execute(String statementName, MappedStatementCallback action) throws DataAccessException {
		MappedStatement stmt = this.sqlMap.getMappedStatement(statementName);
		Connection con = DataSourceUtils.getConnection(this.dataSource);
		try {
			return action.doInMappedStatement(stmt, con);
		}
		catch (SQLException ex) {
			throw getExceptionTranslator().translate("MappedStatementTemplate", null, ex);
		}
		finally {
			DataSourceUtils.closeConnectionIfNecessary(con, this.dataSource);
		}
	}

	/**
	 * Execute the given data access action on the given mapped statement,
	 * expecting a List result.
	 * @param statementName name of the statement mapped in the iBATIS SqlMap config file
	 * @param action callback object that specifies the data access action
	 * @throws DataAccessException in case of Hibernate errors
	 */
	public List executeWithListResult(String statementName, MappedStatementCallback action)
	    throws DataAccessException {
		return (List) execute(statementName, action);
	}

	/**
	 * Execute the given data access action on the given mapped statement,
	 * expecting a Map result.
	 * @param statementName name of the statement mapped in the iBATIS SqlMap config file
	 * @param action callback object that specifies the data access action
	 * @throws DataAccessException in case of Hibernate errors
	 */
	public Map executeWithMapResult(String statementName, MappedStatementCallback action)
	    throws DataAccessException {
		return (Map) execute(statementName, action);
	}

}
