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

package org.springframework.jdbc.core;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

/** 
 * Mock object based tests for JdbcTemplate.
 * @author Rod Johnson
 */
public class JdbcTemplateTestSuite extends AbstractJdbcTests {

	public void testBeanProperties() throws Exception {
		replay();

		JdbcTemplate t = new JdbcTemplate(mockDataSource);
		assertTrue("datasource ok", t.getDataSource() == mockDataSource);
		assertTrue("ignores warnings by default", t.getIgnoreWarnings());
		t.setIgnoreWarnings(false);
		assertTrue("can set NOT to ignore warnings", !t.getIgnoreWarnings());
	}

	public void testCannotRunStaticSqlWithBindParameters() throws Exception {
		final String sql = "UPDATE FOO SET NAME='tony' WHERE ID > ?";

		replay();

		JdbcTemplate t = new JdbcTemplate(mockDataSource);
		try {
			t.query(sql, new RowCountCallbackHandler());
			fail("Should have objected to bind variables");
		}
		catch (InvalidDataAccessApiUsageException ex) {
			// Ok 
		}
	}

	public void testUpdateCount() throws Exception {
		final String sql =
			"UPDATE INVOICE SET DATE_DISPATCHED = SYSDATE WHERE ID = ?";
		int idParam = 11111;

		MockControl ctrlPreparedStatement =
			MockControl.createControl(PreparedStatement.class);
		PreparedStatement mockPreparedStatement =
			(PreparedStatement) ctrlPreparedStatement.getMock();
		mockPreparedStatement.setInt(1, idParam);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeUpdate();
		ctrlPreparedStatement.setReturnValue(1);
		mockPreparedStatement.getWarnings();
		ctrlPreparedStatement.setReturnValue(null);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		ctrlPreparedStatement.replay();
		replay();

		Dispatcher d = new Dispatcher(idParam, sql);
		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		int rowsAffected = template.update(d);
		assertTrue("1 update affected 1 row", rowsAffected == 1);

		/*
		d = new Dispatcher(idParam);
		rowsAffected = template.update(d);
		assertTrue("bogus update affected 0 rows", rowsAffected == 0);
		*/

		ctrlPreparedStatement.verify();
	}

	public void testBogusUpdate() throws Exception {
		final String sql =
			"UPDATE NOSUCHTABLE SET DATE_DISPATCHED = SYSDATE WHERE ID = ?";
		final int idParam = 6666;

		// It's because Integers aren't canonical
		SQLException sex = new SQLException("bad update");

		MockControl ctrlPreparedStatement =
			MockControl.createControl(PreparedStatement.class);
		PreparedStatement mockPreparedStatement =
			(PreparedStatement) ctrlPreparedStatement.getMock();
		mockPreparedStatement.setInt(1, idParam);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeUpdate();
		ctrlPreparedStatement.setThrowable(sex);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		ctrlPreparedStatement.replay();
		replay();

		Dispatcher d = new Dispatcher(idParam, sql);
		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		try {
			template.update(d);
			fail("Bogus update should throw exception");
		}
		catch (UncategorizedDataAccessException ex) {
			// pass
			assertTrue(
				"Correct exception",
				ex instanceof UncategorizedSQLException);
			assertTrue("Root cause is correct", ex.getCause() == sex);
			//assertTrue("no update occurred", !je.getDataWasUpdated());
		}

		ctrlPreparedStatement.verify();
	}

	public void testStringsWithStaticSql() throws Exception {
		doTestStrings(new JdbcTemplateCallback() {
			public void doInJdbcTemplate(JdbcTemplate template, String sql, RowCallbackHandler rch) {
				template.query(sql, rch);
			}
		}, false, null);
	}

	public void testStringsWithEmptyPreparedStatementSetter() throws Exception {
		doTestStrings(new JdbcTemplateCallback() {
			public void doInJdbcTemplate(JdbcTemplate template, String sql, RowCallbackHandler rch) {
				template.query(sql, (PreparedStatementSetter) null, rch);
			}
		}, true, null);
	}

	public void testStringsWithPreparedStatementSetter() throws Exception {
		final Integer argument = new Integer(99);
		doTestStrings(new JdbcTemplateCallback() {
			public void doInJdbcTemplate(JdbcTemplate template, String sql, RowCallbackHandler rch) {
				template.query(sql, new PreparedStatementSetter() {
					public void setValues(PreparedStatement ps) throws SQLException {
						ps.setObject(1, argument);
					}
				}, rch);
			}
		}, true, argument);
	}

	public void testStringsWithEmptyPreparedStatementArgs() throws Exception {
		doTestStrings(new JdbcTemplateCallback() {
			public void doInJdbcTemplate(JdbcTemplate template, String sql, RowCallbackHandler rch) {
				template.query(sql, (Object[]) null, rch);
			}
		}, true, null);
	}

	public void testStringsWithPreparedStatementArgs() throws Exception {
		final Integer argument = new Integer(99);
		doTestStrings(new JdbcTemplateCallback() {
			public void doInJdbcTemplate(JdbcTemplate template, String sql, RowCallbackHandler rch) {
				template.query(sql, new Object[] {argument}, rch);
			}
		}, true, argument);
	}

	protected void doTestStrings(JdbcTemplateCallback jdbcTemplateCallback,
	                             boolean usePreparedStatement, Object argument) throws Exception {
		String sql = "SELECT FORENAME FROM CUSTMR";
		String[] results = { "rod", "gary", " portia" };

		class StringHandler implements RowCallbackHandler {
			private List l = new LinkedList();
			public void processRow(ResultSet rs) throws SQLException {
				l.add(rs.getString(1));
			}
			public String[] getStrings() {
				return (String[]) l.toArray(new String[l.size()]);
			}
		}

		MockControl ctrlResultSet = MockControl.createControl(ResultSet.class);
		ResultSet mockResultSet = (ResultSet) ctrlResultSet.getMock();
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getString(1);
		ctrlResultSet.setReturnValue(results[0]);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getString(1);
		ctrlResultSet.setReturnValue(results[1]);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getString(1);
		ctrlResultSet.setReturnValue(results[2]);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		MockControl ctrlStatement =
			MockControl.createControl(PreparedStatement.class);
		PreparedStatement mockStatement =
			(PreparedStatement) ctrlStatement.getMock();
		if (argument != null) {
			mockStatement.setObject(1, argument);
		}
		if (usePreparedStatement) {
			mockStatement.executeQuery();
		}
		else {
			mockStatement.executeQuery(sql);
		}
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		if (usePreparedStatement) {
			mockConnection.prepareStatement(sql);
		}
		else {
			mockConnection.createStatement();
		}
		ctrlConnection.setReturnValue(mockStatement);

		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		StringHandler sh = new StringHandler();
		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		jdbcTemplateCallback.doInJdbcTemplate(template, sql, sh);

		// Match
		String[] forenames = sh.getStrings();
		assertTrue("same length", forenames.length == results.length);
		for (int i = 0; i < forenames.length; i++) {
			assertTrue("Row " + i + " matches", forenames[i].equals(results[i]));
		}

		ctrlResultSet.verify();
		ctrlStatement.verify();
	}

