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

package org.springframework.orm.jdo;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import javax.jdo.JDOFatalUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;

/**
 * @author Juergen Hoeller
 */
public class LocalPersistenceManagerFactoryTests extends TestCase {

	public void testLocalPersistenceManagerFactoryBean() throws IOException {
		LocalPersistenceManagerFactoryBean pmfb = new LocalPersistenceManagerFactoryBean() {
			protected PersistenceManagerFactory newPersistenceManagerFactory(Properties props) {
				return new MockPersistenceManagerFactory();
			}
		};
		pmfb.setJdoProperties(new Properties());
		pmfb.afterPropertiesSet();
		assertTrue(pmfb.getObject() instanceof MockPersistenceManagerFactory);
	}

	public void testLocalPersistenceManagerFactoryBeanWithInvalidSettings() throws IOException {
		LocalPersistenceManagerFactoryBean pmfb = new LocalPersistenceManagerFactoryBean();
		try {
			pmfb.afterPropertiesSet();
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testLocalPersistenceManagerFactoryBeanWithIncompleteProperties() throws IOException {
		LocalPersistenceManagerFactoryBean pmfb = new LocalPersistenceManagerFactoryBean();
		Properties props = new Properties();
		props.setProperty("myKey", "myValue");
		pmfb.setJdoProperties(props);
		try {
			pmfb.afterPropertiesSet();
			fail("Should have thrown JDOFatalUserException");
		}
		catch (JDOFatalUserException ex) {
			// expected
		}
	}

	public void testLocalPersistenceManagerFactoryBeanWithInvalidProperty() throws IOException {
		LocalPersistenceManagerFactoryBean pmfb = new LocalPersistenceManagerFactoryBean() {
			protected PersistenceManagerFactory newPersistenceManagerFactory(Properties props) {
				throw new IllegalArgumentException(props.getProperty("myKey"));
			}
		};
		Properties props = new Properties();
		props.setProperty("myKey", "myValue");
		pmfb.setJdoProperties(props);
		try {
			pmfb.afterPropertiesSet();
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
			assertTrue("Correct exception", "myValue".equals(ex.getMessage()));
		}
	}

	public void testLocalPersistenceManagerFactoryBeanWithFile() throws IOException {
		LocalPersistenceManagerFactoryBean pmfb = new LocalPersistenceManagerFactoryBean() {
			protected PersistenceManagerFactory newPersistenceManagerFactory(Properties prop) {
				throw new IllegalArgumentException(prop.getProperty("myKey"));
			}
		};
		pmfb.setConfigLocation(new ClassPathResource("test.properties", getClass()));
		try {
			pmfb.afterPropertiesSet();
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
			assertTrue("Correct exception", "myValue".equals(ex.getMessage()));
		}
	}


	public static class MockPersistenceManagerFactory implements PersistenceManagerFactory {

		private Object connectionFactory;

		private Object connectionFactory2;

		public void close() {
		}

		public PersistenceManager getPersistenceManager() {
			return null;
		}

		public PersistenceManager getPersistenceManager(String beanName, String beanName1) {
			return null;
		}

		public void setConnectionUserName(String beanName) {
		}

		public String getConnectionUserName() {
			return null;
		}

		public void setConnectionPassword(String beanName) {
		}

		public void setConnectionURL(String beanName) {
		}

		public String getConnectionURL() {
			return null;
		}

		public void setConnectionDriverName(String beanName) {
		}

		public String getConnectionDriverName() {
			return null;
		}

		public void setConnectionFactoryName(String beanName) {
		}

		public String getConnectionFactoryName() {
			return null;
		}

		public void setConnectionFactory(Object o) {
			this.connectionFactory = o;
		}

		public Object getConnectionFactory() {
			return connectionFactory;
		}

		public void setConnectionFactory2Name(String beanName) {
		}

		public String getConnectionFactory2Name() {
			return null;
		}

		public void setConnectionFactory2(Object o) {
			this.connectionFactory2 = o;
		}

		public Object getConnectionFactory2() {
			return connectionFactory2;
		}

		public void setMultithreaded(boolean b) {
		}

		public boolean getMultithreaded() {
			return false;
		}

		public void setOptimistic(boolean b) {
		}

		public boolean getOptimistic() {
			return false;
		}

		public void setRetainValues(boolean b) {
		}

		public boolean getRetainValues() {
			return false;
		}

		public void setRestoreValues(boolean b) {
		}

		public boolean getRestoreValues() {
			return false;
		}

		public void setNontransactionalRead(boolean b) {
		}

		public boolean getNontransactionalRead() {
			return false;
		}

		public void setNontransactionalWrite(boolean b) {
		}

		public boolean getNontransactionalWrite() {
			return false;
		}

		public void setIgnoreCache(boolean b) {
		}

		public boolean getIgnoreCache() {
			return false;
		}

		public Properties getProperties() {
			return null;
		}

		public Collection supportedOptions() {
			return null;
		}
	}

}
