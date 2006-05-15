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
package org.springframework.orm.jpa.hibernate;

import org.springframework.orm.jpa.spi.AbstractContainerEntityManagerFactoryBeanIntegrationTests;
import org.springframework.orm.jpa.spi.ContainerEntityManagerFactoryBeanIntegrationTests;

/**
 * @author Costin Leau
 * 
 */
public class HibernateContainerEntityManagerFactoryBeanTests extends ContainerEntityManagerFactoryBeanIntegrationTests {
	
	public static final String[] CONFIG_LOCATIONS = new String[] { 
		"/org/springframework/orm/jpa/hibernate/hb-manager.xml",
		"/org/springframework/orm/jpa/spi/memdb.xml",
		"/org/springframework/orm/jpa/spi/inject.xml" 
	};

	@Override
	protected String[] getConfigLocations() {
		return CONFIG_LOCATIONS;
	}
	
	@Override
	protected boolean isDisabledInThisEnvironment(String testMethodName) {
		return AbstractContainerEntityManagerFactoryBeanIntegrationTests.getProvider() != Provider.HIBERNATE;
	}

}
