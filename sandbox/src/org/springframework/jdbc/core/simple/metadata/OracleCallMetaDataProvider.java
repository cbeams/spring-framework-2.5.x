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

import org.springframework.jdbc.core.simple.metadata.AbstractCallMetaDataProvider;
import org.springframework.jdbc.core.simple.CallMetaDataContext;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.ColumnMapRowMapper;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author trisberg
 */
public class OracleCallMetaDataProvider extends AbstractCallMetaDataProvider {

	private static final String REF_CURSOR_NAME = "REF CURSOR";

	public OracleCallMetaDataProvider(DatabaseMetaData databaseMetaData) throws SQLException {
		super(databaseMetaData);
	}

	@Override
	protected String metaDataCatalogNameToUse(CallMetaDataContext context) {
		// Oracle uses catalog name for package name or an empty string if no package
		return context.getCatalogName() == null ? "" : super.metaDataCatalogNameToUse(context);
	}

	@Override
	protected String metaDataSchemaNameToUse(CallMetaDataContext context) {
		// Use current user schema if no schema specified
		return context.getSchemaName() == null ? getUserName() : super.metaDataSchemaNameToUse(context);
	}

	@Override
	protected SqlParameter createDefaultOutParameter(String parameterName, CallParameterMetaData meta) {
		if(meta.getSqlType() == Types.OTHER && REF_CURSOR_NAME.equals(meta.getTypeName()))
			return new SqlOutParameter(parameterName, -10, new ColumnMapRowMapper());
		else
			return super.createDefaultOutParameter(parameterName, meta);
	}
}
