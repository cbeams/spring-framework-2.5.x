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

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * BeanFactory aware lookup implementation. It will lookup for Spring managed beans identified
 * by name and (DataSource) type. 
 * 
 * @author Costin Leau
 * 
 */
public class BeanFactoryDataSourceLookup implements JpaDataSourceLookup, BeanFactoryAware {

	private BeanFactory beanFactory;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.orm.jpa.spi.JpaDataSourceLookup#lookupDataSource(java.lang.String)
	 */
	public DataSource lookupDataSource(String dataSourceName) {
		if (beanFactory == null)
			throw new IllegalArgumentException("bean factory reference is required");
		return (DataSource) beanFactory.getBean(dataSourceName, DataSource.class);
	}

}
