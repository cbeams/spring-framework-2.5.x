/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.jdbc.core;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.test.Person;
import org.springframework.jdbc.core.test.ConcretePerson;

import java.sql.SQLException;
import java.util.List;

/**
 * Mock object based tests for BeanPropertyRowMapper.
 *
 * @author trisberg
 */
public class BeanPropertyRowMapperTests extends AbstractRowMapperTests {

	protected void setUp() throws SQLException {
		super.setUp();
	}

	public void testOverridingClassDefinedForMapping() {
		BeanPropertyRowMapper mapper = new BeanPropertyRowMapper(Person.class);
		try {
			mapper.setMappedClass(Long.class);
			fail("Setting new class should have thrown InvalidDataAccessApiUsageException");
		}
		catch (InvalidDataAccessApiUsageException ex) {
		}
		try {
			mapper.setMappedClass(Person.class);
		}
		catch (InvalidDataAccessApiUsageException ex) {
			fail("Setting same class should not have thrown InvalidDataAccessApiUsageException");
		}
	}
	
	public void testStaticQueryWithRowMapper() throws SQLException {

		List result = jdbcTemplate.query("select name, age, birth_date, balance from people",
				new BeanPropertyRowMapper(Person.class));

		assertEquals(1, result.size());

		Person bean = (Person) result.get(0);

		verifyPerson(bean);

	}

	public void testMappingWithInheritance() throws SQLException {

		List result = jdbcTemplate.query("select name, age, birth_date, balance from people",
				new BeanPropertyRowMapper(ConcretePerson.class));

		assertEquals(1, result.size());

		ConcretePerson bean = (ConcretePerson) result.get(0);

		verifyConcretePerson(bean);

	}

}
