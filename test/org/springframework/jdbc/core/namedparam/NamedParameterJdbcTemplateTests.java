/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.jdbc.core.namedparam;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.easymock.MockControl;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.AbstractJdbcTests;
import org.springframework.jdbc.Customer;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.AssertThrows;

/**
 * @author Rick Evans
 * @author Juergen Hoeller
 */
public class NamedParameterJdbcTemplateTests extends AbstractJdbcTests {

	private static final String SELECT_NAMED_PARAMETERS =
		"select id, forename from custmr where id = :id and country = :country";
	private static final String SELECT_NAMED_PARAMETERS_PARSED =
		"select id, forename from custmr where id = ? and country = ?";

	private static final String UPDATE_NAMED_PARAMETERS =
		"update seat_status set booking_id = null where performance_id = :perfId and price_band_id = :priceId";
	private static final String UPDATE_NAMED_PARAMETERS_PARSED =
		"update seat_status set booking_id = null where performance_id = ? and price_band_id = ?";

	private static final String[] COLUMN_NAMES = new String[] {"id", "forename"};

	private final boolean debugEnabled = LogFactory.getLog(JdbcTemplate.class).isDebugEnabled();

	private MockControl ctrlPreparedStatement;
	private PreparedStatement mockPreparedStatement;
	private MockControl ctrlResultSet;
	private ResultSet mockResultSet;

	protected void setUp() throws Exception {
		super.setUp();
		ctrlPreparedStatement =	MockControl.createControl(PreparedStatement.class);
		mockPreparedStatement =	(PreparedStatement) ctrlPreparedStatement.getMock();
		ctrlResultSet = MockControl.createControl(ResultSet.class);
		mockResultSet = (ResultSet) ctrlResultSet.getMock();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (shouldVerify()) {
			ctrlPreparedStatement.verify();
			ctrlResultSet.verify();
		}
	}

	protected void replay() {
		super.replay();
		ctrlPreparedStatement.replay();
		ctrlResultSet.replay();
	}


	public void testNullDataSourceProvidedToCtor() throws Exception {
		new AssertThrows(IllegalArgumentException.class) {
			public void test() throws Exception {
				new NamedParameterJdbcTemplate((DataSource) null);
			}
		}.runTest();
	}

	public void testNullJdbcTemplateProvidedToCtor() throws Exception {
		new AssertThrows(IllegalArgumentException.class) {
			public void test() throws Exception {
				new NamedParameterJdbcTemplate((JdbcOperations) null);
			}
		}.runTest();
	}

	public void testExecute() throws SQLException {
		mockPreparedStatement.setObject(1, new Integer(1));
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.setObject(2, new Integer(1));
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeUpdate();
		ctrlPreparedStatement.setReturnValue(1);
		if (debugEnabled) {
			mockPreparedStatement.getWarnings();
			ctrlPreparedStatement.setReturnValue(null);
		}
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(UPDATE_NAMED_PARAMETERS_PARSED);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		NamedParameterJdbcTemplate jt = new NamedParameterJdbcTemplate(mockDataSource);
		Map params = new HashMap();
		params.put("perfId", new Integer(1));
		params.put("priceId", new Integer(1));
		assertEquals("result", jt.execute(UPDATE_NAMED_PARAMETERS, params, new PreparedStatementCallback() {
			public Object doInPreparedStatement(PreparedStatement ps) throws SQLException {
				assertEquals(mockPreparedStatement, ps);
				ps.executeUpdate();
				return "result";
			}
		}));
	}

	public void testExecuteWithTypedParameters() throws SQLException {
		mockPreparedStatement.setObject(1, new Integer(1), Types.DECIMAL);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.setObject(2, new Integer(1), Types.INTEGER);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeUpdate();
		ctrlPreparedStatement.setReturnValue(1);
		if (debugEnabled) {
			mockPreparedStatement.getWarnings();
			ctrlPreparedStatement.setReturnValue(null);
		}
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(UPDATE_NAMED_PARAMETERS_PARSED);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		NamedParameterJdbcTemplate jt = new NamedParameterJdbcTemplate(mockDataSource);
		Map params = new HashMap();
		params.put("perfId", new SqlParameterValue(Types.DECIMAL, new Integer(1)));
		params.put("priceId", new SqlParameterValue(Types.INTEGER, new Integer(1)));
		assertEquals("result", jt.execute(UPDATE_NAMED_PARAMETERS, params, new PreparedStatementCallback() {
			public Object doInPreparedStatement(PreparedStatement ps) throws SQLException {
				assertEquals(mockPreparedStatement, ps);
				ps.executeUpdate();
				return "result";
			}
		}));
	}

