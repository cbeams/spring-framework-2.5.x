/*
 * Created on 17-Feb-2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.springframework.jdbc.object;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.easymock.MockControl;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.JdbcTestCase;

/**
 * @author tcook
 */
public class SqlFunctionTestSuite extends JdbcTestCase {

	private static final String FUNCTION = "select count(id) from mytable";
	private static final String FUNCTION_INT =
		"select count(id) from mytable where myparam = ?";
	private static final String FUNCTION_MIXED =
		"select count(id) from mytable where myparam = ? and mystring = ?";

	private MockControl ctrlPreparedStatement;
	private PreparedStatement mockPreparedStatement;
	private MockControl ctrlResultSet;
	private ResultSet mockResultSet;

	public SqlFunctionTestSuite(String name) {
		super(name);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		ctrlPreparedStatement =
			MockControl.createControl(PreparedStatement.class);
		mockPreparedStatement =
			(PreparedStatement) ctrlPreparedStatement.getMock();
		ctrlResultSet = MockControl.createControl(ResultSet.class);
		mockResultSet = (ResultSet) ctrlResultSet.getMock();
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();

		ctrlPreparedStatement.verify();
		ctrlResultSet.verify();
	}

	/**
	 * @see org.springframework.jdbc.object.JdbcTestCase#replay()
	 */
	protected void replay() {
		super.replay();
		ctrlPreparedStatement.replay();
		ctrlResultSet.replay();
	}

	public void testFunction() {
		try {
			mockResultSet.next();
			ctrlResultSet.setReturnValue(true);
			mockResultSet.getObject(1);
			ctrlResultSet.setReturnValue(new Integer(14));
			mockResultSet.next();
			ctrlResultSet.setReturnValue(false);
			mockResultSet.close();
			ctrlResultSet.setVoidCallable();

			mockPreparedStatement.executeQuery();
			ctrlPreparedStatement.setReturnValue(mockResultSet);
			mockPreparedStatement.getWarnings();
			ctrlPreparedStatement.setReturnValue(null);
			mockPreparedStatement.close();
			ctrlPreparedStatement.setVoidCallable();

			mockConnection.prepareStatement(FUNCTION);
			ctrlConnection.setReturnValue(mockPreparedStatement);
		} catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		replay();

		SqlFunction function = new SqlFunction();
		function.setDataSource(mockDataSource);
		function.setSql(FUNCTION);
		function.compile();

		int count = function.run();
		assertTrue("Function returned value 14", count == 14);
	}

	public void testTooManyRows() {
		try {
			mockResultSet.next();
			ctrlResultSet.setReturnValue(true);
			mockResultSet.getInt(1);
			ctrlResultSet.setReturnValue(14);
			mockResultSet.next();
			ctrlResultSet.setReturnValue(true);
			mockResultSet.close();
			ctrlResultSet.setVoidCallable();

			mockPreparedStatement.executeQuery();
			ctrlPreparedStatement.setReturnValue(mockResultSet);
			mockPreparedStatement.close();
			ctrlPreparedStatement.setVoidCallable();

			mockConnection.prepareStatement(FUNCTION);
			ctrlConnection.setReturnValue(mockPreparedStatement);
		} catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		replay();

		SqlFunction function = new SqlFunction(mockDataSource, FUNCTION);
		function.compile();

		try {
			int count = function.run();
			fail("Shouldn't continue when too many rows returned");
		} catch (InvalidDataAccessApiUsageException idaauex) {
			// OK 
		}
	}

	public void testFunctionInt() {
		try {
			mockResultSet.next();
			ctrlResultSet.setReturnValue(true);
			mockResultSet.getInt(1);
			ctrlResultSet.setReturnValue(14);
			mockResultSet.next();
			ctrlResultSet.setReturnValue(false);
			mockResultSet.close();
			ctrlResultSet.setVoidCallable();

			mockPreparedStatement.setObject(1, new Integer(1), Types.INTEGER);
			ctrlPreparedStatement.setVoidCallable();
			mockPreparedStatement.executeQuery();
			ctrlPreparedStatement.setReturnValue(mockResultSet);
			mockPreparedStatement.getWarnings();
			ctrlPreparedStatement.setReturnValue(null);
			mockPreparedStatement.close();
			ctrlPreparedStatement.setVoidCallable();

			mockConnection.prepareStatement(FUNCTION_INT);
			ctrlConnection.setReturnValue(mockPreparedStatement);
		} catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		replay();

		SqlFunction function =
			new SqlFunction(
				mockDataSource,
				FUNCTION_INT,
				new int[] { Types.INTEGER });
		function.compile();

		int count = function.run(1);
		assertTrue("Function returned value 14", count == 14);
	}

	public void testFunctionMixed() {
		try {
			mockResultSet.next();
			ctrlResultSet.setReturnValue(true);
			mockResultSet.getInt(1);
			ctrlResultSet.setReturnValue(14);
			mockResultSet.next();
			ctrlResultSet.setReturnValue(false);
			mockResultSet.close();
			ctrlResultSet.setVoidCallable();

			mockPreparedStatement.setObject(1, new Integer(1), Types.INTEGER);
			ctrlPreparedStatement.setVoidCallable();
			mockPreparedStatement.setString(2, "rod");
			ctrlPreparedStatement.setVoidCallable();
			mockPreparedStatement.executeQuery();
			ctrlPreparedStatement.setReturnValue(mockResultSet);
			mockPreparedStatement.getWarnings();
			ctrlPreparedStatement.setReturnValue(null);
			mockPreparedStatement.close();
			ctrlPreparedStatement.setVoidCallable();

			mockConnection.prepareStatement(FUNCTION_MIXED);
			ctrlConnection.setReturnValue(mockPreparedStatement);
		} catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		replay();

		SqlFunction function =
			new SqlFunction(
				mockDataSource,
				FUNCTION_MIXED,
				new int[] { Types.INTEGER, Types.VARCHAR });
		function.compile();

		int count = function.run(new Object[] { new Integer(1), "rod" });
		assertTrue("Function returned value 14", count == 14);
	}

}
