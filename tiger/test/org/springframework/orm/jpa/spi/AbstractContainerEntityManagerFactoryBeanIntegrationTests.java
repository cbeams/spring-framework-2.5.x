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

import org.springframework.orm.jpa.hibernate.HibernateContainerEntityManagerFactoryBeanTests;
import org.springframework.orm.jpa.toplink.ToplinkContainerEntityManagerFactoryBeanTests;
import org.springframework.test.jpa.AbstractJpaTests;

/**
 * @author Rod Johnson
 *
 */
public abstract class AbstractContainerEntityManagerFactoryBeanIntegrationTests extends AbstractJpaTests {
	
	public enum Provider {
			TOPLINK,
			KODO,
			HIBERNATE
	};
	
	public static Provider getProvider() {
		String provider = System.getProperty("provider");
		if (provider == null) {
			provider= "";
		}
		
		if (provider.indexOf("kodo") > -1) {
			return Provider.KODO;
		}
		else if (provider.indexOf("hibernate") > -1) {
			return Provider.HIBERNATE;
		}
		return Provider.TOPLINK;
	}
	
	@Override
	protected String[] getConfigLocations() {
		
		switch (getProvider()) {
		case KODO : throw new UnsupportedOperationException("Kodo binaries not yet in Spring CVS");
			//return KodoContainerEntityManagerFactoryBeanTests.CONFIG_LOCATIONS;
		case HIBERNATE: return HibernateContainerEntityManagerFactoryBeanTests.CONFIG_LOCATIONS;
		default : 
			// Use the RI as a default
			return ToplinkContainerEntityManagerFactoryBeanTests.CONFIG_LOCATIONS;
		}
		
	
	}
	
	

}
