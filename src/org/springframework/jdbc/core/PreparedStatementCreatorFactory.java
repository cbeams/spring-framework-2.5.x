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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;

/**
 * Helper class that can efficiently create multiple PreparedStatementCreator
 * objects with different parameters based on a SQL statement and a single
 * set of parameter declarations.
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class PreparedStatementCreatorFactory {

	/** The SQL, which won't change when the parameters change */
	private final String sql;

	/** List of SqlParameter objects. May not be null. */
	private final List declaredParameters;

	private int resultSetType = ResultSet.TYPE_FORWARD_ONLY;

	private boolean updatableResults = false;

	private boolean returnGeneratedKeys = false;
	
	private String[] generatedKeysColumnNames = null;

	private NativeJdbcExtractor nativeJdbcExtractor;


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
		this.sql = sql;
		this.declaredParameters = SqlParameter.sqlTypesToAnonymousParameterList(types);
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
		this.declaredParameters.add(param);
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
	 * @param updatableResults Set to true for an updatable ResultSet.
	 */
	public void setUpdatableResults(boolean updatableResults) {
		this.updatableResults = updatableResults;
	}

	/**
	 * Set whether prepared statements should be capable of returning
	 * auto-generated keys.
	 */
	public void setReturnGeneratedKeys(boolean returnGeneratedKeys) {
		this.returnGeneratedKeys = returnGeneratedKeys;
	}

	/**
	 * Set the column names of the auto-generated keys.
	 */
	public void setGeneratedKeysColumnNames(String[] names) {
		this.generatedKeysColumnNames = names;
	}

	/**
	 * Specify the NativeJdbcExtractor to use for unwrapping
	 * PreparedStatements, if any.
	 */
	public void setNativeJdbcExtractor(NativeJdbcExtractor nativeJdbcExtractor) {
		this.nativeJdbcExtractor = nativeJdbcExtractor;
	}
	

	/**
	 * Return a new PreparedStatementCreator for the given parameters.
	 * @param params parameter array. May be null.
	 */
	public PreparedStatementCreator newPreparedStatementCreator(Object[] params) {
		return new PreparedStatementCreatorImpl((params != null) ? Arrays.asList(params) : Collections.EMPTY_LIST);
	}
	
	/**
	 * Return a new PreparedStatementCreator for the given parameters.
	 * @param params List of parameters. May be null.
	 */
	public PreparedStatementCreator newPreparedStatementCreator(List params) {
		return new PreparedStatementCreatorImpl(params != null ? params : Collections.EMPTY_LIST);
	}

	/**
	 * Return a new PreparedStatementSetter for the given parameters.
	 * @param params parameter array. May be null.
	 */
	public PreparedStatementSetter newPreparedStatementSetter(Object[] params) {
		return new PreparedStatementCreatorImpl((params != null) ? Arrays.asList(params) : Collections.EMPTY_LIST);
	}

	/**
	 * Return a new PreparedStatementSetter for the given parameters.
	 * @param params List of parameters. May be null.
	 */
	public PreparedStatementSetter newPreparedStatementSetter(List params) {
		return new PreparedStatementCreatorImpl(params != null ? params : Collections.EMPTY_LIST);
	}


	/**
	 * PreparedStatementCreator implementation returned by this class.
	 */
	private class PreparedStatementCreatorImpl
			implements PreparedStatementCreator, PreparedStatementSetter, SqlProvider, ParameterDisposer {

		private final List parameters;
		
		/**
		 * Create a new PreparedStatementCreatorImpl.
		 * @param parameters list of parameter objects
		 */
		public PreparedStatementCreatorImpl(List parameters) {
			this.parameters = parameters;
			if (this.parameters.size() != declaredParameters.size())
				throw new InvalidDataAccessApiUsageException("SQL=[" + sql + "]: given " + this.parameters.size() +
				                                             " parameter but expected " + declaredParameters.size());
		}
		
		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
			PreparedStatement ps = null;
			if (returnGeneratedKeys) {
				try {
					if (generatedKeysColumnNames == null) {
						ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
					}
					else {
						ps = con.prepareStatement(sql, generatedKeysColumnNames);
					}
				}
				catch (AbstractMethodError ex) {
					throw new InvalidDataAccessResourceUsageException("The JDBC driver is not compliant to JDBC 3.0 and thus " +
																														"does not support retrieval of auto generated keys", ex);
				}
			}
			else if (resultSetType == ResultSet.TYPE_FORWARD_ONLY && !updatableResults) {
				ps = con.prepareStatement(sql);
			}
			else {
				ps = con.prepareStatement(sql, resultSetType,
					updatableResults ? ResultSet.CONCUR_UPDATABLE : ResultSet.CONCUR_READ_ONLY);
			}

			setValues(ps);
			return ps;
		}

		public void setValues(PreparedStatement ps) throws SQLException {
			// determine PreparedStatement to pass to custom types
			PreparedStatement psToUse = ps;
			if (nativeJdbcExtractor != null) {
				psToUse = nativeJdbcExtractor.getNativePreparedStatement(ps);
			}

			// Set arguments: Does nothing if there are no parameters.
			for (int i = 0; i < this.parameters.size(); i++) {
				SqlParameter declaredParameter = (SqlParameter) declaredParameters.get(i);
				Object in = this.parameters.get(i);
				int sqlColIndx = i + 1;
				StatementCreatorUtils.setParameterValue(psToUse, sqlColIndx, declaredParameter, in);
			}
		}
		
		public String getSql() {
			return sql;
		}

		public void cleanupParameters() {
			StatementCreatorUtils.cleanupParameters(this.parameters);
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
