/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.jdbc.CannotGetJdbcConnectionException;

/**
* @author Rod Johnson
* @version $Id: DriverManagerDataSourceTests.java,v 1.7 2004-03-01 09:15:12 jhoeller Exp $
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