	public void testUpdate() throws SQLException {
		mockPreparedStatement.setObject(1, new Integer(1));
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.setObject(2, new Integer(1));
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeUpdate();
		ctrlPreparedStatement.setReturnValue(1);
		if (debugEnabled) {
			mockPreparedStatement.getWarnings();
			ctrlPreparedStatement.setReturnValue(null);
		}
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(UPDATE_NAMED_PARAMETERS_PARSED);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		NamedParameterJdbcTemplate jt = new NamedParameterJdbcTemplate(mockDataSource);
		Map params = new HashMap();
		params.put("perfId", new Integer(1));
		params.put("priceId", new Integer(1));
		int rowsAffected = jt.update(UPDATE_NAMED_PARAMETERS, params);
		assertEquals(1, rowsAffected);
	}

	public void testUpdateWithTypedParameters() throws SQLException {
		mockPreparedStatement.setObject(1, new Integer(1), Types.DECIMAL);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.setObject(2, new Integer(1), Types.INTEGER);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeUpdate();
		ctrlPreparedStatement.setReturnValue(1);
		if (debugEnabled) {
			mockPreparedStatement.getWarnings();
			ctrlPreparedStatement.setReturnValue(null);
		}
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(UPDATE_NAMED_PARAMETERS_PARSED);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		NamedParameterJdbcTemplate jt = new NamedParameterJdbcTemplate(mockDataSource);
		Map params = new HashMap();
		params.put("perfId", new SqlParameterValue(Types.DECIMAL, new Integer(1)));
		params.put("priceId", new SqlParameterValue(Types.INTEGER, new Integer(1)));
		int rowsAffected = jt.update(UPDATE_NAMED_PARAMETERS, params);
		assertEquals(1, rowsAffected);
	}

