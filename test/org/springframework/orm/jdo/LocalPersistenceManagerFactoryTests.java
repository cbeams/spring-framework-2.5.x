/*
 * Copyright 2002-2005 the original author or authors.
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
import java.util.Properties;

import javax.jdo.JDOFatalUserException;
import javax.jdo.PersistenceManagerFactory;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.core.io.ClassPathResource;

/**
 * @author Juergen Hoeller
 */
public class LocalPersistenceManagerFactoryTests extends TestCase {

	public void testLocalPersistenceManagerFactoryBean() throws IOException {
		MockControl pmfControl = MockControl.createControl(PersistenceManagerFactory.class);
		final PersistenceManagerFactory pmf = (PersistenceManagerFactory) pmfControl.getMock();
		LocalPersistenceManagerFactoryBean pmfb = new LocalPersistenceManagerFactoryBean() {
			protected PersistenceManagerFactory newPersistenceManagerFactory(Properties props) {
				return pmf;
			}
		};
		pmfb.setJdoProperties(new Properties());
		pmfb.afterPropertiesSet();
		assertSame(pmf, pmfb.getObject());
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

}
