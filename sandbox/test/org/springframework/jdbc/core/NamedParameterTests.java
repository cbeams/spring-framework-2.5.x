/*
 * Copyright 2002-2005 the original author or authors.
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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.easymock.MockControl;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.jdbc.AbstractJdbcTests;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.SQLWarningException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractorAdapter;

/** 
 * Mock object based tests for JdbcTemplate.
 *
 * @author Rod Johnson
 * @author Thomas Risberg
 */
public class NamedParameterTests extends AbstractJdbcTests {

	public void testCannotRunStaticSqlWithBindParameters() throws Exception {
		final String sql = "UPDATE FOO SET NAME='tony' WHERE ID > :id";

		replay();

		NamedParameterJdbcTemplate t = new NamedParameterJdbcTemplate(mockDataSource);
		try {
			t.getJdbcOperations().query(sql, new RowCountCallbackHandler());
			fail("Should have objected to bind variables");
		}
		catch (InvalidDataAccessApiUsageException ex) {
			// OK
		}
	}


}
