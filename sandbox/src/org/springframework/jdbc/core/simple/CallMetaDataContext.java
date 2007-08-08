/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.jdbc.core.simple;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.metadata.CallMetaDataProvider;
import org.springframework.jdbc.core.simple.metadata.CallMetaDataProviderFactory;
import org.springframework.jdbc.core.simple.metadata.CallParameterMetaData;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import javax.sql.DataSource;
import java.util.*;
import java.sql.DatabaseMetaData;

/**
 * Class to hold context data for one of the MetaData strategy implementations of DatabaseMetaDataProvider.
 *
 * @author trisberg
 */
public class CallMetaDataContext {

	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** name of procedure to call **/
	private String procedureName;

	/** name of catalog for call **/
	private String catalogName;

	/** name of schema for call **/
	private String schemaName;

	/** List of SqlParameter objects to be used in call execution */
	private List<SqlParameter> callParameters = new ArrayList<SqlParameter>();

	/** name to use for the return value in the output map */
	private String functionReturnName = "return";

	/** Set of in parameter names to exclude use for any not listed */
	private HashSet<String> limitedInParameterNames = new HashSet<String>();

	/** List of SqlParameter names for out parameters */
	private List<String> outParameterNames = new ArrayList<String>();

	/** should we access call parameter meta data info or not */
	private boolean accessCallParameterMetaData = true;

	/** indicates whether this is a procedure or a function **/
	private boolean function;

	/** indicates whether this procedure's return value should be included  **/
	private boolean returnValueRequired;

	/** the provider of call meta data */
	private CallMetaDataProvider metaDataProvider;


	public String getFunctionReturnName() {
		return functionReturnName;
	}

	public void setFunctionReturnName(String functionReturnName) {
		this.functionReturnName = functionReturnName;
	}

	public void setLimitedInParameterNames(HashSet<String> limitedInParameterNames) {
		this.limitedInParameterNames = limitedInParameterNames;
	}

	public List<String> getOutParameterNames() {
		return outParameterNames;
	}

	public void setOutParameterNames(List<String> outParameterNames) {
		this.outParameterNames = outParameterNames;
	}

