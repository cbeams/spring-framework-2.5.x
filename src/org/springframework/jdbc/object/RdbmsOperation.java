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

import java.sql.ResultSet;
import java.sql.Types;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;

/**
 * An "RDBMS operation" is a multithreaded, reusable object representing a
 * query, update or stored procedure. An RDBMS operation is <b>not</b> a command,
 * as a command isn't reusable. However, execute methods may take commands as
 * arguments. Subclasses should be Java beans, allowing easy configuration.
 *
 * <p>Root of the JDBC object hierarchy, as described in Chapter 9 of
 * <a href="http://www.amazon.com/exec/obidos/tg/detail/-/0764543857/">
 * Expert One-On-One J2EE Design and Development</a> by Rod Johnson (Wrox, 2002).
 *
 * <p>This class and subclasses throw runtime exceptions, defined in the
 * org.springframework.dao package (and as thrown by the org.springframework.jdbc.core
 * package, which the classes in this package use to perform raw JDBC actions).
 *
 * <p>Subclasses should set SQL and add parameters before invoking the
 * compile() method. The order in which parameters are added is significant.
 * The appropriate execute or update method can then be invoked.
 *
 * @author Rod Johnson
 * @see #compile
 * @see org.springframework.dao
 * @see org.springframework.jdbc.core
 */
