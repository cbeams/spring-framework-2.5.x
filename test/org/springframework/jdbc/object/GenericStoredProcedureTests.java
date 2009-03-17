/*
 * Copyright 2002-2009 the original author or authors.
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

import java.sql.CallableStatement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.easymock.MockControl;
import org.apache.commons.logging.LogFactory;

import org.springframework.jdbc.AbstractJdbcTests;
import org.springframework.jdbc.datasource.TestDataSourceWrapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @author Thomas Risberg
 */
public class GenericStoredProcedureTests extends AbstractJdbcTests {

	private final boolean debugEnabled = LogFactory.getLog(JdbcTemplate.class).isDebugEnabled();

	private MockControl ctrlCallable;
	private CallableStatement mockCallable;

	private BeanFactory bf;

	protected void setUp() throws Exception {
		super.setUp();

		ctrlCallable = MockControl.createControl(CallableStatement.class);
		mockCallable = (CallableStatement) ctrlCallable.getMock();
		this.bf = new XmlBeanFactory(
				new ClassPathResource("org/springframework/jdbc/object/GenericStoredProcedureTests-context.xml"));
		TestDataSourceWrapper testDataSource = (TestDataSourceWrapper) bf.getBean("dataSource");
		testDataSource.setTarget(mockDataSource);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		if (shouldVerify()) {
			ctrlCallable.verify();
		}
	}

	protected void replay() {
		super.replay();
		ctrlCallable.replay();
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
		mockCallable.getUpdateCount();
		ctrlCallable.setReturnValue(-1);
		mockCallable.getObject(3);
		ctrlCallable.setReturnValue(new Integer(4));
		if (debugEnabled) {
			mockCallable.getWarnings();
			ctrlCallable.setReturnValue(null);
		}
		mockCallable.close();
		ctrlCallable.setVoidCallable();

		mockConnection.prepareCall("{call " + "add_invoice" + "(?, ?, ?)}");
		ctrlConnection.setReturnValue(mockCallable);

		replay();

		testAddInvoice(1106, 3);
	}

	private void testAddInvoice(final int amount, final int custid)
		throws Exception {

		StoredProcedure adder = (StoredProcedure) bf.getBean("genericProcedure");
		Map in = new HashMap(2);
		in.put("amount", new Integer(amount));
		in.put("custid", new Integer(custid));
		Map out = adder.execute(in);
		Integer id = (Integer) out.get("newid");
		assertEquals(4, id.intValue());
	}

}