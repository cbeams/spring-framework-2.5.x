/*
 * The Spring Framework is published under the terms
 * of the Apache Software License.
 */

package org.springframework.apptests;

import java.io.FileInputStream;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;


/**
 * AbstractTestCase
 * 
 * @author Darren Davison
 * @version $Id: AbstractTestCase.java,v 1.2 2004-01-05 00:27:44 davison Exp $
 */
public abstract class AbstractTestCase extends TestCase {

	//private static String jdbcPropsLocation = System.getProperty("autobuilds.hsqldb.conf.dir");
	protected String testServer;
	protected DriverManagerDataSource dataSource;
	protected JdbcTemplate jdbcTemplate;	
	
	
    /**
     * @param arg0
     */
    public AbstractTestCase(String arg0) {
        super(arg0);
		
		try {
			Properties props = new Properties();
			props.load(new FileInputStream("build.properties"));
			
			dataSource = new DriverManagerDataSource();
			dataSource.setDriverClassName(props.getProperty("autobuilds.jdbc.driver", "org.hsqldb.jdbcDriver"));
			dataSource.setUrl(props.getProperty("autobuilds.jdbc.url","jdbc:hsqldb:hsql://localhost:9001"));
			dataSource.setUsername(props.getProperty("autobuilds.jdbc.username", "sa")); 
			dataSource.setPassword(props.getProperty("autobuilds.jdbc.password","")); 		
			
			jdbcTemplate = new JdbcTemplate(dataSource);
			
			testServer = "http://localhost:" + props.getProperty("autobuilds.server.http.port", "8080");
			
		} catch (Exception e) {
			throw new BuildException("Failed to create dataSource or jdbcTemplate - are the drivers in $ANT_HOME/lib?  " + e);
		}
    }
}
