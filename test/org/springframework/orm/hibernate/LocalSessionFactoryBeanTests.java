package org.springframework.orm.hibernate;

import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

import junit.framework.TestCase;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.MappingException;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.cfg.Environment;
import net.sf.hibernate.connection.UserSuppliedConnectionProvider;

import org.easymock.MockControl;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * @author Juergen Hoeller
 */
public class LocalSessionFactoryBeanTests extends TestCase {

	public void testLocalSessionFactoryBeanWithDataSourceAndMappingResources() throws HibernateException {
		final DriverManagerDataSource ds = new DriverManagerDataSource();
		final Set invocations = new HashSet();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected Configuration newConfiguration() throws HibernateException {
				return new Configuration() {
					public Configuration addResource(String path, ClassLoader classLoader) throws MappingException {
						assertTrue(Thread.currentThread().getContextClassLoader() == classLoader);
						invocations.add("addResource " + path);
						return this;
					}
				};
			}
			protected SessionFactory newSessionFactory(Configuration config) throws HibernateException {
				assertEquals(LocalDataSourceConnectionProvider.class.getName(), config.getProperty(Environment.CONNECTION_PROVIDER));
				assertEquals(ds, LocalDataSourceConnectionProvider.configTimeDataSourceHolder.get());
				invocations.add("newSessionFactory");
				return null;
			}
		};
		sfb.setMappingResources(new String[] {"mapping.hbm.xml", "/mapping2.hbm.xml"});
		sfb.setDataSource(ds);
		sfb.afterPropertiesSet();
		assertTrue(invocations.contains("addResource mapping.hbm.xml"));
		assertTrue(invocations.contains("addResource mapping2.hbm.xml"));
		assertTrue(invocations.contains("newSessionFactory"));
	}

	public void testLocalSessionFactoryBeanWithDataSourceAndMappingResourceJars() throws HibernateException {
		final DriverManagerDataSource ds = new DriverManagerDataSource();
		final Set invocations = new HashSet();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected Configuration newConfiguration() throws HibernateException {
				return new Configuration() {
					public Configuration addJar(String path) throws MappingException {
						invocations.add("addResource " + path);
						return this;
					}
				};
			}
			protected SessionFactory newSessionFactory(Configuration config) throws HibernateException {
				assertEquals(LocalDataSourceConnectionProvider.class.getName(), config.getProperty(Environment.CONNECTION_PROVIDER));
				assertEquals(ds, LocalDataSourceConnectionProvider.configTimeDataSourceHolder.get());
				invocations.add("newSessionFactory");
				return null;
			}
		};
		sfb.setMappingResourceJars(new String[] {"/mapping.hbm.jar", "mapping2.hbm.jar"});
		sfb.setDataSource(ds);
		sfb.afterPropertiesSet();
		assertTrue(invocations.contains("addResource mapping.hbm.jar"));
		assertTrue(invocations.contains("addResource mapping2.hbm.jar"));
		assertTrue(invocations.contains("newSessionFactory"));
	}

	public void testLocalSessionFactoryBeanWithDataSourceAndProperties() throws HibernateException {
		final DriverManagerDataSource ds = new DriverManagerDataSource();
		final Set invocations = new HashSet();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected Configuration newConfiguration() throws HibernateException {
				return new Configuration() {
					public Configuration addResource(String path, ClassLoader classLoader) throws MappingException {
						assertEquals("mapping.hbm.xml", path);
						assertTrue(Thread.currentThread().getContextClassLoader() == classLoader);
						invocations.add("addResource");
						return this;
					}
				};
			}
			protected SessionFactory newSessionFactory(Configuration config) throws HibernateException {
				assertEquals(LocalDataSourceConnectionProvider.class.getName(), config.getProperty(Environment.CONNECTION_PROVIDER));
				assertEquals(ds, LocalDataSourceConnectionProvider.configTimeDataSourceHolder.get());
				assertEquals("myValue", config.getProperty("myProperty"));
				invocations.add("newSessionFactory");
				return null;
			}
		};
		sfb.setMappingResources(new String[] {"mapping.hbm.xml"});
		sfb.setDataSource(ds);
		Properties prop = new Properties();
		prop.setProperty(Environment.CONNECTION_PROVIDER, "myClass");
		prop.setProperty("myProperty", "myValue");
		sfb.setHibernateProperties(prop);
		sfb.afterPropertiesSet();
		assertTrue(invocations.contains("addResource"));
		assertTrue(invocations.contains("newSessionFactory"));
	}

	public void testLocalSessionFactoryBeanWithValidProperties() throws HibernateException {
		final Set invocations = new HashSet();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected Configuration newConfiguration() throws HibernateException {
				return new Configuration() {
					public Configuration addResource(String path, ClassLoader classLoader) throws MappingException {
						assertEquals("mapping.hbm.xml", path);
						assertTrue(Thread.currentThread().getContextClassLoader() == classLoader);
						invocations.add("addResource");
						return this;
					}
				};
			}
			protected SessionFactory newSessionFactory(Configuration config) throws HibernateException {
				assertEquals(UserSuppliedConnectionProvider.class.getName(), config.getProperty(Environment.CONNECTION_PROVIDER));
				assertEquals("myValue", config.getProperty("myProperty"));
				invocations.add("newSessionFactory");
				return null;
			}
		};
		sfb.setMappingResources(new String[] {"mapping.hbm.xml"});
		Properties prop = new Properties();
		prop.setProperty(Environment.CONNECTION_PROVIDER, UserSuppliedConnectionProvider.class.getName());
		prop.setProperty("myProperty", "myValue");
		sfb.setHibernateProperties(prop);
		sfb.afterPropertiesSet();
		assertTrue(invocations.contains("addResource"));
		assertTrue(invocations.contains("newSessionFactory"));
	}

	public void testLocalSessionFactoryBeanWithInvalidProperties() throws HibernateException {
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

	public void testLocalSessionFactoryBeanWithInvalidMappings() throws HibernateException {
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean();
		sfb.setMappingResources(new String[] {"mapping.hbm.xml"});
		try {
			sfb.afterPropertiesSet();
		}
		catch (MappingException ex) {
			// expected, mapping resource not found
		}
	}

	public void testLocalSessionFactoryBeanWithCustomSessionFactory() throws HibernateException {
		MockControl factoryControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sessionFactory = (SessionFactory) factoryControl.getMock();
		sessionFactory.close();
		factoryControl.setVoidCallable(1);
		factoryControl.replay();
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected SessionFactory newSessionFactory(Configuration config) throws HibernateException {
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

	public void testLocalSessionFactoryBeanWithEntityInterceptor() throws HibernateException {
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean() {
			protected Configuration newConfiguration() throws HibernateException {
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
