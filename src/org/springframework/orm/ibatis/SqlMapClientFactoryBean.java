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

package org.springframework.orm.ibatis;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.sql.DataSource;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;
import com.ibatis.sqlmap.engine.transaction.TransactionManager;
import com.ibatis.sqlmap.engine.transaction.external.ExternalTransactionConfig;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * FactoryBean that creates an iBATIS Database Layer SqlMapClient as singleton
 * in the current bean factory, possibly for use with SqlMapClientTemplate.
 *
 * <p>Allows to specify a DataSource at the SqlMapClient level. This is
 * preferable to per-DAO DataSource references, as it allows for lazy loading
 * and avoids repeated DataSource references.
 *
 * <p>NOTE: The SqlMapClient/SqlMapSession API is the API of iBATIS SQL Maps 2.
 * With SQL Maps 1.x, the SqlMap/MappedStatement API has to be used.
 *
 * @author Juergen Hoeller
 * @since 24.02.2004
 * @see #setConfigLocation
 * @see #setDataSource
 * @see SqlMapClientTemplate#setSqlMapClient
 * @see SqlMapClientTemplate#setDataSource
 */
public class SqlMapClientFactoryBean implements FactoryBean, InitializingBean {

	private Resource configLocation;

	private Properties sqlMapClientProperties;

	private DataSource dataSource;

	private SqlMapClient sqlMapClient;


	/**
	 * Set the location of the iBATIS SqlMapClient config file as class path resource.
	 * A typical value is "WEB-INF/sql-map-config.xml".
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set optional properties to be passed into the SqlMapClientBuilder.
	 * @see com.ibatis.sqlmap.client.SqlMapClientBuilder#buildSqlMapClient(java.io.Reader, java.util.Properties)
	 */
	public void setSqlMapClientProperties(Properties sqlMapClientProperties) {
		this.sqlMapClientProperties = sqlMapClientProperties;
	}

	/**
	 * Set the DataSource to be used by the SqlMapClient. If specified, this
	 * will override corresponding settings in the SqlMapClient properties.
	 * <p>Specifying a DataSource for the SqlMapClient rather than for each
	 * individual DAO allows for lazy loading, for example when using
	 * PaginatedList results.
	 * <p>With a DataSource passed in here, you don't need to specify one
	 * for each DAO. Passing the SqlMapClient to the DAOs is enough, as it
	 * already carries a DataSource. Thus, it's recommended to specify the
	 * DataSource at this central location only.
	 * <p>Thanks to Brandon Goodin from the iBATIS team for the hint on
	 * how to make this work with Spring's integration strategy!
	 * @see com.ibatis.sqlmap.client.SqlMapClient#getDataSource
	 * @see SqlMapClientTemplate#setDataSource
	 * @see SqlMapClientTemplate#queryForPaginatedList
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}


	public void afterPropertiesSet() throws IOException {
		if (this.configLocation == null) {
			throw new IllegalArgumentException("configLocation is required");
		}

		// build the SqlMapClient
		InputStream is = this.configLocation.getInputStream();
		this.sqlMapClient = (this.sqlMapClientProperties != null) ?
				SqlMapClientBuilder.buildSqlMapClient(new InputStreamReader(is), this.sqlMapClientProperties) :
				SqlMapClientBuilder.buildSqlMapClient(new InputStreamReader(is));

		// tell the SqlMapClient to use the given DataSource, if any
		if (this.dataSource != null) {
			if (!(this.sqlMapClient instanceof ExtendedSqlMapClient)) {
				throw new IllegalArgumentException("Cannot set DataSource for SqlMapClient " +
																					 "if not of type ExtendedSqlMapClient");
			}
			ExtendedSqlMapClient extendedClient = (ExtendedSqlMapClient) this.sqlMapClient;
			ExternalTransactionConfig transactionConfig = new ExternalTransactionConfig();
			transactionConfig.setDataSource(this.dataSource);
			transactionConfig.setMaximumConcurrentTransactions(extendedClient.getDelegate().getMaxTransactions());
			extendedClient.getDelegate().setTxManager(new TransactionManager(transactionConfig));
		}
	}


	public Object getObject() {
		return this.sqlMapClient;
	}

	public Class getObjectType() {
		return (this.sqlMapClient != null ? this.sqlMapClient.getClass() : SqlMapClient.class);
	}

	public boolean isSingleton() {
		return true;
	}

}
