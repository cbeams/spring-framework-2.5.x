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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.orm.jpa.spi.DomPersistenceUnitReader;
import org.springframework.orm.jpa.spi.MapDataSourceLookup;
import org.springframework.orm.jpa.spi.PersistenceUnitReader;

/**
 * 
 * @author Costin Leau
 * @since 2.0
 */
public class PersistenceXmlParsingTests extends TestCase {

	protected PersistenceUnitReader reader;

	protected void setUp() throws Exception {
		super.setUp();
		reader = createReader();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		reader = null;
	}

	protected PersistenceUnitReader createReader() {
		return new DomPersistenceUnitReader();
	}

	public void testExample1() throws Exception {
		Resource resource = new ClassPathResource("/org/springframework/orm/jpa/persistence-example1.xml");
		PersistenceUnitInfo[] info = reader.readPersistenceUnitInfo(resource);

		assertNotNull(info);
		assertEquals(1, info.length);

		assertEquals("OrderManagement", info[0].getPersistenceUnitName());
	}

	public void testExample2() throws Exception {
		Resource resource = new ClassPathResource("/org/springframework/orm/jpa/persistence-example2.xml");
		PersistenceUnitInfo[] info = reader.readPersistenceUnitInfo(resource);

		assertNotNull(info);
		assertEquals(1, info.length);

		assertEquals("OrderManagement2", info[0].getPersistenceUnitName());

		assertEquals(1, info[0].getMappingFileNames().size());
		assertEquals("mappings.xml", info[0].getMappingFileNames().get(0));
		assertEquals(0, info[0].getProperties().keySet().size());

	}

	public void testExample3() throws Exception {
		Resource resource = new ClassPathResource("/org/springframework/orm/jpa/persistence-example3.xml");
		PersistenceUnitInfo[] info = reader.readPersistenceUnitInfo(resource);

		assertNotNull(info);
		assertEquals(1, info.length);
		assertEquals("OrderManagement3", info[0].getPersistenceUnitName());

		assertEquals(2, info[0].getJarFileUrls().size());
		assertEquals(new ClassPathResource("order.jar").getURL(), info[0].getJarFileUrls().get(0));
		assertEquals(new ClassPathResource("order-supplemental.jar").getURL(), info[0].getJarFileUrls().get(1));
		assertEquals(0, info[0].getProperties().keySet().size());
		assertNull(info[0].getJtaDataSource());
		assertNull(info[0].getNonJtaDataSource());

	}

	public void testExample4() throws Exception {
		SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		DataSource ds = new DriverManagerDataSource();
		builder.bind("jdbc/MyDB", ds);

		Resource resource = new ClassPathResource("/org/springframework/orm/jpa/persistence-example4.xml");
		PersistenceUnitInfo[] info = null;
		info = reader.readPersistenceUnitInfo(resource);

		assertNotNull(info);
		assertEquals(1, info.length);
		assertEquals("OrderManagement4", info[0].getPersistenceUnitName());

		assertEquals(1, info[0].getMappingFileNames().size());
		assertEquals("order-mappings.xml", info[0].getMappingFileNames().get(0));

		assertEquals(3, info[0].getManagedClassNames().size());
		assertEquals("com.acme.Order", info[0].getManagedClassNames().get(0));
		assertEquals("com.acme.Customer", info[0].getManagedClassNames().get(1));
		assertEquals("com.acme.Item", info[0].getManagedClassNames().get(2));

		assertTrue(info[0].excludeUnlistedClasses());

		assertSame(PersistenceUnitTransactionType.RESOURCE_LOCAL, info[0].getTransactionType());
		assertEquals(0, info[0].getProperties().keySet().size());

		// TODO this is undefined as yet. Do we look up Spring datasource?
		// assertNotNull(info[0].getNonJtaDataSource());
		//
		// assertEquals(ds .toString(),
		// info[0].getNonJtaDataSource().toString());

		builder.clear();
	}

