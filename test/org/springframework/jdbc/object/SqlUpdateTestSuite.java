package org.springframework.jdbc.object;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.easymock.MockControl;
import org.springframework.jdbc.*;
import org
	.springframework
	.jdbc
	.core
	.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.core.SqlParameter;

public class SqlUpdateTestSuite extends JdbcTestCase {

	private static final String UPDATE =
		"update seat_status set booking_id = null";
	private static final String UPDATE_INT =
		"update seat_status set booking_id = null where performance_id = ?";
	private static final String UPDATE_INT_INT =
		"update seat_status set booking_id = null where performance_id = ? and price_band_id = ?";
	private static final String UPDATE_STRING =
		"update seat_status set booking_id = null where name = ?";
	private static final String UPDATE_OBJECTS =
		"update seat_status set booking_id = null where performance_id = ? and price_band_id = ? and name = ? and confirmed = ?";

	private MockControl ctrlPreparedStatement;
	private PreparedStatement mockPreparedStatement;

	public SqlUpdateTestSuite(String name) {
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
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();

		ctrlPreparedStatement.verify();
	}

	/**
	 * @see org.springframework.jdbc.object.JdbcTestCase#replay()
	 */
	protected void replay() {
		super.replay();
		ctrlPreparedStatement.replay();
	}

	public void testUpdate() {
		try {
			mockPreparedStatement.executeUpdate();
			ctrlPreparedStatement.setReturnValue(1);
			mockPreparedStatement.close();
			ctrlPreparedStatement.setVoidCallable();

			mockConnection.prepareStatement(UPDATE);
			ctrlConnection.setReturnValue(mockPreparedStatement);
		} catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		replay();

		Updater pc = new Updater();
		int rowsAffected = pc.run();
		assertEquals(1, rowsAffected);
	}

	public void testUpdateInt() {
		try {
			mockPreparedStatement.setObject(1, new Integer(1), Types.NUMERIC);
			ctrlPreparedStatement.setVoidCallable();
			mockPreparedStatement.executeUpdate();
			ctrlPreparedStatement.setReturnValue(1);
			mockPreparedStatement.close();
			ctrlPreparedStatement.setVoidCallable();

			mockConnection.prepareStatement(UPDATE_INT);
			ctrlConnection.setReturnValue(mockPreparedStatement);
		} catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		replay();

		IntUpdater pc = new IntUpdater();
		int rowsAffected = pc.run(1);
		assertEquals(1, rowsAffected);
	}

	public void testUpdateIntInt() {
		try {
			mockPreparedStatement.setObject(1, new Integer(1), Types.NUMERIC);
			mockPreparedStatement.setObject(2, new Integer(1), Types.NUMERIC);
			ctrlPreparedStatement.setVoidCallable();
			mockPreparedStatement.executeUpdate();
			ctrlPreparedStatement.setReturnValue(1);
			mockPreparedStatement.close();
			ctrlPreparedStatement.setVoidCallable();

			mockConnection.prepareStatement(UPDATE_INT_INT);
			ctrlConnection.setReturnValue(mockPreparedStatement);
		} catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		replay();

		IntIntUpdater pc = new IntIntUpdater();
		int rowsAffected = pc.run(1, 1);
		assertEquals(1, rowsAffected);
	}

	public void testUpdateString() {
		try {
			mockPreparedStatement.setString(1, "rod");
			ctrlPreparedStatement.setVoidCallable();
			mockPreparedStatement.executeUpdate();
			ctrlPreparedStatement.setReturnValue(1);
			mockPreparedStatement.close();
			ctrlPreparedStatement.setVoidCallable();

			mockConnection.prepareStatement(UPDATE_STRING);
			ctrlConnection.setReturnValue(mockPreparedStatement);
		} catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		replay();

		StringUpdater pc = new StringUpdater();
		int rowsAffected = pc.run("rod");
		assertEquals(1, rowsAffected);
	}

