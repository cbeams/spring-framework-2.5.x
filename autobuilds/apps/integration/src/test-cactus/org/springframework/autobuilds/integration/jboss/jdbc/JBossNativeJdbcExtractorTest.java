/*
 * Created on Jun 21, 2004
 *
 * @todo To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

package org.springframework.autobuilds.integration.jboss.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.cactus.ServletTestSuite;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.nativejdbc.JBossNativeJdbcExtractor;

/**
 * Integration test for JBossNativeJdbcExtractor
 */
public class JBossNativeJdbcExtractorTest extends TestCase {

	// --- attributes

	BeanFactory bf;
	JBossNativeJdbcExtractor extractor;

	// --- methods
	
	public JBossNativeJdbcExtractorTest() throws Exception {
	}
	
	public static Test suite() {
		ServletTestSuite suite = new ServletTestSuite();
		suite.addTestSuite(JBossNativeJdbcExtractorTest.class);
		return suite;
	}

	protected void setUp() throws Exception {
		extractor = new JBossNativeJdbcExtractor();
		ClassPathResource res = new ClassPathResource("applicationContext.xml",
				this.getClass());
		assertTrue(res.exists());
		bf = new XmlBeanFactory(res);
	}

	protected void tearDown() throws Exception {
		bf = null;
		extractor = null;
	}

	public void testGetNativeConnection() throws SQLException {
		DataSource ds = (DataSource) bf.getBean("dataSource");
		Connection conn = ds.getConnection();
		Connection realConn = extractor.getNativeConnection(conn);
		conn.close();
	}

	public void testGetNativeStatement() throws SQLException {
		DataSource ds = (DataSource) bf.getBean("dataSource");
		Connection conn = ds.getConnection();
		Statement stmt = conn.createStatement();
		stmt.close();
		conn.close();
	}

	public void testGetNativePreparedStatement() throws SQLException {
		DataSource ds = (DataSource) bf.getBean("dataSource");
		Connection conn = ds.getConnection();
		PreparedStatement stmt = conn.prepareStatement("select * from jboss_native_jdbc_extractor");
		stmt.close();
		conn.close();
	}

	// we dont test the CallableStatement unwrapper at this point since we have o
	// stored procs in the HSQLDB database
	public void testGetNativeCallableStatement() {
	}

	public void testGetNativeResultSet() {
		DataSource ds = (DataSource) bf.getBean("dataSource");
		System.out.println("done");
		JdbcTemplate j = new JdbcTemplate(ds);
		j.query("select * from jboss_native_jdbc_extractor", new ResultSetExtractor() {
			public Object extractData(ResultSet rs) throws SQLException,
					DataAccessException {
				ResultSet realRS = extractor.getNativeResultSet(rs);
				return null;
			}
		});
	}
}