	public void testLeaveConnOpenOnRequest() throws Exception {
		String sql = "SELECT ID, FORENAME FROM CUSTMR WHERE ID < 3";

		MockControl ctrlResultSet = MockControl.createControl(ResultSet.class);
		ResultSet mockResultSet = (ResultSet) ctrlResultSet.getMock();
		ctrlResultSet = MockControl.createControl(ResultSet.class);
		mockResultSet = (ResultSet) ctrlResultSet.getMock();
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		MockControl ctrlStatement = MockControl.createControl(PreparedStatement.class);
		PreparedStatement mockStatement = (PreparedStatement) ctrlStatement.getMock();
		ctrlStatement = MockControl.createControl(PreparedStatement.class);
		mockStatement = (PreparedStatement) ctrlStatement.getMock();
		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.isClosed();
		ctrlConnection.setReturnValue(false, 2);
		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);
		// if close is called entire test will fail
		mockConnection.close();
		ctrlConnection.setDefaultThrowable(new RuntimeException());

		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		SingleConnectionDataSource scf = new SingleConnectionDataSource(mockDataSource.getConnection(), false);
		JdbcTemplate template2 = new JdbcTemplate(scf);
		RowCountCallbackHandler rcch = new RowCountCallbackHandler();
		template2.query(sql, rcch);

