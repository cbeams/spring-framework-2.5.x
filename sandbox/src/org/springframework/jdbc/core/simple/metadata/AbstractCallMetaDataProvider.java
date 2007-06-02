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

package org.springframework.jdbc.core.simple.metadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.simple.CallMetaDataContext;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.*;

/**
 * @author trisberg
 */
public class AbstractCallMetaDataProvider implements CallMetaDataProvider {

	/** Logger available to subclasses */
	protected static final Log logger = LogFactory.getLog(CallMetaDataProvider.class);

	private boolean procedureColumnMetaDataUsed = false;

	private String userName;

	private boolean supportsCatalogsInProcedureCalls = true;

	private boolean supportsSchemasInProcedureCalls = true;

	private boolean storesUpperCaseIdentifiers = true;

	private boolean storesLowerCaseIdentifiers = false;

	private List<CallParameterMetaData> callParameterMetaData = new ArrayList<CallParameterMetaData>();

	protected AbstractCallMetaDataProvider(DatabaseMetaData databaseMetaData) throws SQLException {
		userName = databaseMetaData.getUserName();
	}

	public void initializeWithMetaData(DatabaseMetaData databaseMetaData) throws SQLException {

		try {
			setSupportsCatalogsInProcedureCalls(databaseMetaData.supportsCatalogsInProcedureCalls());
		}
		catch (SQLException se) {
			logger.warn("Error retrieving 'DatabaseMetaData.supportsCatalogsInProcedureCalls' - " + se.getMessage());
		}
		try {
			setSupportsSchemasInProcedureCalls(databaseMetaData.supportsSchemasInProcedureCalls());
		}
		catch (SQLException se) {
			logger.warn("Error retrieving 'DatabaseMetaData.supportsSchemasInProcedureCalls' - " + se.getMessage());
		}
		try {
			setStoresUpperCaseIdentifiers(databaseMetaData.storesUpperCaseIdentifiers());
		}
		catch (SQLException se) {
			logger.warn("Error retrieving 'DatabaseMetaData.storesUpperCaseIdentifiers' - " + se.getMessage());
		}
		try {
			setStoresLowerCaseIdentifiers(databaseMetaData.storesLowerCaseIdentifiers());
		}
		catch (SQLException se) {
			logger.warn("Error retrieving 'DatabaseMetaData.storesLowerCaseIdentifiers' - " + se.getMessage());
		}

	}

	public void initializeWithProcedureColumnMetaData(DatabaseMetaData databaseMetaData, CallMetaDataContext context)
			throws SQLException {

		procedureColumnMetaDataUsed = true;

		processProcedureColumns(databaseMetaData, context);

	}


	protected String procedureNameToUse(String procedureName) {
		if (procedureName == null)
			return null;
		else if (isStoresUpperCaseIdentifiers())
			return procedureName.toUpperCase();
		else if(isStoresLowerCaseIdentifiers())
			return procedureName.toLowerCase();
		else
			return procedureName;
	}

	protected String catalogNameToUse(String catalogName) {
		if (catalogName == null)
			return null;
		else if (isStoresUpperCaseIdentifiers())
			return catalogName.toUpperCase();
		else if(isStoresLowerCaseIdentifiers())
			return catalogName.toLowerCase();
		else
		return catalogName;
	}

	protected String schemaNameToUse(String schemaName) {
		if (schemaName == null)
			return null;
		else if (isStoresUpperCaseIdentifiers())
			return schemaName.toUpperCase();
		else if(isStoresLowerCaseIdentifiers())
			return schemaName.toLowerCase();
		else
		return schemaName;
	}

	protected String metaDataCatalogNameToUse(CallMetaDataContext context) {
		if (isSupportsCatalogsInProcedureCalls())
			return catalogNameToUse(context.getCatalogName());
		else
			return null;
	}

	protected String metaDataSchemaNameToUse(CallMetaDataContext context) {
		if (isSupportsSchemasInProcedureCalls())
			return schemaNameToUse(context.getSchemaName());
		else
			return null;
	}

	protected String parameterNameToUse(String parameterName) {
		if (parameterName == null)
			return null;
		else if (isStoresUpperCaseIdentifiers())
			return parameterName.toUpperCase();
		else if(isStoresLowerCaseIdentifiers())
			return parameterName.toLowerCase();
		else
		return parameterName;
	}

	protected boolean byPassReturnParameter(String parameterName) {
		return false;
	}

	protected SqlParameter createDefaultOutParameter(String parameterName, CallParameterMetaData meta) {
		return new SqlOutParameter(parameterName, meta.getSqlType());		
	}

