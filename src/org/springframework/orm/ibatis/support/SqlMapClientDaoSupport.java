package org.springframework.orm.ibatis.support;

import javax.sql.DataSource;

import com.ibatis.sqlmap.client.SqlMapClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

/**
 * @author Juergen Hoeller
 * @since 29.11.2003
 */
public class SqlMapClientDaoSupport implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private SqlMapClientTemplate sqlMapClientTemplate = new SqlMapClientTemplate();

	/**
	 * Set the JDBC DataSource to be used by this DAO.
	 */
	public final void setDataSource(DataSource dataSource) {
	  this.sqlMapClientTemplate.setDataSource(dataSource);
	}

	/**
	 * Return the JDBC DataSource used by this DAO.
	 */
	protected final DataSource getDataSource() {
		return sqlMapClientTemplate.getDataSource();
	}

	/**
	 * Set the iBATIS Database Layer SqlMap to work with.
	 */
	public final void setSqlMapClient(SqlMapClient sqlMapClient) {
		this.sqlMapClientTemplate.setSqlMapClient(sqlMapClient);
	}

	/**
	 * Return the iBATIS Database Layer SqlMap that this template works with.
	 */
	protected final SqlMapClient getSqlMapClient() {
		return this.sqlMapClientTemplate.getSqlMapClient();
	}

	/**
	 * Set the JdbcTemplate for this DAO explicitly,
	 * as an alternative to specifying a DataSource.
	 */
	public final void setSqlMapClientTemplate(SqlMapClientTemplate sqlMapClientTemplate) {
		this.sqlMapClientTemplate = sqlMapClientTemplate;
	}

	/**
	 * Return the JdbcTemplate for this DAO,
	 * pre-initialized with the DataSource or set explicitly.
	 */
	protected final SqlMapClientTemplate getSqlMapClientTemplate() {
	  return sqlMapClientTemplate;
	}

	public final void afterPropertiesSet() throws Exception {
		this.sqlMapClientTemplate.afterPropertiesSet();
		initDao();
	}

	/**
	 * Subclasses can override this for custom initialization behavior.
	 * Gets called after population of this instance's bean properties.
	 * @throws Exception if initialization fails
	 */
	protected void initDao() throws Exception {
	}

}
