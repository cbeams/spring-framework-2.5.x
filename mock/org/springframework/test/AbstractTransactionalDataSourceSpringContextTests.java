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

package org.springframework.test;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Expects a DataSource, exposes a JdbcTemplate, and provides an easy
 * way to delete from the database in a new transaction.
 * @author Rod Johnson
 * @since 1.1.1
 */
public abstract class AbstractTransactionalDataSourceSpringContextTests
    extends AbstractTransactionalSpringContextTests {

	protected JdbcTemplate jdbcTemplate;

	private boolean zappedTables;

	public void setDataSource(DataSource dataSource) {
		// TODO what if you want to use a JdbcTemplate by preference,
		// for a native extractor?
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * Convenient method to delete all rows from these tables.
	 */
	protected void deleteFromTables(String[] names) {
		for (int i = 0; i < names.length; i++) {
			logger.info("Deleted " +
			    this.jdbcTemplate.update("DELETE FROM " + names[i]) + " rows from table " + names[i]);
		}
		this.zappedTables = true;
	}

	protected final void setComplete() {
		if (this.zappedTables) {
			throw new IllegalStateException("Cannot set complete after deleting tables");
		}
		super.setComplete();
	}

}
