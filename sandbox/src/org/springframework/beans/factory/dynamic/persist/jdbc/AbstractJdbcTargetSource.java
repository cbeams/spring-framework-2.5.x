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

package org.springframework.beans.factory.dynamic.persist.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.dynamic.persist.AbstractPersistenceStoreRefreshableTargetSource;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Rod Johnson
 */
public abstract class AbstractJdbcTargetSource extends AbstractPersistenceStoreRefreshableTargetSource {
	
	private String sql;
	
	protected JdbcTemplate jdbcTemplate;
	
	public void setDataSource(DataSource ds) {
		this.jdbcTemplate = new JdbcTemplate(ds);
	}
	
	/**
	 * SQL must include a single bind variable.
	 * Of course it can be a join or a view.
	 * @param sql
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}
	
	public String getSql() {
		return sql;
	}

	/**
	 * @see org.springframework.beans.factory.dynamic.persist.AbstractPersistenceStoreRefreshableTargetSource#loadFromPersistentStore()
	 */
	protected Object loadFromPersistentStore() {
		List l = jdbcTemplate.query(sql, new Object[] { new Long(getPrimaryKey()) },
				new RowMapper() {
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
				return AbstractJdbcTargetSource.this.mapRow(rs);
			}
		});
		return DataAccessUtils.requiredUniqueResult(l);
	}
	
	/**
	 * Subclasses must implement this method to return an instance of
	 * the persistentClass based on this row in the database.
	 * The SQL can come from a join or a view.
	 * @param rs row from which subclass should extract results
	 * @return an instance of the persistent class based on the 
	 * data in the row
	 * @throws SQLException if there's an error creating an object
	 */
	protected abstract Object mapRow(ResultSet rs) throws SQLException;
	
	protected String storeDetails() {
		return "JDBC: sql=[" + sql + "]";
	}

}
