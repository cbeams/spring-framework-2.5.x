/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.jdbc.datasource.lookup;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Simple DataSourceLookup implementation that relies on a map for doing lookups.
 *
 * <p>Useful for testing environments or applications that need to match arbitrary
 * DataSource names to target DataSource objects.
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @since 2.0
 */
public class MapDataSourceLookup implements DataSourceLookup {

	private final Map dataSources = new HashMap(16);


	public MapDataSourceLookup() {
	}

	public MapDataSourceLookup(Map dataSources) {
		setDataSources(dataSources);
	}


	public void addDataSource(String dataSourceName, DataSource dataSource) {
		this.dataSources.put(dataSourceName, dataSource);
	}

	public void setDataSources(Map dataSources) {
		if (dataSources != null) {
			this.dataSources.putAll(dataSources);
		}
	}

	public Map getDataSources() {
		return Collections.unmodifiableMap(this.dataSources);
	}


	public DataSource getDataSource(String dataSourceName) throws DataAccessResourceFailureException {
		DataSource dataSource = (DataSource) this.dataSources.get(dataSourceName);
		if (dataSource == null) {
			throw new DataAccessResourceFailureException(
					"No DataSource with name '" + dataSourceName + "' registered");
		}
		return dataSource;
	}

}
