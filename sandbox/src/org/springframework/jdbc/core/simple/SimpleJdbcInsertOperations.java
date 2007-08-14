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

import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;

import java.util.Map;

/**
 * @author trisberg
 */
public interface SimpleJdbcInsertOperations {
	
	SimpleJdbcInsert withTableName(String tableName);

	SimpleJdbcInsert withSchemaName(String schemaName);

	SimpleJdbcInsert withCatalogName(String catalogName);

	SimpleJdbcInsert usingColumns(String... columnNames);

	int execute(Map<String, Object> args);

	int execute(SqlParameterSource parameterSource);

	Number executeAndReturnKey(Map<String, Object> args);

	Number executeAndReturnKey(SqlParameterSource parameterSource);

	KeyHolder executeAndReturnKeyHolder(Map<String, Object> args);

	KeyHolder executeAndReturnKeyHolder(SqlParameterSource parameterSource);

	int[] executeBatch(Map<String, Object>[] batch);

	int[] executeBatch(SqlParameterSource[] batch);
}
