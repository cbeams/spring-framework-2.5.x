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

import org.springframework.jdbc.CannotGetJdbcConnectionException;

/**
* @author Rod Johnson
* @version $Id: DriverManagerDataSourceTests.java,v 1.8 2004-03-18 03:01:18 trisberg Exp $
*/
public class DriverManagerDataSourceTests extends TestCase {

	public void testValidUsage() throws Exception {
		final String url = "url";
		final String uname = "uname";
		final String pwd = "pwd";

		MockControl ctrlConnection =
			MockControl.createControl(Connection.class);
		final Connection mockConnection = (Connection) ctrlConnection.getMock();
		ctrlConnection.replay();

		class TestDriverManagerDataSource extends DriverManagerDataSource {
			protected Connection getConnectionFromDriverManager(
				String purl,
				String pusername,
				String ppassword)
				throws SQLException {
				assertTrue(purl.equals(url));
				assertTrue(pusername.equals(uname));
				assertTrue(ppassword.equals(pwd));
				return mockConnection;
			}
		}

		DriverManagerDataSource ds = new TestDriverManagerDataSource();
		ds.setUrl(url);
		ds.setPassword(pwd);
		ds.setUsername(uname);
		//ds.setDriverClassName("foobar");
		Connection actualCon = ds.getConnection();
		assertTrue(actualCon == mockConnection);

		assertTrue(ds.getUrl().equals(url));
		assertTrue(ds.getPassword().equals(pwd));
		assertTrue(ds.getUsername().equals(uname));

		assertTrue(ds.shouldClose(actualCon));
		ctrlConnection.verify();
	}

	public void testInvalidClassname() throws Exception {
		final String url = "url";
		final String uname = "uname";
		final String pwd = "pwd";
		String bogusClassname = "foobar";
		DriverManagerDataSource ds = new DriverManagerDataSource();
		ds.setUrl(url);
		ds.setPassword(pwd);
		ds.setUsername(uname);
		try {
			ds.setDriverClassName(bogusClassname);
			fail("Should have thrown CannotGetJdbcConnectionException");
		}
		catch (CannotGetJdbcConnectionException ex) {
			// OK
		}
	}

}
