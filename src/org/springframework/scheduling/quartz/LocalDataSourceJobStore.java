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

package org.springframework.scheduling.quartz;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.quartz.JobPersistenceException;
import org.quartz.SchedulerConfigException;
import org.quartz.impl.jdbcjobstore.JobStoreCMT;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerSignaler;

import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Subclass of Quartz' JobStoreCMT class that delegates to a Spring-managed
 * DataSource instead of using a Quartz-managed connection pool. This JobStore
 * will be used if SchedulerFactoryBean's "dataSource" property is set.
 *
 * <p>Operations performed by this JobStore will properly participate in any
 * kind of Spring-managed transaction, as it uses Spring's DataSourceUtils
 * connection handling methods that are aware of a current transaction.
 *
 * @author Juergen Hoeller
 * @since 07.06.2004
 * @see SchedulerFactoryBean#setDataSource
 * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection
 * @see org.springframework.jdbc.datasource.DataSourceUtils#closeConnectionIfNecessary
 */
public class LocalDataSourceJobStore extends JobStoreCMT {

	/**
	 * This will hold the DataSource to use for the currently configured
	 * Quartz Scheduler. It will be set just before initialization
	 * of the Scheduler, and reset immediately afterwards.
	 */
	protected static ThreadLocal configTimeDataSourceHolder = new ThreadLocal();

	private DataSource dataSource;

	public void initialize(ClassLoadHelper loadHelper, SchedulerSignaler signaler)
	    throws SchedulerConfigException {

		// DataSource names are not needed here, but checked in base class
		setDataSource("dummy");
		setNonManagedTXDataSource("dummy");

		this.dataSource = (DataSource) configTimeDataSourceHolder.get();
		// absolutely needs thread-bound DataSource to initialize
		if (this.dataSource == null) {
			throw new SchedulerConfigException("No local DataSource found for configuration - " +
			                                   "dataSource property must be set on SchedulerFactoryBean");
		}

		super.initialize(loadHelper, signaler);
	}

	protected Connection getConnection() {
		// not preparing connection, as this is driven by the transaction
		return DataSourceUtils.getConnection(this.dataSource);
	}

	protected Connection getNonManagedTXConnection() throws JobPersistenceException {
		Connection con = DataSourceUtils.getConnection(this.dataSource);
		// following block copied from base class implementation
		try {
			if (!isDontSetNonManagedTXConnectionAutoCommitFalse()) {
				con.setAutoCommit(false);
			}
			if (isTxIsolationLevelReadCommitted()) {
				con.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			}
		}
		catch (SQLException ex) {
			throw new JobPersistenceException("Failed to prepare JDBC connection", ex);
		}
		return con;
	}

	protected void closeConnection(Connection con) {
		DataSourceUtils.closeConnectionIfNecessary(con, this.dataSource);
	}

}
