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
 * @version $Id: PreparedStatementCreatorFactory.java,v 1.7 2004-03-08 16:56:51 jhoeller Exp $
 */
public class PreparedStatementCreatorFactory {

	/** The SQL, which won't change when the parameters change. */
	private String sql;

	/** List of SqlParameter objects. May not be null. */
	private final List declaredParameters;

	private int resultSetType = ResultSet.TYPE_FORWARD_ONLY;

	/**
	 * Boolean to indicate whether the PreparedStatement created is capable
	 * of returning updatable ResultSets.
	 */
	private boolean updatableResults = false;
	
	/**
	 * Create a new factory. Will need to add parameters
	 * via the addParameter() method or have no parameters.
	 */
	public PreparedStatementCreatorFactory(String sql) {
		this.sql = sql;
		this.declaredParameters = new LinkedList();
	}

	/**
	 * Create a new factory with the given SQL and JDBC types.
	 * @param sql SQL to execute
	 * @param types int array of JDBC types
	 */
	public PreparedStatementCreatorFactory(String sql, int[] types) {
		this(sql, SqlParameter.sqlTypesToAnonymousParameterList(types));
	}

	/**
	 * Create a new factory with the given SQL and parameters.
	 * @param sql SQL
	 * @param declaredParameters list of SqlParameter objects
	 * @see SqlParameter
	 */
	public PreparedStatementCreatorFactory(String sql, List declaredParameters) {
		this.sql = sql;
		this.declaredParameters = declaredParameters;
	}

	/**
	 * Add a new declared parameter.
	 * Order of parameter addition is significant.
	 */
	public void addParameter(SqlParameter param) {
		declaredParameters.add(param);
	}

	/**
	 * Set whether to use prepared statements that return a
	 * specific type of ResultSet.
	 * @param resultSetType the ResultSet type
	 * @see java.sql.ResultSet#TYPE_FORWARD_ONLY
	 * @see java.sql.ResultSet#TYPE_SCROLL_INSENSITIVE
	 * @see java.sql.ResultSet#TYPE_SCROLL_SENSITIVE
	 */
	public void setResultSetType(int resultSetType) {
		this.resultSetType = resultSetType;
	}

	/**
	 * Set whether to use prepared statements capable of returning
	 * updatable ResultSets.
	 */
	public void setUpdatableResults(boolean updatableResults) {
		this.updatableResults = updatableResults;
	}

	/**
	 * Return a new PreparedStatementCreator given these parameters.
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
		 * Create a new PreparedStatementCreatorImpl.
		 * @param params list of SqlParameter objects. May not be null.
		 */
		private PreparedStatementCreatorImpl(List params) {
			this.parameters = params;
			if (this.parameters.size() != declaredParameters.size())
				throw new InvalidDataAccessApiUsageException("SQL='" + sql + "': given " + this. parameters.size() +
				                                             " parameter but expected " + declaredParameters.size());
		}
		
		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
			PreparedStatement ps = null;
			if (resultSetType == ResultSet.TYPE_FORWARD_ONLY && !updatableResults) {
				ps = con.prepareStatement(sql);
			}
			else {
				ps = con.prepareStatement(sql, resultSetType,
				                     updatableResults ? ResultSet.CONCUR_UPDATABLE : ResultSet.CONCUR_READ_ONLY);
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
