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

/**
 * @author Andre Biryukov
 * @version $Id: NativeJdbcSupportTests.java,v 1.4 2004-04-30 09:19:34 jhoeller Exp $
 */
public class NativeJdbcSupportTests extends TestCase {

	public void testSimpleNativeJdbcExtractor() throws SQLException {
		SimpleNativeJdbcExtractor extractor = new SimpleNativeJdbcExtractor();

		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl stmtControl = MockControl.createControl(Statement.class);
		Statement stmt = (Statement) stmtControl.getMock();
		con.createStatement();
		conControl.setReturnValue(stmt);
		stmt.getConnection();
		stmtControl.setReturnValue(con, 2);
		stmt.close();
		stmtControl.setVoidCallable();
		conControl.replay();
		stmtControl.replay();

		Connection nativeCon = extractor.getNativeConnection(con);
		assertEquals(nativeCon, con);

		Statement nativeStmt = extractor.getNativeStatement(stmt);
		assertEquals(nativeStmt, stmt);

		nativeCon = extractor.getNativeConnectionFromStatement(stmt);
		assertEquals(con, nativeCon);

		MockControl psControl = MockControl.createControl(PreparedStatement.class);
		PreparedStatement ps = (PreparedStatement) psControl.getMock();
		psControl.replay();

		PreparedStatement nativePrepStmt = extractor.getNativePreparedStatement(ps);
		assertEquals(ps, nativePrepStmt);

		CallableStatement mockCallableStmt =
			(CallableStatement) MockControl.createControl(CallableStatement.class).getMock();
		CallableStatement nativeCallableStmt = extractor.getNativeCallableStatement(mockCallableStmt);
		assertEquals(mockCallableStmt, nativeCallableStmt);

		ResultSet mockResultSet = mockCallableStmt.getResultSet();
		ResultSet nativeResultSet = extractor.getNativeResultSet(mockResultSet);
		assertEquals(mockResultSet, nativeResultSet);

		conControl.verify();
		stmtControl.verify();
		psControl.verify();
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

		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl stmtControl = MockControl.createControl(Statement.class);
		Statement stmt = (Statement) stmtControl.getMock();
		stmt.getConnection();
		stmtControl.setReturnValue(con);
		conControl.replay();
		stmtControl.replay();

		Connection nativeConnection = extractor.getNativeConnection(con);
		assertEquals(con, nativeConnection);

		nativeConnection = extractor.getNativeConnectionFromStatement(stmt);
		assertEquals(con, nativeConnection);
		assertEquals(stmt, extractor.getNativeStatement(stmt));

		MockControl psControl = MockControl.createControl(PreparedStatement.class);
		PreparedStatement ps = (PreparedStatement) psControl.getMock();
		psControl.replay();

		assertEquals(ps, extractor.getNativePreparedStatement(ps));

		CallableStatement mockCallableStmt =
			(CallableStatement) MockControl.createControl(CallableStatement.class).getMock();
		assertEquals(mockCallableStmt, extractor.getNativeCallableStatement(mockCallableStmt));

		ResultSet mockResultSet = mockCallableStmt.getResultSet();
		assertEquals(mockResultSet, extractor.getNativeResultSet(mockResultSet));

		conControl.verify();
		stmtControl.verify();
		psControl.verify();
	}

}
