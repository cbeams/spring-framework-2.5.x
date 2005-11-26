package org.springframework.jdbc.core;

import javax.sql.DataSource;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * Extension of JdbcDaoSupport to expose a JdbcTemplateHelper.
 * Only usable with Java 5 and above.
 * 
 * @author Rod Johnson
 * @since 1.3
 */
public class JdbcTemplateHelperDaoSupport extends JdbcDaoSupport {
	
	private JdbcTemplateHelper jdbcTemplateWrapper;
	
	protected JdbcTemplate createJdbcTemplate(DataSource dataSource) {
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		jdbcTemplateWrapper = new JdbcTemplateHelper(jt);
		return jt;
	}
	
	/**
	 * Return the JdbcTemplateHelper wrapping the current JdbcTemplate.
	 * @return JdbcTemplateHelper for even simpler implementation of common
	 * JDBC usage.
	 */
	public JdbcTemplateHelper getJdbcTemplateHelper() {
	  return jdbcTemplateWrapper;
	}
}
