package org.springframework.jdbc.object;

import java.sql.*;
import java.util.*;

import javax.sql.DataSource;

import org.easymock.MockControl;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.*;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class StoredProcedureTestSuite extends JdbcTestCase {

	private MockControl ctrlCallable;
	private CallableStatement mockCallable;

	public StoredProcedureTestSuite(String name) {
		super(name);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		ctrlCallable = MockControl.createControl(CallableStatement.class);
		mockCallable = (CallableStatement) ctrlCallable.getMock();
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();

		ctrlCallable.verify();
	}

	/**
	 * @see org.springframework.jdbc.object.JdbcTestCase#replay()
	 */
	protected void replay() {
		super.replay();
		ctrlCallable.replay();
	}

	public void testNoSuchStoredProcedure() throws Exception {
		SQLException sex =
			new SQLException(
				"Syntax error or access violation exception",
				"42000");
		mockCallable.execute();
		ctrlCallable.setThrowable(sex);
		mockCallable.close();
		ctrlCallable.setVoidCallable();

		mockConnection.prepareCall(
			"{call " + NoSuchStoredProcedure.SQL + "()}");
		ctrlConnection.setReturnValue(mockCallable);

		replay();

		NoSuchStoredProcedure sproc = new NoSuchStoredProcedure(mockDataSource);
		try {
			sproc.execute();
			fail("Shouldn't succeed in running stored procedure which doesn't exist");
		} catch (BadSqlGrammarException ex) {
			// OK
		}
	}

	private void testAddInvoice(final int amount, final int custid)
		throws Exception {
		AddInvoice adder = new AddInvoice(mockDataSource);
		int id = adder.execute(amount, custid);
		assertEquals(4, id);
	}

	public void testAddInvoices() throws Exception {
		mockCallable.setObject(1, new Integer(1106), Types.INTEGER);
		ctrlCallable.setVoidCallable();
		mockCallable.setObject(2, new Integer(3), Types.INTEGER);
		ctrlCallable.setVoidCallable();
		mockCallable.registerOutParameter(3, Types.INTEGER);
		ctrlCallable.setVoidCallable();
		mockCallable.execute();
		ctrlCallable.setReturnValue(false);
		mockCallable.getObject(3);
		ctrlCallable.setReturnValue(new Integer(4));
		mockCallable.close();
		ctrlCallable.setVoidCallable();

		mockConnection.prepareCall("{call " + AddInvoice.SQL + "(?, ?, ?)}");
		ctrlConnection.setReturnValue(mockCallable);

		replay();

		testAddInvoice(1106, 3);
	}

	public void testAddInvoicesWithinTransaction() throws Exception {
		mockCallable.setObject(1, new Integer(1106), Types.INTEGER);
		ctrlCallable.setVoidCallable();
		mockCallable.setObject(2, new Integer(3), Types.INTEGER);
		ctrlCallable.setVoidCallable();
		mockCallable.registerOutParameter(3, Types.INTEGER);
		ctrlCallable.setVoidCallable();
		mockCallable.execute();
		ctrlCallable.setReturnValue(false);
		mockCallable.getObject(3);
		ctrlCallable.setReturnValue(new Integer(4));
		mockCallable.close();
		ctrlCallable.setVoidCallable();

		mockConnection.prepareCall("{call " + AddInvoice.SQL + "(?, ?, ?)}");
		ctrlConnection.setReturnValue(mockCallable);

		replay();

		DataSourceUtils.getThreadObjectManager().bindThreadObject(
			mockDataSource,
			new ConnectionHolder(mockConnection));

		try {
			testAddInvoice(1106, 3);
		} finally {
			DataSourceUtils.getThreadObjectManager().removeThreadObject(
				mockDataSource);
		}
	}

	public void testNullArg() throws Exception {
		MockControl ctrlResultSet = MockControl.createControl(ResultSet.class);
		ResultSet mockResultSet = (ResultSet) ctrlResultSet.getMock();
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);

		mockCallable.setNull(1, Types.VARCHAR);
		ctrlCallable.setVoidCallable();
		mockCallable.execute();
		ctrlCallable.setReturnValue(false);
		mockCallable.close();
		ctrlCallable.setVoidCallable();

		mockConnection.prepareCall("{call " + NullArg.SQL + "(?)}");
		ctrlConnection.setReturnValue(mockCallable);

		replay();
		ctrlResultSet.replay();

		NullArg na = new NullArg(mockDataSource);
		na.execute((String) null);
	}

	public void testUnnamedParameter() throws Exception {
		replay();
		try {
			UnnamedParameterStoredProcedure unp =
				new UnnamedParameterStoredProcedure(mockDataSource);
			fail("Shouldn't succeed in creating stored procedure with unnamed parameter");
		} catch (InvalidDataAccessApiUsageException idaauex) {
			// OK
		}
	}

	public void testMissingParameter() throws Exception {
		replay();

		try {
			MissingParameterStoredProcedure mp =
				new MissingParameterStoredProcedure(mockDataSource);
			mp.execute();
			fail("Shouldn't succeed in running stored procedure with missing required parameter");
		} catch (InvalidDataAccessApiUsageException idaauex) {
			// OK
		}
	}

	public void testStoredProcedureExceptionTranslator() throws Exception {
		SQLException sex =
			new SQLException(
				"Syntax error or access violation exception",
				"42000");
		mockCallable.execute();
		ctrlCallable.setThrowable(sex);
		mockCallable.close();
		ctrlCallable.setVoidCallable();

		mockConnection.prepareCall(
			"{call " + StoredProcedureExceptionTranslator.SQL + "()}");
		ctrlConnection.setReturnValue(mockCallable);

		replay();

		StoredProcedureExceptionTranslator sproc =
			new StoredProcedureExceptionTranslator(mockDataSource);
		try {
			sproc.execute();
			fail("Custom exception should be thrown");
		} catch (CustomDataException ex) {
			// OK
		}
	}

	public void testStoredProcedureWithResultSet() throws Exception {
		MockControl ctrlResultSet = MockControl.createControl(ResultSet.class);
		ResultSet mockResultSet = (ResultSet) ctrlResultSet.getMock();
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getString(2);
		ctrlResultSet.setReturnValue("Foo");
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getString(2);
		ctrlResultSet.setReturnValue("Bar");
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockCallable.execute();
		ctrlCallable.setReturnValue(true);
		mockCallable.getResultSet();
		ctrlCallable.setReturnValue(mockResultSet);
		mockCallable.getMoreResults();
		ctrlCallable.setReturnValue(false);
		mockCallable.close();
		ctrlCallable.setVoidCallable();

		mockConnection.prepareCall(
			"{call " + StoredProcedureWithResultSet.SQL + "()}");
		ctrlConnection.setReturnValue(mockCallable);

		replay();
		ctrlResultSet.replay();

		StoredProcedureWithResultSet sproc =
			new StoredProcedureWithResultSet(mockDataSource);
		List res = sproc.execute();

		ctrlResultSet.verify();
		
		assertEquals(2, res.size());
		assertEquals("Foo", res.get(0));
		assertEquals("Bar", res.get(1));
		
	}

	private class AddInvoice extends StoredProcedure {
		public static final String SQL = "add_invoice";
		public AddInvoice(DataSource ds) {
			setDataSource(ds);
			setSql(SQL);
			declareParameter(new SqlParameter("amount", Types.INTEGER));
			declareParameter(new SqlParameter("custid", Types.INTEGER));
			declareParameter(new SqlOutParameter("newid", Types.INTEGER));
			compile();
		}

		public int execute(int amount, int custid) {
			Map in = new HashMap();
			in.put("amount", new Integer(amount));
			in.put("custid", new Integer(custid));
			Map out = execute(in);
			Number id = (Number) out.get("newid");
			return id.intValue();
		}
	}

	private class NullArg extends StoredProcedure {
		public static final String SQL = "takes_null";
		/**
		 * Constructor for AddInvoice.
		 * @param cf
		 * @param name
		 */
		public NullArg(DataSource ds) {
			setDataSource(ds);
			setSql(SQL);
			declareParameter(new SqlParameter("ptest", Types.VARCHAR));
			compile();
		}

		public void execute(String s) {
			Map in = new HashMap();
			in.put("ptest", s);
			Map out = execute(in);
		}
	}

	private class NoSuchStoredProcedure extends StoredProcedure {

		public static final String SQL = "no_sproc_with_this_name";

		public NoSuchStoredProcedure(DataSource ds) {
			setDataSource(ds);
			setSql(SQL);
			compile();
		}

		public void execute() {
			execute(new HashMap());
		}
	}

	private class UncompiledStoredProcedure extends StoredProcedure {
		public static final String SQL = "uncompile_sp";
		public UncompiledStoredProcedure(DataSource ds) {
			super(ds, SQL);
		}

		public void execute() {
			execute(new HashMap());
		}
	}

	private class UnnamedParameterStoredProcedure extends StoredProcedure {

		public UnnamedParameterStoredProcedure(DataSource ds) {
			super(ds, "unnamed_parameter_sp");
			declareParameter(new SqlParameter(Types.INTEGER));
			compile();
		}

		public void execute(int id) {
			Map in = new HashMap();
			in.put("id", new Integer(id));
			Map out = execute(in);

		}
	}

	private class MissingParameterStoredProcedure extends StoredProcedure {

		public MissingParameterStoredProcedure(DataSource ds) {
			setDataSource(ds);
			setSql("takes_string");
			declareParameter(new SqlParameter("mystring", Types.VARCHAR));
			compile();
		}

		public void execute() {
			execute(new HashMap());
		}
	}

	private class StoredProcedureWithResultSet extends StoredProcedure {
		public static final String SQL = "sproc_with_result_set";

		private List results = new LinkedList();

		public StoredProcedureWithResultSet(DataSource ds) {
			setDataSource(ds);
			setSql(SQL);
			declareParameter(
				new SqlReturnResultSet("rs", new RowCallbackHandlerImpl()));
			compile();
		}

		public List execute() {
			Map out = execute(new HashMap());
			return results;
		}

		private class RowCallbackHandlerImpl implements RowCallbackHandler {
			public void processRow(ResultSet rs) throws SQLException {
				results.add(rs.getString(2));
			}
		}

	}

	private class StoredProcedureExceptionTranslator extends StoredProcedure {
		public static final String SQL = "no_sproc_with_this_name";
		public StoredProcedureExceptionTranslator(DataSource ds) {
			setDataSource(ds);
			setSql(SQL);
			setExceptionTranslator(new SQLExceptionTranslator() {
				public DataAccessException translate(
					String task,
					String sql,
					SQLException sqlex) {
					return new CustomDataException(sql, sqlex);
				}

			});
			compile();
		}

		public void execute() {
			execute(new HashMap());
		}
	}

	private class CustomDataException extends DataAccessException {

		public CustomDataException(String s) {
			super(s);
		}

		public CustomDataException(String s, Throwable ex) {
			super(s, ex);
		}
	}

}
