package org.springframework.jdbc.object;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.BadSqlGrammarException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.SQLExceptionTranslator;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.mock.SpringMockCallableStatement;
import org.springframework.jdbc.mock.SpringMockConnection;
import org.springframework.jdbc.mock.SpringMockDataSource;
import org.springframework.jdbc.mock.SpringMockJdbcFactory;

import com.mockobjects.sql.MockResultSet;

public class StoredProcedureTestSuite extends TestCase {

	//private String sqlBase = "SELECT seat_id, name FROM SEAT WHERE seat_id = ";

	private SpringMockDataSource mockDataSource;
	private SpringMockConnection mockConnection;
	private SpringMockCallableStatement mockCallable;
	private MockResultSet[] mockResultSet;

	public StoredProcedureTestSuite(String name) {
		super(name);
	}

	public void setUp() throws Exception {
		mockDataSource = SpringMockJdbcFactory.dataSource();
		mockConnection =
			SpringMockJdbcFactory.connection(false, mockDataSource);
	}

	protected void tearDown() throws Exception {
		mockDataSource.verify();
		mockConnection.verify();
		if (mockCallable != null) {
			mockCallable.verify();
		}
	}

	public void testNoSuchStoredProcedure() throws Exception {
		mockCallable = new SpringMockCallableStatement();
		mockConnection.addExpectedCallableStatement(mockCallable);
		mockDataSource.setExpectedConnectCalls(2);

		SQLException sex =
			new SQLException(
				"Syntax error or access violation exception",
				"42000");
		mockCallable.setupThrowExceptionOnExecute(sex);
		mockCallable.setExpectedExecuteCalls(1);

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
		//System.out.println("New row is " + id);
		assertTrue("Received correct new row id", id == 4);
	}

	public void testAddInvoices() throws Exception {
		mockCallable = new SpringMockCallableStatement();
		mockConnection.addExpectedCallableStatement(mockCallable);
		mockCallable.setExpectedExecuteCalls(1);
		mockDataSource.setExpectedConnectCalls(2);
		testAddInvoice(1106, 3);
	}

	public void testAddInvoicesWithinTransaction() throws Exception {
		mockCallable = new SpringMockCallableStatement();
		mockConnection.addExpectedCallableStatement(mockCallable);
		mockCallable.setExpectedExecuteCalls(1);
		mockDataSource.setExpectedConnectCalls(0);
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
		mockCallable = new SpringMockCallableStatement();
		mockConnection.addExpectedCallableStatement(mockCallable);
		mockCallable.setExpectedExecuteCalls(1);
		mockDataSource.setExpectedConnectCalls(2);

		NullArg na = new NullArg(mockDataSource);
		na.execute((String) null);
	}

	/*
		public void testUncompiled() throws Exception {
			UncompiledStoredProcedure uc = new UncompiledStoredProcedure(mockDataSource);
			try {
				uc.execute();
				fail("Shouldn't succeed in executing uncompiled stored procedure");
			} catch (InvalidDataAccessApiUsageException idaauex) {
				// OK
			}
		}
	*/

	public void testUnnamedParameter() throws Exception {
		try {
			UnnamedParameterStoredProcedure unp =
				new UnnamedParameterStoredProcedure(mockDataSource);
			fail("Shouldn't succeed in creating stored procedure with unnamed parameter");
		} catch (InvalidDataAccessApiUsageException idaauex) {
			// OK
		}
	}

	public void testMissingParameter() throws Exception {
		mockCallable = new SpringMockCallableStatement();
		//mockConnection.addExpectedCallableStatement(mockCallable);
		mockCallable.setExpectedExecuteCalls(0);
		mockDataSource.setExpectedConnectCalls(1);

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
		mockDataSource.setExpectedConnectCalls(1);
		mockCallable = new SpringMockCallableStatement();
		mockConnection.addExpectedCallableStatement(mockCallable);

		SQLException sex =
			new SQLException(
				"Syntax error or access violation exception",
				"42000");
		mockCallable.setupThrowExceptionOnExecute(sex);
		mockCallable.setExpectedExecuteCalls(1);

		StoredProcedureExceptionTranslator sproc =
			new StoredProcedureExceptionTranslator(mockDataSource);
		try {
			sproc.execute();
			fail("Custom exception should be thrown");
		} catch (CustomDataException ex) {
			//System.out.println("CAUGHT:" + ex);
			// OK
		}
	}

	public void testStoredProcedureWithResultSet() throws Exception {
		mockCallable = new SpringMockCallableStatement();
		//MockResultSet mockResultSet = SpringMockJdbcFactory.resultSet(new Object[][] { { new Integer(1), "Bubba" } },  new String[] {"ID", "NAME"}, mockCallable);		
		mockConnection.addExpectedCallableStatement(mockCallable);
		mockDataSource.setExpectedConnectCalls(2);
		mockCallable.setExpectedExecuteCalls(1);
		//mockCallable.addResultSet(mockResultSet);

		//TODO: Add real test - need to find a way to return a reult set using mock objects
		StoredProcedureWithResultSet sproc = new StoredProcedureWithResultSet(mockDataSource);
		sproc.execute();
	}


	private class AddInvoice extends StoredProcedure {

		public AddInvoice(DataSource ds) {
			setDataSource(ds);
			setSql("add_invoice");
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

		/**
		 * Constructor for AddInvoice.
		 * @param cf
		 * @param name
		 */
		public NullArg(DataSource ds) {
			setDataSource(ds);
			setSql("takes_null");
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

		public NoSuchStoredProcedure(DataSource ds) {
			setDataSource(ds);
			setSql("no_sproc_with_this_name");
			compile();
		}

		public void execute() {
			execute(new HashMap());
		}
	}

	private class UncompiledStoredProcedure extends StoredProcedure {

		public UncompiledStoredProcedure(DataSource ds) {
			super(ds, "uncompile_sp");
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

		private List results = new LinkedList();

		public StoredProcedureWithResultSet(DataSource ds) {
			setDataSource(ds);
			setSql("sproc_with_result_set");
			declareParameter(new SqlReturnResultSet("rs", new RowCallbackHandlerImpl()));
			compile();
		}

		public List execute() {
			Map out = execute(new HashMap());
			return results;
		}

		private class RowCallbackHandlerImpl implements RowCallbackHandler {
			public void processRow(ResultSet rs)  throws SQLException {
				System.out.println("OK:" +rs.getString(1) + " " +rs.getString(2));
				results.add(rs.getString(2));
			}
		}

	}

	private class StoredProcedureExceptionTranslator extends StoredProcedure {

		public StoredProcedureExceptionTranslator(DataSource ds) {
			setDataSource(ds);
			setSql("no_sproc_with_this_name");
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
