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
 * @see AbstractJpaTests
 * @author Rod Johnson
 */
public class JpaTemplateClinicTests extends JpaClinicTests {
	
	private static final String[] JPA_TEMPLATE_LOCATIONS = new String[] { 
		"/org/springframework/samples/petclinic/jpa/container-applicationContext-jpa.xml",
		"/org/springframework/samples/petclinic/jpa/jpaTemplate-clinic.xml"
	};
	
	
	protected String[] getConfigLocations() {
		return JPA_TEMPLATE_LOCATIONS;
	}
	
}