	protected SqlParameter createDefaultInParameter(String parameterName, CallParameterMetaData meta) {
		return new SqlParameter(parameterName, meta.getSqlType());		
	}

	protected String getUserName() {
		return userName;
	}

	public List<SqlParameter> reconcileParameters(List<SqlParameter> parameters, CallMetaDataContext context) {

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
				String parameterNameToMatch = parameterNameToUse(parameter.getName()).toLowerCase();
				declaredParameters.put(parameterNameToMatch, parameter);
				if (parameter instanceof SqlOutParameter) {
					outParameterNames.add(parameter.getName());
					if (context.isFunction()) {
						if (!returnDeclared)
							context.setFunctionReturnName(parameter.getName());
						returnDeclared = true;
					}
				}
			}
		}
		context.setOutParameterNames(outParameterNames);

		final List<SqlParameter> workParameters = new ArrayList<SqlParameter>();
		workParameters.addAll(declaredReturnParameters);

		if (!procedureColumnMetaDataUsed) {
			workParameters.addAll(declaredParameters.values());
			return workParameters;
		}

		for (CallParameterMetaData meta : callParameterMetaData) {
			String parNameToCheck = null;
			if (meta.getParameterName() != null)
				parNameToCheck = parameterNameToUse(meta.getParameterName()).toLowerCase();
			String parNameToUse = parameterNameToUse(meta.getParameterName());
			if (declaredParameters.containsKey(parNameToCheck) ||
					(meta.getParameterType() == DatabaseMetaData.procedureColumnReturn && returnDeclared)) {
				SqlParameter parameter;
				if (meta.getParameterType() == DatabaseMetaData.procedureColumnReturn) {
					parameter = declaredParameters.get(context.getFunctionReturnName());
					if (parameter == null && context.getOutParameterNames().size() > 0) {
						parameter = declaredParameters.get(context.getOutParameterNames().get(0).toLowerCase());
					}
					if (parameter == null) {
						throw new InvalidDataAccessApiUsageException(
								"Unable to locate declared parameter for function return value - " +
										" add an SqlOutParameter with name \"" + context.getFunctionReturnName() +"\"");
					}
					else {
						context.setFunctionReturnName(parameter.getName());
					}
				}
				else {
					parameter = declaredParameters.get(parNameToCheck);
				}
				if (parameter != null) {
					workParameters.add(parameter);
					if (logger.isDebugEnabled()) {
						logger.debug("Using declared parameter for: " +
								(parNameToUse == null ? context.getFunctionReturnName() : parNameToUse));
					}
				}
			}
			else {
				if (meta.parameterType == DatabaseMetaData.procedureColumnReturn) {
					if (!context.isFunction() &&
							!context.isReturnValueRequired() &&
							byPassReturnParameter(meta.getParameterName())) {
						if (logger.isDebugEnabled()) {
							logger.debug("Bypassing metadata return parameter for: " + meta.getParameterName());
						}
					}
					else {
						String returnNameToUse = meta.getParameterName() == null ? context.getFunctionReturnName() : parNameToUse;
						workParameters.add(new SqlOutParameter(returnNameToUse, meta.getSqlType()));
						if (context.isFunction())
							outParameterNames.add(returnNameToUse);
						if (logger.isDebugEnabled()) {
							logger.debug("Added metadata return parameter for: " + returnNameToUse);
						}
					}
				}
				else {
					if (meta.getParameterType() == DatabaseMetaData.procedureColumnOut ||
							meta.getParameterType() == DatabaseMetaData.procedureColumnInOut) {
						workParameters.add(createDefaultOutParameter(parNameToUse, meta));
						outParameterNames.add(parNameToUse);
						if (logger.isDebugEnabled()) {
							logger.debug("Added metadata out parameter for: " + parNameToUse);
						}
					}
					else {
						workParameters.add(createDefaultInParameter(parNameToUse, meta));
						if (logger.isDebugEnabled()) {
							logger.debug("Added metadata in parameter for: " + parNameToUse);
						}
					}
				}
			}
		}

		return workParameters;
	}

	private void processProcedureColumns(DatabaseMetaData databaseMetaData, CallMetaDataContext context) {
		ResultSet procs = null;
		String metaDataCatalogName = metaDataCatalogNameToUse(context);
		String metaDataSchemaName = schemaNameToUse(context.getSchemaName());
		String procedureName = procedureNameToUse(context.getProcedureName());
		if (logger.isDebugEnabled()) {
			logger.debug("Retrieving metadata for " + metaDataCatalogName + "/" +
					metaDataSchemaName + "/" + procedureName);
		}
		try {
			procs = databaseMetaData.getProcedureColumns(
					metaDataCatalogName,
					metaDataSchemaName,
					procedureName,
					null);
			while (procs.next()) {
				CallParameterMetaData meta = new CallParameterMetaData(
						procs.getString("COLUMN_NAME"),
						procs.getInt("COLUMN_TYPE"),
						procs.getInt("DATA_TYPE"),
						procs.getString("TYPE_NAME"),
						procs.getBoolean("NULLABLE")
				);
				callParameterMetaData.add(meta);
				if (logger.isDebugEnabled()) {
					logger.debug("Retrieved metadata: "
						+ meta.getParameterName() +
						" " + meta.getParameterType() +
						" " + meta.getSqlType() +
						" " + meta.getTypeName() +
						" " + meta.isNullable()
					);
				}
			}
		}
		catch (SQLException se) {
			logger.warn("Error while retreiving metadata for procedure columns: " + se.getMessage());
		}
		finally {
			try {
				if (procs != null)
					procs.close();
			}
			catch (SQLException se) {
				logger.warn("Problem closing resultset for procedure column metadata " + se.getMessage());
			}
		}
		
	}

	public String createCallString(List<SqlParameter> parameters, CallMetaDataContext context) {
		String callString;
		int parameterCount = 0;
		String catalogNameToUse = catalogNameToUse(context.getCatalogName());
		String schemaNameToUse = schemaNameToUse(context.getSchemaName());
		String procedureNameToUse = procedureNameToUse(context.getProcedureName());
		if (context.isFunction() || context.isReturnValueRequired()) {
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
		for (SqlParameter parameter : parameters) {
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

	public Map<String, Object> matchInParameterValuesWithCallParameters(Map<String, Object> inParameters,
																		List<SqlParameter> callParameters) {
		if (!procedureColumnMetaDataUsed) {
			return inParameters;
		}
		Map<String, String> callParameterNames = new HashMap<String, String>(callParameters.size());
		for (SqlParameter parameter : callParameters) {
//			String parameterName =   parameter.getName() : parameter.getName().toLowerCase();
			String parameterName =  parameter.getName();
			String parameterNameToMatch = parameterNameToUse(parameterName);
			if (parameterNameToMatch != null)
				callParameterNames.put(parameterNameToMatch.toLowerCase(), parameterName);
		}
		Map<String, Object> matchedParameters = new HashMap<String, Object>(inParameters.size());
		for (String parameterName : inParameters.keySet()) {
			String parameterNameToMatch = parameterNameToUse(parameterName);
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

	protected boolean isSupportsCatalogsInProcedureCalls() {
		return supportsCatalogsInProcedureCalls;
	}

	protected void setSupportsCatalogsInProcedureCalls(boolean supportsCatalogsInProcedureCalls) {
		this.supportsCatalogsInProcedureCalls = supportsCatalogsInProcedureCalls;
	}

	protected boolean isSupportsSchemasInProcedureCalls() {
		return supportsSchemasInProcedureCalls;
	}

	protected void setSupportsSchemasInProcedureCalls(boolean supportsSchemasInProcedureCalls) {
		this.supportsSchemasInProcedureCalls = supportsSchemasInProcedureCalls;
	}

	protected boolean isStoresUpperCaseIdentifiers() {
		return storesUpperCaseIdentifiers;
	}

	protected void setStoresUpperCaseIdentifiers(boolean storesUpperCaseIdentifiers) {
		this.storesUpperCaseIdentifiers = storesUpperCaseIdentifiers;
	}

	protected boolean isStoresLowerCaseIdentifiers() {
		return storesLowerCaseIdentifiers;
	}

	protected void setStoresLowerCaseIdentifiers(boolean storesLowerCaseIdentifiers) {
		this.storesLowerCaseIdentifiers = storesLowerCaseIdentifiers;
	}

	protected class CallParameterMetaData {
		private String parameterName;
		private int parameterType;
		private int sqlType;
		private String typeName;
		private boolean nullable;


		public CallParameterMetaData(String columnName, int columnType, int sqlType, String typeName, boolean nullable) {
			this.parameterName = columnName;
			this.parameterType = columnType;
			this.sqlType = sqlType;
			this.typeName = typeName;
			this.nullable = nullable;
		}


		public String getParameterName() {
			return parameterName;
		}

		public int getParameterType() {
			return parameterType;
		}

		public int getSqlType() {
			return sqlType;
		}

		public String getTypeName() {
			return typeName;
		}

		public boolean isNullable() {
			return nullable;
		}
	}
}
