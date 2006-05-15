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
package org.springframework.orm.jpa.spi;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

/**
 * Simple implementation that relies on a map for doing lookups. Useful for
 * testing environments or applications that do not depend on JNDI.
 * 
 * @author Costin Leau
 * 
 */
public class MapDataSourceLookup implements JpaDataSourceLookup {

	private Map<String, DataSource> dataSources = new HashMap<String, DataSource>(0);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.orm.jpa.spi.JpaDataSourceLookup#lookupDataSource(java.lang.String)
	 */
	public DataSource lookupDataSource(String dataSourceName) {
		return dataSources.get(dataSourceName);
	}

	/**
	 * @return Returns the dataSources.
	 */
	public Map<String, DataSource> getDataSources() {
		return dataSources;
	}

	/**
	 * @param dataSources
	 *            The dataSources to set.
	 */
	public void setDataSources(Map<String, DataSource> dataSources) {
		this.dataSources = dataSources;
	}

}
