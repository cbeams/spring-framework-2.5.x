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
import java.util.Properties;

import javax.jdo.JDOFatalUserException;
import javax.jdo.PersistenceManagerFactory;

import junit.framework.TestCase;

import org.springframework.core.io.ClassPathResource;

/**
 * @author Juergen Hoeller
 */
public class LocalPersistenceManagerFactoryTests extends TestCase {

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

	public void testLocalPersistenceManagerFactoryBeanWithJdoHelper() throws IOException {
		LocalPersistenceManagerFactoryBean pmfb = new LocalPersistenceManagerFactoryBean();
		Properties prop = new Properties();
		prop.setProperty("myKey", "myValue");
		pmfb.setJdoProperties(prop);
		try {
			pmfb.afterPropertiesSet();
			fail("Should have thrown JDOFatalUserException");
		}
		catch (JDOFatalUserException ex) {
			// expected
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

	public void testLocalPersistenceManagerFactoryBeanWithProperties() throws IOException {
		LocalPersistenceManagerFactoryBean pmfb = new LocalPersistenceManagerFactoryBean() {
			protected PersistenceManagerFactory newPersistenceManagerFactory(Properties prop) {
				throw new IllegalArgumentException(prop.getProperty("myKey"));
			}
		};
		Properties prop = new Properties();
		prop.setProperty("myKey", "myValue");
		pmfb.setJdoProperties(prop);
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