	public void testUpdateMixed() {
		try {
			mockPreparedStatement.setObject(1, new Integer(1), Types.NUMERIC);
			mockPreparedStatement.setObject(2, new Integer(1), Types.NUMERIC);
			mockPreparedStatement.setString(3, "rod");
			mockPreparedStatement.setObject(4, Boolean.TRUE, Types.BOOLEAN);
			ctrlPreparedStatement.setVoidCallable();
			mockPreparedStatement.executeUpdate();
			ctrlPreparedStatement.setReturnValue(1);
			mockPreparedStatement.close();
			ctrlPreparedStatement.setVoidCallable();

			mockConnection.prepareStatement(UPDATE_OBJECTS);
			ctrlConnection.setReturnValue(mockPreparedStatement);
		} catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		replay();

		MixedUpdater pc = new MixedUpdater();
		int rowsAffected = pc.run(1, 1, "rod", true);
		assertEquals(1, rowsAffected);
	}

	public void testUpdateConstructor() {
		try {
			mockPreparedStatement.setObject(1, new Integer(1), Types.NUMERIC);
			mockPreparedStatement.setObject(2, new Integer(1), Types.NUMERIC);
			mockPreparedStatement.setString(3, "rod");
			mockPreparedStatement.setObject(4, Boolean.TRUE, Types.BOOLEAN);
			ctrlPreparedStatement.setVoidCallable();
			mockPreparedStatement.executeUpdate();
			ctrlPreparedStatement.setReturnValue(1);
			mockPreparedStatement.close();
			ctrlPreparedStatement.setVoidCallable();

			mockConnection.prepareStatement(UPDATE_OBJECTS);
			ctrlConnection.setReturnValue(mockPreparedStatement);
		} catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		replay();

		ConstructorUpdater pc = new ConstructorUpdater();
		int rowsAffected = pc.run(1, 1, "rod", true);
		assertEquals(1, rowsAffected);
	}

	public void testUnderMaxRows() {
		try {
			mockPreparedStatement.executeUpdate();
			ctrlPreparedStatement.setReturnValue(3);
			mockPreparedStatement.close();
			ctrlPreparedStatement.setVoidCallable();

			mockConnection.prepareStatement(UPDATE);
			ctrlConnection.setReturnValue(mockPreparedStatement);
		} catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		replay();

		MaxRowsUpdater pc = new MaxRowsUpdater();
		int rowsAffected = pc.run();
		assertEquals(3, rowsAffected);
	}

	public void testMaxRows() {
		try {
			mockPreparedStatement.executeUpdate();
			ctrlPreparedStatement.setReturnValue(5);
			mockPreparedStatement.close();
			ctrlPreparedStatement.setVoidCallable();

			mockConnection.prepareStatement(UPDATE);
			ctrlConnection.setReturnValue(mockPreparedStatement);
		} catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		replay();

		MaxRowsUpdater pc = new MaxRowsUpdater();
		int rowsAffected = pc.run();
		assertEquals(5, rowsAffected);
	}

	public void testOverMaxRows() {
		try {
			mockPreparedStatement.executeUpdate();
			ctrlPreparedStatement.setReturnValue(8);
			mockPreparedStatement.close();
			ctrlPreparedStatement.setVoidCallable();

			mockConnection.prepareStatement(UPDATE);
			ctrlConnection.setReturnValue(mockPreparedStatement);
		} catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		replay();

		MaxRowsUpdater pc = new MaxRowsUpdater();
		try {
			int rowsAffected = pc.run();
			fail("Shouldn't continue when too many rows affected");
		} catch (JdbcUpdateAffectedIncorrectNumberOfRowsException juaicrex) {
			// OK
		}
	}

	public void testRequiredRows() {
		try {
			mockPreparedStatement.executeUpdate();
			ctrlPreparedStatement.setReturnValue(3);
			mockPreparedStatement.close();
			ctrlPreparedStatement.setVoidCallable();

			mockConnection.prepareStatement(UPDATE);
			ctrlConnection.setReturnValue(mockPreparedStatement);
		} catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		replay();

		RequiredRowsUpdater pc = new RequiredRowsUpdater();
		int rowsAffected = pc.run();
		assertEquals(3, rowsAffected);
	}

