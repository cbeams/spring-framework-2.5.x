package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * Helper class that can efficiently create multiple PreparedStatementCreator
 * objects with different parameters based on a SQL statement and a single
 * set of parameter declarations.
 * @author Rod Johnson
 * @version $Id: PreparedStatementCreatorFactory.java,v 1.6 2004-02-17 17:21:25 jhoeller Exp $
 */
public class PreparedStatementCreatorFactory {

	/** The Sql, which won't change when the parameters change. */
	private String sql;

	/** List of SqlParameter objects. May not be null. */
	private List declaredParameters = new LinkedList();

	/**
	 * Boolean to indicate whether the prepared statement created is capable
	 * of returning updatable resultsets.
	 */
	private boolean updatableResults = false;
	
	/**
	 * Create a new factory. Will need to add parameters
	 * via the addParameter() method or have no parameters
	 */
	public PreparedStatementCreatorFactory(String sql) {
		this.sql = sql;
	}

	/**
	 * Create a new factory with sql and parameters with the given JDBC types
	 * @param sql SQL to execute
	 * @param types int array of JDBC types
	 */
	public PreparedStatementCreatorFactory(String sql, int[] types) {
		this(sql, SqlParameter.sqlTypesToAnonymousParameterList(types));
	}

	/**
	 * Create a new factory with sql and the given parameters
	 * @param sql SQL
	 * @param declaredParameters list of SqlParameter objects
	 */
	public PreparedStatementCreatorFactory(String sql, List declaredParameters) {
		this.sql = sql;
		this.declaredParameters = declaredParameters;
	}

	/**
	 * Create a new factory with sql and parameters with the given JDBC types
	 * @param sql SQL to execute
	 * @param types int array of JDBC types
	 * @param updatableResults boolean to indicate that the prepared statement should return
	 *  updatable result sets
	 */
	public PreparedStatementCreatorFactory(String sql, int[] types, boolean updatableResults) {
		this(sql, SqlParameter.sqlTypesToAnonymousParameterList(types), updatableResults);
	}

	/**
	 * Create a new factory with sql and the given parameters and set the 
	 * updatableResults flag
	 * @param sql SQL
	 * @param declaredParameters list of SqlParameter objects
	 * @param updatableResults boolean to indicate that the prepared statement should return
	 *  updatable result sets
	 */
	public PreparedStatementCreatorFactory(String sql, List declaredParameters, boolean updatableResults) {
		this.sql = sql;
		this.declaredParameters = declaredParameters;
		this.updatableResults = updatableResults;
	}

	/**
	 * Add a new declared parameter
	 * Order of parameter addition is significant
	 */
	public void addParameter(SqlParameter p) {
		declaredParameters.add(p);
	}
	
	/**
	 * Return a new PreparedStatementCreator given these parameters
	 * @param params parameter array. May be null.
	 */
	public PreparedStatementCreator newPreparedStatementCreator(Object[] params) {
		return new PreparedStatementCreatorImpl((params != null) ? Arrays.asList(params) : Collections.EMPTY_LIST);
	}
	
	/**
	 * Return a new PreparedStatementCreator instance given this parameters.
	 * @param params List of parameters. May be null.
	 */
	public PreparedStatementCreator newPreparedStatementCreator(List params) {
		return new PreparedStatementCreatorImpl(params != null ? params : Collections.EMPTY_LIST);
	}


	/**
	 * PreparedStatementCreator implementation returned by this class.
	 */
	private class PreparedStatementCreatorImpl implements PreparedStatementCreator {

		private List parameters;
		
		/**
		 * @param params list of SqlParameter objects. May not be null
		 */
		private PreparedStatementCreatorImpl(List params) {
			this.parameters = params;
			if (this.parameters.size() != declaredParameters.size())
				throw new InvalidDataAccessApiUsageException("SQL='" + sql + "': given " + this. parameters.size() +
				                                             " parameter but expected " + declaredParameters.size());
		}
		
		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
			PreparedStatement ps = null;
			if (updatableResults) {
				ps = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			}
			else {
				ps = con.prepareStatement(sql);
			}

			// Set arguments: does nothing if there are no parameters
			for (int i = 0; i < this.parameters.size(); i++) {
				SqlParameter declaredParameter = (SqlParameter) declaredParameters.get(i);
				// We need SQL type to be able to set null
				if (this.parameters.get(i) == null) {
					ps.setNull(i + 1, declaredParameter.getSqlType());
				}
				else {
					// Documentation?
					
					// PARAMETERIZE THIS TO A TYPE MAP INTERFACE?
					switch (declaredParameter.getSqlType()) {
						case Types.VARCHAR : 
							ps.setString(i + 1, (String) this.parameters.get(i));
							break;
						//case Types. : 
						//	ps.setString(i + 1, (String) parameters.get(i));
						//	break;
						default : 
							ps.setObject(i + 1, this.parameters.get(i), declaredParameter.getSqlType());
							break;
					}
				}
			}
			return ps;
		}

		public String toString() {
			StringBuffer buf = new StringBuffer("PreparedStatementCreatorFactory.PreparedStatementCreatorImpl: sql=[");
			buf.append(sql);
			buf.append("]: params=[");
			for (int i = 0; i < this.parameters.size(); i++) {
				if (i > 0) {
					buf.append(',');
				}
				buf.append(this.parameters.get(i));
			}
			return buf.toString() + "]";
		}
	}

}
