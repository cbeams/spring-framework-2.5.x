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

import javax.naming.NamingException;
import javax.persistence.PersistenceException;
import javax.sql.DataSource;

import org.springframework.jndi.JndiTemplate;

/**
 * Jndi based datasource lookup using a jndiTemplate. For specific JNDI
 * configuration, it is recommended to configure a jndiTemplate instance and
 * inject it.
 * 
 * @see org.springframework.jndi.JndiTemplate
 * @author Costin Leau
 * 
 */
public class JndiDataSourceLookup implements JpaDataSourceLookup {

	private JndiTemplate jndiTemplate = new JndiTemplate();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.orm.jpa.spi.JpaDataSourceLookup#lookupDataSource(java.lang.String)
	 */
	public DataSource lookupDataSource(String dataSourceName) {
		try {
			return (DataSource) jndiTemplate.lookup(dataSourceName, DataSource.class);
		}
		catch (NamingException ex) {
			throw new PersistenceException("Failed to look up JNDI datasource with name '" + dataSourceName + "'", ex);
		}
	}

	/**
	 * @return Returns the jndiTemplate.
	 */
	public JndiTemplate getJndiTemplate() {
		return jndiTemplate;
	}

	/**
	 * @param jndiTemplate
	 *            The jndiTemplate to set.
	 */
	public void setJndiTemplate(JndiTemplate jndiTemplate) {
		this.jndiTemplate = jndiTemplate;
	}

}