public abstract class RdbmsOperation implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	/** Lower-level class used to execute SQL */
	private JdbcTemplate jdbcTemplate = new JdbcTemplate();

	private int resultSetType = ResultSet.TYPE_FORWARD_ONLY;

	private boolean updatableResults = false;

	private boolean returnGeneratedKeys = false;
	
	private String[] generatedKeysColumnNames = null;

	/** SQL statement */
	private String sql;

	/** List of SqlParameter objects */
	private List declaredParameters = new LinkedList();

	/**
	 * Has this operation been compiled? Compilation means at
	 * least checking that a DataSource and sql have been provided,
	 * but subclasses may also implement their own custom validation.
	 */
	private boolean compiled;
	
	
	/**
	 * Set the JDBC DataSource to obtain connections from.
	 */
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate.setDataSource(dataSource);
	}

	/**
	 * An alternative to the more commonly used setDataSource() when you want to
	 * use the same JdbcTemplate in multiple RdbmsOperations. This is appropriate if the
	 * JdbcTemplate has special configuration such as a SQLExceptionTranslator that should
	 * apply to multiple RdbmsOperation objects.
	 * @param jdbcTemplate
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Return the JdbcTemplate object used by this object.
	 */
	protected JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	/**
	 * Set whether to use statements that return a specific type of ResultSet.
	 * @param resultSetType the ResultSet type
	 * @see java.sql.ResultSet#TYPE_FORWARD_ONLY
	 * @see java.sql.ResultSet#TYPE_SCROLL_INSENSITIVE
	 * @see java.sql.ResultSet#TYPE_SCROLL_SENSITIVE
	 */
	protected void setResultSetType(int resultSetType) {
		this.resultSetType = resultSetType;
	}

	/**
	 * Return whether statements will return a specific type of ResultSet.
	 */
	protected int getResultSetType() {
		return resultSetType;
	}

	/**
	 * Set whether to use statements that are capable of returning
	 * updatable ResultSets.
	 */
	protected void setUpdatableResults(boolean updatableResults) {
		if (isCompiled()) {
			throw new InvalidDataAccessApiUsageException("The updateableResults flag must be set before the operation is compiled");
		}
		this.updatableResults = updatableResults;
	}

	/**
	 * Return whether statements will return updatable ResultSets.
	 */
	protected boolean isUpdatableResults() {
		return updatableResults;
	}

	/**
	 * Set whether prepared statements should be capable of returning
	 * auto-generated keys.
	 */
	public void setReturnGeneratedKeys(boolean returnGeneratedKeys) {
		if (isCompiled()) {
			throw new InvalidDataAccessApiUsageException("The returnGeneratedKeys flag must be set " +
																									 "before the operation is compiled");
		}
		this.returnGeneratedKeys = returnGeneratedKeys;
	}

	/**
	 * Return whether statements should be capable of returning
	 * auto-generated keys.
	 */
	protected boolean isReturnGeneratedKeys() {
		return returnGeneratedKeys;
	}

	/**
	 * Set the column names of the auto-generated keys.
	 */
	public void setGeneratedKeysColumnNames(String[] names) {
		if (isCompiled()) {
			throw new InvalidDataAccessApiUsageException("The column names for the generated keys must be set " +
																									 "before the operation is compiled");
		}
		this.generatedKeysColumnNames = names;
	}

	/**
	 * Return the column names of the auto generated keys.
	 */
	public String[] getGeneratedKeysColumnNames() {
		return generatedKeysColumnNames;
	}

	/**
	 * Set the SQL executed by this operation.
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * Subclasses can override this to supply dynamic SQL if they wish,
	 * but SQL is normally set by calling the setSql() method
	 * or in a subclass constructor.
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * Add anonymous parameters, specifying only their SQL types
	 * as defined in the java.sql.Types class.
	 * <p>Parameter ordering is significant. This method is an alternative
	 * to the declareParameter() method, which should normally be preferred.
	 * @param types array of SQL types as defined in the
	 * java.sql.Types class
	 * @throws InvalidDataAccessApiUsageException if the operation is already compiled
	 */
	public void setTypes(int[] types) throws InvalidDataAccessApiUsageException {
		if (isCompiled()) {
			throw new InvalidDataAccessApiUsageException("Cannot add parameters once query is compiled");
		}
		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				declareParameter(new SqlParameter(types[i]));
			}
		}
	}

	/**
	 * Declare a parameter. The order in which this method is called is significant.
	 * @param param SqlParameter to add. This will specify SQL type and (optionally)
	 * the parameter's name.
	 * @throws InvalidDataAccessApiUsageException if the operation is already compiled,
	 * and hence cannot be configured further
	 */
	public void declareParameter(SqlParameter param) throws InvalidDataAccessApiUsageException {
		if (isCompiled()) {
			throw new InvalidDataAccessApiUsageException("Cannot add parameters once query is compiled");
		}
		this.declaredParameters.add(param);
	}

	/**
	 * Return a list of the declared SqlParameter objects.
	 */
	protected List getDeclaredParameters() {
		return declaredParameters;
	}


	/**
	 * Ensures compilation if used in a bean factory.
	 */
	public void afterPropertiesSet() {
		compile();
	}

	/**
	 * Compile this query.
	 * Ignore subsequent attempts to compile
	 * @throws InvalidDataAccessApiUsageException if the object hasn't
	 * been correctly initialized, for example if no DataSource has been provided.
	 */
	public final void compile() throws InvalidDataAccessApiUsageException {
		if (!isCompiled()) {
			if (getSql() == null) {
				throw new InvalidDataAccessApiUsageException("sql must be set in class " + getClass().getName());
			}

			try {
				this.jdbcTemplate.afterPropertiesSet();
			}
			catch (IllegalArgumentException ex) {
				throw new InvalidDataAccessApiUsageException(ex.getMessage());
			}	
		
			compileInternal();
			this.compiled = true;
			logger.info("RdbmsOperation with SQL [" + getSql() + "] compiled");
		}
	}

	/**
	 * Subclasses must implement to perform their own compilation.
	 * Invoked after this class's compilation is complete.
	 * Subclasses can assume that sql has been supplied and that
	 * a DataSource has been supplied.
	 * @throws InvalidDataAccessApiUsageException if the subclass
	 * hasn't been properly configured.
	 */
	protected abstract void compileInternal() throws InvalidDataAccessApiUsageException;

	/**
	 * Is this operation "compiled"? Compilation, as in JDO,
	 * means that the operation is fully configured, and ready to use.
	 * The exact meaning of compilation will vary between subclasses.
	 * @return whether this operation is compiled, and ready to use.
	 */
	public boolean isCompiled() {
		return compiled;
	}

	/**
	 * Validate the parameters passed to an execute method based on declared parameters.
	 * Subclasses should invoke this method before every executeQuery() or update() method.
	 * @param parameters parameters supplied. May be null.
	 * @throws InvalidDataAccessApiUsageException if the parameters are invalid
	 */
	protected void validateParameters(Object[] parameters) throws InvalidDataAccessApiUsageException {
		if (!this.compiled) {
			logger.info("SQL operation not compiled before execution - invoking compile");
			compile();
		}

		int declaredInParameters = 0;
		if (this.declaredParameters != null) {
			Iterator iter = this.declaredParameters.iterator();
			while (iter.hasNext()) {
				Object param = iter.next();
				if (!(param instanceof SqlOutParameter) && !(param instanceof SqlReturnResultSet)) {
					if (!supportsLobParameters() &&
							(((SqlParameter)param).getSqlType() == Types.BLOB ||
							((SqlParameter)param).getSqlType() == Types.CLOB)) {
						throw new InvalidDataAccessApiUsageException("BLOB or CLOB parameters are not allowed " +
																												 "for this kind of operation.");
					}
					declaredInParameters++;
				}
			}
		}

		if (parameters != null) {
			if (this.declaredParameters == null) {
				throw new InvalidDataAccessApiUsageException("Didn't expect any parameters: none was declared");
			}
			if (parameters.length < declaredInParameters) {
				throw new InvalidDataAccessApiUsageException(parameters.length + " parameters were supplied, but " +
																										 declaredInParameters + " in parameters were declared in class [" +
																										 getClass().getName() + "]");
			}
			if (parameters.length > this.declaredParameters.size()) {
				throw new InvalidDataAccessApiUsageException(parameters.length + " parameters were supplied, but " +
																										 this.declaredParameters.size() + " parameters were declared " +
				                                             "in class [" + getClass().getName() + "]");
			}
		}
		else {
			// no parameters were supplied
			if (!this.declaredParameters.isEmpty()) {
				throw new InvalidDataAccessApiUsageException(this.declaredParameters.size() + " parameters must be supplied");
			}
		}
	}

	/**
	 * Return whether BLOB or CLOB parameters are supported
	 * for this kind of operation. Default is true.
	 */
	protected boolean supportsLobParameters() {
		return true;
	}

}
