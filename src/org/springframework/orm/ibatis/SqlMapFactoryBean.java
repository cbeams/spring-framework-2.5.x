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

import com.ibatis.db.sqlmap.SqlMap;
import com.ibatis.db.sqlmap.XmlSqlMapBuilder;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * FactoryBean that creates an iBATIS Database Layer SqlMap as singleton in the
 * current bean factory, possibly for use with SqlMapTemplate.
 *
 * <p>NOTE: The SqlMap/MappedStatement API is the one to use with iBATIS SQL Maps 1.x.
 * The SqlMapClient/SqlMapSession API is only available with SQL Maps 2.
 *
 * @author Juergen Hoeller
 * @since 28.11.2003
 * @see SqlMapTemplate#setSqlMap
 */
public class SqlMapFactoryBean implements FactoryBean, InitializingBean {

	private Resource configLocation;

	private Properties sqlMapProperties;

	private SqlMap sqlMap;


	/**
	 * Set the location of the iBATIS SqlMap config file.
	 * A typical value is "WEB-INF/sql-map-config.xml".
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set optional properties to be passed into the XmlSqlMapBuilder.
	 * @see com.ibatis.db.sqlmap.XmlSqlMapBuilder#buildSqlMap(java.io.Reader, java.util.Properties)
	 */
	public void setSqlMapProperties(Properties sqlMapProperties) {
		this.sqlMapProperties = sqlMapProperties;
	}


	public void afterPropertiesSet() throws IOException {
		if (this.configLocation == null) {
			throw new IllegalArgumentException("configLocation is required");
		}

		// build the SqlMap
		InputStream is = this.configLocation.getInputStream();
		this.sqlMap = (this.sqlMapProperties != null) ?
				XmlSqlMapBuilder.buildSqlMap(new InputStreamReader(is), this.sqlMapProperties) :
				XmlSqlMapBuilder.buildSqlMap(new InputStreamReader(is));
	}


	public Object getObject() {
		return this.sqlMap;
	}

	public Class getObjectType() {
		return (this.sqlMap != null ? this.sqlMap.getClass() : SqlMap.class);
	}

	public boolean isSingleton() {
		return true;
	}

}
