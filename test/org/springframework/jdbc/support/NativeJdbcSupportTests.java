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

package org.springframework.jdbc.support;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.jdbc.support.nativejdbc.CommonsDbcpNativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.JBossNativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.SimpleNativeJdbcExtractor;

import com.mockobjects.sql.MockConnection;
import com.mockobjects.sql.MockPreparedStatement;
import com.mockobjects.sql.MockStatement;

/**
 * @author Andre Biryukov
 * @version $Id: NativeJdbcSupportTests.java,v 1.2 2004-03-18 03:01:15 trisberg Exp $
 */
public class NativeJdbcSupportTests extends TestCase {

	public void testSimpleNativeJdbcExtractor() throws SQLException {
		SimpleNativeJdbcExtractor extractor = new SimpleNativeJdbcExtractor();
		extractor.setNativeConnectionNecessaryForNativeStatements(true);
		assertTrue(extractor.isNativeConnectionNecessaryForNativeStatements());

		MockConnection mockConnection = new MockConnection();
		MockStatement mockStatement = new MockStatement();
		mockConnection.setupStatement(mockStatement);
		Connection nativeCon = extractor.getNativeConnection(mockConnection);
		assertEquals(nativeCon, mockConnection.createStatement().getConnection());

		Statement nativeStmt = extractor.getNativeStatement(mockStatement);
		assertEquals(nativeStmt, mockStatement);

		nativeCon = extractor.getNativeConnectionFromStatement(mockStatement);
		assertEquals(mockStatement.getConnection(), nativeCon);

		PreparedStatement mockPrepStmt = new MockPreparedStatement();
		PreparedStatement nativePrepStmt = extractor.getNativePreparedStatement(mockPrepStmt);
		assertEquals(mockPrepStmt, nativePrepStmt);

		CallableStatement mockCallableStmt =
			(CallableStatement) MockControl.createControl(CallableStatement.class).getMock();
		CallableStatement nativeCallableStmt = extractor.getNativeCallableStatement(mockCallableStmt);
		assertEquals(mockCallableStmt, nativeCallableStmt);

		ResultSet mockResultSet = mockCallableStmt.getResultSet();
		ResultSet nativeResultSet = extractor.getNativeResultSet(mockResultSet);
		assertEquals(mockResultSet, nativeResultSet);
	}

	public void testJBossNativeJdbcExtractor() throws SQLException {
		JBossNativeJdbcExtractor extractor = new JBossNativeJdbcExtractor();
		runCommonTests(extractor);
	}

	public void testCommonsDbcpNativeJdbcExtractor() throws SQLException {
		CommonsDbcpNativeJdbcExtractor extractor = new CommonsDbcpNativeJdbcExtractor();
		runCommonTests(extractor);
	}

	private void runCommonTests(NativeJdbcExtractor extractor) throws SQLException {
		assertFalse(extractor.isNativeConnectionNecessaryForNativeStatements());

		MockConnection mockConnection = new MockConnection();
		MockStatement mockStatement = new MockStatement();
		mockConnection.setupStatement(mockStatement);

		Connection nativeConnection = extractor.getNativeConnection(mockConnection);
		assertEquals(mockConnection, nativeConnection);

		nativeConnection = extractor.getNativeConnectionFromStatement(mockStatement);
		assertEquals(mockStatement.getConnection(), nativeConnection);

		assertEquals(mockStatement, extractor.getNativeStatement(mockStatement));

		PreparedStatement mockPrepStmt = new MockPreparedStatement();
		assertEquals(mockPrepStmt, extractor.getNativePreparedStatement(mockPrepStmt));

		CallableStatement mockCallableStmt =
			(CallableStatement) MockControl.createControl(CallableStatement.class).getMock();
		assertEquals(mockCallableStmt, extractor.getNativeCallableStatement(mockCallableStmt));

		ResultSet mockResultSet = mockCallableStmt.getResultSet();
		assertEquals(mockResultSet, extractor.getNativeResultSet(mockResultSet));
	}
}
