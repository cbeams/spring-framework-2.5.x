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

import org.quartz.SchedulerConfigException;
import org.quartz.impl.jdbcjobstore.JobStoreCMT;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerSignaler;
import org.quartz.utils.ConnectionProvider;
import org.quartz.utils.DBConnectionManager;

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
 * <p>Note that all Quartz Scheduler operations that affect the persistent
 * job store should usually be performed within active transaction, as they
 * assume to get proper locks etc.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see SchedulerFactoryBean#setDataSource
 * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection
 * @see org.springframework.jdbc.datasource.DataSourceUtils#closeConnectionIfNecessary
 */
public class LocalDataSourceJobStore extends JobStoreCMT {

	/**
	 * Name used for the transactional ConnectionProvider for Quartz.
	 * This provider will delegate to the local Spring-managed DataSource.
	 * @see org.quartz.utils.DBConnectionManager#addConnectionProvider
	 * @see SchedulerFactoryBean#setDataSource
	 */
	public static final String TX_DATA_SOURCE_NAME = "springTxDataSource";

	/**
	 * Name used for the non-transactional ConnectionProvider for Quartz.
	 * This provider will delegate to the local Spring-managed DataSource.
	 * @see org.quartz.utils.DBConnectionManager#addConnectionProvider
	 * @see SchedulerFactoryBean#setDataSource
	 */
	public static final String NON_TX_DATA_SOURCE_NAME = "springNonTxDataSource";

	private DataSource dataSource;

	public void initialize(ClassLoadHelper loadHelper, SchedulerSignaler signaler)
	    throws SchedulerConfigException {

		this.dataSource = SchedulerFactoryBean.getConfigTimeDataSource();
		// Absolutely needs thread-bound DataSource to initialize.
		if (this.dataSource == null) {
			throw new SchedulerConfigException(
			    "No local DataSource found for configuration - " +
			    "dataSource property must be set on SchedulerFactoryBean");
		}

		// Configure transactional connection settings for Quartz.
		setDataSource(TX_DATA_SOURCE_NAME);
		setDontSetAutoCommitFalse(true);

		// Register transactional ConnectionProvider for Quartz.
		DBConnectionManager.getInstance().addConnectionProvider(
				TX_DATA_SOURCE_NAME,
				new ConnectionProvider() {
					public Connection getConnection() throws SQLException {
						// Return a transactional Connection, if any.
						return DataSourceUtils.getConnection(dataSource);
					}
					public void shutdown() throws SQLException {
						// Do nothing - a Spring-managed DataSource has its own lifecycle.
					}
				}
		);

		// Configure non-transactional connection settings for Quartz.
		setNonManagedTXDataSource(NON_TX_DATA_SOURCE_NAME);

		// Register non-transactional ConnectionProvider for Quartz.
		DBConnectionManager.getInstance().addConnectionProvider(
				NON_TX_DATA_SOURCE_NAME,
				new ConnectionProvider() {
					public Connection getConnection() throws SQLException {
						// Always return a non-transactional Connection.
						return dataSource.getConnection();
					}
					public void shutdown() throws SQLException {
						// Do nothing - a Spring-managed DataSource has its own lifecycle.
					}
				}
		);

		super.initialize(loadHelper, signaler);
	}

	protected void closeConnection(Connection con) {
		// Will work for transactional and non-transactional connections.
		DataSourceUtils.closeConnectionIfNecessary(con, this.dataSource);
	}

}
