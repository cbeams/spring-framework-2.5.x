package org.springframework.orm.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Interceptor;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.cfg.Configuration;
import net.sf.hibernate.cfg.Environment;
import net.sf.hibernate.dialect.Dialect;
import net.sf.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * FactoryBean that creates a local Hibernate SessionFactory instance.
 * Behaves like a SessionFactory instance when used as bean reference, e.g.
 * for HibernateTemplate's "sessionFactory" property. Note that switching
 * to JndiObjectFactoryBean is just a matter of configuration!
 *
 * <p>The typical usage will be to register this as singleton factory
 * (for a certain underlying JDBC DataSource) in an application context,
 * and give bean references to application services that need it.
 *
 * <p>Configuration settings can either be read from a Hibernate XML file,
 * specified as "configLocation", or completely via this class. A typical
 * local configuration consists of one or more "mappingResources", various
 * "hibernateProperties" (not strictly necessary), and a "dataSource" that the
 * SessionFactory should use. The latter can also be specified via Hibernate
 * properties, but "dataSource" supports any Spring-configured DataSource,
 * instead of relying on Hibernate's own connection providers.
 *
 * <p>This SessionFactory handling strategy is appropriate for most types of
 * applications, from Hibernate-only single database apps to ones that need
 * distributed transactions. Either HibernateTransactionManager or
 * JtaTransactionManager can be used for transaction demarcation, the latter
 * only being necessary for transactions that span multiple databases.
 *
 * <p>Registering a SessionFactory with JNDI is only advisable when using
 * Hibernate's JCA Connector, i.e. when the application server cares for
 * initialization. Else, portability is rather limited: Manual JNDI binding
 * isn't supported by some application servers (e.g. Tomcat). Unfortunately,
 * JCA has drawbacks too: Its setup is container-specific and can be tedious.
 *
 * <p>Note that the JCA Connector's sole major strength is its seamless
 * cooperation with EJB containers and JTA services. If you do not use EJB
 * and initiate your JTA transactions via Spring's JtaTransactionManager,
 * you can get all benefits including distributed transactions and proper
 * transactional JVM-level caching with local SessionFactory setup too -
 * without any configuration hassle like container-specific setup.
 *
 * <p>Note: Spring's Hibernate support requires Hibernate 2.1 (as of Spring 1.0).
 *
 * @author Juergen Hoeller
 * @since 05.05.2003
 * @see HibernateTemplate#setSessionFactory
 * @see HibernateTransactionManager#setSessionFactory
 * @see org.springframework.jndi.JndiObjectFactoryBean
 */
