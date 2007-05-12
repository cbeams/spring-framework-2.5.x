/*
 * Copyright 2002-2007 the original author or authors.
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

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.test.Person;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

/**
 * Mock object based tests for BeanPropertyRowMapper.
 *
 * @author trisberg
 */
public class BeanPropertyRowMapperTests extends TestCase {

	private MockControl conControl;

	private Connection con;

	private MockControl rsmdControl;

	private ResultSetMetaData rsmd;

	private MockControl rsControl;

	private ResultSet rs;

	private JdbcTemplate jdbcTemplate;

	private List result;

	protected void setUp() throws SQLException {
		conControl = MockControl.createControl(Connection.class);
		con = (Connection) conControl.getMock();
		con.isClosed();
		conControl.setDefaultReturnValue(false);

		rsmdControl = MockControl.createControl(ResultSetMetaData.class);
		rsmd = (ResultSetMetaData)rsmdControl.getMock();
		rsmd.getColumnCount();
		rsmdControl.setReturnValue(4, 1);
		rsmd.getColumnName(1);
		rsmdControl.setReturnValue("name", 1);
		rsmd.getColumnType(1);
		rsmdControl.setReturnValue(Types.VARCHAR, 1);
		rsmd.getColumnName(2);
		rsmdControl.setReturnValue("age", 1);
		rsmd.getColumnType(2);
		rsmdControl.setReturnValue(Types.NUMERIC, 1);
		rsmd.getColumnName(3);
		rsmdControl.setReturnValue("birth_date", 1);
		rsmd.getColumnType(3);
		rsmdControl.setReturnValue(Types.DATE, 1);
		rsmd.getColumnName(4);
		rsmdControl.setReturnValue("balance", 1);
		rsmd.getColumnType(4);
		rsmdControl.setReturnValue(Types.DECIMAL, 1);
		rsmdControl.replay();

		rsControl = MockControl.createControl(ResultSet.class);
		rs = (ResultSet) rsControl.getMock();
		rs.getMetaData();
		rsControl.setReturnValue(rsmd, 1);
		rs.next();
		rsControl.setReturnValue(true, 1);
		rs.getString("name");
		rsControl.setReturnValue("Bubba", 1);
		rs.getLong("age");
		rsControl.setReturnValue(22, 1);
		rs.getDate("birth_date");
		rsControl.setReturnValue(new java.sql.Date(1221222L), 1);
		rs.getBigDecimal("balance");
		rsControl.setReturnValue(new BigDecimal("1234.56"), 1);
		rs.next();
		rsControl.setReturnValue(false, 1);
		rs.close();
		rsControl.setVoidCallable(1);
		rsControl.replay();

		jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(new SingleConnectionDataSource(con, false));
		jdbcTemplate.setExceptionTranslator(new SQLStateSQLExceptionTranslator());
		jdbcTemplate.afterPropertiesSet();
	}

	public void testOverridingClassDefinedForMapping() {
		BeanPropertyRowMapper mapper = new BeanPropertyRowMapper(Person.class);
		try {
			mapper.setMappedClass(Long.class);
			fail("Setting new class should have thrown InvalidDataAccessApiUsageException");
		}
		catch (InvalidDataAccessApiUsageException ex) {
		}
		try {
			mapper.setMappedClass(Person.class);
		}
		catch (InvalidDataAccessApiUsageException ex) {
			fail("Setting same class should not have thrown InvalidDataAccessApiUsageException");
		}
	}
	
	public void testStaticQueryWithRowMapper() throws SQLException {
		MockControl stmtControl = MockControl.createControl(Statement.class);
		Statement stmt = (Statement) stmtControl.getMock();

		con.createStatement();
		conControl.setReturnValue(stmt, 1);
		stmt.executeQuery("select name, age, birth_date, balance from people");
		stmtControl.setReturnValue(rs, 1);
		stmt.getWarnings();
		stmtControl.setReturnValue(null, 1);
		stmt.close();
		stmtControl.setVoidCallable(1);

		conControl.replay();
		stmtControl.replay();

		result = jdbcTemplate.query("select name, age, birth_date, balance from people",
				new BeanPropertyRowMapper(Person.class));

		stmtControl.verify();
		verify();

		assertEquals(1, result.size());

		Person bean = (Person)result.get(0);
		assertEquals("Bubba", bean.getName());
		assertEquals(22L, bean.getAge());
		assertEquals(new java.util.Date(1221222L), bean.getBirth_date());
		assertEquals(new BigDecimal("1234.56"), bean.getBalance());
	}

	protected void verify() {
		conControl.verify();
		rsControl.verify();
		rsmdControl.verify();
	}

}
