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

package org.springframework.orm.hibernate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.transaction.TransactionManager;

import junit.framework.TestCase;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.cfg.Environment;
import net.sf.hibernate.connection.UserSuppliedConnectionProvider;
import org.easymock.MockControl;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author Juergen Hoeller
 */
public class LocalSessionFactoryBeanTests extends TestCase {

	public void testLocalSessionFactoryBeanWithDataSourceAndMappingResources() throws Exception {
		final DriverManagerDataSource ds = new DriverManagerDataSource();
		MockControl tmControl = MockControl.createControl(TransactionManager.class);
		final TransactionManager tm = (TransactionManager) tmControl.getMock();
		final List invocations = new ArrayList();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected Configuration newConfiguration() {
				return new Configuration() {
					public Configuration addInputStream(InputStream is) {
						try {
							is.close();
						}
						catch (IOException ex) {
						}
						invocations.add("addResource");
						return this;
					}
				};
			}
			protected SessionFactory newSessionFactory(Configuration config) {
				assertEquals(LocalDataSourceConnectionProvider.class.getName(),
				    config.getProperty(Environment.CONNECTION_PROVIDER));
				assertEquals(ds, LocalSessionFactoryBean.getConfigTimeDataSource());
				assertEquals(LocalTransactionManagerLookup.class.getName(),
				    config.getProperty(Environment.TRANSACTION_MANAGER_STRATEGY));
				assertEquals(tm, LocalSessionFactoryBean.getConfigTimeTransactionManager());
				invocations.add("newSessionFactory");
				return null;
			}
		};
		sfb.setMappingResources(new String[] {
			"/org/springframework/beans/factory/xml/test.xml",
			"/org/springframework/beans/factory/xml/child.xml"});
		sfb.setDataSource(ds);
		sfb.setJtaTransactionManager(tm);
		sfb.afterPropertiesSet();
		assertTrue(sfb.getConfiguration() != null);
		assertEquals("addResource", invocations.get(0));
		assertEquals("addResource", invocations.get(1));
		assertEquals("newSessionFactory", invocations.get(2));
	}

	public void testLocalSessionFactoryBeanWithDataSourceAndMappingJarLocations() throws Exception {
		final DriverManagerDataSource ds = new DriverManagerDataSource();
		final Set invocations = new HashSet();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected Configuration newConfiguration() {
				return new Configuration() {
					public Configuration addJar(File file) {
						invocations.add("addResource " + file.getPath());
						return this;
					}
				};
			}
			protected SessionFactory newSessionFactory(Configuration config) {
				assertEquals(LocalDataSourceConnectionProvider.class.getName(),
				    config.getProperty(Environment.CONNECTION_PROVIDER));
				assertEquals(ds, LocalSessionFactoryBean.getConfigTimeDataSource());
				invocations.add("newSessionFactory");
				return null;
			}
		};
		sfb.setMappingJarLocations(new Resource[] {
			new FileSystemResource("mapping.hbm.jar"), new FileSystemResource("mapping2.hbm.jar")});
		sfb.setDataSource(ds);
		sfb.afterPropertiesSet();
		assertTrue(sfb.getConfiguration() != null);
		assertTrue(invocations.contains("addResource mapping.hbm.jar"));
		assertTrue(invocations.contains("addResource mapping2.hbm.jar"));
		assertTrue(invocations.contains("newSessionFactory"));
	}

	public void testLocalSessionFactoryBeanWithDataSourceAndProperties() throws Exception {
		final DriverManagerDataSource ds = new DriverManagerDataSource();
		final Set invocations = new HashSet();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected Configuration newConfiguration() {
				return new Configuration() {
					public Configuration addInputStream(InputStream is) {
						try {
							is.close();
						}
						catch (IOException ex) {
						}
						invocations.add("addResource");
						return this;
					}
				};
			}
			protected SessionFactory newSessionFactory(Configuration config) {
				assertEquals(LocalDataSourceConnectionProvider.class.getName(),
				    config.getProperty(Environment.CONNECTION_PROVIDER));
				assertEquals(ds, LocalSessionFactoryBean.getConfigTimeDataSource());
				assertEquals("myValue", config.getProperty("myProperty"));
				invocations.add("newSessionFactory");
				return null;
			}
		};
		sfb.setMappingLocations(new Resource[] {
			new ClassPathResource("/org/springframework/beans/factory/xml/test.xml")});
		sfb.setDataSource(ds);
		Properties prop = new Properties();
		prop.setProperty(Environment.CONNECTION_PROVIDER, "myClass");
		prop.setProperty("myProperty", "myValue");
		sfb.setHibernateProperties(prop);
		sfb.afterPropertiesSet();
		assertTrue(sfb.getConfiguration() != null);
		assertTrue(invocations.contains("addResource"));
		assertTrue(invocations.contains("newSessionFactory"));
	}

	public void testLocalSessionFactoryBeanWithValidProperties() throws Exception {
		final Set invocations = new HashSet();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected SessionFactory newSessionFactory(Configuration config) {
				assertEquals(UserSuppliedConnectionProvider.class.getName(),
				    config.getProperty(Environment.CONNECTION_PROVIDER));
				assertEquals("myValue", config.getProperty("myProperty"));
				invocations.add("newSessionFactory");
				return null;
			}
		};
		Properties prop = new Properties();
		prop.setProperty(Environment.CONNECTION_PROVIDER, UserSuppliedConnectionProvider.class.getName());
		prop.setProperty("myProperty", "myValue");
		sfb.setHibernateProperties(prop);
		sfb.afterPropertiesSet();
		assertTrue(sfb.getConfiguration() != null);
		assertTrue(invocations.contains("newSessionFactory"));
	}

	public void testLocalSessionFactoryBeanWithInvalidProperties() throws Exception {
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean();
		sfb.setMappingResources(new String[0]);
		Properties prop = new Properties();
		prop.setProperty(Environment.CONNECTION_PROVIDER, "myClass");
		sfb.setHibernateProperties(prop);
		try {
			sfb.afterPropertiesSet();
		}
		catch (HibernateException ex) {
			// expected, provider class not found
		}
	}

	public void testLocalSessionFactoryBeanWithInvalidMappings() throws Exception {
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean();
		sfb.setMappingResources(new String[] {"mapping.hbm.xml"});
		try {
			sfb.afterPropertiesSet();
		}
		catch (IOException ex) {
			// expected, mapping resource not found
		}
	}

	public void testLocalSessionFactoryBeanWithCustomSessionFactory() throws Exception {
		MockControl factoryControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sessionFactory = (SessionFactory) factoryControl.getMock();
		sessionFactory.close();
		factoryControl.setVoidCallable(1);
		factoryControl.replay();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected SessionFactory newSessionFactory(Configuration config) {
				return sessionFactory;
			}
		};
		sfb.setMappingResources(new String[0]);
		sfb.setDataSource(new DriverManagerDataSource());
		sfb.afterPropertiesSet();
		assertTrue(sessionFactory.equals(sfb.getObject()));
		sfb.destroy();
		factoryControl.verify();
	}

	public void testLocalSessionFactoryBeanWithEntityInterceptor() throws Exception {
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected Configuration newConfiguration() {
				return new Configuration() {
					public Configuration setInterceptor(Interceptor interceptor) {
						throw new IllegalArgumentException(interceptor.toString());
					}
				};
			}
		};
		sfb.setMappingResources(new String[0]);
		sfb.setDataSource(new DriverManagerDataSource());
		MockControl interceptorControl = MockControl.createControl(Interceptor.class);
		Interceptor entityInterceptor = (Interceptor) interceptorControl.getMock();
		interceptorControl.replay();
		sfb.setEntityInterceptor(entityInterceptor);
		try {
			sfb.afterPropertiesSet();
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
			assertTrue("Correct exception", ex.getMessage().equals(entityInterceptor.toString()));
		}
	}

}