	public String getProcedureName() {
		return procedureName;
	}

	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}

	public String getCatalogName() {
		return catalogName;
	}

	public void setCatalogName(String catalogName) {
		this.catalogName = catalogName;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public boolean isFunction() {
		return function;
	}

	public void setFunction(boolean function) {
		this.function = function;
	}

	public boolean isReturnValueRequired() {
		return returnValueRequired;
	}

	public void setReturnValueRequired(boolean returnValueRequired) {
		this.returnValueRequired = returnValueRequired;
	}

	public boolean isAccessCallParameterMetaData() {
		return accessCallParameterMetaData;
	}

	public void setAccessCallParameterMetaData(boolean accessCallParameterMetaData) {
		this.accessCallParameterMetaData = accessCallParameterMetaData;
	}

	public SqlParameter createReturnResultSetParameter(String parameterName, ParameterizedRowMapper rowMapper) {
		if (this.metaDataProvider.isReturnResultSetSupported()) {
			return new SqlReturnResultSet(parameterName, rowMapper);
		}
		else {
			if (this.metaDataProvider.isRefCursorSupported()) {
				return new SqlOutParameter(parameterName, this.metaDataProvider.getRefCursorSqlType(), rowMapper);
			}
			else {
				throw new InvalidDataAccessApiUsageException("Return of a ResultSet from a stored procedure is not supported.");
			}
		}
	}

	public String getScalarOutParameterName() {
		if (isFunction()) {
			return functionReturnName;
		}
		else {
			if (outParameterNames.size() > 1) {
				logger.warn("Accessing single output value when procedure has more than one output parameter");
			}
			if (outParameterNames.size() > 0)
				return outParameterNames.get(0);
			else
				return null;
		}
	}

	public List<SqlParameter> getCallParameters() {
		return callParameters;
	}

	public void initializeMetaData(DataSource dataSource) {

		metaDataProvider =
				CallMetaDataProviderFactory.createMetaDataProvider(dataSource, this);

	}

	public void processParameters(List<SqlParameter> parameters) {

		callParameters = reconcileParameters(parameters);

	}

	private List<SqlParameter> reconcileParameters(List<SqlParameter> parameters) {
		final List<SqlParameter> declaredReturnParameters = new ArrayList<SqlParameter>();

		final Map<String, SqlParameter> declaredParameters = new LinkedHashMap<String, SqlParameter>();

		boolean returnDeclared = false;

		List<String> outParameterNames = new ArrayList<String>();

		// separate implicit return parameters from explicit parameters
		for (SqlParameter parameter : parameters) {
			if (parameter.isResultsParameter()) {
				declaredReturnParameters.add(parameter);
			}
			else {
				String parameterNameToMatch = metaDataProvider.parameterNameToUse(parameter.getName()).toLowerCase();
				declaredParameters.put(parameterNameToMatch, parameter);
				if (parameter instanceof SqlOutParameter) {
					outParameterNames.add(parameter.getName());
					if (this.isFunction()) {
						if (!returnDeclared)
							this.setFunctionReturnName(parameter.getName());
						returnDeclared = true;
					}
				}
			}
		}
		this.setOutParameterNames(outParameterNames);

		final List<SqlParameter> workParameters = new ArrayList<SqlParameter>();
		workParameters.addAll(declaredReturnParameters);

		if (!metaDataProvider.isProcedureColumnMetaDataUsed()) {
			workParameters.addAll(declaredParameters.values());
			return workParameters;
		}

		for (CallParameterMetaData meta : metaDataProvider.getCallParameterMetaData()) {
			String parNameToCheck = null;
			if (meta.getParameterName() != null)
				parNameToCheck = metaDataProvider.parameterNameToUse(meta.getParameterName()).toLowerCase();
			String parNameToUse = metaDataProvider.parameterNameToUse(meta.getParameterName());
			if (declaredParameters.containsKey(parNameToCheck) ||
					(meta.getParameterType() == DatabaseMetaData.procedureColumnReturn && returnDeclared)) {
				SqlParameter parameter;
				if (meta.getParameterType() == DatabaseMetaData.procedureColumnReturn) {
					parameter = declaredParameters.get(this.getFunctionReturnName());
					if (parameter == null && this.getOutParameterNames().size() > 0) {
						parameter = declaredParameters.get(this.getOutParameterNames().get(0).toLowerCase());
					}
					if (parameter == null) {
						throw new InvalidDataAccessApiUsageException(
								"Unable to locate declared parameter for function return value - " +
										" add an SqlOutParameter with name \"" + this.getFunctionReturnName() +"\"");
					}
					else {
						this.setFunctionReturnName(parameter.getName());
					}
				}
				else {
					parameter = declaredParameters.get(parNameToCheck);
				}
				if (parameter != null) {
					workParameters.add(parameter);
					if (logger.isDebugEnabled()) {
						logger.debug("Using declared parameter for: " +
								(parNameToUse == null ? this.getFunctionReturnName() : parNameToUse));
					}
				}
			}
			else {
				if (meta.getParameterType() == DatabaseMetaData.procedureColumnReturn) {
					if (!this.isFunction() &&
							!this.isReturnValueRequired() &&
							metaDataProvider.byPassReturnParameter(meta.getParameterName())) {
						if (logger.isDebugEnabled()) {
							logger.debug("Bypassing metadata return parameter for: " + meta.getParameterName());
						}
					}
					else {
						String returnNameToUse = meta.getParameterName() == null ? this.getFunctionReturnName() : parNameToUse;
						workParameters.add(new SqlOutParameter(returnNameToUse, meta.getSqlType()));
						if (this.isFunction())
							outParameterNames.add(returnNameToUse);
						if (logger.isDebugEnabled()) {
							logger.debug("Added metadata return parameter for: " + returnNameToUse);
						}
					}
				}
				else {
					if (meta.getParameterType() == DatabaseMetaData.procedureColumnOut ||
							meta.getParameterType() == DatabaseMetaData.procedureColumnInOut) {
						workParameters.add(metaDataProvider.createDefaultOutParameter(parNameToUse, meta));
						outParameterNames.add(parNameToUse);
						if (logger.isDebugEnabled()) {
							logger.debug("Added metadata out parameter for: " + parNameToUse);
						}
					}
					else {
						if (limitedInParameterNames.size() == 0 || limitedInParameterNames.contains(parNameToUse)) {
							workParameters.add(metaDataProvider.createDefaultInParameter(parNameToUse, meta));
							if (logger.isDebugEnabled()) {
								logger.debug("Added metadata in parameter for: " + parNameToUse);
							}
						}
						else {
							if (logger.isDebugEnabled()) {
								logger.debug("Limited set of parameters " + limitedInParameterNames +
										" -- skipped parameter for: " + parNameToUse);
							}
						}
					}
				}
			}
		}

		return workParameters;

	}

	public Map<String, Object> matchInParameterValuesWithCallParameters(SqlParameterSource parameterSource) {
		Map<String, Object> matchedParameters = new HashMap<String, Object>(callParameters.size());
		for (SqlParameter parameter : callParameters) {
			if (parameter.getName() != null) {
				Object value = null;
				boolean match = false;
				if (parameterSource.hasValue(parameter.getName().toLowerCase())) {
					value = parameterSource.getValue(parameter.getName().toLowerCase());
					match = true;
				}
				else {
					String propertyName = SimpleJdbcUtils.convertUnderscoreNameToPropertyName(parameter.getName());
					if (parameterSource.hasValue(propertyName)) {
						value = parameterSource.getValue(propertyName);
						match = true;
					}
				}
				if (match) {
					matchedParameters.put(parameter.getName(), value);
				}
			}
		}
		return matchedParameters;
	}

	public Map<String, Object> matchInParameterValuesWithCallParameters(Map<String, Object> inParameters) {
		if (!metaDataProvider.isProcedureColumnMetaDataUsed()) {
			return inParameters;
		}
		Map<String, String> callParameterNames = new HashMap<String, String>(callParameters.size());
		for (SqlParameter parameter : callParameters) {
			String parameterName =  parameter.getName();
			String parameterNameToMatch = metaDataProvider.parameterNameToUse(parameterName);
			if (parameterNameToMatch != null)
				callParameterNames.put(parameterNameToMatch.toLowerCase(), parameterName);
		}
		Map<String, Object> matchedParameters = new HashMap<String, Object>(inParameters.size());
		for (String parameterName : inParameters.keySet()) {
			String parameterNameToMatch = metaDataProvider.parameterNameToUse(parameterName);
			String callParameterName = callParameterNames.get(parameterNameToMatch.toLowerCase());
			if (callParameterName == null) {
				logger.warn("Unable to locate the corresponding parameter for \"" + parameterName +
						"\" specified in the provided parameter values: " + inParameters);
			}
			else {
				matchedParameters.put(callParameterName, inParameters.get(parameterName));
			}

		}
		if (logger.isDebugEnabled()) {
			logger.debug("Matching " + inParameters + " with " + callParameterNames);
		}
		return matchedParameters;
	}


	public String createCallString() {
		String callString;
		int parameterCount = 0;
		String catalogNameToUse = metaDataProvider.catalogNameToUse(this.getCatalogName());
		String schemaNameToUse = metaDataProvider.schemaNameToUse(this.getSchemaName());
		String procedureNameToUse = metaDataProvider.procedureNameToUse(this.getProcedureName());
		if (this.isFunction() || this.isReturnValueRequired()) {
			callString = "{? = call " +
					(catalogNameToUse != null && catalogNameToUse.length() > 0 ? catalogNameToUse + "." : "") +
					(schemaNameToUse != null && schemaNameToUse.length() > 0 ? schemaNameToUse + "." : "") +
					procedureNameToUse + "(";
			parameterCount = -1;
		}
		else {
			callString = "{call " +
					(catalogNameToUse != null && catalogNameToUse.length() > 0 ? catalogNameToUse + "." : "") +
					(schemaNameToUse != null && schemaNameToUse.length() > 0 ? schemaNameToUse + "." : "") +
					procedureNameToUse + "(";
		}
		for (SqlParameter parameter : callParameters) {
			if (!(parameter.isResultsParameter())) {
				if (parameterCount > 0) {
					callString += ", ";
				}
				if (parameterCount >= 0) {
					callString += "?";
				}
				parameterCount++;
			}
		}
		callString += ")}";

		return callString;
	}

}