public class LocalSessionFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	private static ThreadLocal configTimeLobHandlerHolder = new ThreadLocal();

	/**
	 * Return the LobHandler for the currently configured Hibernate SessionFactory,
	 * to be used by Type implementations like ClobStringType.
	 * <p>This instance will be set before initialization of the corresponding
	 * SessionFactory, and reset immediately afterwards. It is thus only available
	 * in constructors of UserType implementations.
	 * @see #setLobHandler
	 * @see org.springframework.orm.hibernate.support.ClobStringType
	 * @see net.sf.hibernate.type.Type
	 */
	public static LobHandler getConfigTimeLobHandler() {
		LobHandler result = (LobHandler) configTimeLobHandlerHolder.get();
		if (result == null) {
			throw new IllegalStateException("No LobHandler found for configuration - lobHandler property must be set on LocalSessionFactoryBean");
		}
		return result;
	}


	protected final Log logger = LogFactory.getLog(getClass());

	private String configLocation;

	private String[] mappingResources;

	private String[] mappingResourceJars;

	private Properties hibernateProperties;

	private DataSource dataSource;

	private TransactionManager jtaTransactionManager;

	private LobHandler lobHandler;

	private Interceptor entityInterceptor;

	private boolean schemaUpdate = false;

	private SessionFactory sessionFactory;


	/**
	 * Set the location of the Hibernate XML config file as class path resource.
	 * A typical value is "/hibernate.cfg.xml", in the case of web applications
	 * normally to be found in WEB-INF/classes.
	 * <p>Note: Can be omitted when all necessary properties and mapping resources
	 * are specified locally via this bean.
	 */
	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set Hibernate mapping resources to be found in the class path,
	 * like "example.hbm.xml" or "mypackage/example.hbm.xml".
	 * <p>Can be used to override values from a Hibernate XML config file,
	 * or to specify all mappings locally.
	 */
	public void setMappingResources(String[] mappingResources) {
		this.mappingResources = mappingResources;
	}

	/**
	 * Set jar files in the class path that contain Hibernate mapping resources,
	 * like "example.hbm.jar".
	 * <p>Can be used to override values from a Hibernate XML config file,
	 * or to specify all mappings locally.
	 */
	public void setMappingResourceJars(String[] mappingResourceJars) {
		this.mappingResourceJars = mappingResourceJars;
	}

	/**
	 * Set Hibernate properties, like "hibernate.dialect".
	 * <p>Can be used to override values in a Hibernate XML config file,
	 * or to specify all necessary properties locally.
	 * <p>Note: Do not specify a transaction provider here when using
	 * Spring-driven transactions. It is also advisable to omit connection
	 * provider settings and use a Spring-set DataSource instead.
	 * @see #setDataSource
	 */
	public void setHibernateProperties(Properties hibernateProperties) {
		this.hibernateProperties = hibernateProperties;
	}

	/**
	 * Set the DataSource to be used by the SessionFactory.
	 * If set, this will override corresponding settings in Hibernate properties.
	 * <p>Note: If this is set, the Hibernate settings should not define
	 * a connection provider to avoid meaningless double configuration.
	 * @see LocalDataSourceConnectionProvider
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Set the JTA TransactionManager to be used for Hibernate's
	 * TransactionManagerLookup. If set, this will override corresponding
	 * settings in Hibernate properties. Allows to use a Spring-managed
	 * JTA TransactionManager for Hibernate's cache synchronization.
	 * <p>Note: If this is set, the Hibernate settings should not define a
	 * transaction manager lookup to avoid meaningless double configuration.
	 * @see LocalTransactionManagerLookup
	 */
	public void setJtaTransactionManager(TransactionManager jtaTransactionManager) {
		this.jtaTransactionManager = jtaTransactionManager;
	}

	/**
	 * Set the LobHandler to be used by the SessionFactory.
	 * Will be exposed at config time for Type implementations.
	 * @see #getConfigTimeLobHandler
	 * @see org.springframework.orm.hibernate.support.ClobStringType
	 * @see net.sf.hibernate.type.Type
	 */
	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}

	/**
	 * Set a Hibernate entity interceptor that allows to inspect and change
	 * property values before writing to and reading from the database.
	 * Will get applied to any new Session created by this factory.
	 * <p>Such an interceptor can either be set at the SessionFactory level, i.e. on
	 * LocalSessionFactoryBean, or at the Session level, i.e. on HibernateTemplate,
	 * HibernateInterceptor, and HibernateTransactionManager. It's preferable to set
	 * it on LocalSessionFactoryBean or HibernateTransactionManager to avoid repeated
	 * configuration and guarantee consistent behavior in transactions.
	 * @see HibernateTemplate#setEntityInterceptor
	 * @see HibernateInterceptor#setEntityInterceptor
	 * @see HibernateTransactionManager#setEntityInterceptor
	 */
	public void setEntityInterceptor(Interceptor entityInterceptor) {
		this.entityInterceptor = entityInterceptor;
	}

	/**
	 * Set whether to execute a schema update after SessionFactory initialization.
	 * <p>For details on how to make schema update scripts work, see the Hibernate
	 * documentation, as this class leverages the same schema update script support
	 * in net.sf.hibernate.cfg.Configuration as Hibernate's own SchemaUpdate tool.
	 * @see net.sf.hibernate.cfg.Configuration#generateSchemaUpdateScript
	 * @see net.sf.hibernate.tool.hbm2ddl.SchemaUpdate
	 */
	public void setSchemaUpdate(boolean schemaUpdate) {
		this.schemaUpdate = schemaUpdate;
	}


	/**
	 * Initialize the SessionFactory for the given or the default location.
	 * @throws IllegalArgumentException in case of illegal property values
	 * @throws HibernateException in case of Hibernate initialization errors
	 */
	public void afterPropertiesSet() throws IllegalArgumentException, HibernateException {
		// create Configuration instance
		Configuration config = newConfiguration();

		if (this.lobHandler != null) {
			// make given LobHandler available for SessionFactory configuration
			// do early because because mapping resource might refer to custom types
			configTimeLobHandlerHolder.set(this.lobHandler);
		}

		if (this.configLocation != null) {
			// load Hibernate configuration from given location
			String resourceLocation = this.configLocation;
			if (!resourceLocation.startsWith("/")) {
				// loaded with Class.getResourceStream -> use leading slash to load from root
				resourceLocation = "/" + resourceLocation;
			}
			config.configure(resourceLocation);
		}

		if (this.mappingResources != null) {
			// register given Hibernate mapping definitions, contained in resource files
			for (int i = 0; i < this.mappingResources.length; i++) {
				String resourcePath = this.mappingResources[i];
				if (resourcePath.startsWith("/")) {
					// loaded via ClassLoader.getResourceAsStream -> never use leading slash
					resourcePath = resourcePath.substring(1);
				}
				config.addResource(resourcePath, Thread.currentThread().getContextClassLoader());
			}
		}

		if (this.mappingResourceJars != null) {
			// register given Hibernate mapping definitions, contained in resources in jar files
			for (int i = 0; i < this.mappingResourceJars.length; i++) {
				String resourcePath = this.mappingResourceJars[i];
				if (resourcePath.startsWith("/")) {
					// loaded via ClassLoader.getResourceAsStream -> never use leading slash
					resourcePath = resourcePath.substring(1);
				}
				config.addJar(resourcePath);
			}
		}

		if (this.hibernateProperties != null) {
			// add given Hibernate properties
			config.addProperties(this.hibernateProperties);
		}

		if (this.dataSource != null) {
			// make given DataSource available for SessionFactory configuration
			config.setProperty(Environment.CONNECTION_PROVIDER, LocalDataSourceConnectionProvider.class.getName());
			LocalDataSourceConnectionProvider.configTimeDataSourceHolder.set(this.dataSource);
		}

		if (this.jtaTransactionManager != null) {
			config.setProperty(Environment.TRANSACTION_MANAGER_STRATEGY, LocalTransactionManagerLookup.class.getName());
			LocalTransactionManagerLookup.configTimeTransactionManagerHolder.set(this.jtaTransactionManager);
		}

		if (this.entityInterceptor != null) {
			// set given entity interceptor at SessionFactory level
			config.setInterceptor(this.entityInterceptor);
		}

		// perform custom post-processing in subclasses
		postProcessConfiguration(config);

		// build SessionFactory instance
		logger.info("Building new Hibernate SessionFactory");
		this.sessionFactory = newSessionFactory(config);

		if (this.jtaTransactionManager != null) {
			// reset TransactionManager holder
			LocalTransactionManagerLookup.configTimeTransactionManagerHolder.set(null);
		}

		if (this.dataSource != null) {
			// reset DataSource holder
			LocalDataSourceConnectionProvider.configTimeDataSourceHolder.set(null);
		}

		if (this.lobHandler != null) {
			// reset LobHandler holder
			configTimeLobHandlerHolder.set(null);
		}

		// execute schema update if requested
		if (this.schemaUpdate) {
			executeSchemaUpdate(config);
		}
	}

	/**
	 * Subclasses can override this method to perform custom initialization
	 * of the Configuration instance used for SessionFactory creation.
	 * The properties of this LocalSessionFactoryBean will be applied to
	 * the Configuration object that gets returned here.
	 * <p>The default implementation creates a new Configuration instance.
	 * A custom implementation could prepare the instance in a specific way,
	 * or use a custom Configuration subclass.
	 * @return the Configuration instance
	 * @throws HibernateException in case of Hibernate initialization errors
	 * @see net.sf.hibernate.cfg.Configuration#Configuration()
	 */
	protected Configuration newConfiguration() throws HibernateException {
		return new Configuration();
	}

	/**
	 * To be implemented by subclasses that want to to perform custom post-processing
	 * of the Configuration object after the default initialization took place.
	 * @param config the current Configuration object
	 * @throws HibernateException in case of Hibernate initialization errors
	 */
	protected void postProcessConfiguration(Configuration config) throws HibernateException {
	}

	/**
	 * Subclasses can override this method to perform custom initialization
	 * of the SessionFactory instance, creating it via the given Configuration
	 * object that got prepared by this LocalSessionFactoryBean.
	 * <p>The default implementation invokes Configuration's buildSessionFactory.
	 * A custom implementation could prepare the instance in a specific way,
	 * or use a custom SessionFactoryImpl subclass.
	 * @param config Configuration prepared by this LocalSessionFactoryBean
	 * @return the SessionFactory instance
	 * @throws HibernateException in case of Hibernate initialization errors
	 * @see net.sf.hibernate.cfg.Configuration#buildSessionFactory
	 */
	protected SessionFactory newSessionFactory(Configuration config) throws HibernateException {
		return config.buildSessionFactory();
	}

	/**
	 * Execute schema update script determined by the given Configuration object.
	 * A replacement for Hibernate's SchemaUpdate class, for automatically
	 * executing schema update scripts on application startup.
	 * <p>Uses the SessionFactory that this bean generates for accessing a JDBC
	 * connection to perform the script. Therefore, it gets invoked after
	 * SessionFactory initialization.
	 * @param config the current Configuration object
	 * @throws HibernateException in case of Hibernate initialization errors
	 * @see net.sf.hibernate.cfg.Configuration#generateSchemaUpdateScript
	 * @see net.sf.hibernate.tool.hbm2ddl.SchemaUpdate
	 */
	protected void executeSchemaUpdate(final Configuration config) throws HibernateException {
		logger.info("Executing schema update for Hibernate SessionFactory");
		final Dialect dialect = Dialect.getDialect(config.getProperties());
		HibernateTemplate template = new HibernateTemplate(this.sessionFactory);

		template.execute(
			new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					Connection conn = session.connection();
					DatabaseMetadata metadata = new DatabaseMetadata(conn, dialect);
					String[] sql = config.generateSchemaUpdateScript(dialect, metadata);
					Statement stmt = conn.createStatement();
					try {
						for (int i = 0; i < sql.length; i++) {
							logger.debug("Executing schema update statement: " + sql[i]);
							stmt.executeUpdate(sql[i]);
						}
					}
					finally {
						JdbcUtils.closeStatement(stmt);
					}
					return null;
				}
			}
		);
	}

	/**
	 * Return the singleton SessionFactory.
	 */
	public Object getObject() {
		return this.sessionFactory;
	}

	public Class getObjectType() {
		return (this.sessionFactory != null) ? this.sessionFactory.getClass() : SessionFactory.class;
	}

	public boolean isSingleton() {
		return true;
	}

	/**
	 * Close the SessionFactory on bean factory shutdown.
	 */
	public void destroy() throws HibernateException {
		logger.info("Closing Hibernate SessionFactory");
		this.sessionFactory.close();
	}

}
