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
 * @version $Id: NativeJdbcSupportTests.java,v 1.1 2004-01-30 09:44:34 johnsonr Exp $
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
