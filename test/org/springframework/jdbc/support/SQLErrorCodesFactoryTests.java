/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.jdbc.support;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.easymock.MockControl;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.AbstractDataSource;

/**
 * Tests for SQLErrorCode loading.
 * @author Rod Johnson
 */
public class SQLErrorCodesFactoryTests extends TestCase {

	/**
	 * Check that a default instance returns empty error codes for an unknown
	 * database
	 */
	public void testDefaultInstanceWithNoSuchDatabase() {
		SQLErrorCodes sec = SQLErrorCodesFactory.getInstance().getErrorCodes("xx");
		assertTrue(sec.getBadSqlGrammarCodes().length == 0);
		assertTrue(sec.getDataIntegrityViolationCodes().length == 0);
	}
	
	/**
	 * Check that a known database produces recognizable codes
	 */
	public void testDefaultInstanceWithOracle() {
		SQLErrorCodes sec = SQLErrorCodesFactory.getInstance().getErrorCodes("Oracle");
		assertIsOracle(sec);
	}

	private void assertIsOracle(SQLErrorCodes sec) {
		assertTrue(sec.getBadSqlGrammarCodes().length > 0);
		assertTrue(sec.getDataIntegrityViolationCodes().length > 0);
		// This had better be a Bad SQL Grammar code
		assertTrue(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "942") >= 0);
		// This had better NOT be
		assertFalse(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "9xx42") >= 0);
	}
	
	private void assertIsHsql(SQLErrorCodes sec) {
		assertTrue(sec.getBadSqlGrammarCodes().length > 0);
		assertTrue(sec.getDataIntegrityViolationCodes().length > 0);
		// This had better be a Bad SQL Grammar code
		assertTrue(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "-22") >= 0);
		// This had better NOT be
		assertFalse(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "-9") >= 0);
	}

	private void assertIsDB2(SQLErrorCodes sec) {
		assertTrue(sec.getBadSqlGrammarCodes().length > 0);
		assertTrue(sec.getDataIntegrityViolationCodes().length > 0);
		
		assertFalse(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "942") >= 0);
		// This had better NOT be
		assertTrue(Arrays.binarySearch(sec.getBadSqlGrammarCodes(), "-204") >= 0);
	}
	
	public void testLookupOrder() {
		class TestSQLErrorCodesFactory extends SQLErrorCodesFactory {
			private int lookups = 0;
			protected Resource loadResource(String path) {
				++lookups;
				if (lookups == 1) {
					assertEquals(SQLErrorCodesFactory.SQL_ERROR_CODE_OVERRIDE_PATH, path);
					return null;
				}
				else {
					// Should have only one more lookup
					assertEquals(2, lookups);
					assertEquals(SQLErrorCodesFactory.SQL_ERROR_CODE_DEFAULT_PATH, path);
					return null;
				}
			}
		}
		
		// Should have failed to load without error
		TestSQLErrorCodesFactory sf = new TestSQLErrorCodesFactory();
		assertTrue(sf.getErrorCodes("XX").getBadSqlGrammarCodes().length == 0);
		assertTrue(sf.getErrorCodes("Oracle").getDataIntegrityViolationCodes().length == 0);
	}
	
	/**
	 * Check that user defined error codes take precedence.
	 */
	public void testFindUserDefinedCodes() {
		class TestSQLErrorCodesFactory extends SQLErrorCodesFactory {
			protected Resource loadResource(String path) {
				assertEquals(SQLErrorCodesFactory.SQL_ERROR_CODE_OVERRIDE_PATH, path);
				return new ClassPathResource("test-error-codes.xml", SQLErrorCodesFactoryTests.class);
			}
		}
	
		// Should have loaded without error
		TestSQLErrorCodesFactory sf = new TestSQLErrorCodesFactory();
		assertTrue(sf.getErrorCodes("XX").getBadSqlGrammarCodes().length == 0);
		assertEquals(2, sf.getErrorCodes("Oracle").getBadSqlGrammarCodes().length);
		assertEquals("1", sf.getErrorCodes("Oracle").getBadSqlGrammarCodes()[0]);
	}
	
	public void testInvalidUserDefinedCodeFormat() {
		class TestSQLErrorCodesFactory extends SQLErrorCodesFactory {
			protected Resource loadResource(String path) {
				assertEquals(SQLErrorCodesFactory.SQL_ERROR_CODE_OVERRIDE_PATH, path);
				// Guaranteed to be on the classpath, but most certainly NOT XML
				return new ClassPathResource("SQLExceptionTranslator.class", SQLErrorCodesFactoryTests.class);
			}
		}

		// Should have failed to load without error
		TestSQLErrorCodesFactory sf = new TestSQLErrorCodesFactory();
		assertTrue(sf.getErrorCodes("XX").getBadSqlGrammarCodes().length == 0);
		assertEquals(0, sf.getErrorCodes("Oracle").getBadSqlGrammarCodes().length);
	}
	
	/**
	 * Check that custom error codes take precedence.
	 */
	public void testFindCustomCodes() {
		class TestSQLErrorCodesFactory extends SQLErrorCodesFactory {
			protected Resource loadResource(String path) {
				assertEquals(SQLErrorCodesFactory.SQL_ERROR_CODE_OVERRIDE_PATH, path);
				return new ClassPathResource("custom-error-codes.xml", SQLErrorCodesFactoryTests.class);
			}
		}
	
		// Should have loaded without error
		TestSQLErrorCodesFactory sf = new TestSQLErrorCodesFactory();
		assertEquals(1, sf.getErrorCodes("Oracle").getCustomTranslations().size());
		assertEquals("org.springframework.jdbc.support.CustomErrorCodeException", 
				((CustomSQLErrorCodesTranslation)sf.getErrorCodes("Oracle").getCustomTranslations().get(0)).getExceptionClass());
		assertEquals(1,
				((CustomSQLErrorCodesTranslation)sf.getErrorCodes("Oracle").getCustomTranslations().get(0)).getErrorCodes().length);
	}
	
	public void testDataSourceWithNullMetadata() throws Exception {
		
		MockControl ctrlConnection = MockControl.createControl(Connection.class);
		Connection mockConnection = (Connection) ctrlConnection.getMock();
		mockConnection.getMetaData();
		ctrlConnection.setReturnValue(null);
		mockConnection.close();
		ctrlConnection.setVoidCallable();
		ctrlConnection.replay();

		MockControl ctrlDataSource = MockControl.createControl(DataSource.class);
		DataSource mockDataSource = (DataSource) ctrlDataSource.getMock();
		mockDataSource.getConnection();
		ctrlDataSource.setDefaultReturnValue(mockConnection);
		ctrlDataSource.replay();
	
		SQLErrorCodes sec = SQLErrorCodesFactory.getInstance().getErrorCodes(mockDataSource);
		assertIsEmpty(sec);
	
		ctrlConnection.verify();
		ctrlDataSource.verify();
	}
	
	public void testGetFromDataSourceWithSQLException() throws Exception {
		
		SQLException expectedSQLException = new SQLException();

		MockControl ctrlDataSource = MockControl.createControl(DataSource.class);
		DataSource mockDataSource = (DataSource) ctrlDataSource.getMock();
		mockDataSource.getConnection();
		ctrlDataSource.setThrowable(expectedSQLException);
		ctrlDataSource.replay();

		SQLErrorCodes sec = SQLErrorCodesFactory.getInstance().getErrorCodes(mockDataSource);
		assertIsEmpty(sec);

		ctrlDataSource.verify();
	}

	private void assertIsEmpty(SQLErrorCodes sec) {
		// Codes should be empty
		assertEquals(0, sec.getBadSqlGrammarCodes().length);
		assertEquals(0, sec.getDataIntegrityViolationCodes().length);
	}

	private SQLErrorCodes getErrorCodesFromDataSourceWithGivenMetadata(String productName, SQLErrorCodesFactory factory) throws Exception {
		MockControl mdControl = MockControl.createControl(DatabaseMetaData.class);
		DatabaseMetaData md = (DatabaseMetaData) mdControl.getMock();
		md.getDatabaseProductName();
		mdControl.setReturnValue(productName);
		md.getDriverVersion();
		mdControl.setReturnValue("version");
		mdControl.replay();
		
		MockControl ctrlConnection = MockControl.createControl(Connection.class);
		Connection mockConnection = (Connection) ctrlConnection.getMock();
		mockConnection.getMetaData();
		ctrlConnection.setReturnValue(md);
		mockConnection.close();
		ctrlConnection.setVoidCallable();
		ctrlConnection.replay();

		/* 
		 * We can't use an EasyMock mock object here since we cache product name based on data source hashCode 
		 * and EasyMock always seem to return the same hashCode regardles when/where/how it is created.  
		 * This messes up the meta data calls - calling classes don't think they have to call since they 
		 * erroneously think it is the same data source.
		 */ 
		//MockControl ctrlDataSource = MockControl.createControl(DataSource.class);
		//DataSource mockDataSource = (DataSource) ctrlDataSource.getMock();
		//mockDataSource.getConnection();
		//ctrlDataSource.setDefaultReturnValue(mockConnection);
		//ctrlDataSource.replay();
		DataSource mockDataSource = new SpringMockDataSource(productName, mockConnection);

		SQLErrorCodesFactory secf = null;
		if (factory != null)
			secf = factory;
		else
			secf = SQLErrorCodesFactory.getInstance();

		SQLErrorCodes sec = secf.getErrorCodes(mockDataSource);

		mdControl.verify();
		ctrlConnection.verify();
		//ctrlDataSource.verify();
		
		return sec;
	}
	
	/* 
	 * Stand in for the EasyMock mock object - see comment above 
	 */ 
	private class SpringMockDataSource extends AbstractDataSource {
		String productName;
		Connection conn;
		private SpringMockDataSource(String productName, Connection conn) {
			this.productName = productName;
			this.conn = conn;
		}
		public Connection getConnection() {
			return conn;
		}
		public Connection getConnection(String u, String p) {
			return null;
		}
	}

	public void testOracleRecognizedFromMetadata() throws Exception {
		SQLErrorCodes sec = getErrorCodesFromDataSourceWithGivenMetadata("Oracle", null);
		assertIsOracle(sec);
	}
	
	public void testHsqlRecognizedFromMetadata() throws Exception {
		SQLErrorCodes sec = getErrorCodesFromDataSourceWithGivenMetadata("HSQL Database Engine", null);
		assertIsHsql(sec);
	}

	public void testDB2RecognizedFromMetadata() throws Exception {
		SQLErrorCodes sec = getErrorCodesFromDataSourceWithGivenMetadata("DB2", null);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSourceWithGivenMetadata("DB2/", null);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSourceWithGivenMetadata("DB-2", null);
		assertIsEmpty(sec);
	}

	/**
	 * Check that wild card  database name works.
	 */
	public void testWildCardNameReconized() throws Exception {
		class WildcardSQLErrorCodesFactory extends SQLErrorCodesFactory {
			protected Resource loadResource(String path) {
				assertEquals(SQLErrorCodesFactory.SQL_ERROR_CODE_OVERRIDE_PATH, path);
				return new ClassPathResource("wildcard-error-codes.xml", SQLErrorCodesFactoryTests.class);
			}
		}
	
		WildcardSQLErrorCodesFactory factory = new WildcardSQLErrorCodesFactory();
		SQLErrorCodes sec = getErrorCodesFromDataSourceWithGivenMetadata("DB2", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSourceWithGivenMetadata("DB2 UDB for Xxxxx", factory);
		assertIsDB2(sec);
		
		sec = getErrorCodesFromDataSourceWithGivenMetadata("DB3", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSourceWithGivenMetadata("DB3/", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSourceWithGivenMetadata("/DB3", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSourceWithGivenMetadata("/DB3", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSourceWithGivenMetadata("/DB3/", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSourceWithGivenMetadata("DB-3", factory);
		assertIsEmpty(sec);

		sec = getErrorCodesFromDataSourceWithGivenMetadata("DB1", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSourceWithGivenMetadata("DB1/", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSourceWithGivenMetadata("/DB1", factory);
		assertIsEmpty(sec);
		sec = getErrorCodesFromDataSourceWithGivenMetadata("/DB1/", factory);
		assertIsEmpty(sec);

		sec = getErrorCodesFromDataSourceWithGivenMetadata("DB0", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSourceWithGivenMetadata("/DB0", factory);
		assertIsDB2(sec);
		sec = getErrorCodesFromDataSourceWithGivenMetadata("DB0/", factory);
		assertIsEmpty(sec);
		sec = getErrorCodesFromDataSourceWithGivenMetadata("/DB0/", factory);
		assertIsEmpty(sec);
	}
}
