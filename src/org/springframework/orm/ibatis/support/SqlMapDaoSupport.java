/*
 * Copyright 2002-2005 the original author or authors.
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

import com.ibatis.db.sqlmap.SqlMap;

import org.springframework.dao.support.DaoSupport;
import org.springframework.orm.ibatis.SqlMapTemplate;

/**
 * Convenient super class for iBATIS SqlMap data access objects.
 * Requires a SqlMap to be set, providing a SqlMapTemplate
 * based on it to subclasses.
 *
 * <p>Instead of a plain SqlMap, you can also pass a preconfigured
 * SqlMapTemplate instance in. This allows you to share your
 * SqlMapTemplate configuration for all your DAOs, for example
 * a custom SQLExceptionTranslator to use.
 *
 * @author Juergen Hoeller
 * @since 29.11.2003
 * @see #setSqlMap
 * @see #setSqlMapTemplate
 * @see org.springframework.orm.ibatis.SqlMapTemplate
 * @see org.springframework.orm.ibatis.SqlMapTemplate#setExceptionTranslator
 */
public abstract class SqlMapDaoSupport extends DaoSupport {

	private SqlMapTemplate sqlMapTemplate = new SqlMapTemplate();

	private boolean externalTemplate = false;

	/**
	 * Set the JDBC DataSource to be used by this DAO.
	 * Not required: The SqlMap might carry a shared DataSource.
	 * @see #setSqlMap
	 */
	public final void setDataSource(DataSource dataSource) {
	  this.sqlMapTemplate.setDataSource(dataSource);
	}

	/**
	 * Return the JDBC DataSource used by this DAO.
	 */
	public final DataSource getDataSource() {
		return (this.sqlMapTemplate != null ? this.sqlMapTemplate.getDataSource() : null);
	}

	/**
	 * Set the iBATIS Database Layer SqlMap to work with.
	 * Either this or a "sqlMapTemplate" is required.
	 * @see #setSqlMapTemplate
	 */
	public final void setSqlMap(SqlMap sqlMap) {
		this.sqlMapTemplate.setSqlMap(sqlMap);
	}

	/**
	 * Return the iBATIS Database Layer SqlMap that this template works with.
	 */
	public final SqlMap getSqlMap() {
		return this.sqlMapTemplate.getSqlMap();
	}

	/**
	 * Set the SqlMapTemplate for this DAO explicitly,
	 * as an alternative to specifying a SqlMap.
	 * @see #setSqlMap
	 */
	public final void setSqlMapTemplate(SqlMapTemplate sqlMapTemplate) {
		if (sqlMapTemplate == null) {
			throw new IllegalArgumentException("Cannot set sqlMapTemplate to null");
		}
		this.sqlMapTemplate = sqlMapTemplate;
		this.externalTemplate = true;
	}

	/**
	 * Return the SqlMapTemplate for this DAO,
	 * pre-initialized with the SqlMap or set explicitly.
	 */
	public final SqlMapTemplate getSqlMapTemplate() {
	  return sqlMapTemplate;
	}

	protected final void checkDaoConfig() {
		if (!this.externalTemplate) {
			this.sqlMapTemplate.afterPropertiesSet();
		}
	}

}
