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

import java.lang.reflect.Proxy;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import junit.framework.Assert;

import org.springframework.aop.support.AopUtils;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.orm.jpa.PortableEntityManagerFactoryPlus;
import org.springframework.orm.jpa.PortableEntityManagerPlus;
import org.springframework.orm.jpa.domain.DriversLicense;
import org.springframework.orm.jpa.domain.Person;
import org.springframework.test.ExpectedException;
import org.springframework.test.NotTransactional;
import org.springframework.test.Repeat;
import org.springframework.test.Timed;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for ContainerEntityManagerFactoryBean.
 * Uses an in-memory database. Subclasses should only override the getLocation method.
 * 
 * 
 * @author Rod Johnson
 * @since 2.0
 */
public class ContainerEntityManagerFactoryBeanIntegrationTests extends AbstractContainerEntityManagerFactoryBeanIntegrationTests {
	
	@NotTransactional
	public void testEntityManagerFactoryImplementsEntityManagerFactoryInfo() {
		assertTrue(Proxy.isProxyClass(entityManagerFactory.getClass()));
		assertTrue("Must have introduced config interface", 
				entityManagerFactory instanceof PortableEntityManagerFactoryPlus);
		EntityManagerFactoryInfo pemf = ((PortableEntityManagerFactoryPlus) entityManagerFactory).getEntityManagerFactoryInfo();
		assertEquals("Person", pemf.getPersistenceUnitName());
		assertNotNull("PersistenceUnitInfo must be available", pemf.getPersistenceUnitInfo());
		assertNotNull("Raw EntityManagerFactory must be available", pemf.getNativeEntityManagerFactory());
		assertNotNull("VendorProperties will be available in this test", pemf.getVendorProperties());
	}
	
	@NotTransactional
	public void testSharedEntityManagerProxyImplementsPortableEntityManagerPlus() {
		verifyImplementsPortableEntityManagerPlus(sharedEntityManagerProxy);
	}
	
	
	public static void verifyImplementsPortableEntityManagerPlus(EntityManager em) {
		Assert.assertTrue("Spring shared entity managers must implement value add interface", 
				em instanceof PortableEntityManagerPlus);
		PortableEntityManagerPlus pemp = (PortableEntityManagerPlus) em;
		Assert.assertNotNull(pemp.getNativeEntityManager());
	}
	
	@NotTransactional
	public void testEntityManagerFactoryImplementsPortableEntityManagerFactoryPlus() {
		assertTrue("EntityManager implements Spring subinterface",
				entityManagerFactory instanceof PortableEntityManagerFactoryPlus);
		PortableEntityManagerFactoryPlus pemf = (PortableEntityManagerFactoryPlus) entityManagerFactory;
		pemf.evict(Person.class);
	}
	
	public void testStateClean() {
		assertEquals("Should be no people from previous transactions",
				0, countRowsInTable("person"));
	}
	
	@Repeat(5)
	public void testJdbcTx1() throws Exception {
		testJdbcTx2();
	}
	
	@Timed(millis=273)
	public void testJdbcTx2() throws InterruptedException {
		//Thread.sleep(2000);
		assertEquals("Any previous tx must have been rolled back", 0, countRowsInTable("person"));
		//insertPerson("foo");
		executeSqlScript("/sql/insertPerson.sql", false);
	}
	
	@Transactional(readOnly=true)
	public void testEntityManagerProxyIsProxy() {
		assertTrue(AopUtils.isAopProxy(sharedEntityManagerProxy));
		Query q = sharedEntityManagerProxy.createQuery("select p from Person as p");
		List<Person> people = q.getResultList();
		
		assertTrue("Should be open to start with", sharedEntityManagerProxy.isOpen());
		sharedEntityManagerProxy.close();
		assertTrue("Close should have been silently ignored", sharedEntityManagerProxy.isOpen());
	}
	
	
	@ExpectedException(IllegalArgumentException.class)
	public void testBogusQuery() {
		sharedEntityManagerProxy.createQuery("It's raining toads");
	}

	public void testLazyLoading() {
		try {
			Person tony = new Person();
			tony.setFirstName("Tony");
			tony.setLastName("Blair");
			tony.setDriversLicense(new DriversLicense("8439DK"));
			sharedEntityManagerProxy.persist(tony);
			setComplete();
			endTransaction();
			
			startNewTransaction();
			sharedEntityManagerProxy.clear();
			Person newTony = entityManagerFactory.createEntityManager().getReference(Person.class, tony.getId());
			assertNotSame(newTony, tony);
			endTransaction();
				
			assertNotNull(newTony.getDriversLicense());
			
			newTony.getDriversLicense().getSerialNumber();
		}
		finally {
			deleteFromTables(new String[] { "person", "drivers_license" });
			//setComplete();
		}
	}
	
	public void testMultipleResults() {
		// Add with JDBC
		String firstName = "Tony";
		insertPerson(firstName);
		
		assertTrue(AopUtils.isAopProxy(sharedEntityManagerProxy));
		Query q = sharedEntityManagerProxy.createQuery("select p from Person as p");
		List<Person> people = q.getResultList();
		
		assertEquals(1, people.size());
		assertEquals(firstName, people.get(0).getFirstName());
	}

	private void insertPerson(String firstName) {
		String INSERT_PERSON = "INSERT INTO PERSON (ID, FIRST_NAME, LAST_NAME) VALUES (?, ?, ?)";
		simpleJdbcTemplate.update(INSERT_PERSON, 1, firstName, "Blair");
	}
	
	public void testEntityManagerProxyRejectsProgrammaticTxManagement() {
		try {
			sharedEntityManagerProxy.getTransaction();
			fail("Should not be able to create transactions on container managed EntityManager");
		}
		catch (IllegalStateException ex) {			
		}
	}
	
	public void testSharedEntityManagerProxyRejectsProgrammaticTxJoining() {
		try {
			sharedEntityManagerProxy.joinTransaction();
			fail("Should not be able to join transactions with container managed EntityManager");
		}
		catch (IllegalStateException ex) {		
		}
	}
	
//	public void testAspectJInjectionOfConfigurableEntity() {
//		Person p = new Person();
//		System.err.println(p);
//		assertNotNull("Was injected", p.getTestBean());
//		assertEquals("Ramnivas", p.getTestBean().getName());
//	}
	
	public void testInstantiateAndSaveWithSharedEmProxy() {
		testInstantiateAndSave(sharedEntityManagerProxy);
	}
	
	
	protected void testInstantiateAndSave(EntityManager em) {
		assertEquals("Should be no people from previous transactions",
				0, countRowsInTable("person"));
		Person p = new Person();
//		System.out.println("Context loader=" + Thread.currentThread().getContextClassLoader());
//		System.out.println("Person loader was " + p.getClass().getClassLoader());
		p.setFirstName("Tony");
		p.setLastName("Blair");
		em.persist(p);
		
		em.flush();
		assertEquals("1 row must have been inserted",
				1, countRowsInTable("person"));
	}
	
	
	
//	public void testEntityManagerProxyException() {
//		try {
//			entityManagerProxy.createQuery("select p from Person p where p.o=0").getResultList();
//			fail("Semantic nonsense should be rejected");
//		}
//		catch (DataAccessException ex) {
//			
//		}
//	}
	
	public void testQueryNoPersons() {
		EntityManager em = entityManagerFactory.createEntityManager();
		Query q = em.createQuery("select p from Person as p");
		List<Person> people = q.getResultList();
		assertEquals(0, people.size());
//		for (Person p : people) {
//			System.out.println(p);
//		}
	}
}
