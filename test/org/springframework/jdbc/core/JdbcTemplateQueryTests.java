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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.easymock.MockControl;

import org.springframework.jdbc.AbstractJdbcTests;

/**
 * @author Juergen Hoeller
 * @since 19.12.2004
 */
public class JdbcTemplateQueryTests extends AbstractJdbcTests {

	public void testQueryForList() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID < 3";

		MockControl ctrlResultSetMetaData;
		ResultSetMetaData mockResultSetMetaData;
		MockControl ctrlResultSet;
		ResultSet mockResultSet;
		MockControl ctrlStatement;
		Statement mockStatement;

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

		ctrlResultSetMetaData.replay();
		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		Object o = template.queryForObject(sql, Integer.class);
		assertTrue("Correct result type", o instanceof Integer);

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

	public void testQueryForIntValueNotFound() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = -3";

		MockControl ctrlResultSetMetaData;
		ResultSetMetaData mockResultSetMetaData;
		MockControl ctrlResultSet;
		ResultSet mockResultSet;
		MockControl ctrlStatement;
		Statement mockStatement;

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
		ctrlResultSet.setReturnValue(null);
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

		ctrlResultSetMetaData.replay();
		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		int i = template.queryForInt(sql);
		assertEquals("Return of an int", 0, i);

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

	public void testQueryForListWithArgs() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID < ?";

		MockControl ctrlResultSetMetaData;
		ResultSetMetaData mockResultSetMetaData;
		MockControl ctrlResultSet;
		ResultSet mockResultSet;
		MockControl ctrlStatement;
		PreparedStatement mockStatement;

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

		ctrlStatement = MockControl.createControl(PreparedStatement.class);
		mockStatement = (PreparedStatement) ctrlStatement.getMock();
		mockStatement.setObject(1, new Integer(3));
		ctrlStatement.setVoidCallable();
		mockStatement.executeQuery();
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockStatement);

		ctrlResultSetMetaData.replay();
		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		List li = template.queryForList(sql, new Object[] {new Integer(3)});
		assertEquals("All rows returned", 2, li.size());
		assertEquals("First row is Integer", 11, ((Integer)((Map)li.get(0)).get("age")).intValue());
		assertEquals("Second row is Integer", 12, ((Integer)((Map)li.get(1)).get("age")).intValue());

		ctrlResultSet.verify();
		ctrlStatement.verify();
	}

	public void testQueryForListWithArgsAndSingleRowAndColumn() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID < ?";

		MockControl ctrlResultSetMetaData;
		ResultSetMetaData mockResultSetMetaData;
		MockControl ctrlResultSet;
		ResultSet mockResultSet;
		MockControl ctrlStatement;
		PreparedStatement mockStatement;

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

		ctrlStatement = MockControl.createControl(PreparedStatement.class);
		mockStatement = (PreparedStatement) ctrlStatement.getMock();
		mockStatement.setObject(1, new Integer(3));
		ctrlStatement.setVoidCallable();
		mockStatement.executeQuery();
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockStatement);

		ctrlResultSetMetaData.replay();
		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		List li = template.queryForList(sql, new Object[] {new Integer(3)});
		assertEquals("All rows returned", 1, li.size());
		assertEquals("First row is Integer", 11, ((Integer)((Map)li.get(0)).get("age")).intValue());

		ctrlResultSet.verify();
		ctrlStatement.verify();
	}

	public void testQueryForObjectWithArgs() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = ?";

		MockControl ctrlResultSetMetaData;
		ResultSetMetaData mockResultSetMetaData;
		MockControl ctrlResultSet;
		ResultSet mockResultSet;
		MockControl ctrlStatement;
		PreparedStatement mockStatement;

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

		ctrlStatement = MockControl.createControl(PreparedStatement.class);
		mockStatement = (PreparedStatement) ctrlStatement.getMock();
		mockStatement.setObject(1, new Integer(3));
		ctrlStatement.setVoidCallable();
		mockStatement.executeQuery();
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockStatement);

		ctrlResultSetMetaData.replay();
		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);

		Object o = template.queryForObject(sql, new Object[] {new Integer(3)}, Integer.class);
		assertTrue("Correct result type", o instanceof Integer);

		ctrlResultSet.verify();
		ctrlStatement.verify();
	}

	public void testQueryForIntWithArgs() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = ?";

		MockControl ctrlResultSetMetaData;
		ResultSetMetaData mockResultSetMetaData;
		MockControl ctrlResultSet;
		ResultSet mockResultSet;
		MockControl ctrlStatement;
		PreparedStatement mockStatement;

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

		ctrlStatement = MockControl.createControl(PreparedStatement.class);
		mockStatement = (PreparedStatement) ctrlStatement.getMock();
		mockStatement.setObject(1, new Integer(3));
		ctrlStatement.setVoidCallable();
		mockStatement.executeQuery();
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockStatement);

		ctrlResultSetMetaData.replay();
		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		int i = template.queryForInt(sql, new Object[] {new Integer(3)});
		assertEquals("Return of an int", 22, i);

		ctrlResultSet.verify();
		ctrlStatement.verify();
	}

	public void testQueryForIntWithArgsAndValueNotFound() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = ?";

		MockControl ctrlResultSetMetaData;
		ResultSetMetaData mockResultSetMetaData;
		MockControl ctrlResultSet;
		ResultSet mockResultSet;
		MockControl ctrlStatement;
		PreparedStatement mockStatement;

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
		ctrlResultSet.setReturnValue(null);
		mockResultSet.next();
		ctrlResultSet.setReturnValue(false);
		mockResultSet.close();
		ctrlResultSet.setVoidCallable();

		ctrlStatement = MockControl.createControl(PreparedStatement.class);
		mockStatement = (PreparedStatement) ctrlStatement.getMock();
		mockStatement.setObject(1, new Integer(-3));
		ctrlStatement.setVoidCallable();
		mockStatement.executeQuery();
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockStatement);

		ctrlResultSetMetaData.replay();
		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		int i = template.queryForInt(sql, new Object[] {new Integer(-3)});
		assertEquals("Return of an int", 0, i);

		ctrlResultSet.verify();
		ctrlStatement.verify();
	}

	public void testQueryForLongWithArgs() throws Exception {
		String sql = "SELECT AGE FROM CUSTMR WHERE ID = ?";

		MockControl ctrlResultSetMetaData;
		ResultSetMetaData mockResultSetMetaData;
		MockControl ctrlResultSet;
		ResultSet mockResultSet;
		MockControl ctrlStatement;
		PreparedStatement mockStatement;

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

		ctrlStatement = MockControl.createControl(PreparedStatement.class);
		mockStatement = (PreparedStatement) ctrlStatement.getMock();
		mockStatement.setObject(1, new Integer(3));
		ctrlStatement.setVoidCallable();
		mockStatement.executeQuery();
		ctrlStatement.setReturnValue(mockResultSet);
		mockStatement.getWarnings();
		ctrlStatement.setReturnValue(null);
		mockStatement.close();
		ctrlStatement.setVoidCallable();

		mockConnection.prepareStatement(sql);
		ctrlConnection.setReturnValue(mockStatement);

		ctrlResultSetMetaData.replay();
		ctrlResultSet.replay();
		ctrlStatement.replay();
		replay();

		JdbcTemplate template = new JdbcTemplate(mockDataSource);
		long l = template.queryForLong(sql, new Object[] {new Integer(3)});
		assertEquals("Return of a long", 87, l);

		ctrlResultSet.verify();
		ctrlStatement.verify();
	}

}
