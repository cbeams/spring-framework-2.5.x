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

package org.springframework.jdbc.object;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.CallableStatementCreatorFactory;
import org.springframework.jdbc.core.ParameterMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

/**
 * RdbmsOperation using a JdbcTemplate and representing a SQL-based
 * call such as a stored procedure or a stored function.
 *
 * <p>Configures a CallableStatementCreatorFactory based on the declared
 * parameters.
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 */
public abstract class SqlCall extends RdbmsOperation {

	/**
	 * Object enabling us to create CallableStatementCreators
	 * efficiently, based on this class's declared parameters.
	 */
	private CallableStatementCreatorFactory callableStatementFactory;

	/**
	 * Flag used to indicate that this call is for a function and to
	 * use the {? = call get_invoice_count(?)} syntax.
	 */
	private boolean function = false;

	/**
	 * Flag used to indicate that the sql for this call should be used exactly as it is
	 * defined.  No need to add the escape syntax and parameter place holders.
	 */
	private boolean sqlReadyForUse = false;

	/**
	 * Call string as defined in java.sql.CallableStatement.
	 * String of form {call add_invoice(?, ?, ?)}
	 * or {? = call get_invoice_count(?)} if isFunction is set to true
	 * Updated after each parameter is added.
	 */
	private String callString;


	/**
	 * Set whether this call is for a function.
	 */
	public void setFunction(boolean function) {
		this.function = function;
	}

	/**
	 * Get whether this call is for a function.
	 */
	public boolean isFunction() {
		return function;
	}

	/**
	 * Set whether the SQL can be used as is.
	 */
	public void setSqlReadyForUse(boolean sqlReadyForUse) {
		this.sqlReadyForUse = sqlReadyForUse;
	}

	/**
	 * Return whether the SQL can be used as is.
	 */
	public boolean isSqlReadyForUse() {
		return sqlReadyForUse;
	}


	/**
	 * Overridden method to configure the CallableStatementCreatorFactory
	 * based on our declared parameters.
	 * @see RdbmsOperation#compileInternal()
	 */
	protected final void compileInternal() {
		if (isSqlReadyForUse()) {
			this.callString = getSql();
		}
		else {
			List parameters = getDeclaredParameters();
			int firstParameter = 0;
			if (isFunction()) {
				this.callString = "{? = call " + getSql() + "(";
				firstParameter = 1;
			}
			else {
				this.callString = "{call " + getSql() + "(";
			}
			for (int i = firstParameter; i < parameters.size(); i++) {
				SqlParameter p = (SqlParameter) parameters.get(i);
				if ((p instanceof SqlReturnResultSet)) {
					firstParameter++;
				}
				else {
					if (i > firstParameter) {
						this.callString += ", ";
					}
					this.callString += "?";
				}
			}
			this.callString += ")}";
		}
		if (logger.isInfoEnabled()) {
			logger.info("Compiled stored procedure. Call string is [" + getCallString() + "]");
		}

		this.callableStatementFactory = new CallableStatementCreatorFactory(getCallString(), getDeclaredParameters());
		this.callableStatementFactory.setResultSetType(getResultSetType());
		this.callableStatementFactory.setUpdatableResults(isUpdatableResults());
		this.callableStatementFactory.setNativeJdbcExtractor(getJdbcTemplate().getNativeJdbcExtractor());

		onCompileInternal();
	}

	/**
	 * Hook method that subclasses may override to react to compilation.
	 * This implementation does nothing.
	 */
	protected void onCompileInternal() {
	}

	/**
	 * Get the call string.
	 */
	public String getCallString() {
		return this.callString;
	}

	/**
	 * Return a CallableStatementCreator to perform an operation
	 * with this parameters.
	 * @param inParams parameters. May be null.
	 */
	protected CallableStatementCreator newCallableStatementCreator(Map inParams) {
		return this.callableStatementFactory.newCallableStatementCreator(inParams);
	}

	/**
	 * Return a CallableStatementCreator to perform an operation
	 * with the parameters returned from this ParameterMapper.
	 * @param inParamMapper parametermapper. May not be null.
	 */
	protected CallableStatementCreator newCallableStatementCreator(ParameterMapper inParamMapper) {
		return this.callableStatementFactory.newCallableStatementCreator(inParamMapper);
	}

}
