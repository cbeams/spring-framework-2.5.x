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

import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.Map;

/**
 * @author trisberg
 */
public interface SimpleJdbcCallOperations {

	SimpleJdbcCall withProcedureName(String procedureName);

	SimpleJdbcCall withSchemaName(String schemaName);

	SimpleJdbcCall withCatalogName(String catalogName);

	SimpleJdbcCall withFunctionName(String functionName);

	SimpleJdbcCall declareParameters(SqlParameter... sqlParameters);

	SimpleJdbcCall useInParameterNames(String... inParameterNames);

	SimpleJdbcCall useOutParameterNames(String... inParameterNames);

	SimpleJdbcCall withoutProcedureColumnMetaDataAccess();

	<T> T executeFunction(Class<T> returnType, Map args);

	<T> T executeFunction(Class<T> returnType, MapSqlParameterSource args);

	<T> T executeObject(Class<T> returnType, Map args);

	<T> T executeObject(Class<T> returnType, MapSqlParameterSource args);

	Map<String, Object> execute();

	Map<String, Object> execute(Map args);

	Map<String, Object> execute(MapSqlParameterSource args);
}