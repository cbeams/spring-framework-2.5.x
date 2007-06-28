package org.springframework.jdbc.simple;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.HashMap;

/**
 * Mock object based tests for SimpleJdbcCall.
 *
 * @author Thomas Risberg
 */
public class SimpleJdbcInsertTests extends TestCase {

	private MockControl ctrlDataSource;
	private DataSource mockDataSource;
	private MockControl ctrlConnection;
	private Connection mockConnection;
	private MockControl ctrlDatabaseMetaData;
	private DatabaseMetaData mockDatabaseMetaData;

	protected void setUp() throws Exception {
		super.setUp();

		ctrlDatabaseMetaData = MockControl.createControl(DatabaseMetaData.class);
		mockDatabaseMetaData = (DatabaseMetaData) ctrlDatabaseMetaData.getMock();

		ctrlConnection = MockControl.createControl(Connection.class);
		mockConnection = (Connection) ctrlConnection.getMock();
		mockConnection.getMetaData();
		ctrlConnection.setDefaultReturnValue(mockDatabaseMetaData);
		mockConnection.close();
		ctrlConnection.setDefaultVoidCallable();

		ctrlDataSource = MockControl.createControl(DataSource.class);
		mockDataSource = (DataSource) ctrlDataSource.getMock();
		mockDataSource.getConnection();
		ctrlDataSource.setDefaultReturnValue(mockConnection);

	}

	protected void tearDown() throws Exception {
		super.tearDown();
		ctrlDatabaseMetaData.verify();
		ctrlDataSource.verify();
	}

	protected void replay() {
		ctrlDatabaseMetaData.replay();
		ctrlConnection.replay();
		ctrlDataSource.replay();
	}

	public void testNoSuchTable() throws Exception {
		final String NO_SUCH_TABLE = "x";
		final String USER = "me";

		MockControl ctrlResultSet = MockControl.createControl(ResultSet.class);
		ResultSet mockResultSet = (ResultSet) ctrlResultSet.getMock();
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();
		
//		mockDatabaseMetaData.getDatabaseProductName();
//		ctrlDatabaseMetaData.setReturnValue("MyDB");
		mockDatabaseMetaData.getDatabaseProductName();
		ctrlDatabaseMetaData.setReturnValue("MyDB");
		mockDatabaseMetaData.getUserName();
		ctrlDatabaseMetaData.setReturnValue(USER);
		mockDatabaseMetaData.storesUpperCaseIdentifiers();
		ctrlDatabaseMetaData.setReturnValue(false);
		mockDatabaseMetaData.storesLowerCaseIdentifiers();
		ctrlDatabaseMetaData.setReturnValue(true);
		mockDatabaseMetaData.getTables(null, null, NO_SUCH_TABLE, null);
		ctrlDatabaseMetaData.setReturnValue(mockResultSet);

		ctrlResultSet.replay();
		replay();

		SimpleJdbcInsert insert = new SimpleJdbcInsert(mockDataSource).withTableName(NO_SUCH_TABLE);
		try {
			insert.execute(new HashMap());
			fail("Shouldn't succeed in running stored procedure which doesn't exist");
		} catch (DataAccessResourceFailureException ex) {
			// OK
		}
	}

	public void testInsert() throws Exception {
		replay();
	}
}
