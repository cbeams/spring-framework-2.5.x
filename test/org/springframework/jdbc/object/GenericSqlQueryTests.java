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

package org.springframework.jdbc.object;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.MockControl;
import org.apache.commons.logging.LogFactory;

import org.springframework.jdbc.AbstractJdbcTests;
import org.springframework.jdbc.Customer;
import org.springframework.jdbc.datasource.TestDataSourceWrapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Thomas Risberg
 */
public class GenericSqlQueryTests extends AbstractJdbcTests {

	private static final String SELECT_ID_FORENAME_NAMED_PARAMETERS_PARSED =
		"select id, forename from custmr where id = ? and country = ?";

	private final boolean debugEnabled = LogFactory.getLog(JdbcTemplate.class).isDebugEnabled();

	private MockControl ctrlPreparedStatement;
	private PreparedStatement mockPreparedStatement;
	private MockControl ctrlResultSet;
	private ResultSet mockResultSet;

	private BeanFactory bf;

	
	protected void setUp() throws Exception {
		super.setUp();
		ctrlPreparedStatement =	MockControl.createControl(PreparedStatement.class);
		mockPreparedStatement =	(PreparedStatement) ctrlPreparedStatement.getMock();
		ctrlResultSet = MockControl.createControl(ResultSet.class);
		mockResultSet = (ResultSet) ctrlResultSet.getMock();
		this.bf = new XmlBeanFactory(
				new ClassPathResource("org/springframework/jdbc/object/GenericSqlQueryTests-context.xml"));
		TestDataSourceWrapper testDataSource = (TestDataSourceWrapper) bf.getBean("dataSource");
		testDataSource.setTarget(mockDataSource);
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

	public void testPlaceHoldersCustomerQuery() throws SQLException {
		SqlQuery query = (SqlQuery) bf.getBean("queryWithPlaceHolders");
		testCustomerQuery(query, false);
	}

	public void testNamedParameterCustomerQuery() throws SQLException {
		SqlQuery query = (SqlQuery) bf.getBean("queryWithNamedParameters");
		testCustomerQuery(query, true);
	}

	private void testCustomerQuery(SqlQuery query, boolean namedParameters) throws SQLException {
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

		mockPreparedStatement.setObject(1, new Integer(1), Types.INTEGER);
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

		mockConnection.prepareStatement(SELECT_ID_FORENAME_NAMED_PARAMETERS_PARSED);
		ctrlConnection.setReturnValue(mockPreparedStatement);

		replay();

		List l = null;
		if (namedParameters) {
			Map params = new HashMap(2);
			params.put("id", new Integer(1));
			params.put("country", "UK");
			l = query.executeByNamedParam(params);
		}
		else {
			Object[] params = new Object[] {new Integer(1), "UK"};
			l = query.execute(params);
		}
		assertTrue("Customer was returned correctly", l.size() == 1);
		Customer cust = (Customer) l.get(0);
		assertTrue("Customer id was assigned correctly", cust.getId() == 1);
		assertTrue("Customer forename was assigned correctly", cust.getForename().equals("rod"));
	}

}