package org.springframework.jdbc.core.support;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

/**
 * ListableBeanFactory implementation that reads values from a database
 * table. Expects columns for bean name, property name and value
 * as string. Formats for each are identical to the properties format
 * recognized by DefaultListableBeanFactory.
 * @author Rod Johnson
 * @version $Id: JdbcBeanFactory.java,v 1.4 2003-11-22 17:20:28 jhoeller Exp $
 */
public class JdbcBeanFactory extends DefaultListableBeanFactory {
	
	private JdbcTemplate jdbcTemplate;

	/**
	 * Create a new JdbcBeanFactory.
	 * @param dataSource DataSource to use to obtain database connections.
	 * The DataSource will be cached during the life of this bean factory.
	 * @param sql SQL query to use for loading bean definitions
	 * @see #loadBeanDefinitions
	 */
	public JdbcBeanFactory(DataSource dataSource, String sql) {
		setDataSource(dataSource);
		loadBeanDefinitions(sql);
	}

	/**
	 * Set the DataSource to use to obtain database connections.
	 * Will implicitly create a new JdbcTemplate with the given DataSource.
	 */
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * Set the JdbcTemplate to be used by this bean factory.
	 * Contains settings for DataSource, SQLExceptionTranslator, QueryExecutor, etc.
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * Load bean definitions from the database via the given SQL string.
	 * @param sql SQL query to use for loading bean definitions.
	 * The first three columns must be bean name, property name and value.
	 * Any join and any other columns are permitted: e.g.
	 * SELECT BEAN_NAME, PROPERTY, VALUE FROM CONFIG WHERE CONFIG.APP_ID = 1
	 * It's also possible to perform a join. Column names are not significant --
	 * only the ordering of these first three columns.
	 */
	public void loadBeanDefinitions(String sql) {
		final Properties props = new Properties();
		this.jdbcTemplate.query(sql, new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				String beanName = rs.getString(1);
				String property = rs.getString(2);
				String value = rs.getString(3);
				// Make a properties entry by combining bean name and property
				props.setProperty(beanName + "." + property, value);
			}
		});
		registerBeanDefinitions(props);
	}

}
