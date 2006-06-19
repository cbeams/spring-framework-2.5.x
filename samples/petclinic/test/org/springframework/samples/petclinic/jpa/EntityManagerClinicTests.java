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

package org.springframework.samples.petclinic.jpa;

import java.util.List;

import org.springframework.samples.petclinic.aspects.UsageLogAspect;
import org.springframework.test.jpa.AbstractJpaTests;

/**
 * <p>This class extends AbstractJpaTests,
 * one of the valuable test superclasses provided in the org.springframework.test
 * package. This represents best practice for integration tests with Spring. 
 * The AbstractTransactionalDataSourceSpringContextTests superclass provides the
 * following services:
 * <li>Injects test dependencies, meaning that we don't need to perform application
 * context lookups. See the setClinic() method. Injection uses autowiring by type.
 * <li>Executes each test method in its own transaction, which is automatically
 * rolled back by default. This means that even if tests insert or otherwise
 * change database state, there is no need for a teardown or cleanup script.
 * <li>Provides useful inherited protected fields, such as a JdbcTemplate that can be
 * used to verify database state after test operations, or verify the results of queries
 * performed by application code. An ApplicationContext is also inherited, and can be
 * used for explicit lookup if necessary.
 *
 * <p>The AbstractTransactionalDataSourceSpringContextTests and related classes are shipped
 * in the spring-mock.jar.
 *
 * @author Rod Johnson
 * @see AbstractJpaTests
 */
public class EntityManagerClinicTests extends JpaClinicTests {
	
	private UsageLogAspect usageLogAspect;
	
	public void setUsageLogAspect(UsageLogAspect usageLogAspect) {
		this.usageLogAspect = usageLogAspect;
	}
	
	protected String[] getConfigLocations() {
		return new String[] {
			"/org/springframework/samples/petclinic/jpa/applicationContext-jpaCommon.xml",
			"/org/springframework/samples/petclinic/jpa/applicationContext-entityManager.xml"
		};
	}

	public void testUsageLogAspectIsInvoked() {
		String name1 = "Schuurman";
		String name2 = "Greenwood";
		String name3 = "Leau";
		
		assertTrue(clinic.findOwners(name1).isEmpty());
		assertTrue(clinic.findOwners(name2).isEmpty());
		
		List<String> namesRequested = usageLogAspect.getNamesRequested();
		assertTrue(namesRequested.contains(name1));
		assertTrue(namesRequested.contains(name2));
		assertFalse(namesRequested.contains(name3));
	}
	
}