	public void testNotRequiredRows() {
		try {
			mockPreparedStatement.executeUpdate();
			ctrlPreparedStatement.setReturnValue(2);
			mockPreparedStatement.close();
			ctrlPreparedStatement.setVoidCallable();

			mockConnection.prepareStatement(UPDATE);
			ctrlConnection.setReturnValue(mockPreparedStatement);
		} catch (SQLException sex) {
			throw new RuntimeException("EasyMock initialization of jdbc objects failed");
		}

		replay();

		RequiredRowsUpdater pc = new RequiredRowsUpdater();
		try {
			int rowsAffected = pc.run();
			fail("Shouldn't continue when too many rows affected");
		} catch (JdbcUpdateAffectedIncorrectNumberOfRowsException juaicrex) {
			// OK
		}
	}

	class Updater extends SqlUpdate {
		public Updater() {
			setSql(UPDATE);
			setDataSource(mockDataSource);
			compile();
		}

		public int run() {
			return update();
		}
	}

	class IntUpdater extends SqlUpdate {
		public IntUpdater() {
			setSql(UPDATE_INT);
			setDataSource(mockDataSource);
			declareParameter(new SqlParameter(Types.NUMERIC));
			compile();
		}

		public int run(int performanceId) {
			return update(performanceId);
		}
	}

	class IntIntUpdater extends SqlUpdate {
		public IntIntUpdater() {
			setSql(UPDATE_INT_INT);
			setDataSource(mockDataSource);
			declareParameter(new SqlParameter(Types.NUMERIC));
			declareParameter(new SqlParameter(Types.NUMERIC));
			compile();
		}

		public int run(int performanceId, int type) {
			return update(performanceId, type);
		}
	}

	class StringUpdater extends SqlUpdate {
		public StringUpdater() {
			setSql(UPDATE_STRING);
			setDataSource(mockDataSource);
			declareParameter(new SqlParameter(Types.VARCHAR));
			compile();
		}

		public int run(String name) {
			return update(name);
		}
	}

	class MixedUpdater extends SqlUpdate {
		public MixedUpdater() {
			setSql(UPDATE_OBJECTS);
			setDataSource(mockDataSource);
			declareParameter(new SqlParameter(Types.NUMERIC));
			declareParameter(new SqlParameter(Types.NUMERIC));
			declareParameter(new SqlParameter(Types.VARCHAR));
			declareParameter(new SqlParameter(Types.BOOLEAN));
			compile();
		}

		public int run(
			int performanceId,
			int type,
			String name,
			boolean confirmed) {
			Object[] params =
				new Object[] {
					new Integer(performanceId),
					new Integer(type),
					name,
					new Boolean(confirmed)};
			return update(params);
		}
	}

	class ConstructorUpdater extends SqlUpdate {
		public ConstructorUpdater() {
			super(
				mockDataSource,
				UPDATE_OBJECTS,
				new int[] {
					Types.NUMERIC,
					Types.NUMERIC,
					Types.VARCHAR,
					Types.BOOLEAN });
			compile();
		}

		public int run(
			int performanceId,
			int type,
			String name,
			boolean confirmed) {
			Object[] params =
				new Object[] {
					new Integer(performanceId),
					new Integer(type),
					name,
					new Boolean(confirmed)};
			return update(params);
		}
	}

	class MaxRowsUpdater extends SqlUpdate {
		public MaxRowsUpdater() {
			setSql(UPDATE);
			setDataSource(mockDataSource);
			setMaxRowsAffected(5);
			compile();
		}

		public int run() {
			return update();
		}
	}

	class RequiredRowsUpdater extends SqlUpdate {
		public RequiredRowsUpdater() {
			setSql(UPDATE);
			setDataSource(mockDataSource);
			setRequiredRowsAffected(3);
			compile();
		}

		public int run() {
			return update();
		}
	}

}