	public void testExample5() throws Exception {
		Resource resource = new ClassPathResource("/org/springframework/orm/jpa/persistence-example5.xml");
		PersistenceUnitInfo[] info = reader.readPersistenceUnitInfo(resource);

		assertNotNull(info);
		assertEquals(1, info.length);
		assertEquals("OrderManagement5", info[0].getPersistenceUnitName());

		assertEquals(2, info[0].getMappingFileNames().size());
		assertEquals("order1.xml", info[0].getMappingFileNames().get(0));
		assertEquals("order2.xml", info[0].getMappingFileNames().get(1));

		assertEquals(2, info[0].getJarFileUrls().size());
		assertEquals(new ClassPathResource("order.jar").getURL(), info[0].getJarFileUrls().get(0));
		assertEquals(new ClassPathResource("order-supplemental.jar").getURL(), info[0].getJarFileUrls().get(1));

		assertEquals("com.acme.AcmePersistence", info[0].getPersistenceProviderClassName());
		assertEquals(0, info[0].getProperties().keySet().size());
	}

	public void testExampleComplex() throws Exception {
		DataSource ds = new DriverManagerDataSource();

		Resource resource = new ClassPathResource("/org/springframework/orm/jpa/persistence-complex.xml");
		MapDataSourceLookup dataSourceLookup = new MapDataSourceLookup();
		Map<String, DataSource> dataSources = new HashMap<String, DataSource>();
		dataSources.put("jdbc/MyPartDB", ds);
		dataSources.put("jdbc/MyDB", ds);
		dataSourceLookup.setDataSources(dataSources);
		((DomPersistenceUnitReader) reader).setDataSourceLookup(dataSourceLookup);
		PersistenceUnitInfo[] info = reader.readPersistenceUnitInfo(resource);

		assertEquals(2, info.length);

		PersistenceUnitInfo pu1 = info[0];

		assertEquals("pu1", pu1.getPersistenceUnitName());

		assertEquals("com.acme.AcmePersistence", pu1.getPersistenceProviderClassName());

		assertEquals(1, pu1.getMappingFileNames().size());
		assertEquals("ormap2.xml", pu1.getMappingFileNames().get(0));

		assertEquals(1, pu1.getJarFileUrls().size());
		assertEquals(new ClassPathResource("order.jar").getURL(), pu1.getJarFileUrls().get(0));

		// TODO need to check the default? Where is this defined
		assertFalse(pu1.excludeUnlistedClasses());

		assertSame(PersistenceUnitTransactionType.RESOURCE_LOCAL, pu1.getTransactionType());

		Properties props = pu1.getProperties();
		assertEquals(2, props.keySet().size());
		assertEquals("on", props.getProperty("com.acme.persistence.sql-logging"));
		assertEquals("bar", props.getProperty("foo"));

		assertNull(pu1.getNonJtaDataSource());

		assertSame(ds, pu1.getJtaDataSource());


		PersistenceUnitInfo pu2 = info[1];

		assertSame(PersistenceUnitTransactionType.JTA, pu2.getTransactionType());
		assertEquals("com.acme.AcmePersistence", pu2.getPersistenceProviderClassName());

		assertEquals(1, pu2.getMappingFileNames().size());
		assertEquals("order2.xml", pu2.getMappingFileNames().get(0));

		assertEquals(1, pu2.getJarFileUrls().size());
		assertEquals(new ClassPathResource("order-supplemental.jar").getURL(), pu2.getJarFileUrls().get(0));
		assertTrue(pu2.excludeUnlistedClasses());

		assertNull(pu2.getJtaDataSource());

		// TODO need to define behaviour with non jta datasource
		assertEquals(ds, pu2.getNonJtaDataSource());
	}

	public void testExample6() throws Exception {
		Resource resource = new ClassPathResource("/org/springframework/orm/jpa/persistence-example6.xml");
		PersistenceUnitInfo[] info = reader.readPersistenceUnitInfo(resource);
		assertEquals(1, info.length);
		assertEquals("pu", info[0].getPersistenceUnitName());
		assertEquals(0, info[0].getProperties().keySet().size());
	}

	public void testInvalidPersistence() throws Exception {
		reader.setValidation(true);
		try {
			Resource resource = new ClassPathResource("/org/springframework/orm/jpa/persistence-invalid.xml");
			reader.readPersistenceUnitInfo(resource);
			fail("expected invalid document exception");
		}
		catch (RuntimeException e) {
			// okay
		}

	}

	public void testNoSchemaPersistence() throws Exception {
		reader.setValidation(true);
		try {
			Resource resource = new ClassPathResource("/org/springframework/orm/jpa/persistence-no-schema.xml");
			reader.readPersistenceUnitInfo(resource);
			fail("expected invalid document exception");
		}
		catch (RuntimeException e) {
			// okay
		}
	}
}
