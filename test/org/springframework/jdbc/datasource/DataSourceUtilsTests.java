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

package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.TestCase;
import org.easymock.MockControl;

/**
 * 
 * @author Rod Johnson
 * @since 10-May-2003
 * @version $Revision: 1.4 $
 */
public class DataSourceUtilsTests extends TestCase {

	/**
	 * Constructor for DataSourceUtilsTest.
	 * @param arg0
	 */
	public DataSourceUtilsTests(String arg0) {
		super(arg0);
	}

	/**
	 * Test that we can invoke other methods on the proxy, but close
	 * is suppressed
	 * @throws SQLException
	 */
	public void testGetCloseSuppressingConnectionProxy() throws SQLException {
		MockControl mc = MockControl.createControl(Connection.class);
		Connection mockCon = (Connection) mc.getMock();
		// Test a normal method
		mockCon.isClosed();
		mc.setReturnValue(false, 1);
		mc.replay();
		
		
		Connection noclose = DataSourceUtils.getCloseSuppressingConnectionProxy(mockCon);
		assertTrue(!noclose.isClosed());
		
		// Shouldn't propagate to the mock object
		for (int i = 0; i < 10; i++)
			noclose.close();
		
		mc.verify();
	}

}
