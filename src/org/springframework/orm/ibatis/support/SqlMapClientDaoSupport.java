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

package org.springframework.orm.ibatis.support;

import javax.sql.DataSource;

import com.ibatis.sqlmap.client.SqlMapClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

/**
 * Convenient super class for iBATIS SqlMapClient data access objects.
 * Requires a DataSource to be set, providing a SqlMapClientTemplate
 * based on it to subclasses.
 * @author Juergen Hoeller
 * @since 29.11.2003
 * @see org.springframework.orm.ibatis.SqlMapClientTemplate
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
	public final DataSource getDataSource() {
		return (this.sqlMapClientTemplate != null ? this.sqlMapClientTemplate.getDataSource() : null);
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
	public final SqlMapClient getSqlMapClient() {
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
	public final SqlMapClientTemplate getSqlMapClientTemplate() {
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
