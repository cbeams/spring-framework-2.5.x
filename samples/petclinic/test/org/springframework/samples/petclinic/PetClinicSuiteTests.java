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

package org.springframework.samples.petclinic;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.springframework.samples.petclinic.hibernate.HibernateClinicTests;
import org.springframework.samples.petclinic.jdbc.SimpleJdbcClinicTests;
import org.springframework.samples.petclinic.jpa.EntityManagerClinicTests;
import org.springframework.samples.petclinic.jpa.HibernateEntityManagerClinicTests;
import org.springframework.samples.petclinic.jpa.JpaTemplateClinicTests;
import org.springframework.samples.petclinic.jpa.OpenJpaEntityManagerClinicTests;
import org.springframework.samples.petclinic.toplink.TopLinkClinicTests;

/**
 * <p>
 * JUnit 4 based test suite for all PetClinic tests.
 * </p>
 *
 * @author Sam Brannen
 * @since 2.5
 */
@RunWith(Suite.class)
@SuiteClasses( {

OwnerTests.class,

HibernateClinicTests.class,

SimpleJdbcClinicTests.class,

EntityManagerClinicTests.class,

HibernateEntityManagerClinicTests.class,

JpaTemplateClinicTests.class,

OpenJpaEntityManagerClinicTests.class,

TopLinkClinicTests.class

})
public class PetClinicSuiteTests {
}
