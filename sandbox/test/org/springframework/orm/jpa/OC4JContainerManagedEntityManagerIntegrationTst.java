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
package org.springframework.orm.jpa;

public class OC4JContainerManagedEntityManagerIntegrationTst extends ContainerManagedEntityManagerIntegrationTests {

	public OC4JContainerManagedEntityManagerIntegrationTst() {
		super();
	}

	
	/* (non-Javadoc)
	 * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpBeforeTransaction()
	 */
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		super.onSetUpBeforeTransaction();
		//System.setProperty("org.springframework.orm.jpa.provider", "hibernate");
	}


	/* (non-Javadoc)
	 * @see org.springframework.test.AbstractTransactionalSpringContextTests#onTearDownAfterTransaction()
	 */
	@Override
	protected void onTearDownAfterTransaction() throws Exception {
		super.onTearDownAfterTransaction();
		//System.setProperty("org.springframework.orm.jpa.provider", null);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.test.jpa.AbstractJpaTests#shouldUseShadowLoader()
	 */
	@Override
	protected boolean shouldUseShadowLoader() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.orm.jpa.AbstractEntityManagerFactoryIntegrationTests#getConfigLocations()
	 */
	@Override
	protected String[] getConfigLocations() {
		String[] locations = super.getConfigLocations();
		// override with tomcat loader
		String[] newLocations = new String[locations.length + 1];
		System.arraycopy(locations, 0, newLocations, 0, locations.length);
		newLocations[locations.length] = "/org/springframework/orm/jpa/oc4j/weaver.xml";
		return newLocations;
	}

}