package org.springframework.orm.ibatis.support;

import javax.sql.DataSource;

import com.ibatis.db.sqlmap.SqlMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.orm.ibatis.SqlMapTemplate;

/**
 * @author Juergen Hoeller
 * @since 29.11.2003
 */
public class SqlMapDaoSupport {

	protected final Log logger = LogFactory.getLog(getClass());

	private SqlMapTemplate sqlMapTemplate = new SqlMapTemplate();

	/**
	 * Set the JDBC DataSource to be used by this DAO.
	 */
	public final void setDataSource(DataSource dataSource) {
	  this.sqlMapTemplate.setDataSource(dataSource);
	}

	/**
	 * Return the JDBC DataSource used by this DAO.
	 */
	protected final DataSource getDataSource() {
		return sqlMapTemplate.getDataSource();
	}

	/**
	 * Set the iBATIS Database Layer SqlMap to work with.
	 */
	public final void setSqlMap(SqlMap sqlMap) {
		this.sqlMapTemplate.setSqlMap(sqlMap);
	}

	/**
	 * Return the iBATIS Database Layer SqlMap that this template works with.
	 */
	protected final SqlMap getSqlMap() {
		return this.sqlMapTemplate.getSqlMap();
	}

	/**
	 * Set the JdbcTemplate for this DAO explicitly,
	 * as an alternative to specifying a DataSource.
	 */
	public final void setSqlMapTemplate(SqlMapTemplate sqlMapTemplate) {
		this.sqlMapTemplate = sqlMapTemplate;
	}

	/**
	 * Return the JdbcTemplate for this DAO,
	 * pre-initialized with the DataSource or set explicitly.
	 */
	protected final SqlMapTemplate getSqlMapTemplate() {
	  return sqlMapTemplate;
	}

	public final void afterPropertiesSet() throws Exception {
		if (this.sqlMapTemplate == null) {
			throw new IllegalArgumentException("dataSource or sqlMapTemplate is required");
		}
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
