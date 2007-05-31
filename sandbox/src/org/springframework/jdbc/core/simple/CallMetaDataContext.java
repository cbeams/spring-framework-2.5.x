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
import org.springframework.jdbc.core.simple.metadata.CallMetaDataProvider;
import org.springframework.jdbc.core.simple.metadata.CallMetaDataProviderFactory;

import javax.sql.DataSource;
import java.util.*;

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

	/** List of SqlParameter names for out parameters */
	private List<String> outParameterNames = new ArrayList<String>();

	private boolean accessProcedureColumnMetaData = true;

	/** indicates whether this is a procedure or a function **/
	private boolean function;

	private CallMetaDataProvider metaDataProvider;


	public String getFunctionReturnName() {
		return functionReturnName;
	}

	public void setFunctionReturnName(String functionReturnName) {
		this.functionReturnName = functionReturnName;
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

	public boolean isAccessProcedureColumnMetaData() {
		return accessProcedureColumnMetaData;
	}

	public void setAccessProcedureColumnMetaData(boolean accessProcedureColumnMetaData) {
		this.accessProcedureColumnMetaData = accessProcedureColumnMetaData;
	}

	public String getScalarOutParameterName() {
		if (isFunction()) {
			return functionReturnName;
		}
		else {
			if (outParameterNames.size() > 1) {
				logger.warn("Accessing single output value when procedure has more than one output parameter");
			}
			return outParameterNames.get(0);
		}
	}

	public List<SqlParameter> getCallParameters() {
		return callParameters;
	}

	public void processMetaData(DataSource dataSource, List<SqlParameter> parameters) {

		metaDataProvider =
				CallMetaDataProviderFactory.createMetaDataProcessor(dataSource, this);

		callParameters = metaDataProvider.reconcileParameters(parameters, this);

	}

	public Map<String, Object> matchInParameterValuesWithCallParameters(Map<String, Object> inParameters) {
		return metaDataProvider.matchInParameterValuesWithCallParameters(inParameters, callParameters);
	}


	public String createCallString() {
		return metaDataProvider.createCallString(callParameters, this);
	}

}
