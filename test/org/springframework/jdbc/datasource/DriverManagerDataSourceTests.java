/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */
 
package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.TestCase;

import com.mockobjects.sql.MockConnection;

 /**
 * @author Rod Johnson
 * @version $Id: DriverManagerDataSourceTests.java,v 1.2 2003-08-26 16:44:11 jhoeller Exp $
 */
public class DriverManagerDataSourceTests extends TestCase {

	public void testValidUsage() throws Exception {
		final String url = "url";
		final String uname = "uname";
		final String pwd = "pwd";
		final MockConnection con = new MockConnection();
		class TestDriverManagerDataSource extends DriverManagerDataSource {
			protected Connection getConnectionFromDriverManager(String purl, String pusername, String ppassword)
				throws SQLException {
					assertTrue(purl.equals(url));
					assertTrue(pusername.equals(uname));
					assertTrue(ppassword.equals(pwd));
				return con;
			}
		}
		
		DriverManagerDataSource ds = new TestDriverManagerDataSource();
		ds.setUrl(url);
		ds.setPassword(pwd);
		ds.setUsername(uname);
		//ds.setDriverClassName("foobar");
		Connection actualCon = ds.getConnection();
		assertTrue(actualCon == con);
		
		assertTrue(ds.getUrl().equals(url));
		assertTrue(ds.getPassword().equals(pwd));
		assertTrue(ds.getUsername().equals(uname));
		
		assertTrue(ds.shouldClose(actualCon));
		con.verify();
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
			fail();
		}
		catch (CannotGetJdbcConnectionException ex) {
			// Check the message helpfully included the classname
			assertTrue(ex.getMessage().indexOf(bogusClassname) != -1);
			assertTrue(ex.getRootCause() instanceof ClassNotFoundException);
		}
	}

}
