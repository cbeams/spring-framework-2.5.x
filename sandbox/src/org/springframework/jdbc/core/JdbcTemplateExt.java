/*
 * Temporary sandbox extension of JdbcTemplate
 */
package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.sql.RowSet;
import javax.sql.rowset.CachedRowSet;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.support.SqlRowSet;
import org.springframework.jdbc.core.support.SqlRowSetImpl;

import com.sun.rowset.CachedRowSetImpl;

/**
 * @author trisberg
 */
public class JdbcTemplateExt extends JdbcTemplate {

	/**
	 * 
	 */
	public JdbcTemplateExt() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param dataSource
	 */
	public JdbcTemplateExt(DataSource dataSource) {
		super(dataSource);
		// TODO Auto-generated constructor stub
	}

	public SqlRowSet queryForRowSet(String sql) throws DataAccessException {
		SqlRowSet sqlRowSet = new SqlRowSetImpl((RowSet)query(sql, new RowSetResultSetExtractor()));
		sqlRowSet.setCommand(sql);
		return sqlRowSet;
	}

	public SqlRowSet queryForRowSet(String sql, final Object[] args) throws DataAccessException {
		SqlRowSet sqlRowSet = new SqlRowSetImpl((RowSet) query(sql,
				new ArgPreparedStatementSetter(args),
				new RowSetResultSetExtractor()));
		sqlRowSet.setCommand(sql);
		return sqlRowSet;
	}

	
	/**
	 * Simple adapter for PreparedStatementSetter that applies
	 * a given array of arguments.
	 */
	private static class ArgPreparedStatementSetter implements PreparedStatementSetter {

		private final Object[] args;

		public ArgPreparedStatementSetter(Object[] args) {
			this.args = args;
		}

		public void setValues(PreparedStatement ps) throws SQLException {
			if (this.args != null) {
				for (int i = 0; i < this.args.length; i++) {
					ps.setObject(i + 1, this.args[i]);
				}
			}
		}
	}

	/**
	 * ResultSetExtractor implementation that returns CachedRowSet.
	 */
	private static class RowSetResultSetExtractor implements ResultSetExtractor {

		public Object extractData(ResultSet rs) throws SQLException {			
			CachedRowSet rowSet = new CachedRowSetImpl();
			rowSet.populate(rs);
			return rowSet;
		}
	}
}