		ctrlResultSet.verify();
		ctrlStatement.verify();
	}

	public void testCloseConnOnRequest() throws Exception {
		String sql = "SELECT ID, FORENAME FROM CUSTMR WHERE ID < 3";

		MockControl ctrlResultSet = MockControl.createControl(ResultSet.class);
		ResultSet mockResultSet = (ResultSet) ctrlResultSet.getMock();
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		MockControl ctrlStatement = MockControl.createControl(PreparedStatement.class);
		PreparedStatement mockStatement = (PreparedStatement) ctrlStatement.getMock();
		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);

		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		JdbcTemplate template2 = new JdbcTemplate(mockDataSource);
		RowCountCallbackHandler rcch = new RowCountCallbackHandler();
		template2.query(sql, rcch);

		ctrlResultSet.verify();
		ctrlStatement.verify();
	}

	/**
	 * Test that we see a runtime exception come back.
	 */
	public void testExceptionComesBack() throws Exception {
		final String sql = "SELECT ID FROM CUSTMR";
		final RuntimeException rex = new RuntimeException("What I want to see");

		MockControl ctrlResultSet = MockControl.createControl(ResultSet.class);
		ResultSet mockResultSet = (ResultSet) ctrlResultSet.getMock();
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		MockControl ctrlStatement = MockControl.createControl(PreparedStatement.class);
		PreparedStatement mockStatement = (PreparedStatement) ctrlStatement.getMock();
		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);

		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		try {
			template.query(sql, new RowCallbackHandler() {
				public void processRow(java.sql.ResultSet rs)
					throws java.sql.SQLException {
					throw rex;
				}
			});
			fail("Should have thrown exception");
		}
		catch (RuntimeException ex) {
			assertTrue("Wanted same exception back, not " + ex, ex == rex);
		}
	}

	/**
	 * Test update with static SQL.
	 */
	public void testSqlUpdate() throws Exception {
		final String sql = "UPDATE NOSUCHTABLE SET DATE_DISPATCHED = SYSDATE WHERE ID = 4";
		int rowsAffected = 33;

		MockControl ctrlStatement = MockControl.createControl(Statement.class);
		Statement mockStatement = (Statement) ctrlStatement.getMock();
		mockStatement.executeUpdate(sql);
		ctrlStatement.setReturnValue(rowsAffected);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);

		ctrlStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		int actualRowsAffected = template.update(sql);
		assertTrue(
			"Actual rows affected is correct",
			actualRowsAffected == rowsAffected);

		ctrlStatement.verify();
	}

	public void testSqlUpdateEncountersSqlException() throws Exception {
		SQLException sex = new SQLException("bad update");
		final String sql = "UPDATE NOSUCHTABLE SET DATE_DISPATCHED = SYSDATE WHERE ID = 4";

		MockControl ctrlStatement = MockControl.createControl(Statement.class);
		Statement mockStatement = (Statement) ctrlStatement.getMock();
		mockStatement.executeUpdate(sql);
		ctrlStatement.setThrowable(sex);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);

		ctrlStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		try {
			template.update(sql);
		}
		catch (DataAccessException ex) {
			assertTrue("root cause is correct", ex.getCause() == sex);
		}

		ctrlStatement.verify();
	}

	public void testSqlUpdateWithThreadConnection() throws Exception {
		final String sql = "UPDATE NOSUCHTABLE SET DATE_DISPATCHED = SYSDATE WHERE ID = 4";
		int rowsAffected = 33;

		MockControl ctrlStatement = MockControl.createControl(Statement.class);
		Statement mockStatement = (Statement) ctrlStatement.getMock();
		mockStatement.executeUpdate(sql);
		ctrlStatement.setReturnValue(rowsAffected);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);

		ctrlStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		int actualRowsAffected = template.update(sql);
		assertTrue(
			"Actual rows affected is correct",
			actualRowsAffected == rowsAffected);

		ctrlStatement.verify();
	}

	public void testBatchUpdate() throws Exception {
		final String sql = "UPDATE NOSUCHTABLE SET DATE_DISPATCHED = SYSDATE WHERE ID = ?";
		final int[] ids = new int[] { 100, 200 };

		MockControl ctrlPreparedStatement = MockControl.createControl(PreparedStatement.class);
		PreparedStatement mockPreparedStatement = (PreparedStatement) ctrlPreparedStatement.getMock();
		mockPreparedStatement.getConnection();
		ctrlPreparedStatement.setReturnValue(mockConnection);
		mockPreparedStatement.setInt(1, ids[0]);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.addBatch();
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.setInt(1, ids[1]);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.addBatch();
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeBatch();
		ctrlPreparedStatement.setReturnValue(ids);
		mockPreparedStatement.getWarnings();
		ctrlPreparedStatement.setReturnValue(null);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		MockControl ctrlDatabaseMetaData = MockControl.createControl(DatabaseMetaData.class);
		DatabaseMetaData mockDatabaseMetaData = (DatabaseMetaData) ctrlDatabaseMetaData.getMock();
		mockDatabaseMetaData.getDatabaseProductName();
		ctrlDatabaseMetaData.setReturnValue("MySQL");
		mockDatabaseMetaData.getDriverVersion();
		ctrlDatabaseMetaData.setReturnValue("1.2.3");
		mockDatabaseMetaData.supportsBatchUpdates();
		ctrlDatabaseMetaData.setReturnValue(true);

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockPreparedStatement);
		mockConnection.getMetaData();
		ctrlConnection.setReturnValue(mockDatabaseMetaData, 2);

		ctrlPreparedStatement.replay();
		ctrlDatabaseMetaData.replay();
		replay();

		BatchPreparedStatementSetter setter =
			new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int i)
				throws SQLException {
				ps.setInt(1, ids[i]);
			}
			public int getBatchSize() {
				return ids.length;
			}
		};

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		int[] actualRowsAffected = template.batchUpdate(sql, setter);
		assertTrue("executed 2 updates", actualRowsAffected.length == 2);

		ctrlPreparedStatement.verify();
		ctrlDatabaseMetaData.verify();
	}

	public void testBatchUpdateWithNoBatchSupport() throws Exception {
		final String sql = "UPDATE NOSUCHTABLE SET DATE_DISPATCHED = SYSDATE WHERE ID = ?";
		final int[] ids = new int[] { 100, 200 };

		MockControl ctrlPreparedStatement = MockControl.createControl(PreparedStatement.class);
		PreparedStatement mockPreparedStatement = (PreparedStatement) ctrlPreparedStatement.getMock();
		mockPreparedStatement.getConnection();
		ctrlPreparedStatement.setReturnValue(mockConnection);
		mockPreparedStatement.setInt(1, ids[0]);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeUpdate();
		ctrlPreparedStatement.setReturnValue(ids[0]);
		mockPreparedStatement.setInt(1, ids[1]);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeUpdate();
		ctrlPreparedStatement.setReturnValue(ids[1]);
		mockPreparedStatement.getWarnings();
		ctrlPreparedStatement.setReturnValue(null);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		ctrlPreparedStatement.replay();
		replay();

		BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setInt(1, ids[i]);
			}
			public int getBatchSize() {
				return ids.length;
			}
		};

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		int[] actualRowsAffected = template.batchUpdate(sql, setter);
		assertTrue("executed 2 updates", actualRowsAffected.length == 2);

		ctrlPreparedStatement.verify();
	}

	public void testBatchUpdateFails() throws Exception {
		final String sql = "UPDATE NOSUCHTABLE SET DATE_DISPATCHED = SYSDATE WHERE ID = ?";
		final int[] ids = new int[] { 100, 200 };
		SQLException sex = new SQLException();

		MockControl ctrlPreparedStatement = MockControl.createControl(PreparedStatement.class);
		PreparedStatement mockPreparedStatement = (PreparedStatement) ctrlPreparedStatement.getMock();
		mockPreparedStatement.getConnection();
		ctrlPreparedStatement.setReturnValue(mockConnection);
		mockPreparedStatement.setInt(1, ids[0]);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.addBatch();
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.setInt(1, ids[1]);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.addBatch();
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeBatch();
		ctrlPreparedStatement.setThrowable(sex);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		MockControl ctrlDatabaseMetaData = MockControl.createControl(DatabaseMetaData.class);
		DatabaseMetaData mockDatabaseMetaData = (DatabaseMetaData) ctrlDatabaseMetaData.getMock();
		mockDatabaseMetaData.getDatabaseProductName();
		ctrlDatabaseMetaData.setReturnValue("MySQL");
		mockDatabaseMetaData.getDriverVersion();
		ctrlDatabaseMetaData.setReturnValue("1.2.3");
		mockDatabaseMetaData.supportsBatchUpdates();
		ctrlDatabaseMetaData.setReturnValue(true);

		ctrlConnection.reset();
		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockPreparedStatement);
		mockConnection.getMetaData();
		ctrlConnection.setReturnValue(mockDatabaseMetaData, 2);
		mockConnection.close();
		ctrlConnection.setVoidCallable(2);

		ctrlPreparedStatement.replay();
		ctrlDatabaseMetaData.replay();
		replay();

		BatchPreparedStatementSetter setter = new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setInt(1, ids[i]);
			}
			public int getBatchSize() {
				return ids.length;
			}
		};

		try {
			JdbcTemplate template = new JdbcTemplate(mockDataSource);
			template.batchUpdate(sql, setter);
			fail("Should have failed because of SQLException in bulk update");
		}
		catch (DataAccessException ex) {
			assertTrue("Root cause is SQLException", ex.getCause() == sex);
		}

		ctrlPreparedStatement.verify();
		ctrlDatabaseMetaData.verify();
	}

	public void testCouldntGetConnectionInOperationOrLazilyInstantiatedExceptionTranslator() throws SQLException {
		SQLException sex = new SQLException("foo", "07xxx");

		// Change behaviour in setUp() because we only expect one call to getConnection():
		// none is necessary to get metadata for exception translator
		ctrlDataSource = MockControl.createControl(DataSource.class);
		mockDataSource = (DataSource) ctrlDataSource.getMock();
		mockDataSource.getConnection();
		// Expect two calls (one call after caching data product name): make get Metadata fail also
		ctrlDataSource.setThrowable(sex, 2);
		replay();

		try {
			JdbcTemplate template2 = new JdbcTemplate(mockDataSource);
			RowCountCallbackHandler rcch = new RowCountCallbackHandler();
			template2.query("SELECT ID, FORENAME FROM CUSTMR WHERE ID < 3", rcch);
			fail("Shouldn't have executed query without a connection");
		} 
		catch (CannotGetJdbcConnectionException ex) {
			// pass
			assertTrue("Check root cause", ex.getCause() == sex);
		}
		
		ctrlDataSource.verify();
	}

	/**
	 * Verify that afterPropertiesSet invokes exception translator.
	 */
	public void testCouldntGetConnectionInOperationWithExceptionTranslatorInitialized() throws SQLException {
		SQLException sex = new SQLException("foo", "07xxx");

		ctrlDataSource = MockControl.createControl(DataSource.class);
		mockDataSource = (DataSource) ctrlDataSource.getMock();
		//mockConnection.getMetaData();
		//ctrlConnection.setReturnValue(null, 1);
		//mockConnection.close();
		//ctrlConnection.setVoidCallable(1);
		ctrlConnection.replay();
		
		// Change behaviour in setUp() because we only expect one call to getConnection():
		// none is necessary to get metadata for exception translator
		ctrlDataSource = MockControl.createControl(DataSource.class);
		mockDataSource = (DataSource) ctrlDataSource.getMock();
		// Upfront call for metadata - no longer the case
		//mockDataSource.getConnection();
		//ctrlDataSource.setReturnValue(mockConnection, 1);
		// One call for operation
		mockDataSource.getConnection();
		ctrlDataSource.setThrowable(sex, 2);
		ctrlDataSource.replay();

		try {
			JdbcTemplate template2 = new JdbcTemplate(mockDataSource);
			template2.afterPropertiesSet();
			RowCountCallbackHandler rcch = new RowCountCallbackHandler();
			template2.query("SELECT ID, FORENAME FROM CUSTMR WHERE ID < 3", rcch);
			fail("Shouldn't have executed query without a connection");
		} 
		catch (CannotGetJdbcConnectionException ex) {
			// pass
			assertTrue("Check root cause", ex.getCause() == sex);
		}
	
		ctrlDataSource.verify();
		ctrlConnection.verify();
	}
	
	public void testCouldntGetConnectionInOperationWithExceptionTranslatorInitializedViaBeanProperty()
			throws Exception {
		doTestCouldntGetConnectionInOperationWithExceptionTranslatorInitialized(true);
	}
	
	public void testCouldntGetConnectionInOperationWithExceptionTranslatorInitializedInAfterPropertiesSet()
			throws Exception {
		doTestCouldntGetConnectionInOperationWithExceptionTranslatorInitialized(false);
	}
	
	/**
	 * If beanProperty is true, initialize via exception translator bean property;
	 * if false, use afterPropertiesSet().
	 */
	private void doTestCouldntGetConnectionInOperationWithExceptionTranslatorInitialized(boolean beanProperty)
			throws SQLException {
		SQLException sex = new SQLException("foo", "07xxx");

		ctrlConnection = MockControl.createControl(Connection.class);
		mockConnection = (Connection) ctrlConnection.getMock();
		//mockConnection.getMetaData();
		//ctrlConnection.setReturnValue(null, 1);
		//mockConnection.close();
		//ctrlConnection.setVoidCallable(1);
		ctrlConnection.replay();
	
		// Change behaviour in setUp() because we only expect one call to getConnection():
		// none is necessary to get metadata for exception translator
		ctrlDataSource = MockControl.createControl(DataSource.class);
		mockDataSource = (DataSource) ctrlDataSource.getMock();
		// Upfront call for metadata - no longer the case
		//mockDataSource.getConnection();
		//ctrlDataSource.setReturnValue(mockConnection, 1);
		// One call for operation
		mockDataSource.getConnection();
		ctrlDataSource.setThrowable(sex, 2);
		ctrlDataSource.replay();

		try {
			JdbcTemplate template2 = new JdbcTemplate();
			template2.setDataSource(mockDataSource);
			if (beanProperty) {
				// This will get a connection.
				template2.setExceptionTranslator(new SQLErrorCodeSQLExceptionTranslator(mockDataSource));
			}
			else {
				// This will cause creation of default SQL translator.
				// Note that only call should be effective.
				template2.afterPropertiesSet();
				template2.afterPropertiesSet();
			}
			RowCountCallbackHandler rcch = new RowCountCallbackHandler();
			template2.query(
				"SELECT ID, FORENAME FROM CUSTMR WHERE ID < 3",
				rcch);
			fail("Shouldn't have executed query without a connection");
		} 
		catch (CannotGetJdbcConnectionException ex) {
			// pass
			assertTrue("Check root cause", ex.getCause() == sex);
		}

		ctrlDataSource.verify();
		ctrlConnection.verify();
	}

	public void testPreparedStatementSetterSucceeds() throws Exception {
		final String sql = "UPDATE FOO SET NAME=? WHERE ID = 1";
		final String name = "Gary";
		int expectedRowsUpdated = 1;

		MockControl ctrlPreparedStatement =
			MockControl.createControl(PreparedStatement.class);
		PreparedStatement mockPreparedStatement =
			(PreparedStatement) ctrlPreparedStatement.getMock();
		mockPreparedStatement.setString(1, name);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeUpdate();
		ctrlPreparedStatement.setReturnValue(expectedRowsUpdated);
		mockPreparedStatement.getWarnings();
		ctrlPreparedStatement.setReturnValue(null);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		ctrlPreparedStatement.replay();
		replay();

		PreparedStatementSetter pss = new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, name);
			}
		};
		int actualRowsUpdated =
			new JdbcTemplate(mockDataSource).update(sql, pss);
		assertTrue(
			"updated correct # of rows",
			actualRowsUpdated == expectedRowsUpdated);

		ctrlPreparedStatement.verify();
	}

	public void testPreparedStatementSetterFails() throws Exception {
		final String sql = "UPDATE FOO SET NAME=? WHERE ID = 1";
		final String name = "Gary";
		SQLException sex = new SQLException();

		MockControl ctrlPreparedStatement =
			MockControl.createControl(PreparedStatement.class);
		PreparedStatement mockPreparedStatement =
			(PreparedStatement) ctrlPreparedStatement.getMock();
		mockPreparedStatement.setString(1, name);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeUpdate();
		ctrlPreparedStatement.setThrowable(sex);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		ctrlPreparedStatement.replay();
		replay();

		PreparedStatementSetter pss = new PreparedStatementSetter() {
			public void setValues(PreparedStatement ps) throws SQLException {
				ps.setString(1, name);
			}
		};
		try {
			new JdbcTemplate(mockDataSource).update(sql, pss);
			fail("Should have failed with SQLException");
		}
		catch (DataAccessException ex) {
			assertTrue("root cause was preserved", ex.getCause() == sex);
		}

		ctrlPreparedStatement.verify();
	}

	public void testCouldntClose() throws Exception {
		MockControl ctrlStatement = MockControl.createControl(Statement.class);
		Statement mockStatement = (Statement) ctrlStatement.getMock();
		MockControl ctrlResultSet = MockControl.createControl(ResultSet.class);
		ResultSet mockResultSet = (ResultSet) ctrlResultSet.getMock();
		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);
		String sql = "SELECT ID, FORENAME FROM CUSTMR WHERE ID < 3";
		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		SQLException sex = new SQLException("bar");
		mockResultSet.close();
		ctrlResultSet.setThrowable(sex);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setThrowable(sex);
		mockConnection.close();
		ctrlConnection.setThrowable(sex);

		ctrlStatement.replay();
		ctrlResultSet.replay();
		replay();

		JdbcTemplate template2 = new JdbcTemplate(mockDataSource);
		RowCountCallbackHandler rcch = new RowCountCallbackHandler();
		template2.query("SELECT ID, FORENAME FROM CUSTMR WHERE ID < 3", rcch);

		ctrlStatement.verify();
		ctrlResultSet.verify();
	}

	/**
	 * Mock objects allow us to produce warnings at will
	 */
	public void testFatalWarning() throws Exception {
		String sql = "SELECT forename from custmr";
		SQLWarning warnings = new SQLWarning("My warning");

		MockControl ctrlResultSet = MockControl.createControl(ResultSet.class);
		ResultSet mockResultSet = (ResultSet) ctrlResultSet.getMock();
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		MockControl ctrlStatement = MockControl.createControl(PreparedStatement.class);
		PreparedStatement mockStatement = (PreparedStatement) ctrlStatement.getMock();
		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(warnings);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);

		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		JdbcTemplate t = new JdbcTemplate(mockDataSource);
		t.setIgnoreWarnings(false);
		try {
			t.query(sql, new RowCallbackHandler() {
				public void processRow(java.sql.ResultSet rs)
					throws java.sql.SQLException {
					rs.getByte(1);
				}
			});
			fail("Should have thrown exception on warning");
		}
		catch (SQLWarningException ex) {
			// Pass
			assertTrue(
				"Root cause of warning was correct",
				ex.getCause() == warnings);
		}

		ctrlResultSet.verify();
		ctrlStatement.verify();
	}

	public void testIgnoredWarning() throws Exception {
		String sql = "SELECT forename from custmr";
		SQLWarning warnings = new SQLWarning("My warning");

		MockControl ctrlResultSet = MockControl.createControl(ResultSet.class);
		ResultSet mockResultSet = (ResultSet) ctrlResultSet.getMock();
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		MockControl ctrlStatement =
			MockControl.createControl(PreparedStatement.class);
		PreparedStatement mockStatement =
			(PreparedStatement) ctrlStatement.getMock();
		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(warnings);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);

		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		// Too long: truncation
		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		template.setIgnoreWarnings(true);
		template.query(sql, new RowCallbackHandler() {
			public void processRow(java.sql.ResultSet rs)
				throws java.sql.SQLException {
				rs.getByte(1);
			}
		});

		ctrlResultSet.verify();
		ctrlStatement.verify();
	}

	/**
	 * Test that we see an SQLException translated using Error Code
	 */
	public void testSQLErrorCodeTranslation() throws Exception {
		final SQLException sex = new SQLException("I have a known problem", "99999", 1054);
		final String sql = "SELECT ID FROM CUSTOMER";

		MockControl ctrlResultSet = MockControl.createControl(ResultSet.class);
		ResultSet mockResultSet = (ResultSet) ctrlResultSet.getMock();
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		MockControl ctrlStatement = MockControl.createControl(PreparedStatement.class);
		PreparedStatement mockStatement = (PreparedStatement) ctrlStatement.getMock();
		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		MockControl ctrlDatabaseMetaData = MockControl.createControl(DatabaseMetaData.class);
		DatabaseMetaData mockDatabaseMetaData = (DatabaseMetaData) ctrlDatabaseMetaData.getMock();
		mockDatabaseMetaData.getDatabaseProductName();
		ctrlDatabaseMetaData.setReturnValue("MySQL");
		mockDatabaseMetaData.getDriverVersion();
		ctrlDatabaseMetaData.setReturnValue("1.2.3");

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);
		mockConnection.getMetaData();
		ctrlConnection.setReturnValue(mockDatabaseMetaData);

		ctrlResultSet.replay();
		ctrlStatement.replay();
		ctrlDatabaseMetaData.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		try {
			template.query(sql, new RowCallbackHandler() {
				public void processRow(java.sql.ResultSet rs) throws SQLException {
					throw sex;
				}
			});
			fail("Should have thrown exception");
		}
		catch (BadSqlGrammarException ex) {
			assertTrue("Wanted same exception back, not " + ex, sex == ex.getCause());
		}
		catch (Exception ex) {
			fail("Should have thrown BadSqlGrammarException exception, not " + ex);
		}

		ctrlResultSet.verify();
		ctrlStatement.verify();
	}

	/**
	 * Test that we see an SQLException translated using Error Code.
	 * If we provide the SQLExceptionTranslator, we shouldn't use a connection
	 * to get the metadata
	 */
	public void testUseCustomSQLErrorCodeTranslator() throws Exception {
		// Bad SQL state
		final SQLException sex = new SQLException("I have a known problem", "07000", 1054);
		final String sql = "SELECT ID FROM CUSTOMER";

		MockControl ctrlResultSet = MockControl.createControl(ResultSet.class);
		ResultSet mockResultSet = (ResultSet) ctrlResultSet.getMock();
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		MockControl ctrlStatement = MockControl.createControl(PreparedStatement.class);
		PreparedStatement mockStatement = (PreparedStatement) ctrlStatement.getMock();
		mockStatement.executeQuery(sql);
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement);
		
		// Change behaviour in setUp() because we only expect one call to getConnection():
		// none is necessary to get metadata for exception translator
		ctrlConnection = MockControl.createControl(Connection.class);
		mockConnection = (Connection) ctrlConnection.getMock();
		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement, 1);
		mockConnection.close();
		ctrlConnection.setVoidCallable(1);
		ctrlConnection.replay();

		ctrlDataSource = MockControl.createControl(DataSource.class);
		mockDataSource = (DataSource) ctrlDataSource.getMock();
		mockDataSource.getConnection();
		ctrlDataSource.setReturnValue(mockConnection, 1);
		ctrlDataSource.replay();
		///// end changed behaviour

		ctrlResultSet.replay();
		ctrlStatement.replay();

		JdbcTemplate template = new JdbcTemplate();
		template.setDataSource(mockDataSource);
		// Set custom exception translator
		template.setExceptionTranslator(new SQLStateSQLExceptionTranslator());
		template.afterPropertiesSet();
		try {
			template.query(sql, new RowCallbackHandler() {
				public void processRow(java.sql.ResultSet rs)
					throws SQLException {
					throw sex;
				}
			});
			fail("Should have thrown exception");
		}
		catch (BadSqlGrammarException ex) {
			assertTrue(
				"Wanted same exception back, not " + ex,
				sex == ex.getCause());
		} 

		ctrlResultSet.verify();
		ctrlStatement.verify();
		
		// We didn't call superclass replay() so we need to check these ourselves
		ctrlDataSource.verify();
		ctrlConnection.verify();
	}

	public void testNativeJdbcExtractorInvoked() throws Exception {
		MockControl ctrlResultSet = MockControl.createControl(ResultSet.class);
		final ResultSet mockResultSet = (ResultSet) ctrlResultSet.getMock();
		mockResultSet.close();
		ctrlResultSet.setVoidCallable(2);

		MockControl ctrlStatement = MockControl.createControl(Statement.class);
		final Statement mockStatement = (Statement) ctrlStatement.getMock();
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();
		MockControl ctrlStatement2 = MockControl.createControl(Statement.class);
		final Statement mockStatement2 = (Statement) ctrlStatement2.getMock();
		mockStatement2.executeQuery("my query");
		ctrlStatement2.setReturnValue(mockResultSet, 1);

		MockControl ctrlPreparedStatement =	MockControl.createControl(PreparedStatement.class);
		final PreparedStatement mockPreparedStatement =	(PreparedStatement) ctrlPreparedStatement.getMock();
		mockPreparedStatement.getWarnings();
		ctrlPreparedStatement.setReturnValue(null);
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();
		MockControl ctrlPreparedStatement2 =	MockControl.createControl(PreparedStatement.class);
		final PreparedStatement mockPreparedStatement2 =	(PreparedStatement) ctrlPreparedStatement2.getMock();
		mockPreparedStatement2.executeQuery();
		ctrlPreparedStatement2.setReturnValue(mockResultSet, 1);

		MockControl ctrlCallableStatement =	MockControl.createControl(CallableStatement.class);
		final CallableStatement mockCallableStatement =	(CallableStatement) ctrlCallableStatement.getMock();
		mockCallableStatement.getWarnings();
		ctrlCallableStatement.setReturnValue(null);
		mockCallableStatement.close();
		ctrlCallableStatement.setVoidCallable();
		MockControl ctrlCallableStatement2 =	MockControl.createControl(CallableStatement.class);
		final CallableStatement mockCallableStatement2 =	(CallableStatement) ctrlCallableStatement2.getMock();
		mockCallableStatement2.execute();
		ctrlCallableStatement2.setReturnValue(true);
		mockCallableStatement2.getUpdateCount();
		ctrlCallableStatement2.setReturnValue(-1);
		mockCallableStatement2.getMoreResults();
		ctrlCallableStatement2.setReturnValue(false);
		mockCallableStatement2.getUpdateCount();
		ctrlCallableStatement2.setReturnValue(-1);

		ctrlResultSet.replay();
		ctrlStatement.replay();
		ctrlStatement2.replay();
		ctrlPreparedStatement.replay();
		ctrlPreparedStatement2.replay();
		ctrlCallableStatement.replay();
		ctrlCallableStatement2.replay();

		mockConnection.createStatement();
		ctrlConnection.setReturnValue(mockStatement, 1);
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		template.setNativeJdbcExtractor(new NativeJdbcExtractor() {
			public boolean isNativeConnectionNecessaryForNativeStatements() {
				return false;
			}
			public boolean isNativeConnectionNecessaryForNativePreparedStatements() {
				return false;
			}
			public boolean isNativeConnectionNecessaryForNativeCallableStatements() {
				return false;
			}
			public Connection getNativeConnection(Connection con) {
				return con;
			}
			public Connection getNativeConnectionFromStatement(Statement stmt) throws SQLException {
				return stmt.getConnection();
			}
			public Statement getNativeStatement(Statement stmt) {
				assertTrue(stmt == mockStatement);
				return mockStatement2;
			}
			public PreparedStatement getNativePreparedStatement(PreparedStatement ps) {
				assertTrue(ps == mockPreparedStatement);
				return mockPreparedStatement2;
			}
			public CallableStatement getNativeCallableStatement(CallableStatement cs) {
				assertTrue(cs == mockCallableStatement);
				return mockCallableStatement2;
			}
			public ResultSet getNativeResultSet(ResultSet rs) throws SQLException {
				return rs;
			}
		});

		template.query("my query",	new ResultSetExtractor() {
			public Object extractData(ResultSet rs2) throws SQLException {
				assertEquals(mockResultSet, rs2);
				return null;
			}
		});

		template.query(new PreparedStatementCreator() {
			public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
				return mockPreparedStatement;
			}
		}, new ResultSetExtractor() {
			public Object extractData(ResultSet rs2) throws SQLException {
				assertEquals(mockResultSet, rs2);
				return null;
			}
		});

		template.call(new CallableStatementCreator() {
			public CallableStatement createCallableStatement(Connection con) throws SQLException {
				return mockCallableStatement;
			}
		},
		new ArrayList());

		ctrlStatement.verify();
		ctrlStatement2.verify();
		ctrlPreparedStatement.verify();
		ctrlPreparedStatement2.verify();
		ctrlCallableStatement.verify();
		ctrlCallableStatement2.verify();
	}

	public void testStaticResultSetClosed() throws Exception {
		MockControl ctrlResultSet;
		ResultSet mockResultSet;
		MockControl ctrlStatement;
		Statement mockStatement;
		MockControl ctrlResultSet2;
		ResultSet mockResultSet2;
		MockControl ctrlPreparedStatement;
		PreparedStatement mockPreparedStatement;

		try {
			ctrlResultSet = MockControl.createControl(ResultSet.class);
			mockResultSet = (ResultSet) ctrlResultSet.getMock();
			mockResultSet.close();
			ctrlResultSet.setVoidCallable();

			ctrlStatement = MockControl.createControl(Statement.class);
			mockStatement = (Statement) ctrlStatement.getMock();
			mockStatement.executeQuery("my query");
			ctrlStatement.setReturnValue(mockResultSet);
			mockStatement.close();
			ctrlStatement.setVoidCallable();

			ctrlResultSet2 = MockControl.createControl(ResultSet.class);
			mockResultSet2 = (ResultSet) ctrlResultSet2.getMock();
			mockResultSet2.close();
			ctrlResultSet2.setVoidCallable();

			ctrlPreparedStatement =	MockControl.createControl(PreparedStatement.class);
			mockPreparedStatement =	(PreparedStatement) ctrlPreparedStatement.getMock();
			mockPreparedStatement.executeQuery();
			ctrlPreparedStatement.setReturnValue(mockResultSet2);
			mockPreparedStatement.close();
			ctrlPreparedStatement.setVoidCallable();

			mockConnection.createStatement();
			ctrlConnection.setReturnValue(mockStatement);
			mockConnection.prepareStatement("my query");
			ctrlConnection.setReturnValue(mockPreparedStatement);
		}
		catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		ctrlResultSet.replay();
		ctrlStatement.replay();
		ctrlResultSet2.replay();
		ctrlPreparedStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		try {
			template.query("my query",	new ResultSetExtractor() {
				public Object extractData(ResultSet rs) throws SQLException {
					throw new InvalidDataAccessApiUsageException("");
				}
			});
			fail("Should have thrown InvalidDataAccessApiUsageException");
		}
		catch (InvalidDataAccessApiUsageException idaauex) {
			// ok
		}

		try {
			template.query(new PreparedStatementCreator() {
				public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
					return con.prepareStatement("my query");
				}
				public String getSql() {
					return null;
				}
			}, new ResultSetExtractor() {
				public Object extractData(ResultSet rs2) throws SQLException {
					throw new InvalidDataAccessApiUsageException("");
				}
			});
			fail("Should have thrown InvalidDataAccessApiUsageException");
		}
		catch (InvalidDataAccessApiUsageException idaauex) {
			// ok
		}

		// verify confirms if test is successful by checking if close() called
		ctrlResultSet.verify();
		ctrlStatement.verify();
		ctrlResultSet2.verify();
		ctrlPreparedStatement.verify();
	}

	public void testExecuteClosed() throws Exception {
		MockControl ctrlResultSet;
		ResultSet mockResultSet;
		MockControl ctrlCallable;
		CallableStatement mockCallable;

		try {
			ctrlResultSet = MockControl.createControl(ResultSet.class);
			mockResultSet = (ResultSet) ctrlResultSet.getMock();
			mockResultSet.next();
			ctrlResultSet.setReturnValue(true);
			mockResultSet.close();
			ctrlResultSet.setVoidCallable();

			ctrlCallable = MockControl.createControl(CallableStatement.class);
			mockCallable = (CallableStatement) ctrlCallable.getMock();
			mockCallable.execute();
			ctrlCallable.setReturnValue(true);
			mockCallable.getUpdateCount();
			ctrlCallable.setReturnValue(-1);
			mockCallable.getResultSet();
			ctrlCallable.setReturnValue(mockResultSet);
			mockCallable.close();
			ctrlCallable.setVoidCallable();

			mockConnection.prepareCall("my query");
			ctrlConnection.setReturnValue(mockCallable);

		}
		catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		ctrlResultSet.replay();
		ctrlCallable.replay();
		replay();

		List params = new ArrayList();
		params.add(new SqlReturnResultSet("", new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				throw new InvalidDataAccessApiUsageException("");
			}

		}));

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		try {
			template.call(new CallableStatementCreator() {
				public CallableStatement createCallableStatement(Connection conn)
					throws SQLException {
					return conn.prepareCall("my query");
				}
			}, params);
		}
		catch (InvalidDataAccessApiUsageException idaauex) {
			// ok
		}

		// verify confirms if test is successful by checking if close() called
		ctrlResultSet.verify();
		ctrlCallable.verify();
	}

	public void testQueryForList() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID < 3";

		MockControl ctrlResultSetMetaData;
		ResultSetMetaData mockResultSetMetaData;
		MockControl ctrlResultSet;
		ResultSet mockResultSet;
		MockControl ctrlStatement;
		Statement mockStatement;

		try {
			ctrlResultSetMetaData = MockControl.createControl(ResultSetMetaData.class);
			mockResultSetMetaData = (ResultSetMetaData) ctrlResultSetMetaData.getMock();
			mockResultSetMetaData.getColumnCount();
			ctrlResultSetMetaData.setReturnValue(1);
			mockResultSetMetaData.getColumnName(1);
			ctrlResultSetMetaData.setReturnValue("age", 2);

			ctrlResultSet = MockControl.createControl(ResultSet.class);
			mockResultSet = (ResultSet) ctrlResultSet.getMock();
			mockResultSet.getMetaData();
			ctrlResultSet.setReturnValue(mockResultSetMetaData);
			mockResultSet.next();
			ctrlResultSet.setReturnValue(true);
			mockResultSet.getObject(1);
			ctrlResultSet.setReturnValue(new Integer(11));
			mockResultSet.next();
			ctrlResultSet.setReturnValue(true);
			mockResultSet.getObject(1);
			ctrlResultSet.setReturnValue(new Integer(12));
			mockResultSet.next();
			ctrlResultSet.setReturnValue(false);
			mockResultSet.close();
			ctrlResultSet.setVoidCallable();

			ctrlStatement = MockControl.createControl(Statement.class);
			mockStatement = (Statement) ctrlStatement.getMock();
			mockStatement.executeQuery(sql);
			ctrlStatement.setReturnValue(mockResultSet);
			mockStatement.getWarnings();
			ctrlStatement.setReturnValue(null);
			mockStatement.close();
			ctrlStatement.setVoidCallable();

			mockConnection.createStatement();
			ctrlConnection.setReturnValue(mockStatement);
		}
		catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		ctrlResultSetMetaData.replay();
		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		List li = template.queryForList(sql);
		assertEquals("All rows returned", 2, li.size());
		assertEquals("First row is Integer", 11, ((Integer)((Map)li.get(0)).get("age")).intValue());
		assertEquals("Second row is Integer", 12, ((Integer)((Map)li.get(1)).get("age")).intValue());

		ctrlResultSet.verify();
		ctrlStatement.verify();
	}

	public void testQueryForListWithSingleRowAndColumn() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID < 3";

		MockControl ctrlResultSetMetaData;
		ResultSetMetaData mockResultSetMetaData;
		MockControl ctrlResultSet;
		ResultSet mockResultSet;
		MockControl ctrlStatement;
		Statement mockStatement;

		try {
			ctrlResultSetMetaData = MockControl.createControl(ResultSetMetaData.class);
			mockResultSetMetaData = (ResultSetMetaData) ctrlResultSetMetaData.getMock();
			mockResultSetMetaData.getColumnCount();
			ctrlResultSetMetaData.setReturnValue(1);
			mockResultSetMetaData.getColumnName(1);
			ctrlResultSetMetaData.setReturnValue("age", 2);

			ctrlResultSet = MockControl.createControl(ResultSet.class);
			mockResultSet = (ResultSet) ctrlResultSet.getMock();
			mockResultSet.getMetaData();
			ctrlResultSet.setReturnValue(mockResultSetMetaData);
			mockResultSet.next();
			ctrlResultSet.setReturnValue(true);
			mockResultSet.getObject(1);
			ctrlResultSet.setReturnValue(new Integer(11));
			mockResultSet.next();
			ctrlResultSet.setReturnValue(false);
			mockResultSet.close();
			ctrlResultSet.setVoidCallable();

			ctrlStatement = MockControl.createControl(Statement.class);
			mockStatement = (Statement) ctrlStatement.getMock();
			mockStatement.executeQuery(sql);
			ctrlStatement.setReturnValue(mockResultSet);
			mockStatement.getWarnings();
			ctrlStatement.setReturnValue(null);
			mockStatement.close();
			ctrlStatement.setVoidCallable();

			mockConnection.createStatement();
			ctrlConnection.setReturnValue(mockStatement);
		}
		catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		ctrlResultSetMetaData.replay();
		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		List li = template.queryForList(sql);
		assertEquals("All rows returned", 1, li.size());
		assertEquals("First row is Integer", 11, ((Integer)((Map)li.get(0)).get("age")).intValue());

		ctrlResultSet.verify();
		ctrlStatement.verify();
	}

	public void testQueryForObject() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = 3";

		MockControl ctrlResultSetMetaData;
		ResultSetMetaData mockResultSetMetaData;
		MockControl ctrlResultSet;
		ResultSet mockResultSet;
		MockControl ctrlStatement;
		Statement mockStatement;

		try {
			ctrlResultSetMetaData = MockControl.createControl(ResultSetMetaData.class);
			mockResultSetMetaData = (ResultSetMetaData) ctrlResultSetMetaData.getMock();
			mockResultSetMetaData.getColumnCount();
			ctrlResultSetMetaData.setReturnValue(1);

			ctrlResultSet = MockControl.createControl(ResultSet.class);
			mockResultSet = (ResultSet) ctrlResultSet.getMock();
			mockResultSet.getMetaData();
			ctrlResultSet.setReturnValue(mockResultSetMetaData);
			mockResultSet.next();
			ctrlResultSet.setReturnValue(true);
			mockResultSet.getObject(1);
			ctrlResultSet.setReturnValue(new Integer(22));
			mockResultSet.next();
			ctrlResultSet.setReturnValue(false);
			mockResultSet.close();
			ctrlResultSet.setVoidCallable();

			ctrlStatement = MockControl.createControl(Statement.class);
			mockStatement = (Statement) ctrlStatement.getMock();
			mockStatement.executeQuery(sql);
			ctrlStatement.setReturnValue(mockResultSet);
			mockStatement.getWarnings();
			ctrlStatement.setReturnValue(null);
			mockStatement.close();
			ctrlStatement.setVoidCallable();

			mockConnection.createStatement();
			ctrlConnection.setReturnValue(mockStatement);
		}
		catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		ctrlResultSetMetaData.replay();
		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		Object o = template.queryForObject(sql, Integer.class);
		assertEquals("Return of an object", "java.lang.Integer", o.getClass().getName());

		ctrlResultSet.verify();
		ctrlStatement.verify();
	}

	public void testQueryForInt() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = 3";

		MockControl ctrlResultSetMetaData;
		ResultSetMetaData mockResultSetMetaData;
		MockControl ctrlResultSet;
		ResultSet mockResultSet;
		MockControl ctrlStatement;
		Statement mockStatement;

		try {
			ctrlResultSetMetaData = MockControl.createControl(ResultSetMetaData.class);
			mockResultSetMetaData = (ResultSetMetaData) ctrlResultSetMetaData.getMock();
			mockResultSetMetaData.getColumnCount();
			ctrlResultSetMetaData.setReturnValue(1);
			
			ctrlResultSet = MockControl.createControl(ResultSet.class);
			mockResultSet = (ResultSet) ctrlResultSet.getMock();
			mockResultSet.getMetaData();
			ctrlResultSet.setReturnValue(mockResultSetMetaData);
			mockResultSet.next();
			ctrlResultSet.setReturnValue(true);
			mockResultSet.getObject(1);
			ctrlResultSet.setReturnValue(new Integer(22));
			mockResultSet.next();
			ctrlResultSet.setReturnValue(false);
			mockResultSet.close();
			ctrlResultSet.setVoidCallable();

			ctrlStatement = MockControl.createControl(Statement.class);
			mockStatement = (Statement) ctrlStatement.getMock();
			mockStatement.executeQuery(sql);
			ctrlStatement.setReturnValue(mockResultSet);
			mockStatement.getWarnings();
			ctrlStatement.setReturnValue(null);
			mockStatement.close();
			ctrlStatement.setVoidCallable();

			mockConnection.createStatement();
			ctrlConnection.setReturnValue(mockStatement);
		}
		catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		ctrlResultSetMetaData.replay();
		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		int i = template.queryForInt(sql);
		assertEquals("Return of an int", 22, i);

		ctrlResultSet.verify();
		ctrlStatement.verify();
	}

	public void testQueryForLong() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = 3";

		MockControl ctrlResultSetMetaData;
		ResultSetMetaData mockResultSetMetaData;
		MockControl ctrlResultSet;
		ResultSet mockResultSet;
		MockControl ctrlStatement;
		Statement mockStatement;

		try {
			ctrlResultSetMetaData = MockControl.createControl(ResultSetMetaData.class);
			mockResultSetMetaData = (ResultSetMetaData) ctrlResultSetMetaData.getMock();
			mockResultSetMetaData.getColumnCount();
			ctrlResultSetMetaData.setReturnValue(1);
			
			ctrlResultSet = MockControl.createControl(ResultSet.class);
			mockResultSet = (ResultSet) ctrlResultSet.getMock();
			mockResultSet.getMetaData();
			ctrlResultSet.setReturnValue(mockResultSetMetaData);
			mockResultSet.next();
			ctrlResultSet.setReturnValue(true);
			mockResultSet.getObject(1);
			ctrlResultSet.setReturnValue(new java.math.BigDecimal(87));
			mockResultSet.next();
			ctrlResultSet.setReturnValue(false);
			mockResultSet.close();
			ctrlResultSet.setVoidCallable();

			ctrlStatement = MockControl.createControl(Statement.class);
			mockStatement = (Statement) ctrlStatement.getMock();
			mockStatement.executeQuery(sql);
			ctrlStatement.setReturnValue(mockResultSet);
			mockStatement.getWarnings();
			ctrlStatement.setReturnValue(null);
			mockStatement.close();
			ctrlStatement.setVoidCallable();

			mockConnection.createStatement();
			ctrlConnection.setReturnValue(mockStatement);
		}
		catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		ctrlResultSetMetaData.replay();
		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		long l = template.queryForLong(sql);
		assertEquals("Return of a long", 87, l);

		ctrlResultSet.verify();
		ctrlStatement.verify();
	}


	private static interface JdbcTemplateCallback {

		void doInJdbcTemplate(JdbcTemplate template, String sql, RowCallbackHandler rch);
	}


	private static class Dispatcher implements PreparedStatementCreator, SqlProvider {

		private int id;
		private String sql;

		public Dispatcher(int id, String sql) {
			this.id = id;
			this.sql = sql;
		}

		public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, id);
			return ps;
		}

		public String getSql() {
			return sql;
		}
	}

}
