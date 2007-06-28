package org.springframework.jdbc.core.simple;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;

import java.util.Map;
import java.util.List;

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