	public void testQueryWithResultSetExtractor() throws SQLException {
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getInt("id");
		ctrlResultSet.setReturnValue(1);
		mockResultSet.getString("forename");
		ctrlResultSet.setReturnValue("rod");
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockPreparedStatement.setObject(1, new Integer(1), Types.DECIMAL);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.setString(2, "UK");
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeQuery();
		ctrlPreparedStatement.setReturnValue(mockResultSet);
		if (debugEnabled) {
			mockPreparedStatement.getWarnings();
			ctrlPreparedStatement.setReturnValue(null);
		}
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(SELECT_NAMED_PARAMETERS_PARSED);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		NamedParameterJdbcTemplate jt = new NamedParameterJdbcTemplate(mockDataSource);
		Map params = new HashMap();
		params.put("id", new SqlParameterValue(Types.DECIMAL, new Integer(1)));
		params.put("country", "UK");
		Customer cust = (Customer) jt.query(SELECT_NAMED_PARAMETERS, params, new ResultSetExtractor() {
			public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
				rs.next();
				Customer cust = new Customer();
				cust.setId(rs.getInt(COLUMN_NAMES[0]));
				cust.setForename(rs.getString(COLUMN_NAMES[1]));
				return cust;
			}
		});
		assertTrue("Customer id was assigned correctly", cust.getId() == 1);
		assertTrue("Customer forename was assigned correctly", cust.getForename().equals("rod"));
	}

	public void testQueryWithRowCallbackHandler() throws SQLException {
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getInt("id");
		ctrlResultSet.setReturnValue(1);
		mockResultSet.getString("forename");
		ctrlResultSet.setReturnValue("rod");
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockPreparedStatement.setObject(1, new Integer(1), Types.DECIMAL);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.setString(2, "UK");
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeQuery();
		ctrlPreparedStatement.setReturnValue(mockResultSet);
		if (debugEnabled) {
			mockPreparedStatement.getWarnings();
			ctrlPreparedStatement.setReturnValue(null);
		}
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(SELECT_NAMED_PARAMETERS_PARSED);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		NamedParameterJdbcTemplate jt = new NamedParameterJdbcTemplate(mockDataSource);
		Map params = new HashMap();
		params.put("id", new SqlParameterValue(Types.DECIMAL, new Integer(1)));
		params.put("country", "UK");
		final List customers = new LinkedList();
		jt.query(SELECT_NAMED_PARAMETERS, params, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				Customer cust = new Customer();
				cust.setId(rs.getInt(COLUMN_NAMES[0]));
				cust.setForename(rs.getString(COLUMN_NAMES[1]));
				customers.add(cust);
			}
		});
		assertEquals(1, customers.size());
		Customer cust = (Customer) customers.get(0);
		assertTrue("Customer id was assigned correctly", cust.getId() == 1);
		assertTrue("Customer forename was assigned correctly", cust.getForename().equals("rod"));
	}

	public void testQueryWithRowMapper() throws SQLException {
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getInt("id");
		ctrlResultSet.setReturnValue(1);
		mockResultSet.getString("forename");
		ctrlResultSet.setReturnValue("rod");
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockPreparedStatement.setObject(1, new Integer(1), Types.DECIMAL);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.setString(2, "UK");
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeQuery();
		ctrlPreparedStatement.setReturnValue(mockResultSet);
		if (debugEnabled) {
			mockPreparedStatement.getWarnings();
			ctrlPreparedStatement.setReturnValue(null);
		}
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(SELECT_NAMED_PARAMETERS_PARSED);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		NamedParameterJdbcTemplate jt = new NamedParameterJdbcTemplate(mockDataSource);
		Map params = new HashMap();
		params.put("id", new SqlParameterValue(Types.DECIMAL, new Integer(1)));
		params.put("country", "UK");
		List customers = jt.query(SELECT_NAMED_PARAMETERS, params, new RowMapper() {
			public Object mapRow(ResultSet rs, int rownum) throws SQLException {
				Customer cust = new Customer();
				cust.setId(rs.getInt(COLUMN_NAMES[0]));
				cust.setForename(rs.getString(COLUMN_NAMES[1]));
				return cust;
			}
		});
		assertEquals(1, customers.size());
		Customer cust = (Customer) customers.get(0);
		assertTrue("Customer id was assigned correctly", cust.getId() == 1);
		assertTrue("Customer forename was assigned correctly", cust.getForename().equals("rod"));
	}

	public void testQueryForObjectWithRowMapper() throws SQLException {
		mockResultSet.next();
		ctrlResultSet.setReturnValue(true);
		mockResultSet.getInt("id");
		ctrlResultSet.setReturnValue(1);
		mockResultSet.getString("forename");
		ctrlResultSet.setReturnValue("rod");
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		mockPreparedStatement.setObject(1, new Integer(1), Types.DECIMAL);
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.setString(2, "UK");
		ctrlPreparedStatement.setVoidCallable();
		mockPreparedStatement.executeQuery();
		ctrlPreparedStatement.setReturnValue(mockResultSet);
		if (debugEnabled) {
			mockPreparedStatement.getWarnings();
			ctrlPreparedStatement.setReturnValue(null);
		}
		mockPreparedStatement.close();
		ctrlPreparedStatement.setVoidCallable();

		mockConnection.prepareStatement(SELECT_NAMED_PARAMETERS_PARSED);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		NamedParameterJdbcTemplate jt = new NamedParameterJdbcTemplate(mockDataSource);
		Map params = new HashMap();
		params.put("id", new SqlParameterValue(Types.DECIMAL, new Integer(1)));
		params.put("country", "UK");
		Customer cust = (Customer) jt.queryForObject(SELECT_NAMED_PARAMETERS, params, new RowMapper() {
			public Object mapRow(ResultSet rs, int rownum) throws SQLException {
				Customer cust = new Customer();
				cust.setId(rs.getInt(COLUMN_NAMES[0]));
				cust.setForename(rs.getString(COLUMN_NAMES[1]));
				return cust;
			}
		});
		assertTrue("Customer id was assigned correctly", cust.getId() == 1);
		assertTrue("Customer forename was assigned correctly", cust.getForename().equals("rod"));
	}

}
